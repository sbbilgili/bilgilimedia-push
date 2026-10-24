package com.bilgilimedia.mobile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int NOTIFY_REQUEST = 1408;
    private static final String BASE = "https://bilgilimedia-60d6f.web.app";

    private final List<Models.Article> articles = new ArrayList<>();
    private final List<Models.Category> categories = new ArrayList<>();
    private final List<Models.ExtraPage> extraPages = new ArrayList<>();
    private final List<Models.Sponsor> sponsors = new ArrayList<>();
    private final List<Models.SocialAccount> accounts = new ArrayList<>();
    private final Map<String, Models.Article> articleById = new HashMap<>();

    private LocalStore store;
    private DataRepository repository;
    private LinearLayout root;
    private LinearLayout content;
    private LinearLayout bottomNav;
    private TextView networkState;

    private Models.Feed feed = new Models.Feed();
    private Models.Config config = new Models.Config();
    private String route = "home";
    private String pendingArticleId = "";
    private String selectedCategory = "";
    private String accountPlatform = "Tümü";
    private String searchType = "Tümü";
    private String searchSort = "Yeni";
    private String searchCategory = "Tümü";
    private String searchQuery = "";
    private boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new LocalStore(this);
        repository = new DataRepository(this);
        seedAccounts();
        applyThemePreference();
        buildShell();
        handleIntent(getIntent());
        requestNotificationPermission();
        loadData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        openPendingIfReady();
    }

    @Override
    public void onBackPressed() {
        if ("article".equals(route) || !"home".equals(route)) {
            route = "home";
            render();
            return;
        }
        super.onBackPressed();
    }

    private void applyThemePreference() {
        String theme = store.theme();
        if ("dark".equals(theme)) {
            dark = true;
        } else if ("light".equals(theme)) {
            dark = false;
        } else {
            int mask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            dark = mask == Configuration.UI_MODE_NIGHT_YES;
        }
    }

    private int bg() { return dark ? Color.rgb(18, 20, 24) : Color.rgb(248, 249, 252); }
    private int card() { return dark ? Color.rgb(29, 32, 38) : Color.WHITE; }
    private int primary() { return dark ? Color.rgb(244, 246, 249) : Color.rgb(24, 28, 34); }
    private int secondary() { return dark ? Color.rgb(174, 181, 190) : Color.rgb(92, 100, 110); }
    private int line() { return dark ? Color.rgb(55, 60, 68) : Color.rgb(226, 230, 235); }
    private int red() { return Color.rgb(224, 0, 25); }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg());

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(14), dp(18), dp(10));
        TextView brand = text("BİLGİLİMEDIA", 22, true);
        brand.setTextColor(primary());
        header.addView(brand);
        TextView tag = text("Bilgi • Keşif • Medya", 12, false);
        tag.setTextColor(secondary());
        header.addView(tag);
        networkState = text("Güncelleniyor…", 11, false);
        networkState.setTextColor(secondary());
        networkState.setPadding(0, dp(4), 0, 0);
        header.addView(networkState);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(4), dp(16), dp(28));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(dp(5), dp(5), dp(5), dp(8));
        bottomNav.setBackgroundColor(card());
        root.addView(bottomNav);
        renderBottomNav();

        setContentView(root);
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
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(5), dp(2), dp(5), dp(2));
        TextView i = text(icon, 18, true);
        TextView l = text(label, 10, false);
        boolean selected = routeMatchesNav(target);
        i.setTextColor(selected ? red() : secondary());
        l.setTextColor(selected ? red() : secondary());
        box.addView(i);
        box.addView(l);
        box.setOnClickListener(v -> {
            route = target;
            render();
        });
        bottomNav.addView(box, new LinearLayout.LayoutParams(0, dp(52), 1f));
    }

    private boolean routeMatchesNav(String target) {
        if ("saved".equals(target)) return "saved".equals(route) || "history".equals(route);
        if ("more".equals(target)) return "more".equals(route) || "notifications".equals(route)
                || "privacy".equals(route) || "about".equals(route) || "settings".equals(route);
        return target.equals(route);
    }

    private void loadData() {
        networkState.setText("Güncelleniyor…");
        repository.load(new DataRepository.Callback() {
            @Override
            public void onLoaded(Models.Feed loadedFeed, Models.Config loadedConfig, boolean cacheUsed) {
                runOnUiThread(() -> {
                    feed = loadedFeed;
                    config = loadedConfig;
                    articles.clear();
                    articles.addAll(feed.articles);
                    categories.clear();
                    categories.addAll(config.categories);
                    extraPages.clear();
                    extraPages.addAll(config.extraPages);
                    sponsors.clear();
                    sponsors.addAll(config.sponsors);
                    articleById.clear();
                    for (Models.Article article : articles) articleById.put(article.id, article);
                    networkState.setText((cacheUsed ? "Önbellekten yüklendi" : "Canlı") + " • feed v" + feed.version);
                    BilgiliMediaApp.syncTopics(MainActivity.this, config);
                    render();
                    openPendingIfReady();
                });
            }

            @Override
            public void onError(Exception error) {
                runOnUiThread(() -> {
                    networkState.setText("Bağlantı hatası");
                    Toast.makeText(MainActivity.this, "İçerikler yüklenemedi.", Toast.LENGTH_LONG).show();
                    render();
                });
            }
        });
    }

    private void render() {
        content.removeAllViews();
        renderBottomNav();
        switch (route) {
            case "search": renderSearch(); break;
            case "saved": renderSaved(); break;
            case "accounts": renderAccounts(); break;
            case "more": renderMore(); break;
            case "history": renderHistory(); break;
            case "notifications": renderNotifications(); break;
            case "settings": renderSettings(); break;
            case "privacy": openHttps(BASE + "/privacy/"); route = "more"; renderMore(); break;
            case "about": renderAbout(); break;
            default: renderHome(); break;
        }
    }

    private void renderHome() {
        route = "home";
        sectionTitle("Bugün ne keşfetmek istersin?");
        paragraph("Güncel kısa okumalar, popüler içerikler ve tüm BilgiliMedia hesapları tek uygulamada.");

        LinearLayout live = row();
        TextView liveText = pill("Canlı", true);
        live.addView(liveText);
        Button refresh = smallButton("↻ Yenile");
        refresh.setOnClickListener(v -> loadData());
        live.addView(refresh);
        content.addView(live);

        Models.Article featured = featuredArticle();
        if (featured != null) {
            subsection("Öne Çıkan", "Editör seçimi");
            content.addView(articleCard(featured, true));
        }

        if (config.sponsorAdsEnabled) {
            for (Models.Sponsor sponsor : sponsors) {
                if ("home_after_featured".equals(sponsor.placement)) content.addView(sponsorCard(sponsor));
            }
        }

        subsection("Yeni Eklenenler", "Canlı akıştaki son içerikler");
        for (Models.Article a : newest(5)) content.addView(articleCard(a, false));

        List<Models.Article> recent = recentHistory(3);
        if (!recent.isEmpty()) {
            subsection("Son Okunanlar", "Kaldığın yerden devam et");
            for (Models.Article a : recent) content.addView(articleCard(a, false));
        }

        List<Models.Article> popular = mostRead(4);
        if (!popular.isEmpty()) {
            subsection("En Çok Okunanlar", "Bu cihazdaki okuma sayılarına göre");
            for (Models.Article a : popular) content.addView(articleCard(a, false));
        }

        subsection("Kategoriler", "Bu kategoriden seçtiklerimiz");
        content.addView(categoryRail());

        subsection("Hızlı Erişim", "Sosyal hesaplarımız");
        LinearLayout quick = row();
        for (String platform : new String[]{"Instagram", "YouTube", "TikTok", "Facebook"}) {
            Button b = smallButton(platform);
            b.setOnClickListener(v -> {
                accountPlatform = platform;
                route = "accounts";
                render();
            });
            quick.addView(b);
        }
        content.addView(quick);
    }

    private void renderSearch() {
        sectionTitle("Ara ve keşfet");
        paragraph("Başlık, kategori, tür ve popülerliğe göre filtrele.");

        EditText query = new EditText(this);
        query.setHint("Örn. tarih, doğa, teknoloji...");
        query.setText(searchQuery);
        query.setTextColor(primary());
        query.setHintTextColor(secondary());
        query.setSingleLine(true);
        query.setBackground(rounded(card(), line(), 14));
        query.setPadding(dp(12), dp(8), dp(12), dp(8));
        query.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) {
                searchQuery = s.toString();
                renderSearchResultsOnly();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        content.addView(query, margin(dp(0), dp(8)));

        label("İçerik türü");
        LinearLayout types = row();
        for (String value : new String[]{"Tümü", "Video", "Yazı"}) {
            Button b = smallButton(value);
            if (value.equals(searchType)) accentButton(b);
            b.setOnClickListener(v -> { searchType = value; render(); });
            types.addView(b);
        }
        content.addView(types);

        label("Sıralama");
        LinearLayout sorts = row();
        for (String value : new String[]{"Yeni", "Popüler"}) {
            Button b = smallButton(value);
            if (value.equals(searchSort)) accentButton(b);
            b.setOnClickListener(v -> { searchSort = value; render(); });
            sorts.addView(b);
        }
        content.addView(sorts);

        label("Kategori");
        content.addView(searchCategoryRail());

        LinearLayout holder = new LinearLayout(this);
        holder.setId(View.generateViewId());
        holder.setTag("search-results");
        holder.setOrientation(LinearLayout.VERTICAL);
        content.addView(holder);
        renderSearchResults(holder);
    }

    private void renderSearchResultsOnly() {
        View view = content.findViewWithTag("search-results");
        if (view instanceof LinearLayout) renderSearchResults((LinearLayout) view);
    }

    private void renderSearchResults(LinearLayout holder) {
        holder.removeAllViews();
        List<Models.Article> result = new ArrayList<>();
        String normalized = normalize(searchQuery);
        for (Models.Article a : articles) {
            if (!"Tümü".equals(searchType)) {
                boolean video = !TextUtils.isEmpty(a.videoUrl);
                if ("Video".equals(searchType) && !video) continue;
                if ("Yazı".equals(searchType) && video) continue;
            }
            if (!"Tümü".equals(searchCategory) && !searchCategory.equals(a.category)) continue;
            if (!normalized.isEmpty()) {
                String haystack = normalize(a.title + " " + a.category + " " + a.summary + " " + a.body);
                if (!haystack.contains(normalized)) continue;
            }
            result.add(a);
        }
        if ("Popüler".equals(searchSort)) {
            result.sort((a, b) -> Integer.compare(store.reads(b.id), store.reads(a.id)));
        }
        TextView count = text(result.size() + " sonuç", 12, false);
        count.setTextColor(secondary());
        count.setPadding(0, dp(10), 0, dp(6));
        holder.addView(count);
        if (result.isEmpty()) {
            paragraphInto(holder, "Bu filtrelerle eşleşen içerik bulunamadı.");
        } else {
            for (Models.Article a : result) holder.addView(articleCard(a, false));
        }
    }

    private void renderSaved() {
        sectionTitle("Kaydedilenler");
        paragraph("Favorilerin bu cihazda tutulur.");
        Set<String> saved = store.savedArticles();
        boolean any = false;
        for (Models.Article a : articles) {
            if (saved.contains(a.id)) {
                content.addView(articleCard(a, false));
                any = true;
            }
        }
        if (!any) paragraph("Henüz içerik kaydetmedin. ♡ simgesine dokunarak favori oluşturabilirsin.");

        Set<String> savedAccounts = store.savedAccounts();
        if (!savedAccounts.isEmpty()) {
            subsection("Kaydedilen hesaplar", "Hızlı erişim");
            for (Models.SocialAccount account : accounts) {
                if (savedAccounts.contains(account.url)) content.addView(accountCard(account));
            }
        }

        Button history = actionButton("Okuma Geçmişi");
        history.setOnClickListener(v -> { route = "history"; render(); });
        content.addView(history);
    }

    private void renderHistory() {
        sectionTitle("Okuma Geçmişi");
        paragraph("Daha önce açtığın içerikler ve son okunma zamanları.");
        List<String> history = store.history();
        if (history.isEmpty()) {
            paragraph("Henüz okuma geçmişin yok.");
        } else {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy • HH:mm", new Locale("tr", "TR"));
            for (String id : history) {
                Models.Article a = articleById.get(id);
                if (a == null) continue;
                LinearLayout card = articleCard(a, false);
                TextView when = text(store.historyTime(id) == 0 ? "Okunma zamanı bilinmiyor" : format.format(new Date(store.historyTime(id))), 11, false);
                when.setTextColor(secondary());
                card.addView(when, 1);
                content.addView(card);
            }
            Button clear = actionButton("Geçmişi temizle");
            clear.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("Okuma geçmişi")
                    .setMessage("Geçmiş temizlensin mi?")
                    .setNegativeButton("Vazgeç", null)
                    .setPositiveButton("Temizle", (d, w) -> { store.clearHistory(); render(); })
                    .show());
            content.addView(clear);
        }
    }

    private void renderAccounts() {
        sectionTitle("BilgiliMedia hesapları");
        paragraph("33 sosyal medya hesabımıza tek yerden ulaş.");

        HorizontalScrollView filterScroll = new HorizontalScrollView(this);
        filterScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout filters = row();
        for (String platform : new String[]{"Tümü", "Instagram", "YouTube", "TikTok", "Facebook"}) {
            Button b = smallButton(platform);
            if (platform.equals(accountPlatform)) accentButton(b);
            b.setOnClickListener(v -> { accountPlatform = platform; render(); });
            filters.addView(b);
        }
        filterScroll.addView(filters);
        content.addView(filterScroll);

        for (Models.SocialAccount account : accounts) {
            if ("Tümü".equals(accountPlatform) || accountPlatform.equals(account.platform)) {
                content.addView(accountCard(account));
            }
        }
    }

    private void renderMore() {
        sectionTitle("Daha");
        paragraph("Ayarlar, gizlilik ve BilgiliMedia bağlantıları.");

        subsection("Uygulama", "Tercihler ve kişisel alan");
        addMenu("⚙ Ayarlar", "Tema ve yerel veriler", "settings");
        addMenu("◉ Bildirimler", "Yeni içerik bildirim tercihleri", "notifications");
        addMenu("⌛ Okuma Geçmişi", store.history().size() + " kayıt", "history");

        subsection("Bilgi", "Gizlilik ve uygulama bilgileri");
        addMenu("◌ Gizlilik", "Veri kullanımı ve gizlilik politikası", "privacy");
        addMenu("ⓘ Hakkında", "BilgiliMedia ve sürüm bilgisi", "about");

        if (!extraPages.isEmpty()) {
            subsection("Ek sayfalar", "BilgiliMedia tarafından uzaktan eklenen bölümler");
            for (Models.ExtraPage page : extraPages) {
                Button p = actionButton("↗ " + page.title + (page.subtitle.isEmpty() ? "" : " — " + page.subtitle));
                p.setOnClickListener(v -> openHttps(page.url));
                content.addView(p);
            }
        }

        subsection("BilgiliMedia", "Bağlantılar");
        Button web = actionButton("↗ Web sitemiz");
        web.setOnClickListener(v -> openHttps("https://spoo.me/bilgilimedia"));
        content.addView(web);
        Button mail = actionButton("✉ İletişim");
        mail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:sukrubatuhanbilgili@gmail.com"));
            try { startActivity(intent); } catch (Exception ignored) {}
        });
        content.addView(mail);
        Button share = actionButton("↗ Uygulamayı paylaş");
        share.setOnClickListener(v -> shareText("BilgiliMedia", "https://bilgilimedia-60d6f.web.app/"));
        content.addView(share);
    }

    private void renderSettings() {
        sectionTitle("Ayarlar");
        paragraph("Tema ve yerel veriler");

        label("Tema");
        LinearLayout themes = row();
        for (String value : new String[]{"system", "light", "dark"}) {
            String title = "system".equals(value) ? "Sistem" : "light".equals(value) ? "Açık" : "Koyu";
            Button b = smallButton(title);
            if (value.equals(store.theme())) accentButton(b);
            b.setOnClickListener(v -> {
                store.setTheme(value);
                applyThemePreference();
                buildShell();
                render();
            });
            themes.addView(b);
        }
        content.addView(themes);

        Button clearSaved = actionButton("Tüm kaydedilenleri gözden geçir");
        clearSaved.setOnClickListener(v -> { route = "saved"; render(); });
        content.addView(clearSaved);
    }

    private void renderNotifications() {
        sectionTitle("Bildirimler");
        paragraph("Yeni içeriklerde hangi kategorilerden haber almak istediğini seç. Teknik Firebase işlemleri arka planda otomatik yönetilir.");

        CheckBox master = new CheckBox(this);
        master.setText("Bildirimler etkin");
        master.setTextColor(primary());
        master.setChecked(store.notificationMaster());
        master.setOnCheckedChangeListener((button, checked) -> {
            store.setNotificationMaster(checked);
            BilgiliMediaApp.syncTopics(this, config);
        });
        content.addView(master);

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            TextView warning = text("Bildirim izni verilmedi", 12, true);
            warning.setTextColor(Color.rgb(190, 70, 40));
            content.addView(warning);
            Button allow = actionButton("Bildirim izni ver");
            allow.setOnClickListener(v -> requestNotificationPermission());
            content.addView(allow);
        } else {
            TextView ok = text("Bildirim izni verildi", 12, true);
            ok.setTextColor(Color.rgb(40, 145, 80));
            content.addView(ok);
        }

        for (Models.Category category : categories) {
            CheckBox box = new CheckBox(this);
            box.setText(category.name);
            box.setTextColor(primary());
            box.setChecked(store.topicEnabled(category.topic));
            box.setOnCheckedChangeListener((button, checked) -> {
                store.setTopicEnabled(category.topic, checked);
                if (BilgiliMediaApp.isFirebaseReady()) {
                    if (checked && store.notificationMaster()) FirebaseMessaging.getInstance().subscribeToTopic(category.topic);
                    else FirebaseMessaging.getInstance().unsubscribeFromTopic(category.topic);
                }
            });
            content.addView(box);
        }

        TextView status = text(
                BuildConfig.FIREBASE_ENABLED
                        ? (BilgiliMediaApp.isFirebaseReady() ? "Firebase hazır" : "Firebase yapılandırması bekleniyor")
                        : "QA build: Firebase kapalı",
                11, false);
        status.setTextColor(secondary());
        status.setPadding(0, dp(8), 0, 0);
        content.addView(status);
    }

    private void renderAbout() {
        sectionTitle("Hakkında");
        paragraph("BilgiliMedia; kısa, anlaşılır ve kaynak gösterilen içerikleri tek yerde sunar.");
        paragraph("Şükrü Batuhan Bilgili / BilgiliMedia");

        TextView version = text("BilgiliMedia • 1.4.9" + (BuildConfig.DEBUG ? " QA" : ""), 13, true);
        version.setTextColor(secondary());
        version.setPadding(0, dp(12), 0, dp(12));
        version.setOnLongClickListener(v -> {
            showDiagnostics();
            return true;
        });
        content.addView(version);

        Button privacy = actionButton("Gizlilik politikası");
        privacy.setOnClickListener(v -> openHttps(BASE + "/privacy/"));
        content.addView(privacy);
    }

    private void showDiagnostics() {
        String token = store.fcmToken();
        if (token.length() > 12) token = "…" + token.substring(token.length() - 12);
        StringBuilder topics = new StringBuilder();
        for (Models.Category c : categories) {
            if (store.notificationMaster() && store.topicEnabled(c.topic)) {
                if (topics.length() > 0) topics.append(", ");
                topics.append(c.topic);
            }
        }
        new AlertDialog.Builder(this)
                .setTitle("Geliştirici tanılama")
                .setMessage(
                        "Paket: " + getPackageName() + "\n" +
                        "Sürüm: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n" +
                        "Feed: v" + feed.version + "\n" +
                        "Config: " + config.updatedAt + "\n" +
                        "Firebase: " + (BilgiliMediaApp.isFirebaseReady() ? "hazır" : "kapalı/hazır değil") + "\n" +
                        "Topic: " + (topics.length() == 0 ? "Seçili kategori yok" : topics) + "\n" +
                        "FCM token sonu: " + (token.isEmpty() ? "yok" : token))
                .setPositiveButton("Kapat", null)
                .show();
    }

    private LinearLayout articleCard(Models.Article a, boolean featured) {
        LinearLayout cardView = new LinearLayout(this);
        cardView.setOrientation(LinearLayout.VERTICAL);
        cardView.setPadding(dp(14), dp(13), dp(14), dp(12));
        cardView.setBackground(rounded(card(), line(), 16));

        if (!TextUtils.isEmpty(a.imageUrl)) {
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            cardView.addView(image, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(featured ? 170 : 120)));
            loadImage(image, a.imageUrl);
        }

        TextView meta = text(a.category + (a.minutes > 0 ? " • " + a.minutes + " DK" : "") + (!a.videoUrl.isEmpty() ? " • ▶ VIDEO" : ""), 11, true);
        meta.setTextColor(red());
        meta.setPadding(0, dp(7), 0, dp(3));
        cardView.addView(meta);

        TextView title = text(a.title, featured ? 19 : 16, true);
        title.setTextColor(primary());
        cardView.addView(title);

        TextView summary = text(a.summary, 13, false);
        summary.setTextColor(secondary());
        summary.setPadding(0, dp(5), 0, dp(5));
        cardView.addView(summary);

        LinearLayout actions = row();
        Button save = smallButton(store.savedArticles().contains(a.id) ? "♥" : "♡");
        save.setOnClickListener(v -> {
            store.toggleArticle(a.id);
            render();
        });
        actions.addView(save);
        Button read = smallButton(featured ? "İçeriği oku →" : "Okumaya başla →");
        read.setOnClickListener(v -> openArticle(a));
        actions.addView(read);
        cardView.addView(actions);

        cardView.setOnClickListener(v -> openArticle(a));
        cardView.setLayoutParams(margin(0, dp(9)));
        return cardView;
    }

    private LinearLayout accountCard(Models.SocialAccount account) {
        LinearLayout cardView = row();
        cardView.setPadding(dp(12), dp(10), dp(10), dp(10));
        cardView.setBackground(rounded(card(), line(), 14));

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        TextView name = text(account.name, 14, true);
        name.setTextColor(primary());
        TextView meta = text(account.platform + " • " + account.category, 11, false);
        meta.setTextColor(secondary());
        textBox.addView(name);
        textBox.addView(meta);
        cardView.addView(textBox, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button save = smallButton(store.savedAccounts().contains(account.url) ? "♥" : "♡");
        save.setOnClickListener(v -> { store.toggleAccount(account.url); render(); });
        cardView.addView(save);
        Button open = smallButton("Uygulamada aç →");
        open.setOnClickListener(v -> openSocial(account));
        cardView.addView(open);
        cardView.setLayoutParams(margin(0, dp(7)));
        return cardView;
    }

    private LinearLayout sponsorCard(Models.Sponsor sponsor) {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(14), dp(12), dp(14), dp(12));
        view.setBackground(rounded(card(), Color.rgb(230, 190, 80), 15));
        TextView tag = text("SPONSORLU", 10, true);
        tag.setTextColor(Color.rgb(170, 115, 10));
        view.addView(tag);
        TextView title = text(sponsor.headline, 16, true);
        title.setTextColor(primary());
        view.addView(title);
        TextView desc = text(sponsor.text, 12, false);
        desc.setTextColor(secondary());
        view.addView(desc);
        Button open = smallButton("Reklamı aç →");
        open.setOnClickListener(v -> openHttps(sponsor.targetUrl));
        view.addView(open);
        view.setLayoutParams(margin(0, dp(10)));
        return view;
    }

    private void openArticle(Models.Article a) {
        route = "article";
        store.addHistory(a.id);
        content.removeAllViews();
        renderBottomNav();

        Button back = smallButton("← Geri");
        back.setOnClickListener(v -> { route = "home"; render(); });
        content.addView(back);

        TextView meta = text(a.category + (a.minutes > 0 ? " • " + a.minutes + " DK OKUMA" : ""), 11, true);
        meta.setTextColor(red());
        meta.setPadding(0, dp(10), 0, dp(5));
        content.addView(meta);

        sectionTitle(a.title);
        TextView summary = text(a.summary, 15, true);
        summary.setTextColor(secondary());
        summary.setPadding(0, 0, 0, dp(12));
        content.addView(summary);

        if (!a.imageUrl.isEmpty()) {
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            content.addView(image, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(210)));
            loadImage(image, a.imageUrl);
            if (!a.imageCredit.isEmpty()) {
                TextView credit = text("Görsel: " + a.imageCredit, 10, false);
                credit.setTextColor(secondary());
                content.addView(credit);
            }
        }

        TextView body = text(a.body, 16, false);
        body.setTextColor(primary());
        body.setLineSpacing(0, 1.18f);
        body.setPadding(0, dp(12), 0, dp(12));
        content.addView(body);

        LinearLayout actions = row();
        Button save = smallButton(store.savedArticles().contains(a.id) ? "♥ Kaydedildi" : "♡ Kaydet");
        save.setOnClickListener(v -> { store.toggleArticle(a.id); openArticle(a); });
        actions.addView(save);
        Button share = smallButton("↗ Paylaş");
        share.setOnClickListener(v -> shareArticle(a));
        actions.addView(share);
        if (!a.videoUrl.isEmpty()) {
            Button video = smallButton("▶ Videoyu izle");
            video.setOnClickListener(v -> openHttps(a.videoUrl));
            actions.addView(video);
        }
        content.addView(actions);

        subsection("Kaynaklar", "İçeriğin hazırlanmasında yararlanılan referanslar");
        if (a.sources.isEmpty()) {
            paragraph("Bu çevrimdışı yedek içerik için kaynak bağlantısı henüz bulunmuyor.");
        } else {
            for (Models.Source source : a.sources) {
                Button b = actionButton("↗ " + source.title);
                b.setOnClickListener(v -> openHttps(source.url));
                content.addView(b);
            }
        }

        subsection("BilgiliMedia notu", "Bu içerik kısa okuma formatındadır.");
        paragraph("Detaylı araştırmalarda birincil ve güvenilir kaynakları karşılaştırmayı unutma.");
    }

    private HorizontalScrollView categoryRail() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = row();
        for (Models.Category c : categories) {
            Button b = smallButton(c.name);
            b.setOnClickListener(v -> {
                searchCategory = c.name;
                route = "search";
                render();
            });
            row.addView(b);
        }
        scroll.addView(row);
        return scroll;
    }

    private HorizontalScrollView searchCategoryRail() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout rail = row();
        List<String> names = new ArrayList<>();
        names.add("Tümü");
        for (Models.Category c : categories) names.add(c.name);
        for (String name : names) {
            Button b = smallButton(name);
            if (name.equals(searchCategory)) accentButton(b);
            b.setOnClickListener(v -> { searchCategory = name; render(); });
            rail.addView(b);
        }
        scroll.addView(rail);
        return scroll;
    }

    private void addMenu(String title, String subtitle, String target) {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(12), dp(10), dp(12), dp(10));
        view.setBackground(rounded(card(), line(), 13));
        TextView t = text(title, 14, true);
        t.setTextColor(primary());
        TextView s = text(subtitle, 11, false);
        s.setTextColor(secondary());
        view.addView(t);
        view.addView(s);
        view.setOnClickListener(v -> { route = target; render(); });
        view.setLayoutParams(margin(0, dp(6)));
        content.addView(view);
    }

    private Models.Article featuredArticle() {
        if (!feed.featuredId.isEmpty() && articleById.containsKey(feed.featuredId)) return articleById.get(feed.featuredId);
        return articles.isEmpty() ? null : articles.get(0);
    }

    private List<Models.Article> newest(int count) {
        List<Models.Article> result = new ArrayList<>(articles);
        Collections.reverse(result);
        return result.subList(0, Math.min(count, result.size()));
    }

    private List<Models.Article> recentHistory(int count) {
        List<Models.Article> result = new ArrayList<>();
        for (String id : store.history()) {
            Models.Article a = articleById.get(id);
            if (a != null) result.add(a);
            if (result.size() >= count) break;
        }
        return result;
    }

    private List<Models.Article> mostRead(int count) {
        List<Models.Article> result = new ArrayList<>(articles);
        result.sort((a, b) -> Integer.compare(store.reads(b.id), store.reads(a.id)));
        result.removeIf(a -> store.reads(a.id) <= 0);
        return result.subList(0, Math.min(count, result.size()));
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFY_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == NOTIFY_REQUEST) {
            Toast.makeText(this,
                    results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED
                            ? "Bildirim izni verildi" : "Bildirim izni verilmedi",
                    Toast.LENGTH_SHORT).show();
            if ("notifications".equals(route)) render();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        String id = intent.getStringExtra("open_article_id");
        if (TextUtils.isEmpty(id) && intent.getData() != null) {
            Uri uri = intent.getData();
            if ("https".equalsIgnoreCase(uri.getScheme()) && "bilgilimedia-60d6f.web.app".equalsIgnoreCase(uri.getHost())) {
                List<String> parts = uri.getPathSegments();
                if (parts.size() >= 2 && "content".equals(parts.get(0))) id = parts.get(1);
            }
        }
        if (!TextUtils.isEmpty(id)) pendingArticleId = id;
    }

    private void openPendingIfReady() {
        if (pendingArticleId.isEmpty()) return;
        Models.Article article = articleById.get(pendingArticleId);
        if (article != null) {
            pendingArticleId = "";
            openArticle(article);
        }
    }

    private void openSocial(Models.SocialAccount account) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(account.url));
        String pkg = "";
        if ("Instagram".equals(account.platform)) pkg = "com.instagram.android";
        if ("YouTube".equals(account.platform)) pkg = "com.google.android.youtube";
        if ("TikTok".equals(account.platform)) pkg = "com.zhiliaoapp.musically";
        if ("Facebook".equals(account.platform)) pkg = "com.facebook.katana";
        if (!pkg.isEmpty()) {
            intent.setPackage(pkg);
            try {
                startActivity(intent);
                return;
            } catch (Exception ignored) {
                intent.setPackage(null);
            }
        }
        try { startActivity(intent); } catch (Exception e) { Toast.makeText(this, "Bağlantı açılamadı", Toast.LENGTH_SHORT).show(); }
    }

    private void openHttps(String url) {
        if (url == null || url.isEmpty()) return;
        Uri uri = Uri.parse(url);
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            Toast.makeText(this, "Yalnız güvenli HTTPS bağlantıları açılır.", Toast.LENGTH_SHORT).show();
            return;
        }
        try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
        catch (Exception e) { Toast.makeText(this, "Bağlantı açılamadı", Toast.LENGTH_SHORT).show(); }
    }

    private void shareArticle(Models.Article a) {
        shareText(a.title, a.title + "\n" + BASE + "/content/" + Uri.encode(a.id) + "?v=" + feed.version);
    }

    private void shareText(String subject, String value) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, value);
        startActivity(Intent.createChooser(intent, "Paylaş"));
    }

    private void loadImage(ImageView image, String url) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(8000);
                try (InputStream input = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    if (bitmap != null) runOnUiThread(() -> image.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void seedAccounts() {
        accounts.clear();
        addAccount("bilgilimedia_bayn", "https://www.instagram.com/bilgilimedia_bayn/", "Instagram", "Genel");
        addAccount("bilgilimedia_belgesel", "https://www.instagram.com/bilgilimedia_belgesel/", "Instagram", "Belgesel");
        addAccount("bilgilimedia_tarih", "https://www.instagram.com/bilgilimedia_tarih/", "Instagram", "Tarih");
        addAccount("bilgilimedia_spor", "https://www.instagram.com/bilgilimedia_spor/", "Instagram", "Spor");
        addAccount("bilgilimedia_yemek", "https://www.instagram.com/bilgilimedia_yemek/", "Instagram", "Yemek");
        addAccount("bilgilimedia_doga", "https://www.instagram.com/bilgilimedia_doga/", "Instagram", "Doğa");
        addAccount("bilgilimedia_animasyon", "https://www.instagram.com/bilgilimedia_animasyon/", "Instagram", "Animasyon");
        addAccount("bilgilimedia_cizgifilm", "https://www.instagram.com/bilgilimedia_cizgifilm/", "Instagram", "Çizgi Film");
        addAccount("bilgilimedia_ai", "https://www.instagram.com/bilgilimedia_ai/", "Instagram", "Yapay Zekâ");
        addAccount("bilgilimedia_muzik", "https://www.instagram.com/bilgilimedia_muzik/", "Instagram", "Müzik");

        addAccount("@bilgilimedia_bayn", "https://www.youtube.com/@bilgilimedia_bayn", "YouTube", "Genel");
        addAccount("@bilgilimedia_belgesel", "https://www.youtube.com/@bilgilimedia_belgesel", "YouTube", "Belgesel");
        addAccount("@bilgilimedia_tarih", "https://www.youtube.com/@bilgilimedia_tarih", "YouTube", "Tarih");
        addAccount("@bilgilimedia_spor", "https://www.youtube.com/@bilgilimedia_spor", "YouTube", "Spor");
        addAccount("@bilgilimedia_yemek", "https://www.youtube.com/@bilgilimedia_yemek", "YouTube", "Yemek");
        addAccount("@bilgilimedia_doga", "https://www.youtube.com/@bilgilimedia_doga", "YouTube", "Doğa");
        addAccount("@bilgilimedia_animasyon", "https://www.youtube.com/@bilgilimedia_animasyon", "YouTube", "Animasyon");
        addAccount("@bilgilimedia_cizgifilm", "https://www.youtube.com/@bilgilimedia_cizgifilm", "YouTube", "Çizgi Film");

        addAccount("@bilgilibaynmedia", "https://www.tiktok.com/@bilgilibaynmedia", "TikTok", "Genel");
        addAccount("@bilgilimedia_belgesel", "https://www.tiktok.com/@bilgilimedia_belgesel", "TikTok", "Belgesel");
        addAccount("@bilgilimedia_tarih", "https://www.tiktok.com/@bilgilimedia_tarih", "TikTok", "Tarih");
        addAccount("@bilgilimedia_spor", "https://www.tiktok.com/@bilgilimedia_spor", "TikTok", "Spor");
        addAccount("@bilgilimedia_yemek", "https://www.tiktok.com/@bilgilimedia_yemek", "TikTok", "Yemek");
        addAccount("@bilgilimedia_doa", "https://www.tiktok.com/@bilgilimedia_doa", "TikTok", "Doğa");
        addAccount("@bilgilimedia_english", "https://www.tiktok.com/@bilgilimedia_english", "TikTok", "English");
        addAccount("@bilgilimedia_muzik", "https://www.tiktok.com/@bilgilimedia_muzik", "TikTok", "Müzik");

        addAccount("bilgilimedia", "https://www.facebook.com/bilgilimedia/", "Facebook", "Genel");
        addAccount("bilgilisbmedia", "https://www.facebook.com/bilgilisbmedia", "Facebook", "SB Media");
        addAccount("Bilgilimedia-Belgesel", "https://www.facebook.com/people/Bilgilimedia-Belgesel/61594717468413/", "Facebook", "Belgesel");
        addAccount("BilgiliMedia-Tarih", "https://www.facebook.com/people/BilgiliMedia-Tarih/61594312784284/", "Facebook", "Tarih");
        addAccount("Sukru-Batuhan-Bilgili", "https://www.facebook.com/people/Sukru-Batuhan-Bilgili/61594884595578/", "Facebook", "Şükrü Batuhan Bilgili");
        addAccount("BilgiliMedia-Yemek", "https://www.facebook.com/people/BilgiliMedia-Yemek/61594650695544/", "Facebook", "Yemek");
        addAccount("BilgiliMedia-Doğa", "https://www.facebook.com/people/BilgiliMedia-Doğa/61594535117641/", "Facebook", "Doğa");
    }

    private void addAccount(String name, String url, String platform, String category) {
        accounts.add(new Models.SocialAccount(name, url, platform, category));
    }

    private void sectionTitle(String value) {
        TextView view = text(value, 23, true);
        view.setTextColor(primary());
        view.setPadding(0, dp(5), 0, dp(6));
        content.addView(view);
    }

    private void subsection(String title, String subtitle) {
        TextView t = text(title, 18, true);
        t.setTextColor(primary());
        t.setPadding(0, dp(18), 0, dp(1));
        content.addView(t);
        TextView s = text(subtitle, 11, false);
        s.setTextColor(secondary());
        s.setPadding(0, 0, 0, dp(7));
        content.addView(s);
    }

    private void paragraph(String value) { paragraphInto(content, value); }

    private void paragraphInto(LinearLayout parent, String value) {
        TextView view = text(value, 13, false);
        view.setTextColor(secondary());
        view.setPadding(0, dp(2), 0, dp(10));
        parent.addView(view);
    }

    private void label(String value) {
        TextView view = text(value, 12, true);
        view.setTextColor(secondary());
        view.setPadding(0, dp(12), 0, dp(5));
        content.addView(view);
    }

    private TextView text(String value, float size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(primary());
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private TextView pill(String value, boolean accent) {
        TextView view = text(value, 11, true);
        view.setTextColor(accent ? Color.WHITE : primary());
        view.setPadding(dp(10), dp(5), dp(10), dp(5));
        view.setBackground(rounded(accent ? red() : card(), accent ? red() : line(), 18));
        return view;
    }

    private Button smallButton(String value) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(value);
        button.setTextSize(11);
        button.setTextColor(primary());
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setPadding(dp(9), dp(4), dp(9), dp(4));
        return button;
    }

    private Button actionButton(String value) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(value);
        button.setTextSize(13);
        button.setTextColor(primary());
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setBackground(rounded(card(), line(), 13));
        button.setPadding(dp(12), dp(10), dp(12), dp(10));
        button.setLayoutParams(margin(0, dp(5)));
        return button;
    }

    private void accentButton(Button button) {
        button.setTextColor(Color.WHITE);
        button.setBackground(rounded(red(), red(), 16));
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private GradientDrawable rounded(int fill, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams margin(int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, top, 0, bottom);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String normalize(String value) {
        String s = value == null ? "" : value;
        s = s.replace('İ', 'I').replace('ı', 'i')
                .replace('Ğ', 'G').replace('ğ', 'g')
                .replace('Ü', 'U').replace('ü', 'u')
                .replace('Ş', 'S').replace('ş', 's')
                .replace('Ö', 'O').replace('ö', 'o')
                .replace('Ç', 'C').replace('ç', 'c');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return s.toLowerCase(Locale.ROOT);
    }
}
