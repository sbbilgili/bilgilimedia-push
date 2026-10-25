const fs = require("fs");

function read(path) {
  if (!fs.existsSync(path)) throw new Error("Missing Android source file: " + path);
  return fs.readFileSync(path, "utf8");
}
function requireAll(text, values, label) {
  for (const value of values) {
    if (!text.includes(value)) throw new Error(label + " missing exact golden value: " + value);
  }
}

const gradle = read("android-production/app/build.gradle.kts");
const manifest = read("android-production/app/src/main/AndroidManifest.xml");
const main = read("android-production/app/src/main/java/com/bilgilimedia/mobile/MainActivity.java");
const data = read("android-production/app/src/main/java/com/bilgilimedia/mobile/DataRepository.java");
const social = read("android-production/app/src/main/java/com/bilgilimedia/mobile/SocialData.java");
const messaging = read("android-production/app/src/main/java/com/bilgilimedia/mobile/BilgiliFirebaseMessagingService.java");
const job = read("android-production/app/src/main/java/com/bilgilimedia/mobile/ContentCheckJobService.java");
const notificationSync = read("android-production/app/src/main/java/com/bilgilimedia/mobile/NotificationSync.java");

requireAll(gradle, [
  'applicationId = "com.bilgilimedia.mobile"',
  'compileSdk = 36',
  'targetSdk = 36',
  'minSdk = 26',
  'versionCode = 23',
  'versionName = "1.4.9"',
  'firebase-messaging'
], "Gradle contract");

if (/firebase-messaging-ktx|firebase-analytics|crashlytics|play-services-ads/i.test(gradle)) {
  throw new Error("Unexpected/deprecated Firebase KTX, analytics, crash or ads dependency");
}

requireAll(manifest, [
  'android:allowBackup="false"',
  'android:enableOnBackInvokedCallback="true"',
  'android:usesCleartextTraffic="false"',
  'android:screenOrientation="portrait"',
  'android:autoVerify="true"',
  'android:host="bilgilimedia-60d6f.web.app"',
  'android:pathPrefix="/content/"',
  'android:pathPrefix="/app"',
  'android.permission.POST_NOTIFICATIONS',
  'android.permission.RECEIVE_BOOT_COMPLETED'
], "Manifest contract");

if (!/PREFS\s*=\s*"preferences"/.test(data)) throw new Error("Local-data compatibility must use SharedPreferences name: preferences");

requireAll(main, [
  'header.setPadding(dp(16), dp(9), dp(12), dp(8))',
  'tv("▶", 18, Color.WHITE, true)',
  'new LinearLayout.LayoutParams(dp(42), dp(42))',
  'tv("BilgiliMedia", 21, text(), true)',
  'play.setTranslationY(dp(6))',
  'brandBox.setTranslationY(dp(6))',
  'tv("Bilgi • Keşif • Medya", 11, secondary(), false)',
  'tv("↗", 24, text(), true)',
  'new LinearLayout.LayoutParams(dp(44), dp(44))',

  'content.setPadding(dp(16), dp(10), dp(16), dp(24))',
  'bottomNav.setElevation(dp(12))',
  'root.setOnApplyWindowInsetsListener',
  'bottomNav.setPadding(0, dp(4), 0, dp(4) + bottomInset)',
  'root.requestApplyInsets()',
  'item.setPadding(dp(2), dp(7), dp(2), dp(7))',
  '0xff2d1a1e',
  '0xffffecef',
  'tv(icon, 24',
  'tv(label, 13.5f',
  'dp(30)',
  'dp(29)',
  'dp(76)',

  'tv("BİLGİLİMEDIA", 11, brand(), true)',
  'private void renderQuickAccess()',
  'tv("Hızlı Erişim", 20, text(), true)',
  'tv("İçerik Kanallarımız", 13, secondary(), true)',
  'renderQuickAccess();',
  'tv("Bugün ne keşfetmek istersin?", 28, text(), true)',
  'tv("Ara ve keşfet", 27, text(), true)',
  'new String[]{"Tümü","Yazı","Video","Favoriler"}',
  '"Favoriler".equals(typeFilter)',
  'count.setPadding(0, dp(12), 0, dp(4))',
  'tv("Kaydedilenler",27,text(),true)',
  'tv("Okuma Geçmişi",27,text(),true)',
  'tv("BilgiliMedia hesapları",27,text(),true)',
  'tv("Daha",27,text(),true)',
  'tv("Ayarlar",27,text(),true)',
  'tv("Bildirimler",27,text(),true)',
  'tv("Gizlilik",27,text(),true)',
  'tv("Hakkında",27,text(),true)',
  'while(ids.size()>100)' ,
  'while (ids.size() > 8)',
  'putString("recent", TextUtils.join("|", ids))',
  'contains("p:" + index)',
  'String key = "p:" + index',
  'cover(a, 92, false)',
  'body.addView(tv(a.title, 17, text(), true))',
  'when + " • " + readCount(id) + " okuma"',
  'Geçmişteki içerikler artık canlı listede bulunmuyor.',
  'emptySaved.setPadding(0, dp(60), 0, dp(30))',
  'emptyHistory.setPadding(0, dp(55), 0, dp(30))',
  'tv(statusText, 12',
  'sectionTitle("Öne Çıkan", "Editör seçimi")',

  'cover(a, 190, true)',
  'tv(a.title, 23, text(), true)',
  'new LinearLayout.LayoutParams(dp(250), dp(235))',
  'cover(a, 118, false)',
  'tv(a.title, 17, text(), true)',
  'tv(a.title, 19, text(), true)',

  'a.platform + " • " + socialCategory(a)',
  'tv(handle, 13, secondary(), false)',
  'new LinearLayout.LayoutParams(dp(44), dp(38))',

  'cover(a, 175, true)',
  'tv(a.title, 29, text(), true)',
  'body.setLineSpacing(0, 1.3f)',

  '0xff101216',
  '0xfff7f8fb',
  '0xff1d2026',
  '0xfff4f5f7',
  '0xff191b1f',
  '0xffabb0ba',
  '0xff666c76',
  '0xffe11f30',
  '0xff252932',
  '0xffeef0f4',

  '0xff5b5fef',
  '0xff8d5cf6',
  '0xff075e68',
  '0xff27a69e',
  '0xff7b4129',
  '0xffc78755',
  '0xff1f7040',
  '0xff66b85d',
  '0xffb84128',
  '0xfff09b42',
  '0xff13549c',
  '0xff39a0e7',
  '0xff6a3fb5',
  '0xffd66bb3',
  '0xffe57a18',
  '0xffffc84b',

  'featured ? 50 : 38',
  '0x55ffffff',
  'tv("BİLGİLİMEDIA", 10, 0xeeffffff, true)',
  'new FrameLayout.LayoutParams(dp(82), dp(34)',
  '0xccd71920',

  '"?v=" + feedVersion',
  'private static final String APP_OPEN_LINK = "https://bilgilimedia-60d6f.web.app/open/app"',
  'private String sharedContentLink(Models.Article a)',
  '"/open/content/" + Uri.encode(a.id)',
  'BuildConfig.DEBUG ? url + "&qa=1" : url',
  'private String sharedAppLink()',
  'BuildConfig.DEBUG ? APP_OPEN_LINK + "?qa=1" : APP_OPEN_LINK',
  'i.putExtra(Intent.EXTRA_TEXT,"BilgiliMedia\\n"+sharedAppLink())',
  'private boolean isAppHomeLink(Intent intent)',
  '"/app".equals(data.getPath())',
  'showScreen("home")',
  '"Ana Sayfa"',
  '"Ara"',
  '"Kayıtlar"',
  '"Hesaplar"',
  '"Daha"',
  '"33 sosyal medya hesabımıza tek yerden ulaş."'
], "Exact 1.4.8 DEX UI contract");

const socialCount = (social.match(/x\.add\(a\(/g) || []).length;
if (socialCount !== 33) throw new Error("Golden social-account count must be 33, found " + socialCount);

if (main.includes("İçerik Bölümleri")) throw new Error("Non-golden More-screen section must not be visible in the 1.4.8 parity client");
if (main.includes("setLineSpacing(0,1.08f)") || main.includes("setLineSpacing(0, 1.08f)")) throw new Error("Golden TextView helper must not add global line spacing");
requireAll(main, [
  'private LinearLayout menuCard(String title, String subtitle, Runnable action)',
  'cardView.addView(tv(title, 16, text(), true))',
  'cardView.addView(tv(subtitle == null ? "" : subtitle, 12, secondary(), false))',
  'content.addView(menuCard(title, value, null))'
], "Golden menu-card helper");
requireAll(main, [
  'boolean master = prefs.getBoolean("notify_master", false)',
  'content.addView(menuCard("▣ Bildirim durumu", enabled ? "Açık" : "Kapalı"',
  'if (master) {',
  'syncNotifications(false)',
  'syncNotifications(true)',
  'Toast.makeText(this, "Bildirimler etkin", Toast.LENGTH_SHORT).show()',
  'LinkedHashSet<String> names = availableNotificationCategoryNames()',
  'prefs.getString("app_config_cache", "")',
  'root.optJSONArray("categories")',
  'item.optBoolean("enabled", true)',
  'item.optString("name", "").trim()',
  'for (Models.Article article : articles)',
  'Set<String> selected = getNotifyCategories()',
  'if (index % 2 == 0)',
  'new LinearLayout.LayoutParams(0, dp(44), 1f)',
  'putStringSet("notify_categories", new HashSet<>(next))',
  'private static final int REQ_NOTIFICATION = 901',
  'if (showFeedback && getNotifyCategories().isEmpty())',
  'Toast.makeText(this, "Seçili kategori yok", Toast.LENGTH_SHORT).show()',
  'Henüz kategori seçmedin. Bildirim almak için en az bir kategori seç.',
  'Yeni kategoriler eklendiğinde bildirim seçenekleri otomatik güncellenir.'
], "Golden notification grid, category source and feedback behavior");

requireAll(main, [
  'private void renderNotifications(){',
  'content.removeAllViews();',
  'showScreen("notifications")'
], "Notification screen must redraw cleanly without duplicate category blocks");
if ((main.match(/renderNotifications\(\);/g) || []).length !== 1) {
  throw new Error("renderNotifications() must only be called from the screen router; interactive updates must use showScreen(\"notifications\")");
}
if (main.includes("new CheckBox(this)")) throw new Error("Golden notification screen uses two-column pills, not CheckBox rows");
requireAll(main, [
  'private static final long BACK_EXIT_WINDOW_MS = 2500L',
  'private long lastBackPressAt = 0L',
  'public void onBackPressed()',
  'private void handleBackNavigation()',
  'if (!"home".equals(screen))',
  'showScreen("home")',
  'lastBackPressAt = now',
  'now - lastBackPressAt <= BACK_EXIT_WINDOW_MS',
  'setMessage("Uygulamadan çıkmak istiyor musunuz?")',
  'setNegativeButton("Vazgeç", null)',
  'setPositiveButton("Çık", (dialog, which) -> finishAndRemoveTask())',
  'getOnBackInvokedDispatcher().registerOnBackInvokedCallback',
  'android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT',
  'this::handleBackNavigation'
], "BilgiliMedia double-back exit contract");

if (main.includes('pendingArticleId = extractArticleId(getIntent());\n        requestNotificationPermissionIfNeeded();')) {
  throw new Error("Notification permission must not be requested automatically on app startup");
}

if (main.includes("notify_categories_initialized") || main.includes("selectedCategoryIds") || main.includes("setCategorySelected")) {
  throw new Error("Golden 1.4.8 notification UI stores selected visible category names directly");
}
if (notificationSync.includes("notify_category_ids") || notificationSync.includes("selectedCategoryIds")) {
  throw new Error("Non-golden ID-based notification preference migration detected");
}
requireAll(notificationSync, [
  'prefs.getBoolean("notify_master", false)',
  'prefs.getStringSet("notify_categories", new HashSet<>())',
  'prefs.getString("category_topic_map", "{}")',
  'topicMap.optString(cleanName, "")',
  'AppConfig.slug(cleanName)'
], "Golden selected-name topic mapping semantics");
requireAll(messaging, [
  'getBoolean("notify_master",false)',
  'if(category.isEmpty())category="Genel"',
  'if(!selected.isEmpty()&&!selected.contains(category))return;'
], "Golden FCM delivery gating");
requireAll(job, [
  'getBoolean("notify_master",false)',
  'if(!selected.isEmpty()&&!selected.contains(a.category))continue;'
], "Golden fallback notification gating");
requireAll(main, [
  'typeLabel.setPadding(0, dp(12), 0, 0)',
  'sortLabel.setPadding(0, dp(5), 0, 0)',
  'categoryLabel.setPadding(0, dp(5), 0, 0)',
  'categoryNames.add("Tümü")',
  'for (Models.Article article : articles) categoryNames.add(article.category)',
  'new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54))'
], "Golden search spacing/category source");
requireAll(main, [
  'String[] labels = {"Sistem","Açık","Koyu"}',
  'String[] values = {"system","light","dark"}',
  'getStringSet("saved").size()',
  'tv("Yerel kayıtları temizle",14,brand(),true)',
  'clear.setPadding(0, dp(18), 0, dp(18))',
  'sectionTitle("Bildirimler","Firebase Cloud Messaging")',
  'sectionTitle("İzinler","Mevcut önizleme sürümü")',
  'menuCard("↗ Gizlilik politikasını aç","bilgilimedia-60d6f.web.app/privacy"'
], "Golden Settings/Privacy contract");
if (!main.includes('sectionTitle("BilgiliMedia","İletişim")')) throw new Error("Golden About contact heading missing");
if (!messaging.includes("notified_content_ids")) throw new Error("FCM de-duplication missing");
if (!job.includes("known_content_ids")) throw new Error("Fallback content de-duplication missing");

console.log("Exact golden 1.4.8 DEX contract validated:", socialCount, "social accounts.");

if (main.includes('prefs.getBoolean("notify_master",true)') ||
    main.includes('prefs.getBoolean("notify_master", true)')) {
  throw new Error("notify_master must default false everywhere in the golden-compatible UI");
}

if (main.includes('i.putExtra(Intent.EXTRA_TEXT,"BilgiliMedia\\n"+WEB)')) {
  throw new Error("App share must not expose the external short URL directly");
}
if (main.includes('DataRepository.HOST_BASE+"/content/"+Uri.encode(a.id)+"?v="+feedVersion')) {
  throw new Error("Content sharing must use the app-first /open/content launcher URL");
}
