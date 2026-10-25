# BilgiliMedia — Kimlik geldiği gün Play Console işlem sırası

Tarih referansı: 25 Ekim 2026.

Bu dosya, kimlik belgesi ulaştığında Play Console'da işlemlerin hangi sırayla yapılacağını kaydeder.

## Hazır tutulacaklar

- Geçerli kimlik belgesi.
- Play isterse son 90 gün içinde düzenlenmiş ad/adres içeren adres kanıtı.
- Google Payments profilindeki yasal ad ve adres.
- Developer email ve contact email.
- Telefon numarası.
- BilgiliMedia support email.
- Gizlilik URL'si: `https://bilgilimedia-60d6f.web.app/privacy/`.
- Production package: `com.bilgilimedia.mobile`.
- Production signing sertifika SHA-256:
  `F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`.

## Play Console sırası

1. Developer identity / Payments profile doğrulaması.
2. Package name registration durumunu kontrol et.
   - `com.bilgilimedia.mobile` daha önce Play dışında kullanıldığı için sahiplik kanıtı istenirse `PLAY_SIGNING_AND_PACKAGE_REGISTRATION.md` prosedürünü uygula.
   - Mevcut production sertifika fingerprintini kullan; farklı bir key ile package ownership kanıtlamaya çalışma.
3. Create app:
   - App name: BilgiliMedia
   - Default language: Turkish
   - App
   - Free (mevcut plan)
4. App signing:
   - `PLAY_SIGNING_AND_PACKAGE_REGISTRATION.md` rehberini takip et,
   - mevcut production app-signing key sürekliliğini koru,
   - upload key ile app-signing key'i ayır,
   - Play App Signing certificate SHA-256 değer(ler)ini kaydet.
5. App Links:
   - Play App Signing fingerprint(ler)ini `assetlinks.json` ile karşılaştır,
   - gerekiyorsa ek fingerprintleri Hosting'e yayınla.
6. Final imzalı AAB yükle.
7. App content:
   - App access: No login required
   - Ads: No (sponsorAds kapalıyken)
   - Data Safety
   - Content rating
   - Target audience
   - News declaration
   - Privacy policy
8. Store listing:
   - ad / short description / full description
   - icon / feature graphic
   - final production screenshots
   - support contact
9. Internal test ile Play-delivered build'i kontrol et.
10. Hesap closed-test şartına tabiyse en az 12 tester ile 14 günlük kesintisiz closed test'i hemen başlat.

## Closed test sırasında tutulacak kayıt

- tester listesi ve opt-in tarihleri,
- test edilen cihaz/Android sürümü,
- bulunan hata,
- yapılan düzeltme,
- production readiness başvurusunda kullanılacak test özeti.

## Production access başvurusu için notlar

Closed test şartı uygulanıyorsa başvuruda:
- testten öğrenilenler,
- test geri bildirimlerine göre yapılan değişiklikler,
- uygulamanın production'a hazır olduğunun nasıl doğrulandığı
açıklanmalıdır.
