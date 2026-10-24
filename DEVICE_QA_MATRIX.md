# BilgiliMedia — cihaz ve release QA matrisi

Bu dosya final production AAB ve Play-generated APK'lar için uygulanacak test matrisidir.

## A. Kurulum ve güncelleme

| Test | Beklenen sonuç |
|---|---|
| Temiz kurulum | Uygulama hatasız kurulur ve açılır |
| Eski production üzerine güncelleme | İmza/paket sürekliliği korunur |
| Uygulama yeniden açma | Yerel veriler korunur |
| Cihaz yeniden başlatma | Bildirim/içerik kontrol planlaması bozulmaz |

## B. Android sürümleri

- API 26 / Android 8: minSdk davranışı.
- Android 10/11: normal runtime davranışı.
- Android 13+: POST_NOTIFICATIONS izin akışı.
- Android 15: 16 KB uyumluluk testi mümkünse.
- Android 16 / API 36: target SDK davranışı.

## C. Ana işlevler

- Ana Sayfa açılır.
- Feed güncel sürümü gösterir.
- Kategoriler doğru listelenir.
- AI/HIGGS dahil uzaktan kategori seçenekleri davranışı doğrulanır.
- Arama Türkçe karakterler ve aksansız aramayla denenir.
- İçerik detay ekranı.
- Kaynaklar.
- Video linkleri.
- Favoriler.
- Son Okunanlar.
- Sosyal hesaplar.
- Ayarlar.
- Gizlilik.
- Paylaşım.

## D. Bildirim / FCM

- İlk token üretimi.
- Bildirim izni kabul.
- Bildirim izni reddet.
- Kategori subscribe.
- Kategori unsubscribe.
- Token rotation/reinstall.
- Yeni kategori uzaktan eklendiğinde topic senkronizasyonu.
- Gerçek FCM mesajı.
- Bildirime dokununca doğru article_id açılması.
- Cihaz reboot sonrası bildirim sistemi.

## E. Ağ

- Wi-Fi.
- Mobil veri.
- Uçak modu.
- Açılışta internet yok.
- İçerik önbelleği/fallback.
- Yavaş bağlantı.
- HTTP olmayan yalnız HTTPS uçlar.
- Firebase Hosting geçici hata senaryosu.

## F. App Links

- `https://bilgilimedia-60d6f.web.app/content/<id>` uygulamayı açar.
- Geçersiz içerik ID'si güvenli davranır.
- Uygulama yokken tarayıcı fallback.
- Play App Signing fingerprint sonrası tekrar doğrulama.

## G. Veri ve gizlilik

- Favoriler cihazda kalır.
- Son Okunanlar cihazda kalır.
- Yerel verileri temizle çalışır.
- Uygulama kaldırıldığında yerel veriler gider.
- Hesap/login istemez.
- Kamera, mikrofon, konum, rehber, SMS izni istemez.
- Privacy sayfası açılır.

## H. Erişilebilirlik ve UI

- Küçük ekran.
- Büyük ekran.
- Dikey/yatay.
- Sistem yazı boyutu %100.
- Büyük yazı boyutu.
- TalkBack temel gezinme.
- Buton/ikon içerik açıklamaları.
- Klavye açıkken arama sonuçları.
- Dokunma hedefleri.

## I. Stabilite

- 30 dakika normal kullanım.
- Arka plana al/geri dön döngüsü.
- Hızlı kategori geçişleri.
- Hızlı içerik aç/kapat.
- Düşük bellek yeniden yaratma senaryosu.
- Crash/ANR yok.

## J. Play teknik kontrol

- AAB bundletool doğrulaması.
- Play Console pre-launch report.
- API 36.
- 64-bit gereklilikleri.
- 16 KB page-size uyumluluğu.
- Signing certificate.
- App Links.
- Data Safety/SDK taraması.

## Hata önem seviyeleri

- **Blocker:** kurulum/açılış/yayın engeli.
- **High:** temel özellik çalışmıyor, veri/bildirim güvenilirliği bozuk.
- **Medium:** kısmi işlev veya önemli UI problemi.
- **Low:** kozmetik/iyileştirme.
