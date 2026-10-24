package com.bilgilimedia.mobile;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;

final class DataRepository {
    static final String BASE = "https://bilgilimedia-60d6f.web.app";
    static final String FEED_URL = BASE + "/feed.json";
    static final String CONFIG_URL = BASE + "/app-config.json";

    interface Callback {
        void onLoaded(Models.Feed feed, Models.Config config, boolean cacheUsed);
        void onError(Exception error);
    }

    private final Context context;

    DataRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    void load(Callback callback) {
        new Thread(() -> {
            try {
                boolean[] cacheUsed = {false};
                String feedRaw = remoteOrFallback(FEED_URL, "feed-cache.json", "feed.json", cacheUsed);
                String configRaw = remoteOrFallback(CONFIG_URL, "config-cache.json", "app-config.json", cacheUsed);
                Models.Feed feed = parseFeed(feedRaw);
                Models.Config config = parseConfig(configRaw);
                new LocalStore(context).putConfigCache(configRaw);
                new LocalStore(context).putFeedVersion(feed.version);
                callback.onLoaded(feed, config, cacheUsed[0]);
            } catch (Exception error) {
                callback.onError(error);
            }
        }).start();
    }

    static String fetch(String value) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(value).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "BilgiliMedia/1.4.9");
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) throw new IllegalStateException("HTTP " + code);
        try (InputStream input = connection.getInputStream()) {
            return read(input);
        } finally {
            connection.disconnect();
        }
    }

    private String remoteOrFallback(String url, String cacheName, String assetName, boolean[] cacheUsed) throws Exception {
        try {
            String remote = fetch(url);
            write(new File(context.getFilesDir(), cacheName), remote);
            return remote;
        } catch (Exception remoteError) {
            File cache = new File(context.getFilesDir(), cacheName);
            if (cache.exists()) {
                cacheUsed[0] = true;
                return read(new FileInputStream(cache));
            }
            cacheUsed[0] = true;
            return read(context.getAssets().open(assetName));
        }
    }

    static Models.Feed parseFeed(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        Models.Feed feed = new Models.Feed();
        feed.version = Math.max(1, root.optInt("version", 1));
        feed.featuredId = root.optString("featuredId", "");
        JSONArray array = root.optJSONArray("articles");
        if (array == null) return feed;

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            Models.Article a = new Models.Article();
            a.id = item.optString("id", "");
            a.categoryId = item.optString("categoryId", "");
            a.category = item.optString("category", "Genel");
            a.title = item.optString("title", "Yeni içerik");
            a.summary = item.optString("summary", "");
            a.body = item.optString("body", "");
            a.minutes = Math.max(0, item.optInt("minutes", 0));
            a.imageUrl = item.optString("imageUrl", "");
            a.imageCredit = item.optString("imageCredit", "");
            a.imageSource = item.optString("imageSource", "");
            a.videoUrl = item.optString("videoUrl", "");
            a.videoCredit = item.optString("videoCredit", "");
            a.videoSource = item.optString("videoSource", "");
            JSONArray sources = item.optJSONArray("sources");
            if (sources != null) {
                for (int j = 0; j < sources.length(); j++) {
                    JSONObject source = sources.optJSONObject(j);
                    if (source == null) continue;
                    Models.Source s = new Models.Source();
                    s.title = source.optString("title", "Kaynak");
                    s.url = source.optString("url", "");
                    a.sources.add(s);
                }
            }
            if (!a.id.isEmpty()) feed.articles.add(a);
        }
        return feed;
    }

    static Models.Config parseConfig(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        Models.Config config = new Models.Config();
        config.schemaVersion = root.optInt("schemaVersion", 1);
        config.updatedAt = root.optString("updatedAt", "");

        JSONArray categories = root.optJSONArray("categories");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject item = categories.optJSONObject(i);
                if (item == null) continue;
                Models.Category c = new Models.Category();
                c.id = item.optString("id", item.optString("topic", ""));
                c.name = item.optString("name", "");
                c.topic = item.optString("topic", "");
                c.sectionId = item.optString("sectionId", "");
                c.order = item.optInt("order", i * 10);
                c.enabled = item.optBoolean("enabled", true);
                if (c.enabled && !c.name.isEmpty() && !c.topic.isEmpty()) config.categories.add(c);
            }
        }
        Collections.sort(config.categories, Comparator.comparingInt(value -> value.order));

        JSONArray pages = root.optJSONArray("extraPages");
        if (pages != null) {
            for (int i = 0; i < pages.length(); i++) {
                JSONObject item = pages.optJSONObject(i);
                if (item == null) continue;
                Models.ExtraPage p = new Models.ExtraPage();
                p.id = item.optString("id", "");
                p.title = item.optString("title", "");
                p.subtitle = item.optString("subtitle", "");
                p.type = item.optString("type", "web");
                p.url = item.optString("url", "");
                p.enabled = item.optBoolean("enabled", true);
                if (p.enabled) config.extraPages.add(p);
            }
        }

        JSONObject sponsorAds = root.optJSONObject("sponsorAds");
        if (sponsorAds != null) {
            config.sponsorAdsEnabled = sponsorAds.optBoolean("enabled", false);
            JSONArray items = sponsorAds.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item == null) continue;
                    Models.Sponsor s = new Models.Sponsor();
                    s.id = item.optString("id", "");
                    s.advertiser = item.optString("advertiser", "");
                    s.headline = item.optString("headline", "");
                    s.text = item.optString("text", "");
                    s.targetUrl = item.optString("targetUrl", "");
                    s.placement = item.optString("placement", "");
                    s.enabled = item.optBoolean("enabled", true);
                    if (s.enabled) config.sponsors.add(s);
                }
            }
        }
        return config;
    }

    private static String read(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) builder.append(line).append('\n');
        return builder.toString();
    }

    private static void write(File file, String content) throws Exception {
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }
}
