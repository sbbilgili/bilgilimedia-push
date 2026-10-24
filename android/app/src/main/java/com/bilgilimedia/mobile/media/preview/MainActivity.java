package com.bilgilimedia.mobile.media.preview;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String FEED_URL = "https://bilgilimedia-60d6f.web.app/feed.json";
    private static final String CONFIG_URL = "https://bilgilimedia-60d6f.web.app/app-config.json";
    private static final String HOST_BASE = "https://bilgilimedia-60d6f.web.app";
    private static final String PREFS = "bilgilimedia_prefs";
    private static final String KEY_SAVED = "saved_ids";
    private static final String KEY_HISTORY = "history_json";

    private final List<Article> articles = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private final Map<String, Article> articleById = new HashMap<>();

    private LinearLayout categoryBar;
    private LinearLayout contentList;
    private TextView statusView;
    private TextView sectionTitle;

    private int feedVersion = 1;
    private String configUpdatedAt = "";
    private String activeCategory = "";
    private String currentMode = "discover";
    private String pendingArticleId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        requestNotificationPermission();
        handleIntent(getIntent());
        loadData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        openPendingArticleIfReady();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(248, 249, 251));
        root.setPadding(dp(16), dp(12), dp(16), dp(12));

        TextView title = text("BilgiliMedia", 26, true);
        title.setTextColor(Color.rgb(24, 29, 35));
        root.addView(title);

        TextView subtitle = text("Kısa, kaynaklı ve güncel içerikler", 13, false);
        subtitle.setTextColor(Color.rgb(95, 103, 112));
        subtitle.setPadding(0, dp(2), 0, dp(10));
        root.addView(subtitle);

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        navScroll.addView(nav);
        nav.addView(navButton("Keşfet", v -> showDiscover()));
        nav.addView(navButton("Kaydedilenler", v -> showSaved()));
        nav.addView(navButton("Geçmiş", v -> showHistory()));
        nav.addView(navButton("Bildirimler", v -> showNotificationSettings()));
        nav.addView(navButton("Hakkında", v -> showAbout()));
        root.addView(navScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        HorizontalScrollView categoryScroll = new HorizontalScrollView(this);
        categoryScroll.setHorizontalScrollBarEnabled(false);
        categoryBar = new LinearLayout(this);
        categoryBar.setOrientation(LinearLayout.HORIZONTAL);
        categoryBar.setPadding(0, dp(10), 0, dp(8));
        categoryScroll.addView(categoryBar);
        root.addView(categoryScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        sectionTitle = text("Keşfet", 19, true);
        sectionTitle.setTextColor(Color.rgb(24, 29, 35));
        sectionTitle.setPadding(0, dp(4), 0, dp(4));
        root.addView(sectionTitle);

        statusView = text("İçerikler hazırlanıyor…", 13, false);
        statusView.setTextColor(Color.rgb(95, 103, 112));
        statusView.setPadding(0, 0, 0, dp(8));
        root.addView(statusView);

        ScrollView contentScroll = new ScrollView(this);
        contentList = new LinearLayout(this);
        contentList.setOrientation(LinearLayout.VERTICAL);
        contentList.setPadding(0, 0, 0, dp(24));
        contentScroll.addView(contentList);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(contentScroll, scrollParams);

        setContentView(root);
    }

    private Button navButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(13);
        button.setOnClickListener(listener);
        button.setPadding(dp(10), 0, dp(10), 0);
        return button;
    }

    private void loadData() {
        statusView.setText("İçerikler güncelleniyor…");
        new Thread(() -> {
            try {
                String feedRaw = loadRemoteOrFallback(
                        FEED_URL, "feed-cache.json", "feed.json");
                String configRaw = loadRemoteOrFallback(
                        CONFIG_URL, "config-cache.json", "app-config.json");

                FeedResult feed = parseFeed(feedRaw);
                ConfigResult config = parseConfig(configRaw);

                articles.clear();
                articles.addAll(feed.articles);
                articleById.clear();
                for (Article article : articles) {
                    articleById.put(article.id, article);
                }
                categories.clear();
                categories.addAll(config.categories);
                feedVersion = feed.version;
                configUpdatedAt = config.updatedAt;

                SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                prefs.edit()
                        .putInt("feed_version", feedVersion)
                        .putString("config_updated_at", configUpdatedAt)
                        .apply();

                syncTopics(configRaw, this);

                runOnUiThread(() -> {
                    renderCategoryButtons();
                    showDiscover();
                    statusView.setText(
                            articles.size() + " içerik • feed v" + feedVersion);
                    openPendingArticleIfReady();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    statusView.setText("İçerikler yüklenemedi. İnternet bağlantını kontrol et.");
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String loadRemoteOrFallback(String remoteUrl, String cacheName, String assetName) throws Exception {
        try {
            String remote = fetchText(remoteUrl);
            writeFile(new File(getFilesDir(), cacheName), remote);
            return remote;
        } catch (Exception ignored) {
            File cache = new File(getFilesDir(), cacheName);
            if (cache.exists()) {
                return readStream(new FileInputStream(cache));
            }
            return readStream(getAssets().open(assetName));
        }
    }

    private static String fetchText(String value) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(value).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "BilgiliMedia-Android/1.4.8");
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code);
        }
        try (InputStream input = connection.getInputStream()) {
            return readStream(input);
        } finally {
            connection.disconnect();
        }
    }

    private static String readStream(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    private static void writeFile(File file, String content) throws Exception {
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private FeedResult parseFeed(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        FeedResult result = new FeedResult();
        result.version = Math.max(1, root.optInt("version", 1));
        JSONArray array = root.optJSONArray("articles");
        if (array == null) return result;

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            Article article = new Article();
            article.id = item.optString("id", "");
            article.title = item.optString("title", "");
            article.summary = item.optString("summary", "");
            article.body = item.optString("body", "");
            article.category = item.optString("category", "");
            article.minutes = item.optInt("minutes", 0);
            article.videoUrl = item.optString("videoUrl", "");
            JSONArray sources = item.optJSONArray("sources");
            if (sources != null && sources.length() > 0) {
                JSONObject source = sources.optJSONObject(0);
                if (source != null) {
                    article.sourceTitle = source.optString("title", "Kaynak");
                    article.sourceUrl = source.optString("url", "");
                }
            }
            if (!TextUtils.isEmpty(article.id) && !TextUtils.isEmpty(article.title)) {
                result.articles.add(article);
            }
        }
        return result;
    }

    private ConfigResult parseConfig(String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        ConfigResult result = new ConfigResult();
        result.updatedAt = root.optString("updatedAt", "");
        JSONArray array = root.optJSONArray("categories");
        if (array == null) return result;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null || !item.optBoolean("enabled", true)) continue;
            String name = item.optString("name", "");
            String topic = item.optString("topic", "");
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(topic)) {
                result.categories.add(new Category(name, topic));
            }
        }
        return result;
    }

    private void renderCategoryButtons() {
        categoryBar.removeAllViews();
        categoryBar.addView(categoryButton("Tümü", ""));
        for (Category category : categories) {
            categoryBar.addView(categoryButton(category.name, category.topic));
        }
    }

    private Button categoryButton(String label, String topic) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(12);
        button.setPadding(dp(10), 0, dp(10), 0);
        button.setOnClickListener(v -> {
            activeCategory = topic;
            currentMode = "discover";
            renderCategoryButtons();
            showDiscover();
        });
        boolean selected = TextUtils.equals(activeCategory, topic);
        button.setTextColor(selected ? Color.WHITE : Color.rgb(50, 56, 63));
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(dp(18));
        background.setColor(selected ? Color.rgb(215, 25, 32) : Color.rgb(235, 238, 242));
        button.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(38));
        params.setMargins(0, 0, dp(7), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void showDiscover() {
        currentMode = "discover";
        sectionTitle.setText(TextUtils.isEmpty(activeCategory) ? "Keşfet" : categoryName(activeCategory));
        List<Article> filtered = new ArrayList<>();
        for (Article article : articles) {
            if (TextUtils.isEmpty(activeCategory) ||
                    topicForCategory(article.category).equals(activeCategory)) {
                filtered.add(article);
            }
        }
        renderArticles(filtered);
    }

    private void showSaved() {
        currentMode = "saved";
        activeCategory = "";
        renderCategoryButtons();
        sectionTitle.setText("Kaydedilenler");
        Set<String> saved = getSaved();
        List<Article> filtered = new ArrayList<>();
        for (Article article : articles) {
            if (saved.contains(article.id)) filtered.add(article);
        }
        renderArticles(filtered);
    }

    private void showHistory() {
        currentMode = "history";
        activeCategory = "";
        renderCategoryButtons();
        sectionTitle.setText("Geçmiş");
        List<String> history = getHistory();
        List<Article> filtered = new ArrayList<>();
        for (String id : history) {
            Article article = articleById.get(id);
            if (article != null) filtered.add(article);
        }
        renderArticles(filtered);
    }

    private void renderArticles(List<Article> list) {
        contentList.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = text(
                    currentMode.equals("saved") ? "Henüz kaydedilmiş içerik yok." :
                            currentMode.equals("history") ? "Henüz okuma geçmişi yok." :
                                    "Bu kategoride içerik bulunamadı.",
                    14, false);
            empty.setTextColor(Color.rgb(95, 103, 112));
            empty.setPadding(0, dp(18), 0, 0);
            contentList.addView(empty);
            return;
        }

        Set<String> saved = getSaved();
        for (Article article : list) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(15), dp(14), dp(15), dp(12));
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.WHITE);
            background.setCornerRadius(dp(16));
            background.setStroke(dp(1), Color.rgb(228, 232, 237));
            card.setBackground(background);

            TextView category = text(
                    article.category + (article.minutes > 0 ? " • " + article.minutes + " dk" : ""),
                    12, true);
            category.setTextColor(Color.rgb(215, 25, 32));
            card.addView(category);

            TextView articleTitle = text(article.title, 18, true);
            articleTitle.setTextColor(Color.rgb(24, 29, 35));
            articleTitle.setPadding(0, dp(5), 0, dp(5));
            card.addView(articleTitle);

            TextView summary = text(article.summary, 14, false);
            summary.setTextColor(Color.rgb(76, 84, 93));
            card.addView(summary);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setGravity(Gravity.END);
            actions.setPadding(0, dp(8), 0, 0);

            Button save = compactButton(saved.contains(article.id) ? "Kaldır" : "Kaydet");
            save.setOnClickListener(v -> {
                toggleSaved(article.id);
                if (currentMode.equals("saved")) showSaved();
                else renderArticles(list);
            });
            actions.addView(save);

            Button read = compactButton("Oku");
            read.setOnClickListener(v -> openArticle(article));
            actions.addView(read);

            card.addView(actions);
            card.setOnClickListener(v -> openArticle(article));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(10));
            contentList.addView(card, params);
        }
    }

    private Button compactButton(String label) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(12);
        button.setPadding(dp(9), 0, dp(9), 0);
        return button;
    }

    private void openArticle(Article article) {
        addHistory(article.id);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(8), dp(18), dp(8));
        scroll.addView(body);

        TextView category = text(article.category, 12, true);
        category.setTextColor(Color.rgb(215, 25, 32));
        body.addView(category);

        TextView title = text(article.title, 22, true);
        title.setTextColor(Color.rgb(24, 29, 35));
        title.setPadding(0, dp(6), 0, dp(8));
        body.addView(title);

        TextView summary = text(article.summary, 15, true);
        summary.setTextColor(Color.rgb(65, 72, 80));
        summary.setPadding(0, 0, 0, dp(10));
        body.addView(summary);

        TextView text = text(article.body, 16, false);
        text.setTextColor(Color.rgb(39, 45, 52));
        text.setLineSpacing(0f, 1.18f);
        body.addView(text);

        if (!TextUtils.isEmpty(article.sourceUrl)) {
            Button source = new Button(this);
            source.setAllCaps(false);
            source.setText("Kaynak: " + (TextUtils.isEmpty(article.sourceTitle) ? "Aç" : article.sourceTitle));
            source.setOnClickListener(v -> openHttps(article.sourceUrl));
            body.addView(source);
        }

        if (!TextUtils.isEmpty(article.videoUrl)) {
            Button video = new Button(this);
            video.setAllCaps(false);
            video.setText("Videoyu aç");
            video.setOnClickListener(v -> openHttps(article.videoUrl));
            body.addView(video);
        }

        Button share = new Button(this);
        share.setAllCaps(false);
        share.setText("Paylaş");
        share.setOnClickListener(v -> shareArticle(article));
        body.addView(share);

        Button save = new Button(this);
        save.setAllCaps(false);
        save.setText(getSaved().contains(article.id) ? "Kaydedilenlerden kaldır" : "Kaydet");
        save.setOnClickListener(v -> {
            toggleSaved(article.id);
            save.setText(getSaved().contains(article.id) ? "Kaydedilenlerden kaldır" : "Kaydet");
        });
        body.addView(save);

        new AlertDialog.Builder(this)
                .setView(scroll)
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void shareArticle(Article article) {
        String url = HOST_BASE + "/content/" + Uri.encode(article.id) + "?v=" + feedVersion;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, article.title);
        intent.putExtra(Intent.EXTRA_TEXT, article.title + "\n" + url);
        startActivity(Intent.createChooser(intent, "İçeriği paylaş"));
    }

    private void showNotificationSettings() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(8), dp(18), dp(8));

        TextView intro = text("Bildirim almak istediğin kategorileri seç.", 14, false);
        intro.setTextColor(Color.rgb(65, 72, 80));
        intro.setPadding(0, 0, 0, dp(8));
        body.addView(intro);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        for (Category category : categories) {
            CheckBox box = new CheckBox(this);
            box.setText(category.name);
            box.setTextSize(16);
            String key = "topic_" + category.topic;
            box.setChecked(prefs.getBoolean(key, true));
            box.setOnCheckedChangeListener((button, checked) -> {
                prefs.edit().putBoolean(key, checked).apply();
                if (checked) {
                    FirebaseMessaging.getInstance().subscribeToTopic(category.topic);
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(category.topic);
                }
            });
            body.addView(box);
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            TextView warning = text("Telefon bildirim izni kapalı.", 13, true);
            warning.setTextColor(Color.rgb(180, 65, 40));
            body.addView(warning);
        }

        new AlertDialog.Builder(this)
                .setTitle("Bildirimler")
                .setView(body)
                .setPositiveButton("Tamam", null)
                .show();
    }

    private void showAbout() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(8), dp(18), dp(8));

        TextView about = text(
                "BilgiliMedia, kaynak gösteren kısa bilgi içeriklerini tek yerde sunar.\n\n" +
                        "Geliştirici: Şükrü Batuhan Bilgili",
                15, false);
        about.setTextColor(Color.rgb(45, 52, 60));
        body.addView(about);

        TextView version = text("Sürüm 1.4.8 (22)", 13, true);
        version.setTextColor(Color.rgb(100, 108, 116));
        version.setPadding(0, dp(14), 0, dp(8));
        version.setOnLongClickListener(v -> {
            showDiagnostics();
            return true;
        });
        body.addView(version);

        Button privacy = new Button(this);
        privacy.setAllCaps(false);
        privacy.setText("Gizlilik politikası");
        privacy.setOnClickListener(v -> openHttps(HOST_BASE + "/privacy/"));
        body.addView(privacy);

        new AlertDialog.Builder(this)
                .setTitle("Hakkında")
                .setView(body)
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void showDiagnostics() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    String tokenPart = "alınamadı";
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        tokenPart = token.length() > 10
                                ? "…" + token.substring(token.length() - 10)
                                : token;
                    }

                    SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
                    List<String> active = new ArrayList<>();
                    for (Category category : categories) {
                        if (prefs.getBoolean("topic_" + category.topic, true)) {
                            active.add(category.topic);
                        }
                    }

                    boolean notificationsAllowed = Build.VERSION.SDK_INT < 33 ||
                            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                                    == PackageManager.PERMISSION_GRANTED;

                    String message =
                            "Paket: " + getPackageName() + "\n" +
                            "Sürüm: 1.4.8 (22)\n" +
                            "Feed: v" + feedVersion + "\n" +
                            "Config: " + (TextUtils.isEmpty(configUpdatedAt) ? "bilinmiyor" : configUpdatedAt) + "\n" +
                            "Bildirim izni: " + (notificationsAllowed ? "açık" : "kapalı") + "\n" +
                            "Aktif topic: " + TextUtils.join(", ", active) + "\n" +
                            "FCM token sonu: " + tokenPart;

                    new AlertDialog.Builder(this)
                            .setTitle("Tanılama")
                            .setMessage(message)
                            .setPositiveButton("Kapat", null)
                            .show();
                });
    }

    private void openHttps(String url) {
        try {
            Uri uri = Uri.parse(url);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                Toast.makeText(this, "Yalnız güvenli HTTPS bağlantıları açılır.", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception error) {
            Toast.makeText(this, "Bağlantı açılamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    private Set<String> getSaved() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        return new LinkedHashSet<>(prefs.getStringSet(KEY_SAVED, Collections.emptySet()));
    }

    private void toggleSaved(String id) {
        Set<String> saved = getSaved();
        if (saved.contains(id)) saved.remove(id);
        else saved.add(id);
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putStringSet(KEY_SAVED, new HashSet<>(saved))
                .apply();
    }

    private List<String> getHistory() {
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_HISTORY, "[]");
        List<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String id = array.optString(i, "");
                if (!TextUtils.isEmpty(id)) result.add(id);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private void addHistory(String id) {
        List<String> history = getHistory();
        history.remove(id);
        history.add(0, id);
        while (history.size() > 50) history.remove(history.size() - 1);
        JSONArray array = new JSONArray();
        for (String item : history) array.put(item);
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORY, array.toString())
                .apply();
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String articleId = intent.getStringExtra("article_id");
        if (TextUtils.isEmpty(articleId) && intent.getData() != null) {
            Uri data = intent.getData();
            List<String> segments = data.getPathSegments();
            if (segments.size() >= 2 && "content".equals(segments.get(0))) {
                articleId = segments.get(1);
            }
        }
        if (!TextUtils.isEmpty(articleId)) pendingArticleId = articleId;
    }

    private void openPendingArticleIfReady() {
        if (TextUtils.isEmpty(pendingArticleId)) return;
        Article article = articleById.get(pendingArticleId);
        if (article != null) {
            pendingArticleId = null;
            openArticle(article);
        }
    }

    private String categoryName(String topic) {
        for (Category category : categories) {
            if (category.topic.equals(topic)) return category.name;
        }
        return "Keşfet";
    }

    private String topicForCategory(String categoryName) {
        for (Category category : categories) {
            if (category.name.equals(categoryName)) return category.topic;
        }
        return "";
    }

    private TextView text(String value, int sizeSp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1408);
        }
    }

    public static void syncTopicsFromCachedConfig(Context context) {
        try {
            File cache = new File(context.getFilesDir(), "config-cache.json");
            String raw;
            if (cache.exists()) {
                raw = readStream(new FileInputStream(cache));
            } else {
                raw = readStream(context.getAssets().open("app-config.json"));
            }
            syncTopics(raw, context);
        } catch (Exception ignored) {
        }
    }

    private static void syncTopics(String raw, Context context) {
        try {
            JSONObject root = new JSONObject(raw);
            JSONArray array = root.optJSONArray("categories");
            if (array == null) return;
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null || !item.optBoolean("enabled", true)) continue;
                String topic = item.optString("topic", "");
                if (TextUtils.isEmpty(topic)) continue;
                boolean enabled = prefs.getBoolean("topic_" + topic, true);
                if (enabled) {
                    FirebaseMessaging.getInstance().subscribeToTopic(topic);
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static class FeedResult {
        int version = 1;
        final List<Article> articles = new ArrayList<>();
    }

    private static class ConfigResult {
        String updatedAt = "";
        final List<Category> categories = new ArrayList<>();
    }

    private static class Article {
        String id = "";
        String title = "";
        String summary = "";
        String body = "";
        String category = "";
        int minutes = 0;
        String sourceTitle = "";
        String sourceUrl = "";
        String videoUrl = "";
    }

    private static class Category {
        final String name;
        final String topic;

        Category(String name, String topic) {
            this.name = name;
            this.topic = topic;
        }
    }
}
