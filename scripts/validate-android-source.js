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
  'android:usesCleartextTraffic="false"',
  'android:autoVerify="true"',
  'android:host="bilgilimedia-60d6f.web.app"',
  'android:pathPrefix="/content/"',
  'android.permission.POST_NOTIFICATIONS',
  'android.permission.RECEIVE_BOOT_COMPLETED'
], "Manifest contract");

if (!/PREFS\s*=\s*"preferences"/.test(data)) throw new Error("Local-data compatibility must use SharedPreferences name: preferences");

requireAll(main, [
  'header.setPadding(dp(16), dp(9), dp(12), dp(8))',
  'tv("▶", 18, Color.WHITE, true)',
  'new LinearLayout.LayoutParams(dp(42), dp(42))',
  'tv("BilgiliMedia", 21, text(), true)',
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
  'tv("Bugün ne keşfetmek istersin?", 28, text(), true)',
  'tv("Ara ve keşfet", 27, text(), true)',
  'new String[]{"Tümü","Yazı","Video","Favoriler"}',
  'margin(-1, dp(54), 0, 0, 0, dp(12))',
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

  '"?v="+feedVersion',
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
  'content.addView(menuCard("▣ Bildirim durumu", enabled ? "Açık" : "Kapalı"',
  'if (index % 2 == 0)',
  'new LinearLayout.LayoutParams(0, dp(44), 1f)',
  'putBoolean("notify_categories_initialized", true)',
  'Henüz kategori seçmedin. Bildirim almak için en az bir kategori seç.',
  'Yeni kategoriler eklendiğinde bildirim seçenekleri otomatik güncellenir.'
], "Golden notification grid");
if (main.includes("new CheckBox(this)")) throw new Error("Golden notification screen uses two-column pills, not CheckBox rows");
if (!messaging.includes("notified_content_ids")) throw new Error("FCM de-duplication missing");
if (!job.includes("known_content_ids")) throw new Error("Fallback content de-duplication missing");

console.log("Exact golden 1.4.8 DEX contract validated:", socialCount, "social accounts.");
