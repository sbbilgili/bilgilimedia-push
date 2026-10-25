# BilgiliMedia — Google Play Data Safety çalışma taslağı

Bu dosya, Play Console'daki **Veri güvenliği** formunu son üretim AAB ile tutarlı doldurmak için hazırlanmıştır. Play Console'da o gün görünen soru metni her zaman önceliklidir.

## Final 1.4.9 kaynak baseline

25 Ekim 2026'da kullanıcı tarafından onaylanan ve `main`e alınan Android kaynak:

- Production package: `com.bilgilimedia.mobile`
- versionName: `1.4.9`
- versionCode: `23`
- targetSdk: `36`
- minSdk: `26`
- Firebase Cloud Messaging: mevcut
- Firebase Installations: firebase-messaging üzerinden transitif olarak mevcut
- Google Analytics for Firebase: yok
- Crashlytics: yok
- AdMob / Google Mobile Ads: yok
- Hesap / giriş sistemi: yok
- Konum, kamera, mikrofon, rehber, SMS, telefon ve genel depolama erişimi: yok
- cleartext HTTP: kapalı
- uygulama backup: kapalı
- favoriler, okuma geçmişi, tema ve bildirim tercihleri: cihazda yerel SharedPreferences

CI release candidate APK/AAB üzerinde izin, package, API seviyesi ve artifact audit çalıştırır. Play'e yüklenecek imzalı final AAB üzerinde bu tespitler son kez tekrar doğrulanmalıdır.

## Console için hazır cevap özeti

Play Console'daki güncel soru metni her zaman önceliklidir. Mevcut 1.4.9 davranışına göre çalışma cevabı:

| Soru / alan | Hazır cevap |
| --- | --- |
| Uygulama kullanıcı verisi topluyor mu? | **Evet** |
| Device or other IDs | **Toplanıyor** — Firebase Installation ID (FID) |
| Amaç | **App functionality / Uygulama işlevselliği** — FCM kurulum hedefleme ve kategori bildirimleri |
| Toplama isteğe bağlı mı? | **Hayır** — Firebase Installations/FCM teknik kimliği SDK tarafından otomatik oluşturulabilir |
| Üçüncü tarafla paylaşım | **Hayır olarak değerlendirilir**, Firebase/Google yalnız BilgiliMedia adına hizmet sağlayıcı olarak işliyorsa Play'in service-provider istisnası uygulanır |
| Veri aktarımda şifreli mi? | **Evet** — Firebase ve BilgiliMedia ağ trafiği HTTPS/TLS |
| Hesap oluşturma | **Yok** |
| Hesap silme | **Uygulanamaz** — kullanıcı hesabı yok |
| Reklam içeriyor mu? | **Hayır**, `sponsorAds.enabled=false` olduğu sürece |
| Analytics | **Yok** |
| Crash reporting SDK | **Yok** |
| Konum / kamera / mikrofon | **Toplanmıyor / izin istenmiyor** |

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

Mevcut mimaride BilgiliMedia, FID/FCM teknik kimliklerini reklam ağına satmaz ve bağımsız üçüncü taraf profilleme amacıyla paylaşmaz.

Firebase/Google, FCM ve Firebase Installations verilerini uygulama işlevselliğini sağlamak üzere BilgiliMedia adına hizmet sağlayıcı olarak işler. Play'in güncel Data Safety tanımında bir service provider'ın geliştirici adına veri işlemesi “sharing” beyanından istisna tutulabilir. Bu nedenle mevcut kullanım için çalışma cevabı **Shared: No / Hayır**dır; Console'daki güncel soru metni ve Firebase kullanım yapılandırması son yükleme gününde yeniden kontrol edilmelidir.

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

`sponsorAds.enabled=false` olduğu sürece:

- “Contains ads” için önerilen cevap: **No / Hayır**.
- AdMob veya başka reklam SDK'sı yoktur.

Sponsorlu içerik etkinleştirilmeden önce “Contains ads”, gizlilik politikası ve Data Safety beyanı yeniden değerlendirilmelidir.

## Final AAB için zorunlu tekrar kontrol

- [x] Production kaynakta yalnız Firebase Messaging bağımlılığı var; Installations transitif.
- [x] Analytics/Crashlytics/AdMob dependency yok.
- [x] CI manifest izinlerini ve hassas izin yokluğunu doğruluyor.
- [x] cleartext trafik kapalı.
- [ ] Play'e yüklenecek final imzalı AAB üzerinde SDK/manifest tekrar doğrulandı.
- [ ] Data Safety formundaki “collected/shared/purpose/optional” seçenekleri o günkü Console metniyle eşleştirildi.
- [ ] Gizlilik URL'si Play Console'a girildi.
- [ ] Play review sonrası Data Safety ekranı mağaza girişinde beklenen şekilde görünüyor.
