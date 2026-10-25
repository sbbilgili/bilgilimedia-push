"use strict";

const HOST = "bilgilimedia-60d6f.web.app";
const params = new URLSearchParams(location.search);
const qa = params.get("qa") === "1";
const packageName = qa
  ? "com.bilgilimedia.mobile.qa"
  : "com.bilgilimedia.mobile";
const parts = location.pathname.split("/").filter(Boolean);

let targetPath = "/app";
let targetQuery = "";
let fallbackUrl = "https://spoo.me/bilgilimedia";

if (parts[0] === "open" && parts[1] === "content" && parts[2]) {
  const id = decodeURIComponent(parts[2]);
  const version = params.get("v");
  targetPath = "/content/" + encodeURIComponent(id);
  targetQuery = version ? "?v=" + encodeURIComponent(version) : "";
  fallbackUrl =
    "https://" +
    HOST +
    targetPath +
    targetQuery;
}

const intentUrl =
  "intent://" +
  HOST +
  targetPath +
  targetQuery +
  "#Intent;scheme=https;package=" +
  packageName +
  ";S.browser_fallback_url=" +
  encodeURIComponent(fallbackUrl) +
  ";end";

const appLink = document.getElementById("open-app");
const webLink = document.getElementById("open-web");
appLink.href = intentUrl;
webLink.href = fallbackUrl;

let fallbackTimer = window.setTimeout(() => {
  window.location.replace(fallbackUrl);
}, 1800);

document.addEventListener("visibilitychange", () => {
  if (document.hidden && fallbackTimer) {
    window.clearTimeout(fallbackTimer);
    fallbackTimer = 0;
  }
});

window.location.replace(intentUrl);
