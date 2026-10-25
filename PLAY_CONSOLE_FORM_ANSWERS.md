# BilgiliMedia — Google Play Console hazır form cevapları

Tarih: 25 Ekim 2026

Bu dosya final 1.4.9 Android kaynak davranışına göre hazırlanmıştır. Play Console'daki güncel soru metni her zaman önceliklidir.

## Uygulama kimliği

- Uygulama adı: **BilgiliMedia**
- Package: `com.bilgilimedia.mobile`
- Varsayılan dil: Türkçe
- Tür: App / Uygulama
- Fiyatlandırma planı: mevcut ürün kararıyla ücretsiz
- targetSdk: 36
- minSdk: 26

## App access / Uygulama erişimi

**Hazır cevap:** Tüm işlevler oturum açmadan kullanılabilir; özel erişim, kullanıcı adı veya parola gerekmez.

Play Console seçimi: **No special access required / Giriş gerekmiyor**.

## Ads / Reklam

`sponsorAds.enabled=false` olduğu ve reklam SDK'sı bulunmadığı sürece:

**Hazır cevap: No / Uygulama reklam içermiyor.**

Sponsor sistemi gelecekte etkinleştirilirse bu cevap, gizlilik politikası ve Data Safety yeniden değerlendirilmelidir.

## Privacy policy

URL: `https://bilgilimedia-60d6f.web.app/privacy/`

Politika FCM/Firebase Installations teknik kimliklerini, yerel tercihleri ve Hosting ağ işlemesini açıklamalıdır.

## Data Safety

Detaylı çalışma dosyası: `PLAY_DATA_SAFETY_DRAFT.md`

Özet:
- User data collected: **Yes**
- Device or other IDs: **Yes — Firebase Installation ID (FID)**
- Purpose: **App functionality**
- Collection optional: **No**
- Shared: **No çalışma cevabı** — Firebase/Google service-provider istisnası mevcut kullanım için uygulanıyorsa
- Encrypted in transit: **Yes**
- Analytics: **No**
- Crashlytics: **No**
- AdMob: **No**
- User account: **No**

## News declaration

BilgiliMedia'nın mevcut ürün amacı, çeşitli kategorilerde kaynaklı kısa bilgi/içerik sunmaktır; bir haber yayıncısı veya haber toplama uygulaması olarak tasarlanmamıştır.

**Hazır çalışma cevabı: Uygulama bir News app değildir.**

Final store metni ve canlı içerik davranışı haber odaklı hale getirilirse bu cevap yeniden değerlendirilmelidir.

## User generated content / Sosyal özellik

- Kullanıcı içerik yükleme: yok
- Kullanıcılar arası mesajlaşma: yok
- Yorum sistemi: yok
- Kullanıcı profili/hesabı: yok

UGC politikası gerektiren ürün özelliği mevcut değildir.

## Sensitive permissions

Final kaynakta istenmeyen hassas izin yok:
- konum yok
- kamera yok
- mikrofon yok
- rehber yok
- SMS yok
- telefon yok
- genel depolama erişimi yok

Mevcut temel izinler ağ, bildirim, reboot/job ve FCM işlevleri içindir.

## Target audience / Hedef kitle

**Console'da son ürün kararı gerektirir.**

Çocuklara özel tasarım, çocuklara yönelik karakter/oyunlaştırma veya çocuk hedefli reklam altyapısı mevcut değildir. Ancak yaş grubu seçimi store görünürlüğü ve policy kapsamını etkilediğinden, Console'daki güncel yaş grupları görülmeden otomatik işaretlenmez.

Çocuk yaş grupları seçilirse Families Policy yükümlülükleri ayrıca incelenmelidir.

## IARC content rating

**Console anketinde final canlı içerik üzerinden tamamlanır.**

Yanıt verirken:
- kullanıcı üretimli içerik yok
- kumar/para oyunu özelliği yok
- uygulama içi satın alma yok
- kullanıcılar arası iletişim yok
- web/sosyal bağlantılar harici tarayıcı/uygulamalara açılabilir
gerçek ürün davranışıyla uyumlu beyan edilmelidir.

## Store listing

Kaynak dosya: `PLAY_STORE_LISTING_TR.md`

Hazır varlıklar:
- 512×512 ikon
- 1024×500 feature graphic
- Türkçe kısa açıklama
- Türkçe tam açıklama
- destek e-postası
- gizlilik URL'si

Kalan:
- final Play-delivered production build'den telefon ekran görüntüleri
- store açıklamasıyla final build parity kontrolü

## Contact

Play Console'da developer/contact bilgileri hesap doğrulamasıyla ve Payments profiliyle tutarlı olmalıdır.

Support email mevcut store listing dosyasında kayıtlıdır.

## Testing

Hesap 13 Kasım 2023 sonrasında oluşturulmuş kişisel geliştirici hesabıysa:
- minimum 12 opted-in tester
- kesintisiz minimum 14 gün closed testing
- ardından production access başvurusu
gereklidir.

Internal testing için bu 12/14 gün şartı yoktur; final Play-delivered build'i erken doğrulamak için kullanılabilir.

## İmza / package registration

Detaylı prosedür: `PLAY_SIGNING_AND_PACKAGE_REGISTRATION.md`

`com.bilgilimedia.mobile` Play dışında daha önce kullanıldığı için package registration sırasında mevcut private signing key ownership kanıtı istenebilir. Farklı bir signing key ile sahiplik kanıtı yapılmamalıdır.
