# BilgiliMedia — Google Play Data Safety çalışma taslağı

Bu dosya, Play Console'daki **Veri güvenliği** formunu son üretim AAB ile tutarlı doldurmak için hazırlanmıştır. Play Console'da o gün görünen soru metni her zaman önceliklidir.

## Doğrulanmış referans build

24 Ekim 2026'da denetlenen orijinal arayüzlü preview APK:

- Paket: `com.bilgilimedia.mobile.media.preview`
- Sürüm: `1.4.8`
- versionCode: `22`
- SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`
- Firebase Cloud Messaging: mevcut
- Google Analytics for Firebase: yok
- Crashlytics: yok
- AdMob / üçüncü taraf reklam SDK'sı: yok
- Hesap / giriş sistemi: yok
- Konum, kamera, mikrofon, rehber, SMS, genel depolama izni: yok

Final Play AAB üretildikten sonra bu tespitler AAB üzerinde yeniden doğrulanmalıdır.

## Data Safety için önerilen temel beyan

### Uygulama kullanıcı verisi topluyor mu?

**Evet.**

Firebase Cloud Messaging ve Firebase Installations nedeniyle uygulama kurulumuna bağlı teknik tanımlayıcı cihazdan Google/Firebase altyapısına iletilir.

### Veri türü

**Device or other IDs / Cihaz veya diğer kimlikler: EVET**

Firebase Installation ID (FID), Google Play'in veri türü tanımında açıkça “Device or other IDs” örnekleri arasındadır.

FCM ayrıca topic aboneliği ve mesaj teslimi için uygulama sürümü, Firebase user agent bilgileri ve kurulum/bildirim teslimiyle ilgili teknik tanımlayıcıları işler.

### Amaç

Mevcut ürün davranışında temel amaç:

- **App functionality / Uygulama işlevselliği** — kategori/topic bildirimlerinin doğru uygulama kurulumuna ulaştırılması.

Play Console'un o günkü açıklamalarına göre yeni içerik push'larının “Developer communications” kapsamında değerlendirilmesi isteniyorsa bu amaç da seçilebilir. Reklam/marketing amacı, sponsor sistemi kapalıyken seçilmemelidir.

### Veri paylaşımı

Mevcut mimaride BilgiliMedia, bu teknik kimlikleri reklam ağına veya bağımsız üçüncü taraflara satmaz ya da paylaşmaz. Firebase/Google teknik verileri BilgiliMedia adına hizmet sağlayıcı olarak işler.

Play Console'un güncel “sharing” tanımındaki **service provider exception** kontrol edilerek önerilen cevap **No / Hayır** olarak doğrulanmalıdır.

### Zorunlu / isteğe bağlı

Mevcut 1.4.8 build'inde Firebase Messaging otomatik başlatılabildiği için FID/FCM teknik kimliği, kullanıcı Android bildirim iznini reddetse bile SDK çalışması kapsamında oluşturulabilir. Bu nedenle veri toplama kullanıcı tarafından tamamen isteğe bağlı kabul edilmemelidir.

### Şifreleme

Firebase ve BilgiliMedia Hosting bağlantıları HTTPS/TLS kullanır. Play formunda “data encrypted in transit” için final build üzerinde tekrar doğrulanarak **Yes** seçilmesi beklenir.

## Diğer veri türleri

Mevcut denetlenen APK'ya göre BilgiliMedia tarafından aşağıdaki veri türleri toplanmıyor:

- Ad, e-posta, telefon numarası veya hesap bilgisi
- Kesin/yaklaşık konum
- Rehber
- Fotoğraf/video dosyaları
- Ses kayıtları
- SMS
- Finansal bilgi
- Sağlık verisi
- Uygulama içi mesajlaşma/UGC
- Reklam kimliği
- Crash logları (Crashlytics yok)
- Ayrı analytics olayları (Analytics SDK yok)

Kaydedilenler, okuma geçmişi, tema ve bildirim tercihleri cihazda yerel tutulur; sunucuya kullanıcı profili olarak yüklenmez.

## Firebase Hosting

Uygulama `feed.json` ve `app-config.json` dosyalarını Firebase Hosting üzerinden indirir. Firebase Hosting standart ağ isteklerinde IP adresini hizmetin işletilmesi, güvenlik ve kötüye kullanım tespiti için işleyebilir. BilgiliMedia bu IP bilgisini konum çıkarmak, reklam hedeflemek veya kullanıcı profili oluşturmak için kullanmaz.

## Hesap silme

Uygulamada kullanıcı hesabı oluşturma özelliği yoktur. Bu nedenle Google Play'in “account deletion” zorunluluğu mevcut ürün davranışında tetiklenmez.

Yerel uygulama verileri uygulama içindeki temizleme seçenekleriyle veya uygulama kaldırılarak silinebilir.

## Reklam

Mevcut production release'te iki kapı birlikte kapalıdır: `sponsorAds.enabled=false` ve Android kaynakta `SPONSOR_FEATURE_ENABLED=false`.

- “Contains ads” için önerilen cevap: **No / Hayır**.
- AdMob veya başka reklam SDK'sı yoktur.
- Remote config tek başına sponsor kartını açamaz.

Sponsorlu içerik etkinleştirilmeden önce `SPONSOR_FEATURE_ENABLED` bilinçli bir release değişikliğiyle açılmalı; aynı release öncesinde “Contains ads”, gizlilik politikası ve Data Safety beyanı yeniden değerlendirilmelidir.

## Final AAB için zorunlu tekrar kontrol

- [ ] Production package `com.bilgilimedia.mobile` üzerinde SDK taraması yapıldı.
- [ ] Firebase Messaging/Installations dışında veri toplayan SDK yok.
- [ ] Analytics/Crashlytics/AdMob eklenmedi.
- [ ] Manifest izinleri tekrar doğrulandı.
- [ ] Data Safety formundaki “collected/shared/purpose/optional” seçenekleri final AAB davranışıyla eşleşiyor.
- [ ] Gizlilik URL'si canlı ve Play Console'a girildi.
