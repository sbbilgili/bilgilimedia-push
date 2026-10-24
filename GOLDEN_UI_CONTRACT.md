# BilgiliMedia Golden UI Contract — 1.4.8 referansı

Bu sözleşme, SHA-256 `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76` olan orijinal arayüzlü 1.4.8 Preview APK'dan çıkarılmıştır. Production rebuild bu işlev/ekran yapısını bilinçsizce değiştirmemelidir.

## Alt navigasyon
1. `⌂` Ana Sayfa
2. `⌕` Ara
3. `♡` Kayıtlar
4. `◉` Hesaplar
5. `⋯` Daha

## Ana Sayfa
- `BİLGİLİMEDIA`
- `Bugün ne keşfetmek istersin?`
- Canlı durum ve `↻ Yenile`
- Dinamik kategori rail'i
- Öne Çıkan / Editör seçimi
- Sponsor alanı yalnız `sponsorAds.enabled=true` ise
- Yeni Eklenenler
- Son Okunanlar
- En Çok Okunanlar
- Kategori seçkisi
- Hızlı Erişim / sosyal platformlar

## Ara
- Başlık/kategori/gövde araması
- Türkçe karakter normalizasyonu
- İçerik türü: Tümü / Video / Yazı
- Sıralama: Yeni / Popüler
- Dinamik kategori filtresi

## Kayıtlar
- Yerel favori içerikler
- Yerel favori sosyal hesaplar
- Uygulama yeniden açıldığında kalıcı

## Okuma Geçmişi
- Son açılan içerikler
- Son okunma zamanı
- Geçmiş temizleme

## Hesaplar
- 33 sosyal hesap
- Instagram / YouTube / TikTok / Facebook filtreleri
- Resmi uygulamada açma denemesi, HTTPS fallback
- Hesap favorisi

## Bildirimler
- Android 13+ bildirim izni
- Master bildirim durumu
- Dinamik kategori bazlı topic tercihleri
- Yeni kategori geldiğinde topic eşleşmesi güncellenir
- FCM token yenilenince topic eşleşmesi tekrar kurulur

## Ayarlar
- Tema: Sistem / Açık / Koyu
- Bildirim ayarlarına geçiş
- Yerel veri sayacı
- Yerel kayıt temizleme

## Gizlilik
- Yerel veriler
- FCM açıklaması
- Hassas izinlerin kullanılmadığı açıklaması
- Canlı privacy sayfası

## Hakkında
- Marka/geliştirici bilgisi
- Sürüm/paket
- Feed sürümü ve içerik sayısı
- Android App Links durumu
- Web/e-posta
- Sürüm satırına uzun basınca gizli geliştirici tanılama

## Geliştirici Tanılama
- FCM durumu ve token sonu
- Topic doğrulama/yenileme
- Yerel test bildirimi
- Paket/sürüm/feed/config bilgisi

## İçerik detay
- Geri
- Kategori + dakika + video durumu
- Kapak
- Başlık/özet/gövde
- Kaydet/paylaş
- Video bağlantısı
- Kaynak listesi
- BilgiliMedia notu

## Teknik sözleşme
- HTTPS-only network security
- `allowBackup=false`
- App Links `/content/*`
- FCM primary push + de-duplicated periodic fallback
- Feed/app-config cache ve offline fallback
- Schema v2 kategori/section desteği; eski `categories` uyumluluğu
- Sponsor sistemi varsayılan kapalı
- Hassas Android izinleri yok
