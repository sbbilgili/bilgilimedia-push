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
    private static final int MAX_ARTICLES = 1_000;
    private static final int MAX_SOURCES_PER_ARTICLE = 20;
    private static final long CACHE_FRESH_MS = 300_000L;

    private interface RawValidator {
        void validate(String raw) throws Exception;
    }

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
                context,
                FEED_URL,
                "feed-cache.json",
                "feed.json",
                forceNetwork,
                raw -> parseFeed(raw));
        String config = loadRemoteOrFallback(
                context,
                CONFIG_URL,
                "config-cache.json",
                "app-config.json",
                forceNetwork,
                raw -> AppConfig.parse(raw));

        Snapshot snapshot = parseFeed(feed);
        AppConfig parsedConfig = AppConfig.parse(config);
        snapshot.feedRaw = feed;
        snapshot.configRaw = config;
        snapshot.config = parsedConfig;

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt("feed_version", snapshot.version)
                .putString("content_cache", feed)
                .putString("app_config_cache", config)
                .putString("config_updated_at", parsedConfig.updatedAt)
                .putLong("content_updated_at", System.currentTimeMillis())
                .apply();
        return snapshot;
    }

    static Snapshot parseFeed(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        int version = root.optInt("version", 0);
        if (version < 1) throw new IllegalArgumentException("Invalid feed version");

        JSONArray array = root.optJSONArray("articles");
        if (array == null || array.length() < 1 || array.length() > MAX_ARTICLES) {
            throw new IllegalArgumentException("Invalid article count");
        }

        Snapshot snapshot = new Snapshot();
        snapshot.version = version;
        snapshot.featuredId = root.optString("featuredId", "").trim();

        Set<String> ids = new HashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.optJSONObject(i);
            if (object == null) {
                throw new IllegalArgumentException("Invalid article at index " + i);
            }

            Models.Article article = new Models.Article();
            article.id = object.optString("id", "").trim();
            article.categoryId = object.optString("categoryId", "").trim();
            article.category = object.optString("category", "").trim();
            article.title = object.optString("title", "").trim();
            article.summary = object.optString("summary", "").trim();
            article.body = object.optString("body", "").trim();

            if (article.id.isEmpty()
                    || article.category.isEmpty()
                    || article.title.isEmpty()
                    || article.summary.isEmpty()
                    || article.body.isEmpty()) {
                throw new IllegalArgumentException("Article missing required data");
            }
            if (!ids.add(article.id)) {
                throw new IllegalArgumentException("Duplicate article id: " + article.id);
            }

            article.minutes = Math.max(1, object.optInt("minutes", 1));
            article.imageUrl = optionalHttps(object.optString("imageUrl", ""), "imageUrl");
            article.imageCredit = object.optString("imageCredit", "").trim();
            article.imageSource = optionalHttps(
                    object.optString("imageSource", ""), "imageSource");
            article.videoUrl = optionalHttps(object.optString("videoUrl", ""), "videoUrl");
            article.videoCredit = object.optString("videoCredit", "").trim();
            article.videoSource = optionalHttps(
                    object.optString("videoSource", ""), "videoSource");

            JSONArray sources = object.optJSONArray("sources");
            if (sources == null
                    || sources.length() < 1
                    || sources.length() > MAX_SOURCES_PER_ARTICLE) {
                throw new IllegalArgumentException("Invalid article sources: " + article.id);
            }

            for (int j = 0; j < sources.length(); j++) {
                JSONObject sourceObject = sources.optJSONObject(j);
                if (sourceObject == null) {
                    throw new IllegalArgumentException(
                            "Invalid source for article: " + article.id);
                }

                Models.Source source = new Models.Source();
                source.title = sourceObject.optString("title", "Kaynak").trim();
                source.url = requiredHttps(
                        sourceObject.optString("url", ""), "source URL");
                if (source.title.isEmpty()) source.title = "Kaynak";
                article.sources.add(source);
            }

            snapshot.articles.add(article);
        }

        if (!snapshot.featuredId.isEmpty() && !ids.contains(snapshot.featuredId)) {
            throw new IllegalArgumentException("featuredId is not present in articles");
        }

        return snapshot;
    }

    private static String loadRemoteOrFallback(
            Context context,
            String url,
            String cacheName,
            String assetName,
            boolean forceNetwork,
            RawValidator validator) throws Exception {
        File cache = new File(context.getFilesDir(), cacheName);

        if (!forceNetwork
                && cache.exists()
                && System.currentTimeMillis() - cache.lastModified() < CACHE_FRESH_MS) {
            try {
                String cached = readFile(cache);
                validator.validate(cached);
                return cached;
            } catch (Exception invalidCache) {
                cache.delete();
            }
        }

        try {
            String remote = fetch(url);
            validator.validate(remote);
            writeAtomic(cache, remote);
            return remote;
        } catch (Exception remoteFailure) {
            if (cache.exists()) {
                try {
                    String cached = readFile(cache);
                    validator.validate(cached);
                    return cached;
                } catch (Exception invalidCache) {
                    cache.delete();
                }
            }

            SharedPreferences prefs =
                    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String key = assetName.equals("feed.json")
                    ? "content_cache"
                    : "app_config_cache";
            String stored = prefs.getString(key, "");
            if (stored != null && !stored.isEmpty()) {
                try {
                    validator.validate(stored);
                    return stored;
                } catch (Exception ignored) {
                    // Continue to the immutable bundled fallback.
                }
            }

            String bundled = read(context.getAssets().open(assetName));
            validator.validate(bundled);
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

    private static String optionalHttps(String value, String label) throws Exception {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) return "";
        return requiredHttps(trimmed, label);
    }

    private static String requiredHttps(String value, String label) throws Exception {
        String trimmed = value == null ? "" : value.trim();
        URL url = new URL(trimmed);
        if (!"https".equalsIgnoreCase(url.getProtocol())
                || url.getHost() == null
                || url.getHost().isEmpty()) {
            throw new IllegalArgumentException(label + " must use HTTPS");
        }
        return trimmed;
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
