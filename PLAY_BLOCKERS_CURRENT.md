# BilgiliMedia — Google Play kalan yayın kapıları (25 Ekim 2026)

Bu dosya yalnız **henüz kapanmamış gerçek release kapılarını** listeler. Teknik aday kaynak PR #22 altında tutulur; `main` fiziksel golden QA tamamlanmadan değiştirilmez.

## A. Yayını bloke edenler

### 1. Geliştirici hesabı kimlik doğrulaması
Durum: **BEKLİYOR**.

Kimlik belgesi henüz kullanıcıya ulaşmadığı için Play Console geliştirici kimlik doğrulaması tamamlanmadı.

### 2. Golden 1.4.8 fiziksel regresyon testi
Durum: **BEKLİYOR — MERGE ÖNCESİ ZORUNLU**.

Yeniden üretilebilir production Android kaynak adayı artık `android-final-parity-1-4-8` dalında ve PR #22'de bulunuyor. Kod seviyesinde golden sözleşme CI ile korunuyor.

Final merge öncesinde gerçek cihazda 1.4.8 golden APK ile yan yana doğrulanacak:
- ana ekran ve tüm ekranların görünümü,
- Bildirimler ekranı,
- klasik geri tuşu + Android 13+ gesture back,
- ana sayfada geri hareketinde uygulamanın kapanmaması,
- rotation / yeniden açma,
- favoriler/geçmiş/tema,
- bildirim kategori seçimleri.

### 3. PR #22 final head otomatik release denetimi
Durum: **ÇALIŞACAK / YEŞİL OLMAK ZORUNDA**.

Korunan otomatik kapılar:
- repository/content/config doğrulaması,
- Android golden/security source contract,
- release Java derleme,
- Android lint,
- debug/release APK + release AAB üretimi,
- package/version/minSdk/targetSdk doğrulaması,
- ZIP 16 KB hizası,
- AAB `PAGE_ALIGNMENT_16K`,
- 64-bit ELF LOAD ve GNU_RELRO 16 KB denetimi,
- CodeQL Java/Kotlin + JavaScript,
- signing/private-key materyalinin Git'e girmemesi.

Herhangi biri kırmızıysa PR merge edilmez.

### 4. Final upload-signed AAB
Durum: **BEKLİYOR**.

CI bilerek unsigned release adayı üretir. Fiziksel QA + final CI sonrasında AAB, repository dışındaki keystore ile `scripts/sign-android-aab.sh` üzerinden imzalanacak.

Script:
- parolayı repo veya komut satırı literalinden almaz,
- `jarsigner -verify -strict` çalıştırır,
- isteğe bağlı beklenen sertifika SHA-256 eşleşmesini zorunlu kılar,
- imzalı AAB SHA-256 özetini üretir.

### 5. Play App Signing
Durum: **BEKLİYOR**.

Kimlik doğrulaması / app creation sonrası:
- mevcut production app-signing key sürekliliği planlanacak,
- Play App Signing certificate SHA-256 alınacak,
- `assetlinks.json` fingerprint final olarak karşılaştırılacak,
- upload key ayrı tutulacak.

## B. Play Console form işlemleri

Kimlik geldikten sonra:
- Create app / package registration
- App access
- Ads declaration
- Data Safety
- Privacy URL
- IARC content rating
- Target audience
- News declaration
- Store listing
- App signing
- Closed test gerekiyorsa test + production access

## C. Fiziksel QA

Final signed/QA eşdeğer build üzerinde:
- temiz kurulum,
- eski `com.bilgilimedia.mobile` üstüne güncelleme,
- API 26,
- Android 13+ notification permission,
- Android 16 / API 36,
- 16 KB page-size emulator/device,
- FCM token/topic + hızlı aç/kapat yarış senaryosu,
- reboot/update sonrası job/topic,
- App Links,
- sosyal app -> browser fallback,
- favoriler/geçmiş/tema/bildirim tercihleri,
- offline/yavaş ağ,
- bozuk/uyumsuz remote feed/config fallback,
- rotation,
- büyük font/erişilebilirlik.

## D. Store varlıkları

Hazır:
- 512×512 ikon,
- 1024×500 feature graphic,
- Türkçe mağaza metni,
- privacy URL,
- support email.

Kalan:
- final production build ekran görüntüleri,
- final build ile mağaza metni parity kontrolü.

## E. Hesap türüne bağlı kapalı test

Hesap Google Play'in yeni kişisel geliştirici test şartına tabi ise Play Console'un o gün gösterdiği kapalı test koşulları tamamlanmadan production access alınmayacak.

## F. Teknik olarak kapatılan başlıca riskler

- Production package: `com.bilgilimedia.mobile`
- versionCode 23 / versionName 1.4.9 release adayı
- target/compile SDK 36, minSdk 26
- Production Firebase config
- Golden 1.4.8 back davranışı + Android 13+ callback
- kullanıcı etkileşimi olmadan startup notification prompt yok
- FCM confirmation-based topic sync + son kullanıcı tercihi race convergence
- sabit category ID tercih migrasyonu
- mevcut 24 feed içeriğinde categoryId migrasyonu
- feed/config birlikte doğrulanan last-known-good snapshot
- HTTPS/trusted-host remote config/feed
- atomik cache ve bozuk veride fallback
- strict bounded Schema v2/nested navigation
- remote developer ekranı açılamaz
- sponsor özelliği compile-time + config release kilitli
- güvenli görsel decode/lifecycle
- Firebase hatasında core app graceful degradation
- CodeQL + Dependabot Gradle
- protected `main`, PR + required `validate`, force-push/delete engeli
- keystore/private key/service-account Git koruması
