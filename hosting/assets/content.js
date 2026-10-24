"use strict";

const FEED = "/feed.json";
const parts = location.pathname.split("/").filter(Boolean);
const id =
  parts[0] === "content" && parts[1]
    ? decodeURIComponent(parts[1])
    : new URLSearchParams(location.search).get("id");
const root = document.getElementById("content");

function element(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined && text !== null) node.textContent = String(text);
  return node;
}

function renderMessage(title, message) {
  root.replaceChildren(
    element("h1", "", title),
    element("p", "muted", message)
  );
}

(async () => {
  try {
    const response = await fetch(FEED, {
      cache: "no-store",
      credentials: "omit",
      referrerPolicy: "no-referrer",
    });
    if (!response.ok) throw new Error("Feed request failed");

    const feed = await response.json();
    if (!feed || !Array.isArray(feed.articles)) throw new Error("Invalid feed");

    const article = feed.articles.find((item) => String(item.id) === String(id));
    if (!article) {
      renderMessage("İçerik bulunamadı", "Bağlantı güncel olmayabilir.");
      return;
    }

    document.title = String(article.title || "BilgiliMedia İçerik") + " • BilgiliMedia";

    const category = element(
      "div",
      "cat",
      String(article.category || "") + " • " + String(article.minutes || "") + " dk okuma"
    );
    const title = element("h1", "", article.title || "BilgiliMedia İçerik");
    const summary = element("p", "summary", article.summary || "");
    const body = element("div", "body", article.body || "");

    const socialParagraph = document.createElement("p");
    const socialLink = document.createElement("a");
    socialLink.href = "https://spoo.me/bilgilimedia";
    socialLink.rel = "noopener noreferrer";
    socialLink.textContent = "BilgiliMedia hesapları";
    socialParagraph.appendChild(socialLink);

    root.replaceChildren(category, title, summary, body, socialParagraph);
  } catch (error) {
    console.error("Content load failed");
    renderMessage(
      "İçerik yüklenemedi",
      "İnternet bağlantını kontrol edip tekrar deneyebilirsin."
    );
  }
})();
