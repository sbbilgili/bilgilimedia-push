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

function requireStableId(value, label) {
  const id = String(value || "").trim();
  if (!/^[a-z0-9][a-z0-9_-]{0,63}$/.test(id)) {
    throw new Error(label + " must be a stable lowercase id: " + id);
  }
  return id;
}

function requireOrder(value, label) {
  if (!Number.isInteger(value) || value < 0) {
    throw new Error(label + " must be a non-negative integer");
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

if (!Number.isInteger(config.schemaVersion) || config.schemaVersion < 2) {
  throw new Error("app-config schemaVersion must be 2 or newer");
}
if (!config.compatibility || config.compatibility.legacyCategoryList !== true) {
  throw new Error("app-config must preserve the legacy category list for current clients");
}
if (!Array.isArray(config.categories) || config.categories.length < 1) {
  throw new Error("app-config categories must be a non-empty array");
}
if (!Array.isArray(config.sections) || config.sections.length < 1) {
  throw new Error("app-config sections must be a non-empty array");
}

const sections = new Map();
for (const section of config.sections) {
  if (!section || section.enabled === false) continue;
  const id = requireStableId(section.id, "Section id");
  const title = String(section.title || "").trim();
  if (!title) throw new Error("Enabled section is missing title: " + id);
  requireOrder(section.order, "Section order " + id);
  if (sections.has(id)) throw new Error("Duplicate section id: " + id);
  sections.set(id, section);
}

const enabledCategories = new Map();
const categoriesById = new Map();
const topics = new Set();
for (const item of config.categories) {
  if (!item || item.enabled === false) continue;
  const id = requireStableId(item.id, "Category id");
  const name = String(item.name || "").trim();
  const topic = String(item.topic || "").trim();
  const sectionId = requireStableId(item.sectionId, "Category sectionId " + id);
  if (!name || !topic) throw new Error("Enabled category is missing name/topic: " + id);
  if (!/^[a-zA-Z0-9._~%-]+$/.test(topic)) {
    throw new Error("Invalid FCM topic: " + topic);
  }
  requireOrder(item.order, "Category order " + id);
  if (!sections.has(sectionId)) throw new Error("Unknown section for category " + id + ": " + sectionId);
  if (categoriesById.has(id)) throw new Error("Duplicate category id: " + id);
  if (enabledCategories.has(name)) throw new Error("Duplicate category name: " + name);
  if (topics.has(topic)) throw new Error("Duplicate FCM topic: " + topic);
  categoriesById.set(id, item);
  enabledCategories.set(name, item);
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

  const categoryConfig = enabledCategories.get(category);
  if (!categoryConfig) {
    throw new Error("Article category is not enabled in app-config: " + category);
  }
  if (article.categoryId != null) {
    const categoryId = requireStableId(article.categoryId, "Article categoryId " + id);
    if (!categoriesById.has(categoryId)) {
      throw new Error("Article categoryId is not enabled in app-config: " + categoryId);
    }
    if (categoriesById.get(categoryId).name !== category) {
      throw new Error("Article category/categoryId mismatch: " + id);
    }
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

const enabledPages = new Map();
if (Array.isArray(config.extraPages)) {
  for (const page of config.extraPages) {
    if (!page || page.enabled === false) continue;
    const id = requireStableId(page.id, "Extra page id");
    if (enabledPages.has(id)) throw new Error("Duplicate extra page id: " + id);
    requireHttps(page.url, "Extra page URL " + id);
    const url = new URL(page.url);
    if (url.hostname !== "bilgilimedia-60d6f.web.app") {
      throw new Error("Extra page must stay on BilgiliMedia Hosting: " + page.url);
    }
    enabledPages.set(id, page);
  }
}

const navigation = config.navigation;
if (!navigation || navigation.version !== 1 || !Array.isArray(navigation.items) || navigation.items.length < 1) {
  throw new Error("navigation.version=1 with a non-empty items array is required");
}
if (!Number.isInteger(navigation.maxDepth) || navigation.maxDepth < 1 || navigation.maxDepth > 6) {
  throw new Error("navigation.maxDepth must be between 1 and 6");
}

const allowedScreens = new Set(["home", "saved", "history", "accounts", "notifications", "about"]);
const menuIds = new Set();
const navigationCategoryIds = new Set();

function validateMenuNode(node, depth) {
  if (!node || node.enabled === false) return;
  if (depth > navigation.maxDepth) throw new Error("Navigation exceeds maxDepth");
  const id = requireStableId(node.id, "Navigation item id");
  const title = String(node.title || "").trim();
  const type = String(node.type || "").trim();
  if (!title) throw new Error("Navigation item is missing title: " + id);
  requireOrder(node.order, "Navigation order " + id);
  if (menuIds.has(id)) throw new Error("Duplicate navigation item id: " + id);
  menuIds.add(id);

  if (type === "group") {
    if (!Array.isArray(node.children) || node.children.length < 1) {
      throw new Error("Enabled navigation group must have children: " + id);
    }
    for (const child of node.children) validateMenuNode(child, depth + 1);
    return;
  }

  if (type === "category") {
    const categoryId = requireStableId(node.categoryId, "Navigation categoryId " + id);
    if (!categoriesById.has(categoryId)) {
      throw new Error("Navigation references unknown category: " + categoryId);
    }
    navigationCategoryIds.add(categoryId);
    return;
  }

  if (type === "screen") {
    const screen = String(node.screen || "").trim();
    if (!allowedScreens.has(screen)) throw new Error("Navigation references unsupported screen: " + screen);
    return;
  }

  if (type === "page") {
    const pageId = requireStableId(node.pageId, "Navigation pageId " + id);
    if (!enabledPages.has(pageId)) throw new Error("Navigation references unknown page: " + pageId);
    return;
  }

  throw new Error("Unsupported navigation item type: " + type);
}

for (const item of navigation.items) validateMenuNode(item, 1);
for (const categoryId of categoriesById.keys()) {
  if (!navigationCategoryIds.has(categoryId)) {
    throw new Error("Enabled category is missing from navigation: " + categoryId);
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

if (!Array.isArray(assetLinks) || assetLinks.length < 2) {
  throw new Error("assetlinks.json must contain preview and production statements");
}

const requiredAppLinkPackages = new Set([
  "com.bilgilimedia.mobile.media.preview",
  "com.bilgilimedia.mobile",
]);
const seenAppLinkPackages = new Set();

for (const statement of assetLinks) {
  const target = statement?.target || {};
  if (target.namespace !== "android_app") {
    throw new Error("assetlinks target namespace must be android_app");
  }
  const packageName = String(target.package_name || "");
  if (!packageName.startsWith("com.bilgilimedia.mobile")) {
    throw new Error("Unexpected Android package in assetlinks: " + packageName);
  }
  if (requiredAppLinkPackages.has(packageName)) {
    seenAppLinkPackages.add(packageName);
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

for (const packageName of requiredAppLinkPackages) {
  if (!seenAppLinkPackages.has(packageName)) {
    throw new Error("Missing required App Links package: " + packageName);
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
  categoriesById.size,
  "enabled categories,",
  sections.size,
  "sections,",
  menuIds.size,
  "navigation nodes,",
  assetLinks.length,
  "assetlinks statement(s)."
);
