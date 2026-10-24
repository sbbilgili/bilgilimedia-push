# BilgiliMedia 1.4.8 — teknik APK denetimi

Denetim tarihi: 24 Ekim 2026

Bu belge Google Play'e doğrudan yüklenecek bir artifact onayı değildir. Orijinal arayüzü koruyan **altın referans APK** üzerinde yapılan teknik kontrolleri kaydeder.

## Referans dosya

- Dosya: `BilgiliMedia_Medya_Onizleme_1.4.8_ORIJINAL_ARAYUZ.apk`
- SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`
- package: `com.bilgilimedia.mobile.media.preview`
- versionName: `1.4.8`
- versionCode: `22`
- minSdkVersion: `26`
- targetSdkVersion: `36`
- compileSdkVersion: `36`
- label: `BilgiliMedia Önizleme`

## İmza

APK signing block içinde:
- APK Signature Scheme v2: mevcut
- APK Signature Scheme v3: mevcut

Preview sertifikası:
`E7:F2:BB:FC:73:E8:F9:00:40:E8:10:C6:15:9E:0F:1D:D3:BF:78:09:05:E6:78:90:B9:A4:82:DB:B3:5B:17:D6`

Bu fingerprint mevcut preview App Links kaydıyla eşleşir.

## Manifest / izinler

Manifestte görülen izinler:

- `android.permission.INTERNET`
- `android.permission.POST_NOTIFICATIONS`
- `android.permission.RECEIVE_BOOT_COMPLETED`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.WAKE_LOCK`
- Firebase/FCM'nin `com.google.android.c2dm.permission.RECEIVE` izni
- AndroidX dinamik receiver koruma izni

Hassas kullanıcı izinleri bulunmadı:
- konum yok
- kamera yok
- mikrofon yok
- rehber yok
- SMS yok
- telefon yok
- genel depolama erişimi yok

Application:
- `allowBackup=false`
- `usesCleartextTraffic=false`
- `extractNativeLibs=false`

## Exported bileşenler

- `MainActivity`: exported=true — launcher + doğrulanmış App Link için gerekli.
- `BilgiliFirebaseMessagingService`: exported=false.
- `ContentCheckJobService`: exported=false ve BIND_JOB_SERVICE ile korunuyor.
- `BootReceiver`: exported=false.
- Firebase'in kendi exported receiver'ı gerekli SEND izniyle korunuyor.

## App Links

Manifest:
- scheme: `https`
- host: `bilgilimedia-60d6f.web.app`
- pathPrefix: `/content/`
- autoVerify: true

## Ağ / gömülü adresler

Uygulama içindeki BilgiliMedia uçları HTTPS:
- `https://bilgilimedia-60d6f.web.app/feed.json`
- `https://bilgilimedia-60d6f.web.app/app-config.json`
- `https://bilgilimedia-60d6f.web.app/content/`
- `https://bilgilimedia-60d6f.web.app/privacy`

Sosyal ve kaynak bağlantıları da HTTPS olarak gömülü.

WebView veya JavaScript bridge işareti bulunmadı.

## Firebase / SDK

- Firebase Cloud Messaging mevcut.
- FCM sürüm dizesi: `26.0.0`.
- Firebase Installations transitive olarak mevcut.
- Google Analytics for Firebase sınıfları bulunmadı.
- Crashlytics sınıfları bulunmadı.
- AdMob / Google Mobile Ads SDK bulunmadı.
- FCM server key/private service-account key işareti bulunmadı.
- Firebase client API key uygulama kaynaklarında bulunabilir; bu bir FCM server key değildir ve Firebase client yapılandırmasının normal parçasıdır.

## 16 KB page-size / native library

APK yalnız Firebase/AndroidX DataStore'a ait küçük native shared-counter kütüphaneleri içeriyor.

Tüm ABI'lerde ELF LOAD segment alignment:
- `0x4000` = 16 KB

APK içinde uncompressed `.so` girişlerinin veri başlangıçları:
- 16 KB sınırlarına hizalı

`resources.arsc`:
- uncompressed
- 4-byte aligned

Referans APK bu teknik kontrolde 16 KB açısından uygun görünüyor. Final Play AAB ve Play-generated split APK'lar ayrıca test edilmelidir.

## Yerel / arka plan özellikleri

APK içinde doğrulanan BilgiliMedia bileşenleri:
- `MainActivity`
- `BilgiliFirebaseMessagingService`
- `ContentCheckJobService`
- `BootReceiver`

Uygulama FCM, boot/update sonrası kontrol, favoriler/son okunanlar, sosyal hesaplar, bildirim ayarları, uzaktan feed/config ve sponsor sistemine ait kod yollarını içeriyor.

## Play için blocker olan farklar

Bu APK doğrudan final Play artifact değildir:

1. package adı preview: `com.bilgilimedia.mobile.media.preview`
2. Google Play yeni uygulamalar için AAB ister
3. final production package `com.bilgilimedia.mobile` olmalı
4. production Firebase Android app/config ayrı doğrulanmalı
5. production signing / Play App Signing yapılandırılmalı
6. final production AAB'den güncel ekran görüntüleri alınmalı
7. gerçek cihaz testleri tamamlanmalı

## Production imza sürekliliği

Mevcut BilgiliMedia production imza yedeği sertifikası:
`F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`

Eski production AAB de aynı sertifikayla imzalıdır. Sideload edilmiş eski `com.bilgilimedia.mobile` kullanıcılarının Play sürümüne güncelleme sürekliliğini korumak için Play App Signing kurulumunda bu mevcut app-signing key'in korunması planlanmalıdır.
