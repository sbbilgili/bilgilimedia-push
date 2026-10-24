# BilgiliMedia — Google Play yayın kontrol listesi

Bu dosya 1.4.8 ve sonraki üretim adayları için yayın öncesi kontrol kapısıdır.

## Android build

- [ ] Native Android kaynak projesi ve son üretim APK/AAB ayrıca denetlendi.
- [ ] Üretim package name: `com.bilgilimedia.mobile`.
- [ ] `versionCode` önceki Play sürümünden büyüktür.
- [ ] Target SDK API 36 veya Google Play'in o tarihte istediği daha yeni seviye.
- [ ] Release AAB doğru upload key ile imzalandı.
- [ ] Kamera, mikrofon, konum, rehber gibi gereksiz izinler manifestte yok.
- [ ] Android 13+ bildirim izni akışı gerçek cihazda test edildi.
- [ ] FCM topic aboneliği, yeni kategori ekleme ve topic temizleme gerçek cihazda test edildi.
- [ ] App Link ile içerik açma ve tarayıcı fallback gerçek cihazda test edildi.

## App Links

- [ ] `assetlinks.json` üretim paketi `com.bilgilimedia.mobile` için bir kayıt içeriyor.
- [ ] SHA-256 parmak izi, Google Play Console > App integrity altında görünen **App signing certificate** parmak iziyle birebir aynı.
- [ ] Preview paketinin kaydı üretim kaydından ayrı tutuluyor.
- [ ] `https://bilgilimedia-60d6f.web.app/.well-known/assetlinks.json` herkese açık ve doğru Content-Type ile yanıt veriyor.

## Gizlilik ve Veri Güvenliği

- [ ] Herkese açık gizlilik politikası canlı.
- [ ] Play Console Data safety beyanı FCM/Firebase Installations kullanımını hesaba katıyor; eski "hiç veri toplanmıyor" taslağı kullanılmıyor. `PLAY_DATA_SAFETY_DRAFT.md` güncel form ile karşılaştırıldı.
- [ ] Uygulamada ayrı analytics veya reklam SDK'sı yoksa Play beyanı buna göre dolduruluyor.
- [ ] Sponsor reklam sistemi yayına alınacaksa "Contains ads" ve ilgili gizlilik/Data safety beyanları **reklam açılmadan önce** güncelleniyor.
- [ ] Veri satışı yapılmadığı beyanı ürün davranışıyla uyumlu.

## Mağaza varlıkları

- [ ] 512x512 uygulama simgesi.
- [ ] 1024x500 feature graphic.
- [ ] Güncel gerçek uygulamadan telefon ekran görüntüleri.
- [ ] Kısa açıklama, uzun açıklama, destek e-postası ve gizlilik URL'si güncel.
- [ ] İçerik derecelendirme ve hedef yaş grubu gerçek uygulama içeriğine göre yeniden kontrol edildi.

## Test ve yayın

- [ ] Fiziksel Android cihazda temiz kurulum testi.
- [ ] Önceki sürümün üzerine güncelleme testi.
- [ ] İnternetsiz durum, yavaş ağ, geri tuşu ve uygulama yeniden açılışı testi.
- [ ] Kaydedilenler/geçmiş/tema gibi yerel verilerin sürüm yükseltmede korunması kontrol edildi.
- [ ] Yeni kişisel Play geliştirici hesabı kuralına tabi olunuyorsa gerekli kapalı test tamamlandı.
- [ ] Play App Signing kurulumu ve upload key yedeği güvenli yerde saklanıyor.

## Altyapı güvenliği

- [x] GitHub -> Google Cloud anahtarsız OIDC/WIF.
- [x] Eski `FIREBASE_SERVICE_ACCOUNT` secret kaldırıldı.
- [x] Eski kullanıcı tarafından yönetilen servis hesabı JSON anahtarı kaldırıldı.
- [x] FCM erişimi dry-run ile doğrulanıyor.
- [x] GitHub Actions bağımlılıkları tam commit SHA'larına sabitlendi.
- [ ] GitHub `main` dalı için branch/ruleset koruması açıldı.
