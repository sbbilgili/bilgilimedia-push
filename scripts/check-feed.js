const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

const FEED_URL =
  "https://superjsonblob.com/api/jsonBlob/075eca5d-5acd-49fe-87ba-579a4c6ab434";
const STATE_PATH = path.join(process.cwd(), "state", "known.json");

const TOPICS = {
  "Genel": "genel",
  "Belgesel": "belgesel",
  "Tarih": "tarih",
  "Doğa": "doga",
  "Yemek": "yemek",
  "Teknoloji": "teknoloji",
  "Animasyon": "animasyon",
  "Çizgi Film": "cizgifilm",
};

function readState() {
  if (!fs.existsSync(STATE_PATH)) {
    return { initialized: false, lastFeedVersion: 0, knownIds: [] };
  }
  return JSON.parse(fs.readFileSync(STATE_PATH, "utf8"));
}

function writeState(feed, ids, fresh) {
  fs.mkdirSync(path.dirname(STATE_PATH), { recursive: true });
  fs.writeFileSync(
    STATE_PATH,
    JSON.stringify(
      {
        initialized: true,
        lastFeedVersion: feed.version || 0,
        lastCheckedAt: new Date().toISOString(),
        lastNewIds: fresh.map((a) => String(a.id)),
        knownIds: ids,
      },
      null,
      2
    ) + "\n"
  );
}

async function getFeed() {
  const response = await fetch(FEED_URL, {
    headers: { "user-agent": "BilgiliMedia-GitHub-Actions/1.0" },
  });

  if (!response.ok) {
    throw new Error("Feed HTTP " + response.status);
  }

  const feed = await response.json();
  if (!feed || !Array.isArray(feed.articles)) {
    throw new Error("Invalid feed response");
  }
  return feed;
}

function createMessage(article) {
  const topic = TOPICS[article.category];
  if (!topic) return null;

  return {
    topic,
    data: {
      article_id: String(article.id || ""),
      category: String(article.category || ""),
      title: String(article.title || "Yeni BilgiliMedia içeriği"),
      body: String(
        article.summary ||
          article.title ||
          "BilgiliMedia'da yeni içerik yayımlandı."
      ),
      content_type: article.videoUrl ? "video" : "article",
    },
    android: {
      priority: "high",
      ttl: 3600 * 1000,
    },
  };
}

async function main() {
  const feed = await getFeed();
  const state = readState();
  const ids = feed.articles.map((article) => String(article.id));

  if (!state.initialized) {
    writeState(feed, ids, []);
    console.log(
      "Initial baseline created. Existing articles will not generate notifications."
    );
    return;
  }

  const known = new Set((state.knownIds || []).map(String));
  const fresh = feed.articles.filter(
    (article) => !known.has(String(article.id))
  );

  if (fresh.length === 0) {
    console.log(
      "No new articles. Feed version:",
      feed.version || 0,
      "Articles:",
      ids.length
    );
    writeState(feed, ids, []);
    return;
  }

  const rawCredential = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (!rawCredential) {
    throw new Error(
      "FIREBASE_SERVICE_ACCOUNT GitHub secret is missing. No push was sent and state was not advanced."
    );
  }

  const serviceAccount = JSON.parse(rawCredential);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });

  let sent = 0;
  for (const article of fresh) {
    const message = createMessage(article);
    if (!message) {
      console.log(
        "Skipping unsupported category:",
        article.category,
        article.id
      );
      continue;
    }

    const id = await admin.messaging().send(message);
    sent += 1;
    console.log(
      "Push sent:",
      article.id,
      "->",
      message.topic,
      "message:",
      id
    );
  }

  writeState(feed, ids, fresh);
  console.log("New articles:", fresh.length, "Pushes sent:", sent);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
