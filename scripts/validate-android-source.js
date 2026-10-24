const fs = require('fs');
function read(p){ if(!fs.existsSync(p)) throw new Error('Missing Android source file: '+p); return fs.readFileSync(p,'utf8'); }
const gradle=read('android-production/app/build.gradle.kts');
const manifest=read('android-production/app/src/main/AndroidManifest.xml');
const main=read('android-production/app/src/main/java/com/bilgilimedia/mobile/MainActivity.java');
const messaging=read('android-production/app/src/main/java/com/bilgilimedia/mobile/BilgiliFirebaseMessagingService.java');
const job=read('android-production/app/src/main/java/com/bilgilimedia/mobile/ContentCheckJobService.java');

for(const needle of [
 'applicationId = "com.bilgilimedia.mobile"',
 'compileSdk = 36','targetSdk = 36','minSdk = 26','versionCode = 23','versionName = "1.4.9"',
 'firebase-messaging'
]) if(!gradle.includes(needle)) throw new Error('Android Gradle contract missing: '+needle);

for(const needle of [
 'android:allowBackup="false"','android:usesCleartextTraffic="false"','android:autoVerify="true"',
 'android:host="bilgilimedia-60d6f.web.app"','android:pathPrefix="/content/"',
 'android.permission.POST_NOTIFICATIONS','android.permission.RECEIVE_BOOT_COMPLETED'
]) if(!manifest.includes(needle)) throw new Error('Manifest contract missing: '+needle);

for(const needle of [
 'BİLGİLİMEDIA','Bugün ne keşfetmek istersin?','Öne Çıkan','Yeni Eklenenler','Son Okunanlar','En Çok Okunanlar',
 'Ana Sayfa','Ara','Kayıtlar','Hesaplar','Daha','Ara ve keşfet','Kaydedilenler','Okuma Geçmişi',
 'Bildirimler','Ayarlar','Gizlilik','Hakkında','Geliştirici Tanılama','Topic aboneliklerini doğrula',
 '33 sosyal medya hesabımıza tek yerden ulaş.','İçeriği oku  →','Okumaya başla →'
]) if(!main.includes(needle)) throw new Error('Golden UI contract missing: '+needle);

if(!messaging.includes('notified_content_ids')) throw new Error('FCM de-duplication missing');
if(!job.includes('known_content_ids')) throw new Error('Fallback content de-duplication missing');
if(/firebase-analytics|crashlytics|play-services-ads/i.test(gradle)) throw new Error('Unexpected analytics/ads dependency');
console.log('Android production source contract validated.');
