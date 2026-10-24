const fs = require("fs");
const path = require("path");
const { applicationDefault, initializeApp } = require("firebase-admin/app");
const { getMessaging } = require("firebase-admin/messaging");

const FEED_PATH = path.join(process.cwd(), "content", "feed.json");
const CONFIG_PATH = path.join(process.cwd(), "hosting", "app-config.json");
const PREVIOUS_FEED_PATH = process.env.PREVIOUS_FEED_PATH || "";

function loadTopicMap() {
  const config = JSON.parse(fs.readFileSync(CONFIG_PATH, "utf8"));
  if (!config || !Array.isArray(config.categories)) {
    throw new Error("Invalid app-config categories");
  }

  const map = {};
  const topics = new Set();

  for (const item of config.categories) {
    if (!item || item.enabled === false) continue;

    const name = String(item.name || "").trim();
    const topic = String(item.topic || "").trim();

    if (!name || !topic) {
      throw new Error("Enabled category is missing name or topic");
    }
    if (!/^[a-zA-Z0-9._~%-]+$/.test(topic)) {
      throw new Error("Invalid FCM topic name: " + topic);
    }
    if (Object.prototype.hasOwnProperty.call(map, name)) {
      throw new Error("Duplicate category name: " + name);
    }
    if (topics.has(topic)) {
      throw new Error("Duplicate FCM topic: " + topic);
    }

    map[name] = topic;
    topics.add(topic);
  }

  return map;
}

function readFeed(filePath, label) {
  const feed = JSON.parse(fs.readFileSync(filePath, "utf8"));
  if (!feed || !Array.isArray(feed.articles)) {
    throw new Error("Invalid " + label + " feed");
  }

  const seenIds = new Set();
  for (const article of feed.articles) {
    const id = String(article?.id || "").trim();
    const title = String(article?.title || "").trim();
    const category = String(article?.category || "").trim();

    if (!id || !title || !category) {
      throw new Error(label + " feed contains an article missing id, title, or category");
    }
    if (seenIds.has(id)) {
      throw new Error(label + " feed contains duplicate article id: " + id);
    }
    seenIds.add(id);
  }

  return feed;
}

const TOPICS = loadTopicMap();

function createMessage(article) {
  const topic = TOPICS[article.category];
  if (!topic) {
    console.log("Skipping disabled or unmapped category:", article.category, article.id);
    return null;
  }

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
      content_url:
        "https://bilgilimedia-60d6f.web.app/content/" +
        encodeURIComponent(String(article.id || "")),
    },
    android: {
      priority: "high",
      ttl: 3600 * 1000,
    },
  };
}

async function main() {
  const feed = readFeed(FEED_PATH, "current");
  const ids = feed.articles.map((article) => String(article.id));

  if (!PREVIOUS_FEED_PATH || !fs.existsSync(PREVIOUS_FEED_PATH)) {
    console.log(
      "Feed validation completed. No previous feed was supplied, so no notifications will be sent.",
      "Version:",
      feed.version || 0,
      "Articles:",
      ids.length
    );
    return;
  }

  const previousFeed = readFeed(PREVIOUS_FEED_PATH, "previous");
  const previousIds = new Set(
    previousFeed.articles.map((article) => String(article.id))
  );
  const fresh = feed.articles.filter(
    (article) => !previousIds.has(String(article.id))
  );

  if (fresh.length === 0) {
    console.log(
      "No new articles.",
      "Feed version:",
      feed.version || 0,
      "Articles:",
      ids.length
    );
    return;
  }

  initializeApp({
    credential: applicationDefault(),
    projectId: "bilgilimedia-60d6f",
  });

  let sent = 0;
  for (const article of fresh) {
    const message = createMessage(article);
    if (!message) continue;

    const id = await getMessaging().send(message);
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

  console.log("New articles:", fresh.length, "Pushes sent:", sent);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
