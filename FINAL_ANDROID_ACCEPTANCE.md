# BilgiliMedia Android — Final Release Acceptance Gate

Bu belge **Google Play öncesi tek kaynak yayın kapısıdır**. Bir madde doğrulanmadan "final" kabul edilmez.

## Otomatik olarak doğrulananlar

Final hardening dalındaki CI aşağıdakileri zorunlu kılar:

- repository içerik/config doğrulaması,
- Android source contract doğrulaması,
- Android lint (release),
- debug APK build,
- unsigned release APK build,
- release AAB build,
- production applicationId: `com.bilgilimedia.mobile`,
- versionCode >= 23,
- minSdk 26,
- targetSdk 36,
- `allowBackup=false`,
- `usesCleartextTraffic=false`,
- POST_NOTIFICATIONS ve RECEIVE_BOOT_COMPLETED izinlerinin varlığı,
- konum/kamera/mikrofon/rehber/SMS/telefon/depolama gibi beklenmeyen hassas izinlerin yokluğu,
- APK ZIP bütünlüğü,
- 16 KB zip/page alignment kontrolü,
- APK ve AAB SHA-256 üretimi,
- CodeQL JavaScript güvenlik analizi.

## Altın referans

Kabul edilen 1.4.8 APK:

- SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`
- package: `com.bilgilimedia.mobile.media.preview`
- versionName: `1.4.8`
- versionCode: `22`

Golden APK'nin kendisi UI ve davranış açısından otoritedir. Kaynak içindeki string kontrolleri tek başına parity kanıtı sayılmaz.

## Kullanıcı onaylı UI baseline

25 Ekim 2026 tarihinde kullanıcı son QA build'ini gerçek cihazda onayladı.

Onaylanan QA APK:
- versionName: `1.4.9-qa`
- production source versionCode: `23`
- SHA-256: `3d7fe84c2dd826539b6eec5858277ef6b04d91d77dedef5c72cc2e58215275db`

Bu build ile onaylanan başlıca UI/UX kararları:
- BilgiliMedia header görünümü,
- küçültülmüş kırmızı oynatıcı sembolü,
- alt etiket `İçerikler • Medya`,
- Hızlı Erişim / İçerik Kanallarımız yerleşimi,
- Bildirimler ekranı ve kategori seçimi,
- Hesaplar içi platform geçişi,
- çift geri çıkış davranışı,
- içerik paylaşımı app-first + web fallback.

Bu onaydan sonra 1.4.9 UI baseline kilitlidir. Yeni görsel değişiklik yalnız açık yeni sürüm kararıyla yapılır.

## Fiziksel cihaz / Play-delivered zorunlu kabul testleri

Aşağıdaki maddeler Play production yayını açılmadan tamamlanmalıdır:

- [x] Ana Sayfa / final header görünümü kullanıcı tarafından kabul edildi.
- [x] Ara ekranı ve filtre davranışı kaynak/QA regresyon kontrolünden geçti.
- [x] Kayıtlar/Favoriler mevcut final UI baseline içinde kabul edildi.
- [x] Hesaplar ekranı ve platform geçişi kullanıcı tarafından kabul edildi.
- [x] Daha ekranı mevcut final UI baseline içinde kabul edildi.
- [x] Bildirimler ekranı ve kategori seçimi kullanıcı tarafından kabul edildi.
- [x] Android geri tuşu / çift geri çıkış davranışı kullanıcı tarafından kabul edildi.
- [x] Makale ekranından geri dönüş kaynak/QA regresyon kontrolünde korunuyor.
- [ ] Açık/koyu/sistem tema davranışı doğru.
- [ ] Favoriler, geçmiş ve tercihler güncellemede korunuyor.
- [ ] Android 13+ bildirim izni doğru.
- [ ] FCM token yenileniyor.
- [ ] Topic subscribe/unsubscribe doğru.
- [ ] AI/HIGGS ve yeni uzaktan kategori davranışı doğru.
- [ ] App Links ilgili içeriği açıyor.
- [ ] Sosyal link: uygulama varsa uygulama, yoksa HTTPS tarayıcı fallback.
- [ ] Offline/yavaş ağ fallback davranışı doğru.
- [ ] API 26 testinden geçti.
- [ ] API 36 testinden geçti.
- [ ] 16 KB page-size ortamında final Play-generated APK seti doğrulandı.

## Production Firebase

Final imzalı build için production Firebase Android kaydı:

- package: `com.bilgilimedia.mobile`
- project: `bilgilimedia-60d6f`

`google-services-production.json` kurtarma arşivinde saklıdır. Servis hesabı private key'i repository'ye eklenmez.

## Kaynak merge ve yayın kuralı

`main` korumalı kalır.

Android kaynak baseline'ı `main`e alınabilir ancak:
1. CI tamamen yeşil olmalı,
2. kullanıcı final UI baseline'ını gerçek cihazda onaylamış olmalı,
3. kaynak ve güvenlik regresyon kontrolleri geçmeli.

Google Play production yayını ise ayrıca:
1. geliştirici kimlik doğrulaması,
2. Play App Signing / upload signing yapılandırması,
3. Play App Signing fingerprint doğrulaması,
4. final imzalı AAB,
5. Play-delivered fiziksel QA,
6. hesap uygunsa kapalı test şartı
tamamlanmadan açılamaz.
