package com.bilgilimedia.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AtomicFile;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class DataRepository {
    static final String FEED_URL = "https://bilgilimedia-60d6f.web.app/feed.json";
    static final String CONFIG_URL = "https://bilgilimedia-60d6f.web.app/app-config.json";
    static final String HOST_BASE = "https://bilgilimedia-60d6f.web.app";
    static final String PREFS = "preferences";

    private static final String TRUSTED_HOST = "bilgilimedia-60d6f.web.app";
    private static final int MAX_BYTES = 4_000_000;
    private static final long CACHE_FRESH_MS = 300_000L;

    static final class Snapshot {
        int version = 1;
        String featuredId = "";
        String feedRaw = "";
        String configRaw = "";
        AppConfig config = new AppConfig();
        final List<Models.Article> articles = new ArrayList<>();
    }

    static Snapshot load(Context context, boolean forceNetwork) throws Exception {
        String feed = loadRemoteOrFallback(
                context, FEED_URL, "feed-cache.json", "feed.json", forceNetwork);
        String config = loadRemoteOrFallback(
                context, CONFIG_URL, "config-cache.json", "app-config.json", forceNetwork);

        Snapshot snapshot = parseFeed(feed);
        snapshot.feedRaw = feed;
        snapshot.configRaw = config;
        snapshot.config = AppConfig.parse(config);

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt("feed_version", snapshot.version)
                .putString("content_cache", feed)
                .putString("app_config_cache", config)
                .putString("config_updated_at", snapshot.config.updatedAt)
                .putLong("content_updated_at", System.currentTimeMillis())
                .apply();
        return snapshot;
    }

    static Snapshot parseFeed(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        Snapshot snapshot = new Snapshot();
        snapshot.version = Math.max(1, root.optInt("version", 1));
        snapshot.featuredId = root.optString("featuredId", "");

        JSONArray array = root.optJSONArray("articles");
        if (array == null) return snapshot;

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) continue;

            Models.Article article = new Models.Article();
            article.id = object.optString("id", "");
            article.categoryId = object.optString("categoryId", "");
            article.category = object.optString("category", "Genel");
            article.title = object.optString("title", "Yeni içerik");
            article.summary = object.optString("summary", "");
            article.body = object.optString("body", "");
            article.minutes = Math.max(1, object.optInt("minutes", 1));
            article.imageUrl = httpsOrEmpty(object.optString("imageUrl", ""));
            article.imageCredit = object.optString("imageCredit", "");
            article.imageSource = httpsOrEmpty(object.optString("imageSource", ""));
            article.videoUrl = httpsOrEmpty(object.optString("videoUrl", ""));
            article.videoCredit = object.optString("videoCredit", "");
            article.videoSource = httpsOrEmpty(object.optString("videoSource", ""));

            JSONArray sources = object.optJSONArray("sources");
            if (sources != null) {
                for (int j = 0; j < sources.length(); j++) {
                    JSONObject sourceObject = sources.optJSONObject(j);
                    if (sourceObject == null) continue;

                    String sourceUrl = httpsOrEmpty(sourceObject.optString("url", ""));
                    if (sourceUrl.isEmpty()) continue;

                    Models.Source source = new Models.Source();
                    source.title = sourceObject.optString("title", "Kaynak");
                    source.url = sourceUrl;
                    article.sources.add(source);
                }
            }

            if (!article.id.isEmpty() && !article.title.isEmpty()) {
                snapshot.articles.add(article);
            }
        }
        return snapshot;
    }

    private static String loadRemoteOrFallback(
            Context context,
            String url,
            String cacheName,
            String assetName,
            boolean forceNetwork) throws Exception {
        File cache = new File(context.getFilesDir(), cacheName);

        if (!forceNetwork
                && cache.exists()
                && System.currentTimeMillis() - cache.lastModified() < CACHE_FRESH_MS) {
            String cached = readFile(cache);
            if (isJsonObject(cached)) return cached;
            cache.delete();
        }

        try {
            String remote = fetch(url);
            if (!isJsonObject(remote)) {
                throw new IllegalStateException("Invalid JSON response");
            }
            writeAtomic(cache, remote);
            return remote;
        } catch (Exception networkFailure) {
            if (cache.exists()) {
                try {
                    String cached = readFile(cache);
                    if (isJsonObject(cached)) return cached;
                } catch (Exception ignored) {
                    // Continue to SharedPreferences/assets fallback.
                }
            }

            SharedPreferences prefs =
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String key = assetName.equals("feed.json")
                    ? "content_cache"
                    : "app_config_cache";
            String stored = prefs.getString(key, "");
            if (stored != null && isJsonObject(stored)) return stored;

            String bundled = read(context.getAssets().open(assetName));
            if (!isJsonObject(bundled)) {
                throw new IllegalStateException("Invalid bundled JSON: " + assetName);
            }
            return bundled;
        }
    }

    static String fetch(String value) throws Exception {
        URL url = new URL(value);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalArgumentException("HTTPS required");
        }
        if (!TRUSTED_HOST.equalsIgnoreCase(url.getHost())) {
            throw new IllegalArgumentException("Untrusted host");
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(8_000);
        connection.setReadTimeout(10_000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("User-Agent", "BilgiliMedia/1.4.9");

        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("HTTP " + status);
            }

            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_BYTES) {
                throw new IllegalStateException("Response too large");
            }

            return read(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    static String read(InputStream input) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
                if (builder.length() > MAX_BYTES) {
                    throw new IllegalStateException("Input too large");
                }
            }
            return builder.toString();
        }
    }

    private static String readFile(File file) throws Exception {
        return read(new FileInputStream(file));
    }

    private static void writeAtomic(File file, String raw) throws Exception {
        AtomicFile atomicFile = new AtomicFile(file);
        FileOutputStream output = null;
        try {
            output = atomicFile.startWrite();
            output.write(raw.getBytes(StandardCharsets.UTF_8));
            output.flush();
            atomicFile.finishWrite(output);
        } catch (Exception e) {
            if (output != null) atomicFile.failWrite(output);
            throw e;
        }
    }

    private static boolean isJsonObject(String raw) {
        if (raw == null || raw.isEmpty()) return false;
        try {
            new JSONObject(raw);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String httpsOrEmpty(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            URL url = new URL(value);
            return "https".equalsIgnoreCase(url.getProtocol()) ? value : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    static Set<String> getKnownIds(Context context) {
        return new HashSet<>(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getStringSet("known_content_ids", new HashSet<>()));
    }

    static void setKnownIds(Context context, Set<String> ids) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean("known_content_initialized", true)
                .putStringSet("known_content_ids", new HashSet<>(ids))
                .apply();
    }
}
