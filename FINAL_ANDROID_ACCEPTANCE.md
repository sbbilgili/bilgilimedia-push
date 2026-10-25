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

## Fiziksel cihazda zorunlu kabul testleri

Aşağıdaki maddeler kullanıcı tarafından gerçek cihazda kabul edilmeden dal `main`e merge edilmez:

- [ ] Ana Sayfa 1.4.8 ile görsel olarak kabul edildi.
- [ ] Ara ekranı ve filtre davranışı kabul edildi.
- [ ] Kayıtlar/Favoriler kabul edildi.
- [ ] Hesaplar ekranı ve 33 sosyal hesap davranışı kabul edildi.
- [ ] Daha ekranı kabul edildi.
- [ ] Bildirimler ekranı 1.4.8 ile aynı kullanıcı deneyimini veriyor.
- [ ] Android geri tuşu: alt ekranlardan doğru üst ekrana dönüyor; beklenmedik uygulama kapanması yok.
- [ ] Makale ekranından geri dönüş doğru.
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

## Merge kuralı

`main` korumalı kalır. Final Android dalı ancak:
1. CI tamamen yeşil,
2. bu belgedeki fiziksel QA maddeleri kabul edilmiş,
3. kullanıcı tarafından 1.4.8 arayüz parity'si onaylanmış,
4. Play Console app-signing fingerprint doğrulanmış
olduğunda merge edilir.
