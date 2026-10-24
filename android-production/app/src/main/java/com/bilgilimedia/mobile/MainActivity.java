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
