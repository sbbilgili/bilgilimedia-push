const fs = require("fs");
const { URL } = require("url");

function readJson(file) {
  return JSON.parse(fs.readFileSync(file, "utf8"));
}

function requireHttps(value, label) {
  if (!value) return;
  let url;
  try {
    url = new URL(String(value));
  } catch {
    throw new Error(label + " is not a valid URL: " + value);
  }
  if (url.protocol !== "https:") {
    throw new Error(label + " must use HTTPS: " + value);
  }
}

const feed = readJson("content/feed.json");
const config = readJson("hosting/app-config.json");
const assetLinks = readJson("hosting/.well-known/assetlinks.json");
const firebase = readJson("firebase.json");

if (!Number.isInteger(feed.version) || feed.version < 1) {
  throw new Error("feed.version must be a positive integer");
}
if (!Array.isArray(feed.articles) || feed.articles.length < 1) {
  throw new Error("feed.articles must be a non-empty array");
}

if (!Array.isArray(config.categories) || config.categories.length < 1) {
  throw new Error("app-config categories must be a non-empty array");
}

const enabledCategories = new Map();
const topics = new Set();
for (const item of config.categories) {
  if (!item || item.enabled === false) continue;
  const name = String(item.name || "").trim();
  const topic = String(item.topic || "").trim();
  if (!name || !topic) throw new Error("Enabled category is missing name/topic");
  if (!/^[a-zA-Z0-9._~%-]+$/.test(topic)) {
    throw new Error("Invalid FCM topic: " + topic);
  }
  if (enabledCategories.has(name)) throw new Error("Duplicate category: " + name);
  if (topics.has(topic)) throw new Error("Duplicate FCM topic: " + topic);
  enabledCategories.set(name, topic);
  topics.add(topic);
}

const ids = new Set();
for (const article of feed.articles) {
  const id = String(article?.id || "").trim();
  const title = String(article?.title || "").trim();
  const category = String(article?.category || "").trim();
  const summary = String(article?.summary || "").trim();
  const body = String(article?.body || "").trim();

  if (!id || !title || !category || !summary || !body) {
    throw new Error("Article missing required data: " + (id || title || "<unknown>"));
  }
  if (ids.has(id)) throw new Error("Duplicate article id: " + id);
  ids.add(id);

  if (!enabledCategories.has(category)) {
    throw new Error("Article category is not enabled in app-config: " + category);
  }

  if (!Array.isArray(article.sources) || article.sources.length < 1) {
    throw new Error("Article has no sources: " + id);
  }
  for (const source of article.sources) {
    requireHttps(source?.url, "Source URL for " + id);
  }
  requireHttps(article.imageUrl, "Image URL for " + id);
  requireHttps(article.videoUrl, "Video URL for " + id);
}

if (Array.isArray(config.extraPages)) {
  const pageIds = new Set();
  for (const page of config.extraPages) {
    if (!page || page.enabled === false) continue;
    const id = String(page.id || "").trim();
    if (!id || pageIds.has(id)) throw new Error("Invalid or duplicate extra page id");
    pageIds.add(id);
    requireHttps(page.url, "Extra page URL " + id);
    const url = new URL(page.url);
    if (url.hostname !== "bilgilimedia-60d6f.web.app") {
      throw new Error("Extra page must stay on BilgiliMedia Hosting: " + page.url);
    }
  }
}

const sponsorAds = config.sponsorAds || {};
if (sponsorAds.enabled) {
  if (!Array.isArray(sponsorAds.items)) throw new Error("sponsorAds.items must be an array");
  for (const item of sponsorAds.items) {
    if (!item || item.enabled === false) continue;
    if (!item.id || !item.advertiser || !item.headline) {
      throw new Error("Enabled sponsor item is missing required fields");
    }
    requireHttps(item.targetUrl, "Sponsor target URL " + item.id);
  }
}

if (!Array.isArray(assetLinks) || assetLinks.length < 1) {
  throw new Error("assetlinks.json must contain at least one statement");
}
for (const statement of assetLinks) {
  const target = statement?.target || {};
  if (target.namespace !== "android_app") {
    throw new Error("assetlinks target namespace must be android_app");
  }
  if (!String(target.package_name || "").startsWith("com.bilgilimedia.mobile")) {
    throw new Error("Unexpected Android package in assetlinks: " + target.package_name);
  }
  if (!Array.isArray(target.sha256_cert_fingerprints) || target.sha256_cert_fingerprints.length < 1) {
    throw new Error("assetlinks target must include a SHA-256 fingerprint");
  }
  for (const fp of target.sha256_cert_fingerprints) {
    if (!/^([0-9A-F]{2}:){31}[0-9A-F]{2}$/i.test(String(fp))) {
      throw new Error("Invalid SHA-256 fingerprint format: " + fp);
    }
  }
}

if (!firebase?.hosting?.public || !Array.isArray(firebase?.hosting?.headers)) {
  throw new Error("firebase.json hosting configuration is incomplete");
}

const globalHeaderRule = firebase.hosting.headers.find((rule) => rule.source === "**");
const cspHeader = globalHeaderRule?.headers?.find(
  (header) => String(header.key).toLowerCase() === "content-security-policy"
);
if (!cspHeader || !String(cspHeader.value).includes("script-src 'self'")) {
  throw new Error("Strict Content-Security-Policy is missing");
}
if (String(cspHeader.value).includes("'unsafe-inline'")) {
  throw new Error("Content-Security-Policy must not allow unsafe-inline");
}
for (const file of [
  "hosting/assets/site.css",
  "hosting/assets/content.js",
]) {
  if (!fs.existsSync(file)) throw new Error("Missing hosting asset: " + file);
}

console.log(
  "Validation passed:",
  feed.articles.length,
  "articles,",
  enabledCategories.size,
  "enabled categories,",
  assetLinks.length,
  "assetlinks statement(s)."
);
