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
        results.setOrientation(LinearLayout.VERTICAL);
        content.addView(results);
        q.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            @Override public void onTextChanged(CharSequence s,int st,int before,int count) { query=s.toString(); }
            @Override public void afterTextChanged(Editable e) { renderSearchResults(results, e.toString()); }
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
            String hay = normalize(a.title+" "+a.summary+" "+a.body+" "+a.category);
            if (!needle.isEmpty() && !hay.contains(needle)) continue;
            results.add(a);
        }
        if ("Popüler".equals(sortFilter)) results.sort((a,b)->Integer.compare(readCount(b.id),readCount(a.id)));
        else Collections.reverse(results);
        TextView count = tv(results.size()+" sonuç",12,secondary(),true); count.setPadding(0,dp(10),0,dp(6)); target.addView(count);
        if (results.isEmpty()) { target.addView(empty("Bu filtrelerle eşleşen içerik bulunamadı.")); return; }
        for (Models.Article a : results) target.addView(articleCard(a));
    }

    private View articleCard(Models.Article a) {
        LinearLayout c=card();
        c.setPadding(dp(12),dp(12),dp(12),dp(10));
        c.addView(tv(a.category+" • "+a.minutes+" DK"+(a.hasVideo()?" • ▶ VIDEO":""),11,brand(),true));
        TextView title=tv(a.title,18,text(),true); title.setPadding(0,dp(5),0,dp(4)); c.addView(title);
        TextView sum=tv(a.summary,13,secondary(),false); sum.setMaxLines(3); sum.setEllipsize(TextUtils.TruncateAt.END); c.addView(sum);
        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.END); actions.setPadding(0,dp(6),0,0);
        TextView fav=pill(isSavedArticle(a.id)?"♥":"♡",false); fav.setContentDescription(isSavedArticle(a.id)?"Favorilerden çıkar":"Favorilere kaydet");
        fav.setOnClickListener(v->{toggleSavedArticle(a.id); showScreen(screen);}); actions.addView(fav);
        TextView read=pill("Okumaya başla →",true); read.setOnClickListener(v->openArticle(a)); actions.addView(read);
        c.addView(actions); c.setOnClickListener(v->openArticle(a));
        LinearLayout wrap=new LinearLayout(this); wrap.setOrientation(LinearLayout.VERTICAL); wrap.addView(c,margin(-1,-2,0,0,0,dp(10))); return wrap;
    }

    private void renderSaved() {
        renderHeader(false);
        content.addView(tv("Kaydedilenler",24,text(),true));
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
        content.addView(tv("Okuma Geçmişi",24,text(),true));
        TextView d=tv("Daha önce açtığın içerikler ve son okunma zamanları.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(10)); content.addView(d);
        List<String> ids=historyIds();
        if(ids.isEmpty()) content.addView(empty("Henüz okuma geçmişin yok."));
        DateFormat fmt=new SimpleDateFormat("dd MMM yyyy • HH:mm",new Locale("tr","TR"));
        for(String id:ids){
            Models.Article a=findArticle(id);
            if(a==null) continue;
            LinearLayout box=card(); box.setPadding(dp(12),dp(10),dp(12),dp(10));
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
        content.addView(tv("BilgiliMedia hesapları",24,text(),true));
        TextView d=tv("33 sosyal medya hesabımıza tek yerden ulaş.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(10)); content.addView(d);
        content.addView(chipRow(new String[]{"Tümü","Instagram","YouTube","TikTok","Facebook"},accountPlatformFilter,v->{accountPlatformFilter=(String)v;renderAccounts();}));
        for(Models.SocialAccount a:accounts){ if("Tümü".equals(accountPlatformFilter)||accountPlatformFilter.equals(a.platform)) content.addView(accountCard(a)); }
    }

    private View accountCard(Models.SocialAccount a) {
        LinearLayout c=card(); c.setPadding(dp(13),dp(10),dp(10),dp(10));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout words=new LinearLayout(this); words.setOrientation(LinearLayout.VERTICAL);
        words.addView(tv(a.name,16,text(),true));
        words.addView(tv(a.platform+" • "+a.handle,12,secondary(),false));
        row.addView(words,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        TextView open=pill("Uygulamada aç →",true); open.setOnClickListener(v->openSocial(a)); row.addView(open);
        TextView fav=pill(isSavedAccount(a.url)?"♥":"♡",false); fav.setContentDescription(isSavedAccount(a.url)?"Hesabı favorilerden çıkar":"Hesabı kaydet"); fav.setOnClickListener(v->{toggleSavedAccount(a.url);showScreen(screen);}); row.addView(fav);
        c.addView(row); c.setOnClickListener(v->openSocial(a));
        LinearLayout wrap=new LinearLayout(this);wrap.addView(c,margin(-1,-2,0,0,0,dp(8)));return wrap;
    }

    private void renderMore() {
        renderHeader(false);
        content.addView(tv("Daha",24,text(),true));
        TextView d=tv("Ayarlar, gizlilik ve BilgiliMedia bağlantıları.",14,secondary(),false); d.setPadding(0,dp(3),0,dp(10)); content.addView(d);
        sectionTitle("Uygulama","Tercihler ve kişisel alan");
        content.addView(menuCard("⚙ Ayarlar","Tema ve yerel veriler",()->showScreen("settings")));
        String notifyState=notificationsAllowed()?"Açık":"Kapalı";
        content.addView(menuCard("▣ Bildirimler",notifyState,()->showScreen("notifications")));
        content.addView(menuCard("▤ Okuma Geçmişi",historyIds().size()+" kayıt",()->showScreen("history")));

        if(!config.sections.isEmpty()){
            sectionTitle("İçerik Bölümleri","Uzaktan yönetilen kategori yapısı");
            for(Models.Section s:config.sections){
                int n=0; for(Models.Category c:config.categories) if(s.id.equals(c.sectionId)) n++;
                final String sec=s.id;
                content.addView(menuCard(s.title,n+" kategori",()->showSection(sec)));
            }
        }

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

    private View menuCard(String title,String subtitle,Runnable action){
        LinearLayout c=card();c.setPadding(dp(13),dp(11),dp(13),dp(11));
        c.addView(tv(title,16,text(),true));
        if(subtitle!=null&&!subtitle.isEmpty()){TextView s=tv(subtitle,12,secondary(),false);s.setPadding(0,dp(2),0,0);c.addView(s);}
        c.setClickable(true);c.setFocusable(true);c.setOnClickListener(v->action.run());
        LinearLayout w=new LinearLayout(this);w.addView(c,margin(-1,-2,0,0,0,dp(8)));return w;
    }

    private void renderNotifications(){
        renderHeader(false);
        content.addView(tv("Bildirimler",24,text(),true));
        TextView d=tv("Yeni içeriklerde hangi kategorilerden haber almak istediğini seç. Teknik Firebase işlemleri arka planda otomatik yönetilir.",14,secondary(),false);d.setPadding(0,dp(3),0,dp(12));content.addView(d);
        boolean master=prefs.getBoolean("notify_master",true);
        LinearLayout status=card();status.setPadding(dp(13),dp(11),dp(13),dp(11));
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
        content.addView(tv("Ayarlar",24,text(),true));
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
        renderHeader(false);content.addView(tv("Gizlilik",24,text(),true));
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
        renderHeader(false);content.addView(tv("Hakkında",24,text(),true));
        content.addView(tv("BilgiliMedia • Bilgi, keşif ve medya",14,secondary(),false));
        sectionTitle("Sahiplik","Geliştirici bilgileri");
        addInfo("Geliştirici ve Kurucu","Şükrü Batuhan Bilgili");addInfo("Marka","BilgiliMedia");
        content.addView(tv("© 2026 Şükrü Batuhan Bilgili / BilgiliMedia",12,secondary(),false));
        sectionTitle("Uygulama","Sürüm ve paket bilgileri");
        TextView version=menuLine("Sürüm","1.4.9 • versionCode 23");version.setOnLongClickListener(v->{showScreen("developer");return true;});content.addView(version);
        content.addView(menuLine("Paket",getPackageName()));
        content.addView(menuLine("Canlı içerik akışı","Sürüm "+feedVersion+" • "+articles.size()+" içerik"));
        content.addView(menuLine("Android App Links","Etkin • web.app/content/…"));
        sectionTitle("İletişim","");
        content.addView(menuCard("▣ Web sitemiz","spoo.me/bilgilimedia",()->openUrl(WEB)));
        content.addView(menuCard("✉ E-posta",EMAIL,this::email));
    }

    private void renderDeveloper(){
        renderHeader(false);content.addView(tv("Geliştirici Tanılama",24,text(),true));
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

    private void openArticle(Models.Article a){
        screen="article";addHistory(a.id);incrementRead(a.id);content.removeAllViews();
        TextView back=pill("← Geri",false);back.setOnClickListener(v->showScreen("home"));content.addView(back);
        TextView meta=tv(a.category+" • "+a.minutes+" DK OKUMA"+(a.hasVideo()?" • ▶ VIDEO":""),12,brand(),true);meta.setPadding(0,dp(12),0,dp(5));content.addView(meta);
        if(!a.imageUrl.isEmpty()) content.addView(cover(a,dp(210)),new LinearLayout.LayoutParams(-1,dp(210)));
        TextView title=tv(a.title,25,text(),true);title.setPadding(0,dp(12),0,dp(8));content.addView(title);
        TextView summary=tv(a.summary,16,secondary(),true);summary.setPadding(0,0,0,dp(12));content.addView(summary);
        TextView body=tv(a.body,16,text(),false);body.setLineSpacing(0,1.18f);content.addView(body);
        LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);actions.setPadding(0,dp(12),0,dp(5));
        TextView fav=pill(isSavedArticle(a.id)?"♥ Kaydedildi":"♡ Kaydet",false);fav.setOnClickListener(v->{toggleSavedArticle(a.id);openArticle(a);});actions.addView(fav);
        TextView share=pill("↗ Paylaş",false);share.setOnClickListener(v->shareArticle(a));actions.addView(share);content.addView(actions);
        if(a.hasVideo()){TextView video=pill("▶  Videoyu izle",true);video.setOnClickListener(v->openUrl(a.videoUrl));content.addView(video);}
        sectionTitle("Kaynaklar","İçeriğin hazırlanmasında yararlanılan referanslar");
        if(a.sources.isEmpty()) content.addView(tv("Bu çevrimdışı yedek içerik için kaynak bağlantısı henüz bulunmuyor.",13,secondary(),false));
        for(Models.Source s:a.sources) content.addView(menuCard("↗ "+s.title,host(s.url),()->openUrl(s.url)));
        sectionTitle("BilgiliMedia notu","Bu içerik kısa okuma formatındadır.");
        content.addView(tv("Detaylı araştırmalarda birincil ve güvenilir kaynakları karşılaştırmayı unutma.",13,secondary(),false));
        renderBottomNav();scroll.post(()->scroll.scrollTo(0,0));
    }

    private FrameLayout cover(Models.Article a,int height){
        FrameLayout frame=new FrameLayout(this);frame.setBackground(gradientFor(a.category));frame.setClipToOutline(false);
        ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.CENTER_CROP);image.setAlpha(0f);frame.addView(image,new FrameLayout.LayoutParams(-1,-1));
        TextView mark=tv(categoryMark(a.category),36,Color.WHITE,true);mark.setGravity(Gravity.CENTER);frame.addView(mark,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout overlay=new LinearLayout(this);overlay.setOrientation(LinearLayout.VERTICAL);overlay.setGravity(Gravity.BOTTOM);overlay.setPadding(dp(12),dp(8),dp(12),dp(10));
        TextView logo=tv("BİLGİLİMEDIA",11,Color.WHITE,true);overlay.addView(logo);
        if(a.hasVideo()) overlay.addView(tv("▶ VIDEO",11,Color.WHITE,true));
        frame.addView(overlay,new FrameLayout.LayoutParams(-1,-1));
        if(!a.imageUrl.isEmpty()) loadImage(a.imageUrl,image,mark);
        frame.setContentDescription((a.imageUrl.isEmpty()?"Renkli ":"Fotoğraflı ")+a.category+" kapak görseli");
        return frame;
    }

    private void loadImage(String url,ImageView view,TextView fallback){
        io.execute(()->{
            try{
                File dir=new File(getCacheDir(),"covers");dir.mkdirs();File f=new File(dir,sha(url)+".img");
                if(!f.exists()){
                    URL u=new URL(url);if(!"https".equalsIgnoreCase(u.getProtocol()))return;HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(7000);c.setReadTimeout(9000);c.setRequestProperty("User-Agent","BilgiliMedia/1.4.9");
                    if(c.getResponseCode()>=200&&c.getResponseCode()<300){try(java.io.InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(f)){byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))>0){total+=n;if(total>4_000_000)throw new IllegalStateException("image too large");out.write(buf,0,n);}}}else return;c.disconnect();
                }
                Bitmap b=BitmapFactory.decodeFile(f.getAbsolutePath());if(b!=null)runOnUiThread(()->{view.setImageBitmap(b);view.animate().alpha(1f).setDuration(220).start();fallback.setVisibility(View.GONE);});
            }catch(Exception ignored){}
        });
    }

