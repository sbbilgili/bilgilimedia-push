package com.bilgilimedia.mobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int REQ_NOTIFICATION = 1408;
    private static final String WEB = "https://spoo.me/bilgilimedia";
    private static final String PRIVACY = "https://bilgilimedia-60d6f.web.app/privacy/";
    private static final String EMAIL = "sukrubatuhanbilgili@gmail.com";

    private final ExecutorService io = Executors.newFixedThreadPool(4);
    private final List<Models.Article> articles = new ArrayList<>();
    private final List<Models.SocialAccount> accounts = SocialData.all();
    private AppConfig config = new AppConfig();
    private SharedPreferences prefs;
    private LinearLayout root;
    private LinearLayout content;
    private LinearLayout bottomNav;
    private ScrollView scroll;
    private String screen = "home";
    private String query = "";
    private String typeFilter = "Tümü";
    private String sortFilter = "Yeni";
    private String categoryFilter = "Tümü";
    private String accountPlatformFilter = "Tümü";
    private String pendingArticleId = "";
    private int feedVersion = 1;
    private String featuredId = "";
    private boolean dark = false;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(DataRepository.PREFS, MODE_PRIVATE);
        applyThemeMode();
        buildShell();
        pendingArticleId = extractArticleId(getIntent());
        requestNotificationPermissionIfNeeded();
        load(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        pendingArticleId = extractArticleId(intent);
        openPendingArticle();
    }

    @Override
    public void onBackPressed() {
        if (!"home".equals(screen)) {
            showScreen("home");
        } else {
            Toast.makeText(this, "Ana sayfadasın", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg());

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(10), dp(16), dp(26));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(dp(4), dp(5), dp(4), dp(5));
        bottomNav.setBackgroundColor(card());
        root.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));
        setContentView(root);
        renderBottomNav();
        styleSystemBars();
    }

    private void load(boolean force) {
        if (loading) return;
        loading = true;
        if (articles.isEmpty()) renderLoading();
        io.execute(() -> {
            try {
                DataRepository.Snapshot snapshot = DataRepository.load(this, force);
                runOnUiThread(() -> {
                    articles.clear();
                    articles.addAll(snapshot.articles);
                    config = snapshot.config;
                    feedVersion = snapshot.version;
                    featuredId = snapshot.featuredId;
                    NotificationSync.saveTopicMap(this, config);
                    NotificationSync.sync(this, config);
                    initializeKnownContent();
                    loading = false;
                    showScreen(screen);
                    openPendingArticle();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loading = false;
                    showScreen(screen);
                    Toast.makeText(this, "İçerikler yüklenemedi. Çevrimdışı yedek kullanılıyor olabilir.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void initializeKnownContent() {
        if (prefs.getBoolean("known_content_initialized", false)) return;
        Set<String> ids = new HashSet<>();
        for (Models.Article a : articles) ids.add(a.id);
        DataRepository.setKnownIds(this, ids);
    }

    private void renderLoading() {
        content.removeAllViews();
        TextView t = tv("Güncelleniyor…", 16, secondary(), false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(80), 0, 0);
        content.addView(t);
    }

    private void showScreen(String target) {
        screen = target == null ? "home" : target;
        content.removeAllViews();
        switch (screen) {
            case "search": renderSearch(); break;
            case "saved": renderSaved(); break;
            case "accounts": renderAccounts(); break;
            case "more": renderMore(); break;
            case "history": renderHistory(); break;
            case "notifications": renderNotifications(); break;
            case "settings": renderSettings(); break;
            case "privacy": renderPrivacy(); break;
            case "about": renderAbout(); break;
            case "developer": renderDeveloper(); break;
            default: screen = "home"; renderHome(); break;
        }
        renderBottomNav();
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    private void renderBottomNav() {
        bottomNav.removeAllViews();
        addNav("⌂", "Ana Sayfa", "home");
        addNav("⌕", "Ara", "search");
        addNav("♡", "Kayıtlar", "saved");
        addNav("◉", "Hesaplar", "accounts");
        addNav("⋯", "Daha", "more");
    }

    private void addNav(String icon, String label, String target) {
        boolean selected = target.equals(screen) || ("more".equals(target) && isMoreSubscreen(screen));
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(3), dp(2), dp(3), dp(2));
        item.setClickable(true);
        item.setFocusable(true);
        item.setContentDescription(label);
        TextView i = tv(icon, 20, selected ? brand() : secondary(), true);
        i.setGravity(Gravity.CENTER);
        item.addView(i, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        TextView l = tv(label, 11, selected ? brand() : secondary(), selected);
        l.setGravity(Gravity.CENTER);
        l.setMaxLines(1);
        item.addView(l);
        item.setOnClickListener(v -> showScreen(target));
        bottomNav.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
    }

    private boolean isMoreSubscreen(String value) {
        return "history".equals(value) || "notifications".equals(value) || "settings".equals(value)
                || "privacy".equals(value) || "about".equals(value) || "developer".equals(value);
    }

    private void renderHeader(boolean refresh) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView brandTitle = tv("BİLGİLİMEDIA", 24, text(), true);
        row.addView(brandTitle, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (refresh) {
            TextView live = pill("Canlı", true);
            row.addView(live);
            TextView r = pill("↻ Yenile", false);
            r.setOnClickListener(v -> load(true));
            row.addView(r);
        }
        content.addView(row);
    }

    private void renderHome() {
        renderHeader(true);
        TextView hello = tv("Bugün ne keşfetmek istersin?", 23, text(), true);
        hello.setPadding(0, dp(12), 0, dp(3));
        content.addView(hello);
        TextView sub = tv("Güncel kısa okumalar, popüler içerikler ve tüm BilgiliMedia hesapları tek uygulamada.", 14, secondary(), false);
        sub.setPadding(0, 0, 0, dp(12));
        content.addView(sub);
        content.addView(categoryRail(false));

        Models.Article featured = featuredArticle();
        if (featured != null) {
            sectionTitle("Öne Çıkan", "Editör seçimi");
            content.addView(featuredCard(featured));
        }
        renderSponsor("home_after_featured");

        List<Models.Article> newest = new ArrayList<>(articles);
        Collections.reverse(newest);
        sectionTitle("Yeni Eklenenler", "Canlı akıştaki son içerikler");
        content.addView(horizontalArticles(limit(newest, 8)));

        List<Models.Article> recent = historyArticles();
        if (!recent.isEmpty()) {
            sectionTitle("Son Okunanlar", "Kaldığın yerden devam et");
            content.addView(horizontalArticles(limit(recent, 8)));
        }

        List<Models.Article> popular = new ArrayList<>(articles);
        popular.sort((a,b) -> Integer.compare(readCount(b.id), readCount(a.id)));
        sectionTitle("En Çok Okunanlar", "Bu cihazdaki okuma sayılarına göre");
        content.addView(horizontalArticles(limit(popular, 8)));

        if (!"Tümü".equals(categoryFilter)) {
            List<Models.Article> cat = filterCategory(categoryFilter);
            if (!cat.isEmpty()) {
                sectionTitle("Bu kategoriden seçtiklerimiz", categoryFilter);
                content.addView(horizontalArticles(limit(cat, 8)));
            }
        }

        sectionTitle("Hızlı Erişim", "Sosyal hesaplarımız");
        LinearLayout platforms = new LinearLayout(this);
        platforms.setOrientation(LinearLayout.HORIZONTAL);
        for (String p : new String[]{"Instagram","YouTube","TikTok","Facebook"}) {
            TextView chip = pill(p, false);
            chip.setOnClickListener(v -> { accountPlatformFilter = p; showScreen("accounts"); });
            platforms.addView(chip);
        }
        HorizontalScrollView ps = new HorizontalScrollView(this);
        ps.setHorizontalScrollBarEnabled(false);
        ps.addView(platforms);
        content.addView(ps);
    }

    private View categoryRail(boolean compact) {
        HorizontalScrollView sc = new HorizontalScrollView(this);
        sc.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2), 0, dp(8));
        TextView all = chip("Tümü", categoryFilter.equals("Tümü"));
        all.setOnClickListener(v -> { categoryFilter = "Tümü"; if (compact) renderSearch(); else renderHome(); });
        row.addView(all);
        for (Models.Category c : config.categories) {
            TextView t = chip(c.name, categoryFilter.equals(c.name));
            t.setOnClickListener(v -> { categoryFilter = c.name; if (compact) renderSearch(); else renderHome(); });
            row.addView(t);
        }
        sc.addView(row);
        return sc;
    }

    private View featuredCard(Models.Article a) {
        LinearLayout card = card();
        card.setPadding(0,0,0,dp(14));
        FrameLayout cover = cover(a, dp(190));
        card.addView(cover, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(190)));
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(13), dp(16), 0);
        TextView meta = tv(a.category + " • " + a.minutes + " DK" + (a.hasVideo() ? " • ▶ VIDEO" : ""), 12, brand(), true);
        body.addView(meta);
        TextView title = tv(a.title, 21, text(), true);
        title.setPadding(0, dp(6), 0, dp(6));
        body.addView(title);
        TextView summary = tv(a.summary, 14, secondary(), false);
        summary.setMaxLines(3);
        body.addView(summary);
        TextView read = pill("İçeriği oku  →", true);
        read.setGravity(Gravity.CENTER);
        read.setOnClickListener(v -> openArticle(a));
        body.addView(read, margin(-1, dp(44), 0, dp(12),0,0));
        card.addView(body);
        card.setOnClickListener(v -> openArticle(a));
        return card;
    }

    private HorizontalScrollView horizontalArticles(List<Models.Article> list) {
        HorizontalScrollView sc = new HorizontalScrollView(this);
        sc.setHorizontalScrollBarEnabled(false);
        sc.setFillViewport(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (Models.Article a : list) {
            LinearLayout c = card();
            c.setPadding(0,0,0,dp(10));
            FrameLayout cover = cover(a, dp(105));
            c.addView(cover, new LinearLayout.LayoutParams(dp(250), dp(105)));
            LinearLayout b = new LinearLayout(this);
            b.setOrientation(LinearLayout.VERTICAL);
            b.setPadding(dp(12), dp(9), dp(12), 0);
            b.addView(tv(a.category + " • " + a.minutes + " DK" + (a.hasVideo()?" • ▶ VIDEO":""), 11, brand(), true));
            TextView title = tv(a.title, 16, text(), true);
            title.setMaxLines(2);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setPadding(0, dp(5), 0, dp(8));
            b.addView(title);
            TextView action = tv("Okumaya başla →", 12, brand(), true);
            b.addView(action);
            c.addView(b);
            c.setOnClickListener(v -> openArticle(a));
            row.addView(c, margin(dp(250), ViewGroup.LayoutParams.WRAP_CONTENT, 0,0,dp(10),0));
        }
        sc.addView(row);
        return sc;
    }

    private void renderSponsor(String placement) {
        if (!config.sponsorEnabled) return;
        for (Models.Sponsor s : config.sponsors) {
            if (!s.enabled || (!s.placement.isEmpty() && !placement.equals(s.placement))) continue;
            LinearLayout c = card();
            c.addView(tv("SPONSORLU", 10, secondary(), true));
            c.addView(tv(s.advertiser, 12, brand(), true));
            TextView h = tv(s.headline, 18, text(), true); h.setPadding(0,dp(4),0,dp(4)); c.addView(h);
            c.addView(tv(s.text, 13, secondary(), false));
            TextView open = pill("Reklamı aç →", false); open.setOnClickListener(v -> openUrl(s.targetUrl)); c.addView(open);
            content.addView(c, margin(-1,-2,0,dp(10),0,dp(6)));
        }
    }

    private void renderSearch() {
        renderHeader(false);
        content.addView(tv("Ara ve keşfet", 24, text(), true));
        TextView desc = tv("Başlık, kategori, tür ve popülerliğe göre filtrele.", 14, secondary(), false);
        desc.setPadding(0,dp(3),0,dp(12)); content.addView(desc);

        EditText q = new EditText(this);
        q.setSingleLine(true);
        q.setText(query);
        q.setSelection(q.length());
        q.setHint("Örn. tarih, doğa, teknoloji...");
        q.setTextColor(text()); q.setHintTextColor(muted());
        q.setBackground(round(card(), 16));
        q.setPadding(dp(14),0,dp(14),0);
        content.addView(q, margin(-1,dp(48),0,0,0,dp(12)));
        filterLabel("İçerik türü");
        content.addView(chipRow(new String[]{"Tümü","Video","Yazı"}, typeFilter, v -> {typeFilter=(String)v; renderSearch();}));
        filterLabel("Sıralama");
        content.addView(chipRow(new String[]{"Yeni","Popüler"}, sortFilter, v -> {sortFilter=(String)v; renderSearch();}));
        filterLabel("Kategori");
        content.addView(categoryRail(true));

        LinearLayout results = new LinearLayout(this);
