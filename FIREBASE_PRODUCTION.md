# BilgiliMedia Production Firebase kaydı

Bu dosya yalnız üretim Android uygulamasının **gizli olmayan kimlik bilgilerini** kaydeder. Firebase client API key, servis hesabı anahtarı, upload/app-signing private key veya parola burada tutulmaz.

## Production Android app

- Firebase project: `bilgilimedia-60d6f`
- Project number: `190776938532`
- Android package: `com.bilgilimedia.mobile`
- Firebase Android app ID: `1:190776938532:android:714bfdfa9b2ab78c4788b1`
- App nickname: `BilgiliMedia Production`
- Registration date: 24 Ekim 2026

## Preview Android app

Preview ayrı Firebase istemcisi olarak korunur:

- Android package: `com.bilgilimedia.mobile.media.preview`
- Firebase Android app ID: `1:190776938532:android:56fff2dc4359a7ac4788b1`

Production ve Preview paketleri karıştırılmamalıdır.

## google-services.json

Production kaydı oluşturulduktan sonra Firebase Console'dan güncel `google-services.json` indirildi ve dosyada hem production hem preview client kaydı doğrulandı.

Güvenlik ve build disiplini:

- Production build, package `com.bilgilimedia.mobile` client kaydını kullanmalıdır.
- Preview build, package `com.bilgilimedia.mobile.media.preview` client kaydını kullanmalıdır.
- `google-services.json` servis hesabı private key'i değildir; yine de final build girdisi olarak kontrollü tutulur ve gereksiz yere public repo'ya eklenmez.
- Servis hesabı JSON key'i oluşturulmaz. Backend kimliği GitHub OIDC + Workload Identity Federation ile sağlanır.

## Play öncesi kalan Firebase testleri

- Production AAB oluşturulduktan sonra FCM token alma.
- Topic subscribe/unsubscribe.
- Android 13+ notification permission.
- Token rotation sonrası topic yeniden eşleme.
- Device reboot / app update sonrası bildirim kontrol planlaması.
- AI/HIGGS dahil uzaktan kategori eşleşmesi.
