# BilgiliMedia — Google Play kalan işler (24 Ekim 2026)

Bu dosya yalnız **henüz kapanmamış** maddeleri listeler.

## A. Yayını bloke edenler

### 1. Geliştirici hesabı kimlik doğrulaması
Durum: BEKLİYOR.

Kimlik belgesi henüz kullanıcıya ulaşmadığı için Play Console geliştirici kimlik doğrulaması tamamlanmadı.

### 2. Yeniden üretilebilir production Android kaynak projesi
Durum: DEVAM EDİYOR.

Orijinal 1.4.8 APK golden reference olarak sabitlendi. APK programatik Java arayüzü kullanıyor ve kullanıcıya ait ana sınıfların ekran/özellik haritası çıkarıldı.

Kalan:
- source project yeniden oluşturma/kurtarma,
- golden UI regression,
- production flavor/package `com.bilgilimedia.mobile`.

### 3. Final production AAB
Durum: BEKLİYOR.

Final AAB ancak source parity doğrulandıktan sonra üretilecek.

Zorunlu:
- package: `com.bilgilimedia.mobile`
- versionCode >= 23
- targetSdk 36
- minSdk 26
- production Firebase config
- güncel firebase-messaging ana modülü
- AAB
- 16 KB uyumluluk testi

### 4. Play App Signing
Durum: BEKLİYOR.

Kimlik doğrulaması / app creation sonrası:
- mevcut production app-signing key sürekliliği planlanacak,
- Play App Signing certificate SHA-256 alınacak,
- `assetlinks.json` fingerprint final olarak karşılaştırılacak,
- upload key ayrı tutulacak.

## B. Play Console form işlemleri

Kimlik geldikten sonra:

- Create app
- package registration
- app access
- ads declaration
- Data Safety
- privacy URL
- IARC content rating
- target audience
- news declaration
- store listing
- app signing
- closed test

## C. Fiziksel QA

Final production build üzerinde:

- temiz kurulum
- eski production APK üstüne güncelleme
- Android 13+ notification permission
- FCM token/topic
- reboot/update sonrası topic
- App Links
- sosyal app -> browser fallback
- favoriler/geçmiş korunumu
- offline/yavaş ağ
- rotation
- büyük font / erişilebilirlik
- API 26 minimum
- API 36
- 16 KB page-size device/emulator

## D. Store varlıkları

Hazır:
- 512×512 ikon
- 1024×500 feature graphic
- TR mağaza metni
- privacy URL
- support email

Kalan:
- final production build ekran görüntüleri,
- final build ile mağaza metni parity kontrolü.

## E. Hesap türüne bağlı kapalı test

Eğer kişisel geliştirici hesabı 13 Kasım 2023 sonrasında oluşturulduysa:
- en az 12 tester
- en az 14 gün kesintisiz opted-in kapalı test
- ardından production access başvurusu

## F. Tamamlanmış altyapı

- Production Firebase Android app kayıtlı
- production google-services.json indirildi
- OIDC/WIF
- service account JSON key yok
- FCM dry-run
- Schema v2
- AI/HIGGS topics
- App Links preview + production kaydı
- privacy/Data Safety taslağı
- Protect main
- PR + validate
- force push/delete engeli
- CSP/security headers
