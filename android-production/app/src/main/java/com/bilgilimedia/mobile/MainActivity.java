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
import android.view.WindowInsets;
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
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(9), dp(12), dp(8));
        header.setBackgroundColor(card());

        TextView play = tv("▶", 18, Color.WHITE, true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(round(brand(), 16));
        header.addView(play, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout brandBox = new LinearLayout(this);
        brandBox.setOrientation(LinearLayout.VERTICAL);
        brandBox.setPadding(dp(10), 0, 0, 0);
        brandBox.addView(tv("BilgiliMedia", 21, text(), true));
        brandBox.addView(tv("Bilgi • Keşif • Medya", 11, secondary(), false));
        header.addView(brandBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView external = tv("↗", 24, text(), true);
        external.setGravity(Gravity.CENTER);
        external.setOnClickListener(v -> openUrl(WEB));
        header.addView(external, new LinearLayout.LayoutParams(dp(44), dp(44)));
        root.addView(header);

        scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(10), dp(16), dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setBackgroundColor(card());
        bottomNav.setElevation(dp(12));
        root.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        renderBottomNav();
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int bottomInset;
            if (Build.VERSION.SDK_INT >= 30) {
                bottomInset = insets.getInsets(WindowInsets.Type.systemBars()).bottom;
            } else {
                bottomInset = insets.getSystemWindowInsetBottom();
            }
            bottomNav.setPadding(0, dp(4), 0, dp(4) + bottomInset);
            return insets;
        });
        root.requestApplyInsets();
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
        item.setPadding(dp(2), dp(7), dp(2), dp(7));
        item.setClickable(true);
        item.setFocusable(true);
        item.setContentDescription(label);
        item.setBackgroundColor(selected ? (dark ? 0xff2d1a1e : 0xffffecef) : Color.TRANSPARENT);

        TextView iconView = tv(icon, 24, selected ? brand() : secondary(), true);
        iconView.setGravity(Gravity.CENTER);
        item.addView(iconView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(30)));

        TextView labelView = tv(label, 13.5f, selected ? brand() : text(), selected);
        labelView.setGravity(Gravity.CENTER);
        labelView.setMaxLines(1);
        labelView.setEllipsize(TextUtils.TruncateAt.END);
        item.addView(labelView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(29)));

        item.setOnClickListener(v -> showScreen(target));
        bottomNav.addView(item, new LinearLayout.LayoutParams(0, dp(76), 1f));
    }

    private boolean isMoreSubscreen(String value) {
        return "history".equals(value) || "notifications".equals(value) || "settings".equals(value)
                || "privacy".equals(value) || "about".equals(value) || "developer".equals(value);
    }

    private void renderHeader(boolean refresh) {
        if (!refresh) return;
        TextView brandTitle = tv("BİLGİLİMEDIA", 11, brand(), true);
        brandTitle.setPadding(0, 4, 0, 4);
        content.addView(brandTitle);
    }

    private void renderHome() {
        renderHeader(true);
        content.addView(tv("Bugün ne keşfetmek istersin?", 28, text(), true));
        TextView sub = tv("Güncel kısa okumalar, popüler içerikler ve tüm BilgiliMedia hesapları tek uygulamada.", 14, secondary(), false);
        sub.setPadding(0, 4, 0, dp(8));
        content.addView(sub);

        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setGravity(Gravity.CENTER_VERTICAL);
        String statusText = loading ? "Güncelleniyor…" : "Canlı";
        TextView live = tv(statusText, 12, statusText.startsWith("Canlı") ? 0xff1f8b4c : secondary(), true);
        statusRow.addView(live, new LinearLayout.LayoutParams(0, dp(38), 1f));
        TextView refresh = pill("↻ Yenile", false);
        refresh.setOnClickListener(v -> load(true));
        statusRow.addView(refresh);
        content.addView(statusRow);

        sectionTitle("Öne Çıkan", "Editör seçimi");
        Models.Article featured = featuredArticle();
        if (featured != null) content.addView(featuredCard(featured));
        renderSponsor("home_after_featured");

        LinkedHashSet<String> categoryNames = new LinkedHashSet<>();
        for (Models.Article a : articles) categoryNames.add(a.category);
        LinearLayout categoryRow = new LinearLayout(this);
        for (String name : categoryNames) {
            TextView category = pill(name, false);
            category.setOnClickListener(v -> { categoryFilter = name; showScreen("home"); });
            categoryRow.addView(category);
        }
        HorizontalScrollView categoryScroll = new HorizontalScrollView(this);
        categoryScroll.setHorizontalScrollBarEnabled(false);
        categoryScroll.addView(categoryRow);
        content.addView(categoryScroll);

        List<Models.Article> newest = new ArrayList<>(articles);
        Collections.reverse(newest);
        sectionTitle("Yeni Eklenenler", "Canlı akıştaki son içerikler");
        content.addView(horizontalArticles(limit(newest, 6)));

        List<Models.Article> recent = historyArticles();
        if (!recent.isEmpty()) {
            sectionTitle("Son Okunanlar", "Kaldığın yerden devam et");
            content.addView(horizontalArticles(limit(recent, 6)));
        }

        List<Models.Article> popular = new ArrayList<>(articles);
        popular.sort((a,b) -> Integer.compare(readCount(b.id), readCount(a.id)));
        sectionTitle("En Çok Okunanlar", "Bu cihazdaki okuma sayılarına göre");
        content.addView(horizontalArticles(limit(popular, 6)));

        if (!"Tümü".equals(categoryFilter)) {
            List<Models.Article> cat = filterCategory(categoryFilter);
            if (!cat.isEmpty()) {
                sectionTitle("Bu kategoriden seçtiklerimiz", categoryFilter);
                content.addView(horizontalArticles(limit(cat, 6)));
            }
        }

        sectionTitle("Hızlı Erişim", "Sosyal hesaplarımız");
        LinearLayout platforms = new LinearLayout(this);
        for (String platform : new String[]{"Instagram","YouTube","TikTok","Facebook"}) {
            TextView chip = pill(platform, false);
            chip.setOnClickListener(v -> { accountPlatformFilter = platform; showScreen("accounts"); });
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
        LinearLayout cardView = panel();
        cardView.setPadding(0, 0, 0, dp(14));
        cardView.setClipToPadding(false);
        cardView.addView(cover(a, 190, true));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(14), dp(16), 0);

        body.addView(tv(a.category.toUpperCase(Locale.ROOT) + " • " + a.minutes + " DK", 10, brand(), true));
        if (a.hasVideo()) body.addView(tv("▶ VIDEO", 10, 0xffd71920, true));

        TextView title = tv(a.title, 23, text(), true);
        title.setPadding(0, dp(6), 0, dp(5));
        body.addView(title);
        body.addView(tv(a.summary, 14, secondary(), false));

        TextView read = tv("İçeriği oku  →", 14, brand(), true);
        read.setPadding(0, dp(12), 0, 0);
        read.setOnClickListener(v -> openArticle(a));
        body.addView(read);

        cardView.addView(body);
        cardView.setOnClickListener(v -> openArticle(a));
        return cardView;
    }

    private HorizontalScrollView horizontalArticles(List<Models.Article> list) {
        HorizontalScrollView sc = new HorizontalScrollView(this);
        sc.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (Models.Article a : list) {
            LinearLayout cardView = new LinearLayout(this);
            cardView.setOrientation(LinearLayout.VERTICAL);
            cardView.setBackground(round(card(), 18));
            cardView.setClickable(true);
            cardView.setFocusable(true);
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(dp(250), dp(235));
            cardLp.setMargins(0, dp(5), dp(10), dp(5));
            cardView.setLayoutParams(cardLp);

            cardView.addView(cover(a, 118, false));

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(13), dp(10), dp(13), dp(10));
            body.addView(tv(a.category.toUpperCase(Locale.ROOT) + " • " + a.minutes + " DK", 10, brand(), true));
            if (a.hasVideo()) body.addView(tv("▶ VIDEO", 10, 0xffd71920, true));

            TextView title = tv(a.title, 17, text(), true);
            title.setMaxLines(2);
            title.setEllipsize(TextUtils.TruncateAt.END);
            title.setPadding(0, dp(6), 0, dp(4));
            body.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

            int reads = readCount(a.id);
            body.addView(tv(reads > 0 ? reads + " okuma  →" : "Yeni  →", 12, reads > 0 ? secondary() : brand(), true));
            cardView.addView(body, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
            cardView.setOnClickListener(v -> openArticle(a));
            row.addView(cardView);
        }
        sc.addView(row);
        return sc;
    }

    private void renderSponsor(String placement) {
        if (!config.sponsorEnabled) return;
        for (Models.Sponsor s : config.sponsors) {
            if (!s.enabled || (!s.placement.isEmpty() && !placement.equals(s.placement))) continue;
            LinearLayout c = panel();
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
        content.addView(tv("Ara ve keşfet", 27, text(), true));
        TextView desc = tv("Başlık, kategori, tür ve popülerliğe göre filtrele.", 14, secondary(), false);
        desc.setPadding(0, dp(3), 0, dp(12));
        content.addView(desc);

        EditText q = new EditText(this);
        q.setSingleLine(true);
        q.setText(query);
        q.setSelection(q.length());
        q.setHint("Örn. tarih, doğa, teknoloji...");
        q.setTextColor(text());
        q.setHintTextColor(secondary());
        q.setBackground(round(card(), 16));
        q.setPadding(dp(14), 0, dp(14), 0);
        content.addView(q, margin(-1, dp(54), 0, 0, 0, dp(12)));

        filterLabel("İçerik türü");
        content.addView(chipRow(new String[]{"Tümü","Yazı","Video","Favoriler"}, typeFilter, v -> {
            typeFilter = (String) v;
            renderSearch();
        }));

        filterLabel("Sıralama");
        content.addView(chipRow(new String[]{"Yeni","Popüler"}, sortFilter, v -> {
            sortFilter = (String) v;
            renderSearch();
        }));

        filterLabel("Kategori");
        content.addView(categoryRail(true));

        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        content.addView(results);

        q.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                query = value.toString();
            }
            @Override public void afterTextChanged(Editable value) {
                renderSearchResults(results, value.toString());
            }
        });
        renderSearchResults(results, query);
    }

    private interface Choice { void select(Object value); }

    private View chipRow(String[] values, String selected, Choice choice) {
        HorizontalScrollView sc = new HorizontalScrollView(this); sc.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        for (String s : values) {
            TextView c=chip(s,s.equals(selected)); c.setOnClickListener(v->choice.select(s)); row.addView(c);
        }
        sc.addView(row); return sc;
    }

    private void filterLabel(String s) {
        TextView t=tv(s,12,secondary(),true); t.setPadding(0,dp(8),0,dp(5)); content.addView(t);
    }

    private void renderSearchResults(LinearLayout target, String q) {
        target.removeAllViews();
        List<Models.Article> results = new ArrayList<>();
        String needle = normalize(q);
        for (Models.Article a : articles) {
            if (!"Tümü".equals(categoryFilter) && !categoryFilter.equals(a.category)) continue;
            if ("Video".equals(typeFilter) && !a.hasVideo()) continue;
            if ("Yazı".equals(typeFilter) && a.hasVideo()) continue;
            if ("Favoriler".equals(typeFilter) && !isSavedArticle(a.id)) continue;
            String haystack = normalize(a.title + " " + a.summary + " " + a.body + " " + a.category);
            if (!needle.isEmpty() && !haystack.contains(needle)) continue;
            results.add(a);
        }
        if ("Popüler".equals(sortFilter)) {
            results.sort((a,b) -> Integer.compare(readCount(b.id), readCount(a.id)));
        } else {
            Collections.reverse(results);
        }

        TextView count = tv(results.size() + " sonuç", 12, secondary(), true);
        count.setPadding(0, dp(12), 0, dp(4));
        target.addView(count);
        if (results.isEmpty()) {
            TextView emptyState = tv("Bu filtrelerle eşleşen içerik bulunamadı.", 14, secondary(), false);
            emptyState.setGravity(Gravity.CENTER);
            emptyState.setPadding(dp(14), dp(30), dp(14), dp(20));
            target.addView(emptyState);
            return;
        }
        for (Models.Article a : results) target.addView(articleCard(a));
    }

    private View articleCard(Models.Article a) {
        LinearLayout cardView = panel();
        cardView.setPadding(0, 0, 0, dp(10));
        cardView.addView(cover(a, 118, false));

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(11), dp(14), 0);
        body.addView(tv(a.category.toUpperCase(Locale.ROOT) + " • " + a.minutes + " DK", 10, brand(), true));
        if (a.hasVideo()) body.addView(tv("▶ VIDEO", 10, 0xffd71920, true));

        TextView title = tv(a.title, 19, text(), true);
        title.setPadding(0, dp(5), 0, dp(4));
        body.addView(title);
        body.addView(tv(a.summary, 13, secondary(), false));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        TextView read = tv("Okumaya başla →", 13, brand(), true);
        read.setOnClickListener(v -> openArticle(a));
        actions.addView(read, new LinearLayout.LayoutParams(0, dp(40), 1f));

        TextView fav = tv(isSavedArticle(a.id) ? "♥" : "♡", 23, isSavedArticle(a.id) ? brand() : secondary(), true);
        fav.setGravity(Gravity.CENTER);
        fav.setOnClickListener(v -> { toggleSavedArticle(a.id); showScreen(screen); });
        actions.addView(fav, new LinearLayout.LayoutParams(dp(46), dp(40)));

        body.addView(actions);
        cardView.addView(body);
        cardView.setOnClickListener(v -> openArticle(a));
        return cardView;
    }

    private void renderSaved() {
        renderHeader(false);
        content.addView(tv("Kaydedilenler",27,text(),true));
        TextView d=tv("Favorilerin bu cihazda tutulur.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(12)); content.addView(d);
        Set<String> saved=getStringSet("saved");
        int count=0;
        for(Models.Article a:articles) if(saved.contains("a:"+a.id) || saved.contains(a.id)){ content.addView(articleCard(a)); count++; }
        if(count==0) content.addView(empty("Henüz içerik kaydetmedin. ♡ simgesine dokunarak favori oluşturabilirsin."));

        List<Models.SocialAccount> savedAccounts=new ArrayList<>();
        for(Models.SocialAccount a:accounts) if(saved.contains("p:"+a.url)) savedAccounts.add(a);
        if(!savedAccounts.isEmpty()){
            sectionTitle("Kaydedilen hesaplar","Hızlı erişim");
            for(Models.SocialAccount a:savedAccounts) content.addView(accountCard(a));
        }
    }

    private void renderHistory() {
        renderHeader(false);
        content.addView(tv("Okuma Geçmişi",27,text(),true));
        TextView d=tv("Daha önce açtığın içerikler ve son okunma zamanları.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(10)); content.addView(d);
        List<String> ids=historyIds();
        if(ids.isEmpty()) content.addView(empty("Henüz okuma geçmişin yok."));
        DateFormat fmt=new SimpleDateFormat("dd MMM yyyy • HH:mm",new Locale("tr","TR"));
        for(String id:ids){
            Models.Article a=findArticle(id);
            if(a==null) continue;
            LinearLayout box=panel(); box.setPadding(dp(12),dp(10),dp(12),dp(10));
            box.addView(tv(a.title,16,text(),true));
            long ts=prefs.getLong("history_time_"+id,0);
            box.addView(tv(ts>0?fmt.format(new Date(ts)):"Okunma zamanı bilinmiyor",11,secondary(),false));
            box.setOnClickListener(v->openArticle(a));
            content.addView(box,margin(-1,-2,0,0,0,dp(8)));
        }
        if(!ids.isEmpty()){
            TextView clear=pill("Geçmişi temizle",false); clear.setOnClickListener(v->confirm("Okuma geçmişi temizlensin mi?",()->{prefs.edit().remove("history_all").apply();renderHistory();})); content.addView(clear);
        }
    }

    private void renderAccounts() {
        renderHeader(false);
        content.addView(tv("BilgiliMedia hesapları",27,text(),true));
        TextView d=tv("33 sosyal medya hesabımıza tek yerden ulaş.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(10)); content.addView(d);
        content.addView(chipRow(new String[]{"Tümü","Instagram","YouTube","TikTok","Facebook"},accountPlatformFilter,v->{accountPlatformFilter=(String)v;renderAccounts();}));
        for(Models.SocialAccount a:accounts){ if("Tümü".equals(accountPlatformFilter)||accountPlatformFilter.equals(a.platform)) content.addView(accountCard(a)); }
    }

    private View accountCard(Models.SocialAccount a) {
        LinearLayout cardView = panel();
        cardView.addView(tv(a.platform + " • " + socialCategory(a), 16, text(), true));
        String handle = a.handle.startsWith("@") ? a.handle : "@" + a.handle;
        cardView.addView(tv(handle, 13, secondary(), false));

        LinearLayout actions = new LinearLayout(this);
        TextView open = tv("Uygulamada aç →", 13, brand(), true);
        open.setOnClickListener(v -> openSocial(a));
        actions.addView(open, new LinearLayout.LayoutParams(0, dp(38), 1f));

        TextView fav = tv(isSavedAccount(a.url) ? "♥" : "♡", 22, isSavedAccount(a.url) ? brand() : secondary(), true);
        fav.setGravity(Gravity.CENTER);
        fav.setOnClickListener(v -> { toggleSavedAccount(a.url); showScreen(screen); });
        actions.addView(fav, new LinearLayout.LayoutParams(dp(44), dp(38)));

        cardView.addView(actions);
        cardView.setOnClickListener(v -> openSocial(a));
        return cardView;
    }

    private String socialCategory(Models.SocialAccount a) {
        String n = a.name == null ? "" : a.name;
        if ("BilgiliMedia".equals(n)) return "Genel";
        if (n.startsWith("BilgiliMedia ")) return n.substring("BilgiliMedia ".length());
        if (n.startsWith("BilgiliMedia-")) return n.substring("BilgiliMedia-".length());
        if (n.startsWith("Bilgilimedia-")) return n.substring("Bilgilimedia-".length());
        if (n.startsWith("Bilgili ")) return n.substring("Bilgili ".length());
        return n.isEmpty() ? "Genel" : n;
    }

    private void renderMore() {
        renderHeader(false);
        content.addView(tv("Daha",27,text(),true));
        TextView d=tv("Ayarlar, gizlilik ve BilgiliMedia bağlantıları.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(10)); content.addView(d);
        sectionTitle("Uygulama","Tercihler ve kişisel alan");
        content.addView(menuCard("⚙ Ayarlar","Tema ve yerel veriler",()->showScreen("settings")));
        String notifyState=notificationsAllowed()?"Açık":"Kapalı";
        content.addView(menuCard("▣ Bildirimler",notifyState,()->showScreen("notifications")));
        content.addView(menuCard("▤ Okuma Geçmişi",historyIds().size()+" kayıt",()->showScreen("history")));



        sectionTitle("Bilgi","Gizlilik ve uygulama bilgileri");
        content.addView(menuCard("▣ Gizlilik","Veri kullanımı ve gizlilik politikası",()->showScreen("privacy")));
        content.addView(menuCard("ⓘ Hakkında","BilgiliMedia ve sürüm bilgisi",()->showScreen("about")));

        if(!config.extraPages.isEmpty()){
            sectionTitle("Ek sayfalar","BilgiliMedia tarafından uzaktan eklenen bölümler");
            for(Models.ExtraPage p:config.extraPages) content.addView(menuCard("↗ "+p.title,p.subtitle,()->openUrl(p.url)));
        }
        sectionTitle("Bağlantılar","BilgiliMedia");
        content.addView(menuCard("▣ Web sitemiz","spoo.me/bilgilimedia",()->openUrl(WEB)));
        content.addView(menuCard("✉ İletişim",EMAIL,()->email()));
        content.addView(menuCard("↗ Uygulamayı paylaş","Arkadaşlarınla paylaş",this::shareApp));
        TextView v=tv("BilgiliMedia • 1.4.9",11,muted(),false);v.setPadding(0,dp(12),0,dp(8));content.addView(v);
    }

    private void showSection(String sectionId){
        Models.Section section=null; for(Models.Section s:config.sections) if(s.id.equals(sectionId)) section=s;
        if(section==null)return;
        StringBuilder names=new StringBuilder();
        List<Models.Category> list=new ArrayList<>(); for(Models.Category c:config.categories) if(sectionId.equals(c.sectionId)) list.add(c);
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(4),dp(16),dp(4));
        for(Models.Category c:list){ TextView t=pill(c.name,false);t.setOnClickListener(v->{categoryFilter=c.name;showScreen("home");});box.addView(t); }
        new AlertDialog.Builder(this).setTitle(section.title).setView(box).setPositiveButton("Kapat",null).show();
    }

    private LinearLayout menuCard(String title, String subtitle, Runnable action) {
        LinearLayout cardView = panel();
        cardView.addView(tv(title, 16, text(), true));
        cardView.addView(tv(subtitle == null ? "" : subtitle, 12, secondary(), false));
        if (action != null) {
            cardView.setOnClickListener(v -> action.run());
        }
        return cardView;
    }

    private void renderNotifications(){
        renderHeader(false);
        content.addView(tv("Bildirimler",27,text(),true));
        TextView d=tv("Yeni içeriklerde hangi kategorilerden haber almak istediğini seç. Teknik Firebase işlemleri arka planda otomatik yönetilir.",14,secondary(),false);d.setPadding(0,dp(3),0,dp(12));content.addView(d);
        boolean master=prefs.getBoolean("notify_master",true);
        LinearLayout status=panel();status.setPadding(dp(13),dp(11),dp(13),dp(11));
        status.addView(tv("▣ Bildirim durumu",14,text(),true));
        TextView state=pill(master&&notificationsAllowed()?"Açık":"Kapalı",master&&notificationsAllowed());
        state.setOnClickListener(v->{ if(Build.VERSION.SDK_INT>=33&&!notificationsAllowed()){requestNotificationPermissionIfNeeded();return;} prefs.edit().putBoolean("notify_master",!master).apply();NotificationSync.sync(this,config);renderNotifications();});status.addView(state);
        content.addView(status,margin(-1,-2,0,0,0,dp(12)));

        sectionTitle("Kategoriler","İstediğin kategorileri açıp kapatabilirsin");
        Set<String> selected=getNotifyCategories();
        if(selected.isEmpty()) content.addView(tv("Henüz kategori seçmedin. Bildirim almak için en az bir kategori seç.",12,secondary(),false));
        for(Models.Category c:config.categories){
            CheckBox box=new CheckBox(this);box.setText(c.name);box.setTextColor(text());box.setTextSize(15);box.setChecked(selected.contains(c.name));
            box.setOnCheckedChangeListener((b,checked)->{Set<String> s=getNotifyCategories();if(checked)s.add(c.name);else s.remove(c.name);prefs.edit().putStringSet("notify_categories",new HashSet<>(s)).putBoolean("notify_categories_initialized",true).apply();NotificationSync.sync(this,config);});
            content.addView(box);
        }
        TextView auto=tv("Yeni kategoriler eklendiğinde bildirim seçenekleri otomatik güncellenir.",12,secondary(),false);auto.setPadding(0,dp(8),0,0);content.addView(auto);
    }

    private void renderSettings(){
        renderHeader(false);
        content.addView(tv("Ayarlar",27,text(),true));
        TextView d=tv("Görünüm ve cihazında tutulan yerel verileri yönet.",14,secondary(),false);d.setPadding(0,dp(3),0,dp(12));content.addView(d);
        sectionTitle("Görünüm","Temanı seç");
        String theme=prefs.getString("theme","system");
        content.addView(chipRow(new String[]{"Sistem","Açık","Koyu"},themeLabel(theme),v->{String label=(String)v;String val="Sistem".equals(label)?"system":"Koyu".equals(label)?"dark":"light";prefs.edit().putString("theme",val).apply();recreate();}));
        sectionTitle("Bildirimler","Kategori bazlı bildirim tercihleri");
        content.addView(menuCard("▣ Bildirim ayarları",notificationsAllowed()?"Açık":"Kapalı",()->showScreen("notifications")));
        sectionTitle("Yerel veriler","Bu cihazda saklanan kişisel kullanım verileri");
        int saved=countSavedArticles();int hist=historyIds().size();
        content.addView(tv("Kaydedilenler: "+saved+" • Okuma geçmişi: "+hist+" kayıt",13,secondary(),false));
        TextView clear=pill("Yerel kayıtları temizle",false);clear.setOnClickListener(v->confirm("Favoriler, geçmiş, tema ve yerel önbellek temizlensin mi?",this::clearLocalData));content.addView(clear);
    }

    private void renderPrivacy(){
        renderHeader(false);content.addView(tv("Gizlilik",27,text(),true));
        addInfo("BilgiliMedia Android uygulamasında veri kullanımının özeti.","");
        sectionTitle("Cihazda tutulanlar","Yerel tercihler");
        content.addView(tv("Kaydedilen içerikler, okuma geçmişi, son okunanlar, tema seçimi, bildirim tercihleri ve içerik önbelleği cihazında saklanabilir.",14,secondary(),false));
        sectionTitle("Firebase Cloud Messaging","Bildirimler");
        content.addView(tv("Bildirimleri açtığında Firebase Cloud Messaging, bildirimi bu uygulama kurulumuna ulaştırmak için bir kayıt tokenı ve gerekli teknik bilgileri işler.",14,secondary(),false));
        sectionTitle("İzinler","Mevcut üretim adayı");
        content.addView(tv("Uygulama internet erişimi ve Android sürümüne bağlı olarak bildirim izni kullanır. Konum, rehber, kamera, mikrofon, SMS veya genel depolama izni istemez.",14,secondary(),false));
        TextView p=pill("↗ Gizlilik politikasını aç",true);p.setOnClickListener(v->openUrl(PRIVACY));content.addView(p);
    }

    private void renderAbout(){
        renderHeader(false);
        content.addView(tv("Hakkında",27,text(),true));
        content.addView(tv("BilgiliMedia • Bilgi, keşif ve medya",14,secondary(),false));

        sectionTitle("Sahiplik","Geliştirici bilgileri");
        addInfo("Geliştirici ve Kurucu","Şükrü Batuhan Bilgili");
        addInfo("Marka","BilgiliMedia");
        content.addView(tv("© 2026 Şükrü Batuhan Bilgili / BilgiliMedia",12,secondary(),false));

        sectionTitle("Uygulama","Sürüm ve paket bilgileri");
        LinearLayout version = menuCard("Sürüm","1.4.9 • versionCode 23",null);
        version.setOnLongClickListener(v->{showScreen("developer");return true;});
        content.addView(version);
        content.addView(menuCard("Paket",getPackageName(),null));
        content.addView(menuCard("Canlı içerik akışı","Sürüm "+feedVersion+" • "+articles.size()+" içerik",null));
        content.addView(menuCard("Android App Links","Etkin • web.app/content/…",null));

        sectionTitle("","İletişim");
        content.addView(menuCard("▣ Web sitemiz","spoo.me/bilgilimedia",()->openUrl(WEB)));
        content.addView(menuCard("✉ E-posta",EMAIL,this::email));
    }

    private void renderDeveloper(){
        renderHeader(false);content.addView(tv("Geliştirici Tanılama",27,text(),true));
        content.addView(tv("Bu ekran normal kullanıcı menüsünde gösterilmez. Firebase push sorunlarını teşhis etmek için saklanır.",13,secondary(),false));
        String token=prefs.getString("fcm_token","");
        String state=token.isEmpty()?"FCM Push: Başlatıldı • cihaz kaydı bekleniyor":"FCM Push: Hazır • cihaz kayıtlı";
        addInfo("FCM durumu",state);
        addInfo("Cihaz tokenı",token.isEmpty()?"…":"…"+token.substring(Math.max(0,token.length()-12)));
        addInfo("Paket",getPackageName());addInfo("Sürüm","1.4.9 (23)");addInfo("Feed","v"+feedVersion);addInfo("Config",prefs.getString("config_updated_at","bilinmiyor"));
        TextView copy=pill("FCM tokenını kopyala",false);copy.setOnClickListener(v->{ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("FCM token",token));Toast.makeText(this,"Token kopyalandı",Toast.LENGTH_SHORT).show();});content.addView(copy);
        TextView verify=pill("Topic aboneliklerini doğrula",false);verify.setOnClickListener(v->{NotificationSync.sync(this,config);Toast.makeText(this,"Topic eşleşmesi yenilendi",Toast.LENGTH_SHORT).show();});content.addView(verify);
        TextView renew=pill("FCM kaydını yenile",false);renew.setOnClickListener(v->FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(t->{FirebaseMessaging.getInstance().getToken().addOnSuccessListener(x->{prefs.edit().putString("fcm_token",x).apply();NotificationSync.sync(this,config);renderDeveloper();});}));content.addView(renew);
        TextView local=pill("Yerel test bildirimi gönder",true);local.setOnClickListener(v->NotificationHelper.show(this,"","Genel","BilgiliMedia test bildirimi","Bildirim altyapısı çalışıyor."));content.addView(local);
    }

    private void openArticle(Models.Article a) {
        screen = "article";
        addHistory(a.id);
        incrementRead(a.id);
        content.removeAllViews();
        renderBottomNav();

        TextView back = tv("← Geri", 14, brand(), true);
        back.setPadding(0, 4, 0, dp(14));
        back.setOnClickListener(v -> showScreen("home"));
        content.addView(back);

        content.addView(cover(a, 175, true));

        TextView meta = tv(a.category.toUpperCase(Locale.ROOT) + " • " + a.minutes + " DK OKUMA", 11, brand(), true);
        meta.setPadding(0, dp(14), 0, 0);
        content.addView(meta);

        TextView title = tv(a.title, 29, text(), true);
        title.setPadding(0, dp(8), 0, dp(10));
        content.addView(title);
        content.addView(tv(a.summary, 16, secondary(), false));

        if (!a.imageCredit.isEmpty()) {
            TextView credit = tv("Görsel: " + a.imageCredit + (!a.imageSource.isEmpty() ? " • Kaynağı aç" : ""), 11, secondary(), false);
            if (!a.imageSource.isEmpty()) credit.setOnClickListener(v -> openUrl(a.imageSource));
            content.addView(credit);
        }

        LinearLayout actions = new LinearLayout(this);
        TextView fav = pill(isSavedArticle(a.id) ? "♥ Kaydedildi" : "♡ Kaydet", false);
        fav.setOnClickListener(v -> { toggleSavedArticle(a.id); openArticle(a); });
        actions.addView(fav);
        TextView share = pill("↗ Paylaş", false);
        share.setOnClickListener(v -> shareArticle(a));
        actions.addView(share);
        content.addView(actions);

        if (a.hasVideo()) {
            TextView video = tv("▶  Videoyu izle", 15, Color.WHITE, true);
            video.setGravity(Gravity.CENTER);
            video.setBackground(round(brand(), 16));
            video.setPadding(dp(14), 0, dp(14), 0);
            video.setOnClickListener(v -> openUrl(a.videoUrl));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
            lp.setMargins(0, dp(10), 0, dp(4));
            content.addView(video, lp);

            if (!a.videoCredit.isEmpty()) {
                TextView vc = tv("Video: " + a.videoCredit + (!a.videoSource.isEmpty() ? " • Kaynağı aç" : ""), 11, secondary(), false);
                vc.setPadding(0, dp(5), 0, dp(5));
                if (!a.videoSource.isEmpty()) vc.setOnClickListener(v -> openUrl(a.videoSource));
                content.addView(vc);
            }
        }

        TextView body = tv(a.body, 16, text(), false);
        body.setLineSpacing(0, 1.3f);
        body.setPadding(0, dp(10), 0, dp(16));
        content.addView(body);

        sectionTitle("Kaynaklar", "İçeriğin hazırlanmasında yararlanılan referanslar");
        if (a.sources.isEmpty()) content.addView(tv("Bu çevrimdışı yedek içerik için kaynak bağlantısı henüz bulunmuyor.", 13, secondary(), false));
        for (Models.Source source : a.sources) content.addView(menuCard("↗ " + source.title, host(source.url), () -> openUrl(source.url)));
        sectionTitle("BilgiliMedia notu", "Bu içerik kısa okuma formatındadır.");
        content.addView(tv("Detaylı araştırmalarda birincil ve güvenilir kaynakları karşılaştırmayı unutma.", 13, secondary(), false));
        scroll.post(() -> scroll.scrollTo(0, 0));
    }

    private FrameLayout cover(Models.Article a, int height) {
        return cover(a, height, false);
    }

    private FrameLayout cover(Models.Article a, int height, boolean featured) {
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(goldenGradientFor(a.category));

        String imageUrl = a.imageUrl == null ? "" : a.imageUrl;
        boolean hasImage = !imageUrl.isEmpty();
        if (hasImage) {
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setAlpha(0f);
            frame.addView(image, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            loadImage(imageUrl, image, null);
            View shade = new View(this);
            shade.setBackgroundColor(0x44000000);
            frame.addView(shade, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        TextView mark = tv(goldenCategoryMark(a.category), featured ? 50 : 38, 0x55ffffff, true);
        mark.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams markLp = new FrameLayout.LayoutParams(dp(88), dp(88), Gravity.CENTER_VERTICAL | Gravity.END);
        markLp.setMargins(0, 0, dp(10), 0);
        frame.addView(mark, markLp);

        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.BOTTOM);
        overlay.setPadding(dp(15), dp(12), dp(15), dp(12));
        overlay.addView(tv("BİLGİLİMEDIA", 10, 0xeeffffff, true));
        TextView category = tv(a.category, featured ? 19 : 16, Color.WHITE, true);
        category.setPadding(0, dp(3), 0, 0);
        overlay.addView(category);
        frame.addView(overlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (a.hasVideo()) {
            TextView video = tv("▶ VIDEO", 10, Color.WHITE, true);
            video.setGravity(Gravity.CENTER);
            video.setBackground(round(0xccd71920, 12));
            video.setClickable(true);
            video.setFocusable(true);
            video.setContentDescription("Videoyu doğrudan aç");
            video.setOnClickListener(v -> openUrl(a.videoUrl));
            FrameLayout.LayoutParams videoLp = new FrameLayout.LayoutParams(dp(82), dp(34), Gravity.TOP | Gravity.END);
            videoLp.setMargins(0, dp(9), dp(9), 0);
            frame.addView(video, videoLp);
        }

        frame.setContentDescription((hasImage ? "Fotoğraflı " : "") + a.category + " kapak görseli");
        frame.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(height)));
        return frame;
    }

    private GradientDrawable goldenGradientFor(String category) {
        int[] colors;
        switch (category) {
            case "Genel": colors = new int[]{0xff5b5fef, 0xff8d5cf6}; break;
            case "Belgesel": colors = new int[]{0xff075e68, 0xff27a69e}; break;
            case "Tarih": colors = new int[]{0xff7b4129, 0xffc78755}; break;
            case "Doğa": colors = new int[]{0xff1f7040, 0xff66b85d}; break;
            case "Yemek": colors = new int[]{0xffb84128, 0xfff09b42}; break;
            case "Teknoloji": colors = new int[]{0xff13549c, 0xff39a0e7}; break;
            case "Animasyon": colors = new int[]{0xff6a3fb5, 0xffd66bb3}; break;
            case "Çizgi Film": colors = new int[]{0xffe57a18, 0xffffc84b}; break;
            default: colors = new int[]{0xffe11f30, 0xffb80f23};
        }
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        gradient.setCornerRadius(dp(18));
        return gradient;
    }

    private String goldenCategoryMark(String category) {
        switch (category) {
            case "Genel": return "✦";
            case "Belgesel": return "◉";
            case "Tarih": return "⌛";
            case "Doğa": return "❧";
            case "Yemek": return "◌";
            case "Teknoloji": return "⌁";
            case "Animasyon": return "◆";
            case "Çizgi Film": return "✎";
            default: return "▶";
        }
    }

    private void loadImage(String url,ImageView view,TextView fallback){
        io.execute(()->{
            try{
                File dir=new File(getCacheDir(),"covers");dir.mkdirs();File f=new File(dir,sha(url)+".img");
                if(!f.exists()){
                    URL u=new URL(url);if(!"https".equalsIgnoreCase(u.getProtocol()))return;HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(7000);c.setReadTimeout(9000);c.setRequestProperty("User-Agent","BilgiliMedia/1.4.9");
                    if(c.getResponseCode()>=200&&c.getResponseCode()<300){try(java.io.InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(f)){byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))>0){total+=n;if(total>4_000_000)throw new IllegalStateException("image too large");out.write(buf,0,n);}}}else return;c.disconnect();
                }
                Bitmap b=BitmapFactory.decodeFile(f.getAbsolutePath());if(b!=null)runOnUiThread(()->{view.setImageBitmap(b);view.animate().alpha(1f).setDuration(180).start();if (fallback != null) fallback.setVisibility(View.GONE);});
            }catch(Exception ignored){}
        });
    }

    private GradientDrawable gradientFor(String category){
        int[] colors;
        switch(category){
            case "Belgesel":colors=new int[]{0xff16324f,0xff376996};break;
            case "Tarih":colors=new int[]{0xff5b3b22,0xff9a6a3a};break;
            case "Doğa":colors=new int[]{0xff1d5b3b,0xff46a06b};break;
            case "Yemek":colors=new int[]{0xff9b3d23,0xffe07a38};break;
            case "Teknoloji":colors=new int[]{0xff263b80,0xff5e78df};break;
            case "Animasyon":colors=new int[]{0xff6930c3,0xffb5179e};break;
            case "Çizgi Film":colors=new int[]{0xff1f6f8b,0xff4cc9f0};break;
            case "AI":colors=new int[]{0xff3a0ca3,0xff7209b7};break;
            case "HIGGS":colors=new int[]{0xff111827,0xffe50914};break;
            default:colors=new int[]{0xff7f1d1d,0xffef4444};
        }
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,colors);g.setCornerRadius(dp(18));return g;
    }

    private String categoryMark(String c){
        switch(c){case "Belgesel":return "◆";case "Tarih":return "⌁";case "Doğa":return "❧";case "Yemek":return "◌";case "Teknoloji":return "⌛";case "Animasyon":return "◉";case "Çizgi Film":return "✦";case "AI":return "AI";case "HIGGS":return "H";default:return "✎";}
    }

    private Models.Article featuredArticle(){if(!featuredId.isEmpty()){Models.Article f=findArticle(featuredId);if(f!=null)return f;}return articles.isEmpty()?null:articles.get(0);}
    private Models.Article findArticle(String id){for(Models.Article a:articles)if(a.id.equals(id))return a;return null;}
    private List<Models.Article> filterCategory(String category){List<Models.Article> out=new ArrayList<>();for(Models.Article a:articles)if(category.equals(a.category))out.add(a);return out;}
    private List<Models.Article> limit(List<Models.Article> src,int max){return new ArrayList<>(src.subList(0,Math.min(max,src.size())));}
    private List<Models.Article> historyArticles(){List<Models.Article> out=new ArrayList<>();for(String id:historyIds()){Models.Article a=findArticle(id);if(a!=null)out.add(a);}return out;}

    private void openPendingArticle(){if(pendingArticleId==null||pendingArticleId.isEmpty()||articles.isEmpty())return;Models.Article a=findArticle(pendingArticleId);if(a!=null){pendingArticleId="";openArticle(a);}}
    private String extractArticleId(Intent intent){if(intent==null)return "";String id=intent.getStringExtra("open_article_id");if(id!=null&&!id.isEmpty())return id;Uri data=intent.getData();if(data!=null&&"https".equalsIgnoreCase(data.getScheme())&&"bilgilimedia-60d6f.web.app".equalsIgnoreCase(data.getHost())){List<String>s=data.getPathSegments();if(s.size()>=2&&"content".equals(s.get(0)))return s.get(1);}return "";}

    private void addHistory(String id){List<String> ids=historyIds();ids.remove(id);ids.add(0,id);while(ids.size()>100)ids.remove(ids.size()-1);prefs.edit().putString("history_all",TextUtils.join("|",ids)).putLong("history_time_"+id,System.currentTimeMillis()).apply();}
    private List<String> historyIds(){String raw=prefs.getString("history_all","");List<String> out=new ArrayList<>();if(raw==null||raw.isEmpty())return out;for(String x:raw.split("\\|"))if(!x.isEmpty())out.add(x);return out;}
    private void incrementRead(String id){prefs.edit().putInt("reads_"+id,readCount(id)+1).apply();}
    private int readCount(String id){return prefs.getInt("reads_"+id,0);}

    private Set<String> getStringSet(String key){return new LinkedHashSet<>(prefs.getStringSet(key,new HashSet<>()));}
    private boolean isSavedArticle(String id){Set<String>s=getStringSet("saved");return s.contains("a:"+id)||s.contains(id);}
    private boolean isSavedAccount(String url){return getStringSet("saved").contains("p:"+url);}
    private void toggleSavedArticle(String id){Set<String>s=getStringSet("saved");String k="a:"+id;if(s.contains(k)||s.contains(id)){s.remove(k);s.remove(id);}else s.add(k);prefs.edit().putStringSet("saved",new HashSet<>(s)).apply();}
    private void toggleSavedAccount(String url){Set<String>s=getStringSet("saved");String k="p:"+url;if(s.contains(k))s.remove(k);else s.add(k);prefs.edit().putStringSet("saved",new HashSet<>(s)).apply();}
    private int countSavedArticles(){int n=0;for(Models.Article a:articles)if(isSavedArticle(a.id))n++;return n;}
    private Set<String> getNotifyCategories(){return new LinkedHashSet<>(prefs.getStringSet("notify_categories",new HashSet<>()));}

    private void shareArticle(Models.Article a){String url=DataRepository.HOST_BASE+"/content/"+Uri.encode(a.id)+"?v="+feedVersion;Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,a.title);i.putExtra(Intent.EXTRA_TEXT,a.title+"\n"+url);startActivity(Intent.createChooser(i,"İçeriği paylaş"));}
    private void shareApp(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"BilgiliMedia\n"+WEB);startActivity(Intent.createChooser(i,"Uygulamayı paylaş"));}
    private void email(){try{startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:"+EMAIL)));}catch(Exception e){Toast.makeText(this,"E-posta uygulaması açılamadı",Toast.LENGTH_SHORT).show();}}

    private void openSocial(Models.SocialAccount a){
        String pkg=null;if("Instagram".equals(a.platform))pkg="com.instagram.android";else if("YouTube".equals(a.platform))pkg="com.google.android.youtube";else if("Facebook".equals(a.platform))pkg="com.facebook.katana";else if("TikTok".equals(a.platform))pkg="com.zhiliaoapp.musically";
        try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(a.url));if(pkg!=null)i.setPackage(pkg);startActivity(i);}catch(Exception e){openUrl(a.url);}
    }
    private void openUrl(String url){try{Uri u=Uri.parse(url);if(!"https".equalsIgnoreCase(u.getScheme()))throw new IllegalArgumentException();Intent i=new Intent(Intent.ACTION_VIEW,u);i.addCategory(Intent.CATEGORY_BROWSABLE);startActivity(i);}catch(Exception e){Toast.makeText(this,"Bağlantı açılamadı",Toast.LENGTH_SHORT).show();}}
    private String host(String url){try{String h=Uri.parse(url).getHost();if(h==null)return "";return h.startsWith("www.")?h.substring(4):h;}catch(Exception e){return "";}}

    private void requestNotificationPermissionIfNeeded(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFICATION);}
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==REQ_NOTIFICATION){boolean ok=grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED;prefs.edit().putBoolean("notify_master",ok).apply();NotificationSync.sync(this,config);Toast.makeText(this,ok?"Bildirim izni verildi":"Bildirim izni verilmedi",Toast.LENGTH_SHORT).show();}}
    private boolean notificationsAllowed(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false;android.app.NotificationManager nm=(android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE);return nm==null||Build.VERSION.SDK_INT<24||nm.areNotificationsEnabled();}

    private void clearLocalData(){prefs.edit().remove("saved").remove("history_all").remove("theme").remove("content_cache").remove("app_config_cache").apply();File f=new File(getFilesDir(),"feed-cache.json");if(f.exists())f.delete();File c=new File(getFilesDir(),"config-cache.json");if(c.exists())c.delete();Toast.makeText(this,"Yerel kayıtlar temizlendi",Toast.LENGTH_SHORT).show();recreate();}
    private void confirm(String message,Runnable yes){new AlertDialog.Builder(this).setMessage(message).setNegativeButton("Vazgeç",null).setPositiveButton("Temizle",(d,w)->yes.run()).show();}

    private void applyThemeMode(){String mode=getSharedPreferences(DataRepository.PREFS,MODE_PRIVATE).getString("theme","system");boolean sys=(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;dark="dark".equals(mode)||("system".equals(mode)&&sys);}
    private String themeLabel(String t){if("dark".equals(t))return "Koyu";if("light".equals(t))return "Açık";return "Sistem";}
    private void styleSystemBars() {
        Window w = getWindow();
        w.setStatusBarColor(dark ? 0xff0e1013 : Color.WHITE);
        w.setNavigationBarColor(card());
        if (Build.VERSION.SDK_INT >= 23) w.getDecorView().setSystemUiVisibility(dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private int bg() { return dark ? 0xff101216 : 0xfff7f8fb; }
    private int card() { return dark ? 0xff1d2026 : 0xffffffff; }
    private int text() { return dark ? 0xfff4f5f7 : 0xff191b1f; }
    private int secondary() { return dark ? 0xffabb0ba : 0xff666c76; }
    private int muted() { return secondary(); }
    private int brand() { return 0xffe11f30; }

    private LinearLayout panel() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(15), dp(14), dp(15), dp(10));
        c.setBackground(round(card(), 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        c.setLayoutParams(lp);
        return c;
    }
    private GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private TextView tv(String s, float sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private TextView chip(String s, boolean selected) {
        return pill(s, selected);
    }
    private TextView pill(String s, boolean primary) {
        TextView v = tv(s, 13, primary ? Color.WHITE : text(), true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(14), 0, dp(14), 0);
        v.setBackground(round(primary ? brand() : (dark ? 0xff252932 : 0xffeef0f4), 16));
        v.setClickable(true);
        v.setFocusable(true);
        v.setContentDescription(s);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(38));
        lp.setMargins(0, 5, 8, 5);
        v.setLayoutParams(lp);
        return v;
    }
    private void sectionTitle(String title, String subtitle) {
        TextView titleView = tv(title, 20, text(), true);
        titleView.setPadding(0, dp(18), 0, 0);
        content.addView(titleView);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subtitleView = tv(subtitle, 12, secondary(), false);
            subtitleView.setPadding(0, 2, 0, dp(8));
            content.addView(subtitleView);
        }
    }
    private TextView empty(String s){TextView v=tv(s,14,secondary(),false);v.setGravity(Gravity.CENTER);v.setPadding(dp(14),dp(30),dp(14),dp(30));return v;}
    private void addInfo(String title, String value) {
        content.addView(menuCard(title, value, null));
    }
    private TextView menuLine(String title,String value){TextView v=tv(title+"\n"+value,14,text(),false);v.setPadding(dp(13),dp(10),dp(13),dp(10));v.setBackground(round(card(),14));v.setLayoutParams(margin(-1,-2,0,0,0,dp(7)));return v;}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dpv(l),dpv(t),dpv(r),dpv(b));return p;}
    private int dpv(int v){if(v<0)return v;return dp(v);}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private String normalize(String s){if(s==null)return "";String x=s.replace('İ','I').replace('ı','i').replace('Ğ','G').replace('ğ','g').replace('Ü','U').replace('ü','u').replace('Ş','S').replace('ş','s').replace('Ö','O').replace('ö','o').replace('Ç','C').replace('ç','c');return Normalizer.normalize(x,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);}
    private String sha(String s)throws Exception{MessageDigest d=MessageDigest.getInstance("SHA-256");byte[] x=d.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();for(byte v:x)b.append(String.format(Locale.ROOT,"%02x",v));return b.toString();}
}
