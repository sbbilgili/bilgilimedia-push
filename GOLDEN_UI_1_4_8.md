# BilgiliMedia 1.4.8 — Golden UI & Behavior Contract

Bu belge, Google Play production sürümlerinde korunması gereken **orijinal BilgiliMedia 1.4.8 arayüz ve davranış sözleşmesidir**.

Kaynak referans:

- APK: `BilgiliMedia_Medya_Onizleme_1.4.8_ORIJINAL_ARAYUZ.apk`
- SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`
- package: `com.bilgilimedia.mobile.media.preview`
- versionName: `1.4.8`
- versionCode: `22`
- targetSdk: `36`
- minSdk: `26`

Bu APK görsel/işlevsel regresyon için altın referanstır. Final production package `com.bilgilimedia.mobile` olacaktır; package farkı, arayüz veya kullanıcı davranışını sadeleştirmek için gerekçe değildir.

## 1. Ana navigasyon

Orijinal uygulama aşağıdaki ana yolları içerir:

- Ana Sayfa
- Ara
- Kayıtlar
- Hesaplar
- Daha

Uygulama içindeki durum/route adları arasında şunlar bulunur:

- `home`
- `search`
- `saved`
- `accounts`
- `more`
- `history`
- `notifications`
- `settings`
- `privacy`
- `about`
- `developer`
- `article`

Production rebuild bu yolların işlevsel karşılıklarını korumalıdır.

## 2. Ana Sayfa

Ana sayfada korunması gereken içerik blokları:

- BİLGİLİMEDIA marka başlığı
- “Bugün ne keşfetmek istersin?” karşılama metni
- Güncel kısa okumalar / popüler içerikler / sosyal hesaplar özeti
- Canlı durum göstergesi
- Yenile
- Öne Çıkan / Editör seçimi
- Yeni Eklenenler
- Son Okunanlar / Kaldığın yerden devam et
- En Çok Okunanlar / cihazdaki okuma sayılarına göre
- Kategoriye göre seçkiler
- Hızlı Erişim
- Sosyal hesaplarımız

Ana sayfa yeniden yapılırken bu bölümlerden herhangi biri kaldırılmamalıdır; tasarım iyileştirmesi yapılabilir ancak işlev kaybı kabul edilmez.

## 3. İçerik kartları ve makale ekranı

İçerik kartlarında desteklenen davranışlar:

- kategori
- okuma süresi
- video etiketi
- kaydet / favori
- içerik açma
- görsel kapak
- popülerlik / okunma
- kaynak göstergesi

Makale ekranında:

- geri
- kategori ve okuma süresi
- içerik gövdesi
- kapak/görsel kredisi
- video açma
- favoriye ekleme / çıkarma
- paylaşma
- kaynaklar listesi
- BilgiliMedia notu

Paylaşım bağlantısı `https://bilgilimedia-60d6f.web.app/content/<id>?v=<feedVersion>` yapısını korumalıdır.

## 4. Arama ve filtreleme

Arama ekranında korunacak özellikler:

- başlık
- kategori
- tür
- popülerlik
- arama metni
- içerik türü: Tümü / Video / Yazı
- sıralama: Yeni / Popüler
- kategori filtresi

Türkçe karakterlerle ve aksansız eşleşme davranışı korunmalıdır.

## 5. Favoriler / Kayıtlar

- Favori içerikler cihazda yerel tutulur.
- Favori ekleme/kaldırma.
- Favori içerikler ayrı ekranda gösterilir.
- Kaydedilen sosyal hesaplar / hızlı erişim desteklenir.
- Uygulama güncellemesinde SharedPreferences tabanlı yerel veriler korunmalıdır.

## 6. Okuma Geçmişi

- Son açılan içerikler gösterilir.
- Okunma zamanı tutulur.
- Geçmiş temizlenebilir.
- Canlı listeden kaldırılmış içerikler için bozuk/crash davranışı olmamalıdır.
- Güncelleme sırasında geçmiş verisi korunmalıdır.

## 7. Sosyal hesaplar

Orijinal 1.4.8, BilgiliMedia sosyal ağ hesaplarını ayrı ekranda sunar.

Desteklenen platformlar:

- Instagram
- YouTube
- TikTok
- Facebook

Toplam sosyal hesap seti referans build'de 33 hesaptır.

Açma davranışı:

1. ilgili resmi sosyal uygulama varsa uygulamada açmayı dene,
2. uygun uygulama yoksa HTTPS tarayıcı fallback.

## 8. Kategoriler

Referans APK fallback olarak şu kategorileri bilir:

- Genel
- Belgesel
- Tarih
- Doğa
- Yemek
- Teknoloji
- Animasyon
- Çizgi Film

Canlı `app-config.json` artık Schema v2 kullanır ve ayrıca şunları destekler:

- AI
- HIGGS

Production istemcisi canlı kategori listesini ve sabit `category.id` / FCM `topic` eşleşmesini kullanmalıdır. Yeni kategori eklenmesi mümkün olduğunca APK güncellemesi gerektirmemelidir.

## 9. Çok dallı navigasyon hedefi

Schema v2 ile server tarafında aşağıdaki model desteklenir:

- group
- category
- screen
- page

Production 1.4.9+ istemcisi nested navigation ağacını okuyabilmeli; ancak golden 1.4.8'in ana kullanıcı deneyimini kaybetmemelidir.

## 10. Bildirimler

Korunması gereken davranış:

- Android 13+ POST_NOTIFICATIONS izni
- master bildirim anahtarı
- kategori bazlı topic tercihleri
- Firebase token saklama/yenileme
- topic subscribe/unsubscribe
- FCM data-only mesajdan yerel bildirim
- bildirime dokununca ilgili içerik
- cihaz yeniden başlatma / uygulama güncellemesi sonrası işlerin yeniden planlanması
- teknik Firebase durumunun normal kullanıcıya gereksiz gösterilmemesi

Backend topic eşleşmesi `app-config.json` üzerinden gelir.

## 11. Ayarlar / Daha

“Daha” ekranında referans davranış:

- Ayarlar
- tema ve yerel veriler
- Bildirimler
- Okuma Geçmişi
- Gizlilik
- Hakkında
- uzaktan `extraPages`
- BilgiliMedia web sitesi
- iletişim
- uygulamayı paylaş

Hakkında sürüm satırı gizli geliştirici/tanılama erişimi için kullanılabilir.

## 12. Gizlilik

- Gizlilik sayfası: `https://bilgilimedia-60d6f.web.app/privacy/`
- Hesap sistemi yok.
- Favoriler/geçmiş/tema/tercihler yerel.
- Analytics/Crashlytics/AdMob mevcut sürümde yok.
- FCM/Firebase Installations kullanımı gizlilik politikasında açıklanır.
- Konum, kamera, mikrofon, rehber, SMS ve genel depolama izni yok.

## 13. Sponsor sistemi

Referans APK uzaktan sponsor kartı kod yolları içerir, ancak:

`sponsorAds.enabled=false`

olduğu sürece sponsor gösterilmemelidir.

Sponsor sistemi ileride açılırsa:

- açık “Sponsorlu” etiketi,
- yalnız HTTPS hedef,
- Play “Contains ads” yeniden değerlendirmesi,
- privacy/Data Safety yeniden değerlendirmesi

zorunludur.

## 14. Güvenlik sözleşmesi

Production build için:

- `usesCleartextTraffic=false`
- `allowBackup=false`
- yalnız gerekli exported bileşenler
- FCM service exported=false
- App Links autoVerify
- HTTPS içerik/config/feed
- gereksiz hassas izin yok
- servis hesabı private key APK içinde yok
- Google Cloud backend anahtarsız OIDC/WIF

## 15. Golden regression kabul kriteri

Final production build ancak aşağıdakiler sağlanırsa kabul edilir:

- [ ] Ana navigasyon parity
- [ ] Ana Sayfa blokları parity
- [ ] Arama/filtre parity
- [ ] Favoriler parity
- [ ] Okuma geçmişi parity
- [ ] Sosyal hesap parity
- [ ] Bildirim/topic parity
- [ ] Ayarlar/Gizlilik/Hakkında parity
- [ ] App Links parity
- [ ] paylaşım `?v=` parity
- [ ] canlı feed/config parity
- [ ] offline/cache/fallback davranışı
- [ ] AI/HIGGS ve yeni uzaktan kategori desteği
- [ ] mevcut yerel verilerin production güncellemesinde korunması
- [ ] temiz kurulum ve eski production paket üzerine güncelleme testi
