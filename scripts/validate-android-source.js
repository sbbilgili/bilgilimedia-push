const fs = require('fs');

function read(p) {
  if (!fs.existsSync(p)) throw new Error('Missing Android source file: ' + p);
  return fs.readFileSync(p, 'utf8');
}

function requireAll(text, needles, label) {
  for (const needle of needles) {
    if (!text.includes(needle)) throw new Error(label + ' missing: ' + needle);
  }
}

const gradle = read('android-production/app/build.gradle.kts');
const manifest = read('android-production/app/src/main/AndroidManifest.xml');
const main = read('android-production/app/src/main/java/com/bilgilimedia/mobile/MainActivity.java');
const social = read('android-production/app/src/main/java/com/bilgilimedia/mobile/SocialData.java');
const config = read('android-production/app/src/main/java/com/bilgilimedia/mobile/AppConfig.java');
const sync = read('android-production/app/src/main/java/com/bilgilimedia/mobile/NotificationSync.java');
const messaging = read('android-production/app/src/main/java/com/bilgilimedia/mobile/BilgiliFirebaseMessagingService.java');
const job = read('android-production/app/src/main/java/com/bilgilimedia/mobile/ContentCheckJobService.java');

requireAll(gradle, [
  'applicationId = "com.bilgilimedia.mobile"',
  'compileSdk = 36',
  'targetSdk = 36',
  'minSdk = 26',
  'versionCode = 23',
  'versionName = "1.4.9"',
  'firebase-messaging'
], 'Android Gradle contract');

if (/firebase-messaging-ktx|firebase-analytics|crashlytics|play-services-ads/i.test(gradle)) {
  throw new Error('Unexpected/deprecated analytics, ads or KTX Firebase dependency');
}

requireAll(manifest, [
  'android:allowBackup="false"',
  'android:usesCleartextTraffic="false"',
  'android:autoVerify="true"',
  'android:host="bilgilimedia-60d6f.web.app"',
  'android:pathPrefix="/content/"',
  'android.permission.POST_NOTIFICATIONS',
  'android.permission.RECEIVE_BOOT_COMPLETED',
  'android.permission.ACCESS_NETWORK_STATE',
  'android.permission.WAKE_LOCK'
], 'Manifest contract');

requireAll(main, [
  'BİLGİLİMEDIA',
  'Bugün ne keşfetmek istersin?',
  'Öne Çıkan',
  'Yeni Eklenenler',
  'Son Okunanlar',
  'En Çok Okunanlar',
  'Ana Sayfa',
  'Ara',
  'Kayıtlar',
  'Hesaplar',
  'Daha',
  'Ara ve keşfet',
  'Kaydedilenler',
  'Okuma Geçmişi',
  'Bildirimler',
  'Ayarlar',
  'Gizlilik',
  'Hakkında',
  'Geliştirici Tanılama',
  'Topic aboneliklerini doğrula',
  '33 sosyal medya hesabımıza tek yerden ulaş.',
  'İçeriği oku  →',
  'Okumaya başla →',
  '"?v="+feedVersion',
  'WEB = "https://spoo.me/bilgilimedia"',
  'PRIVACY = "https://bilgilimedia-60d6f.web.app/privacy/"'
], 'Golden UI/behavior contract');

// Visual invariants recovered from the accepted 1.4.8 programmatic layout.
// These prevent accidental fallback to the rejected simplified QA interface.
requireAll(main, [
  'bottomNav.setPadding(dp(4), dp(5), dp(4), dp(5))',
  'ViewGroup.LayoutParams.MATCH_PARENT, dp(68)',
  'content.setPadding(dp(16), dp(10), dp(16), dp(26))',
  'FrameLayout cover = cover(a, dp(190))',
  'new LinearLayout.LayoutParams(dp(250), dp(105))',
  'return dark?0xff0e1116:0xfff6f7f9',
  'return dark?0xff191d24:0xffffffff',
  'return 0xffe50914'
], 'Golden visual contract');

const socialCount = (social.match(/x\.add\(a\(/g) || []).length;
if (socialCount !== 33) {
  throw new Error('Golden social-account contract requires exactly 33 accounts; found ' + socialCount);
}

requireAll(config, [
  'schemaVersion',
  'categories',
  'extraPages',
  'sponsorAds',
  'categoryTopicMap'
], 'Remote configuration contract');

requireAll(sync, [
  'subscribeToTopic',
  'unsubscribeFromTopic',
  'known_fcm_topics',
  'notify_categories'
], 'Notification topic synchronization');

if (!messaging.includes('notified_content_ids')) {
  throw new Error('FCM de-duplication missing');
}
if (!job.includes('known_content_ids')) {
  throw new Error('Fallback content de-duplication missing');
}

console.log(
  'Android production source contract validated:',
  'golden UI/visual invariants,',
  socialCount + ' social accounts,',
  'API 36, FCM, App Links and security constraints.'
);
