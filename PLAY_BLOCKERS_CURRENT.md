# BilgiliMedia — Google Play kalan işler (25 Ekim 2026)

Bu dosya yalnız **henüz kapanmamış** veya kullanıcı/Play Console doğrulaması bekleyen maddeleri listeler.

## A. Yayını bloke edenler

### 1. Geliştirici hesabı kimlik doğrulaması
Durum: BEKLİYOR — kimlik belgesi kullanıcıya ulaşmadı.

Türkiye'de bireysel hesap doğrulamasında, Play Console akışına göre:
- geçerli devlet tarafından verilmiş kimlik belgesi,
- gerekirse son 90 gün içinde düzenlenmiş ad/adres eşleşen adres kanıtı,
- Google Payments profilindeki ad ve adres ile belgelerin birebir eşleşmesi
hazır olmalıdır.

Kimlik gelir gelmez Play Console'da doğrulama tamamlanacak.

### 2. Final Android kaynak onayı
Durum: TAMAMLANDI.

Kullanıcı 25 Ekim 2026'da final 1.4.9 UI baseline'ını gerçek cihazda onayladı.
PR #23 `main`e merge edildi ve Android kaynak projesi artık ana repoda yeniden üretilebilir durumda.

UI kilit kaydı:
- `ANDROID_UI_LOCK_1_4_9.md`
- final kabul kapısı: `FINAL_ANDROID_ACCEPTANCE.md`

CI kontrolleri:
- source contract,
- Android lint,
- QA APK,
- release APK,
- release AAB,
- production artifact audit,
- CodeQL.

### 3. Final imzalı production AAB
Durum: İMZA / PLAY CONSOLE AŞAMASI BEKLİYOR.

CI, production package `com.bilgilimedia.mobile` için release AAB üretiyor ve artifact audit'ten geçiriyor.
Ancak Play'e yüklenecek final AAB için:
- final UI cihaz onayı,
- production/upload signing yapılandırması,
- imzalı upload AAB,
- Play Console App Bundle Explorer / pre-launch kontrolü
tamamlanmalıdır.

Teknik hedefler:
- package: `com.bilgilimedia.mobile`
- versionCode: `23` veya daha yüksek
- targetSdk: `36`
- minSdk: `26`
- cleartext: kapalı
- backup: kapalı
- 16 KB uyumluluk artifact audit'i: aktif

### 4. Play App Signing / imza sürekliliği
Durum: PLAY CONSOLE UYGULAMASI OLUŞTURULUNCA.

Mevcut production sertifika SHA-256:
`F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`

Amaç:
- mevcut `com.bilgilimedia.mobile` imza sürekliliğini korumak,
- Play App Signing yapılandırmasında mevcut app-signing key'i kullanmak,
- upload key'i ayrı tutmak,
- Play'in gösterdiği App Signing SHA-256 fingerprint(ler)ini `assetlinks.json` ile karşılaştırmak.

## B. Kimlik gelir gelmez Play Console sırası

1. Kimlik / Payments profil doğrulamasını tamamla.
2. `com.bilgilimedia.mobile` paket kaydını doğrula/kaydet.
3. BilgiliMedia uygulamasını oluştur.
4. Play App Signing'i mevcut imza sürekliliğine göre yapılandır.
5. Final imzalı AAB'yi yükle.
6. App access: giriş gerekmiyor.
7. Ads: sponsor sistemi kapalıyken No.
8. Privacy URL.
9. Data Safety.
10. IARC içerik derecelendirme.
11. Target audience.
12. News declaration.
13. Store listing.
14. Internal / Closed testing release.

## C. Kapalı test gerekliliği

Eğer kişisel geliştirici hesabı 13 Kasım 2023 sonrasında oluşturulduysa:
- en az 12 tester,
- testerların en az 14 gün kesintisiz opted-in kalması,
- sonrasında production access başvurusu
gerekiyor.

Bu süre Play tarafından zorunluysa kimlik doğrulaması tamamlanınca mümkün olan en erken anda closed test başlatılmalıdır.

## D. Fiziksel QA

Final production-signed veya Play-delivered build üzerinde:
- temiz kurulum,
- eski `com.bilgilimedia.mobile` sürümünün üzerine güncelleme,
- API 26 ve API 36,
- Android 13+ notification permission,
- FCM token/topic,
- reboot/update sonrası topic,
- kategori seçimi boşken bildirim gelmemesi,
- App Links,
- app-first içerik paylaşımı + web fallback,
- sosyal app -> HTTPS fallback,
- favoriler/geçmiş/tema/bildirim tercihlerinin güncellemede korunması,
- offline/yavaş ağ,
- büyük font/erişilebilirlik,
- 16 KB page-size ortamı.

## E. Store varlıkları

Hazır:
- 512×512 ikon,
- 1024×500 feature graphic,
- Türkçe mağaza metni,
- privacy URL,
- support email.

Kalan:
- final production build ekran görüntüleri,
- final build ile mağaza metni parity kontrolü.

## F. Tamamlanan altyapı

- Production Firebase Android app kayıtlı.
- Firebase Messaging / topic altyapısı.
- OIDC/WIF; uzun ömürlü service-account JSON key yok.
- Schema v2, AI/HIGGS ve uzaktan kategori yapısı.
- App-first paylaşım + web fallback.
- Preview + production App Links.
- Hosting CSP/security headers.
- Protect main, PR + validate, force push/delete engeli.
- Android CI: lint/build/AAB/artifact audit/CodeQL.
