# BilgiliMedia — Play Signing ve Package Name Registration Runbook

Tarih referansı: 25 Ekim 2026

Bu belge `com.bilgilimedia.mobile` için Google Play / Android developer verification ve signing adımlarını güvenli biçimde yürütmek için hazırlanmıştır.

## Sabit production kimliği

- Package: `com.bilgilimedia.mobile`
- versionCode başlangıç adayı: `23`
- targetSdk: `36`
- minSdk: `26`
- Mevcut production sertifika SHA-256:
  `F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`

## Güvenlik kuralı

Private keystore, private key veya parola:
- GitHub commit'ine eklenmez,
- README/dokümana yazılmaz,
- issue/PR metnine yazılmaz,
- public artifact içine konmaz.

Repo yalnız public sertifika fingerprintini ve signing prosedürünü tutar.

## Package name registration

30 Eylül 2026 itibarıyla Play package name registration gereklidir.

Yeni Play uygulaması oluşturulurken Play çoğu yeni package adını otomatik kaydeder. Ancak `com.bilgilimedia.mobile` Play dışında daha önce kullanıldığı için Play mevcut signing key sahipliğini doğrulamak isteyebilir.

Play Console package registration akışında mevcut package için sahiplik kanıtı istenirse:

1. Play Console'da `com.bilgilimedia.mobile` package kaydını aç.
2. Uygun public certificate olarak production SHA-256 fingerprintini seç/doğrula.
3. Play'in verdiği tek kullanımlık registration snippet'ini kopyala.
4. Final Android kaynakta geçici olarak `android-production/app/src/main/assets/adi-registration.properties` dosyasını oluştur ve snippet'i içine koy.
5. Registration APK'sını package `com.bilgilimedia.mobile` ile derle.
6. APK'yı mevcut production private signing key ile imzala.
7. Play Console'a ownership verification için yükle.
8. Package durumu Registered olduktan sonra geçici `adi-registration.properties` dosyasını kaynak baseline'dan kaldır.
9. Snippet'i repo geçmişinde bırakma; işlem için geçici branch/artifact kullan.

Bu APK normal production release olmak zorunda değildir; amacı private key ownership kanıtıdır.

## Play App Signing

Amaç, mevcut sideload edilmiş `com.bilgilimedia.mobile` kurulumlarının güncelleme sürekliliğini bozmamaktır.

Play App Signing onboarding sırasında:
1. Mevcut production app-signing key sürekliliğini koruyan seçenek kullanılmalı.
2. Play mevcut app-signing key'i içe aktarmayı isterse yalnız Play'in resmi export/encryption akışı kullanılmalı.
3. Upload key, app-signing key'den ayrı tutulmalı.
4. Play Console > App integrity ekranındaki App signing certificate SHA-256 kaydedilmeli.
5. Bu fingerprint mevcut production fingerprint ile karşılaştırılmalı.
6. Play ek/rotated signing certificate gösterirse gerekli fingerprintler `assetlinks.json`a eklenmeli.

## Upload key

Upload key sadece Play'e bundle yüklemek içindir; kullanıcı cihazına dağıtılan APK'yı imzalayan anahtar Play App Signing key'idir.

Upload key oluşturulduğunda:
- ayrı şifreli/offline yedek tutulmalı,
- GitHub'a private key yüklenmemeli,
- CI gerekiyorsa keystore ve parolalar repository secrets/secure environment üzerinden sağlanmalı,
- kaynak dosyada sabit parola bulunmamalı.

## Final AAB

Final Play AAB ancak identity verification tamamlandıktan, package registration durumu doğrulandıktan, Play App Signing planı kesinleştikten ve upload signing yapılandırıldıktan sonra üretilip Play'e yüklenir.

Upload öncesi:
- applicationId = `com.bilgilimedia.mobile`
- versionCode >= 23
- targetSdk = 36
- minSdk = 26
- release lint başarılı
- artifact audit başarılı
- hassas izin yok
- cleartext kapalı
- backup kapalı
- 16 KB uyumluluk kontrolü başarılı
- final UI lock ile parity
doğrulanmalıdır.

## App Links sonrası kontrol

Play-delivered build kurulduktan sonra:
1. Play App Signing SHA-256 değerini canlı `assetlinks.json` ile karşılaştır.
2. `https://bilgilimedia-60d6f.web.app/content/<id>` linkini test et.
3. Uygulama kuruluysa içerik app'te açılmalı.
4. Uygulama yoksa web fallback çalışmalı.
5. `/open/content/...` app-first paylaşım akışı ayrıca test edilmeli.
