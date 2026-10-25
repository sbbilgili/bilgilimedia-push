# BilgiliMedia — Google Play yayın kontrol listesi

Bu dosya 1.4.8 referans build'i ve sonraki üretim adayları için yayın öncesi kontrol kapısıdır.

## 1. Altın referans / arayüz

- [x] Orijinal arayüzlü 1.4.8 APK referans olarak sabitlendi.
- [x] Referans SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`.
- [x] Preview paket: `com.bilgilimedia.mobile.media.preview`.
- [x] Preview imza sertifikası App Links kaydıyla eşleşiyor.
- [x] Yeniden üretilebilir production Android kaynak projesi PR #23 ile `main`e alındı.
- [x] Kullanıcı final 1.4.9 UI baseline'ını gerçek cihazda onayladı; `ANDROID_UI_LOCK_1_4_9.md` ile kilitlendi.
- [ ] Play-delivered final production build aynı baseline ile son regresyon testinden geçti.

## 2. Android build / Play AAB

- [x] Üretim package name kararı: `com.bilgilimedia.mobile`.
- [x] Production sürüm politikası: final AAB versionCode `23` veya daha yüksek olacak.
- [x] Referans hedef: API 36; 31 Ağustos 2026 itibarıyla yeni Play uygulamaları için minimum gereklilik API 36.
- [x] minSdk: API 26 kararı korunuyor.
- [x] CI, production package `com.bilgilimedia.mobile` için yeniden üretilebilir release AAB üretiyor; Play'e yüklenecek final imzalı AAB henüz oluşturulmadı.
- [ ] Release AAB üretim/upload anahtarıyla imzalandı.
- [x] Referans APK'da gereksiz hassas izin bulunmuyor.
- [x] Referans APK'da cleartext HTTP kapalı ve `allowBackup=false`.
- [x] Referans APK v2 + v3 APK signature block içeriyor.
- [x] Referans APK'daki native `.so` kütüphaneleri 16 KB ELF hizasına sahip.
- [x] Referans APK'da uncompressed native kütüphaneler ZIP içinde 16 KB hizalı.
- [x] CI release candidate APK/AAB için 16 KB packaging/alignment artifact audit çalışıyor ve geçiyor.
- [ ] Play-generated split APK seti 16 KB page-size ortamında ayrıca doğrulandı.
- [ ] Final AAB bundletool/Play Console pre-launch raporu ile doğrulandı.

## 3. Firebase / bildirim

- [x] Firebase Console'da production paket `com.bilgilimedia.mobile` Android uygulaması kayıtlı.
- [x] Production `google-services.json` Firebase Console'dan indirildi ve production paket kaydı doğrulandı.\n- [x] Production Firebase client kimliği final kaynakta production `BuildConfig` değerleriyle entegre; preview client production build yolunda kullanılmıyor.
- [x] Backend FCM gönderimi OIDC/WIF ile anahtarsız çalışıyor.
- [x] FCM erişimi dry-run ile doğrulanıyor.
- [ ] Android 13+ bildirim izni gerçek cihazda test edildi.
- [ ] FCM token yenileme, topic subscribe/unsubscribe gerçek cihazda test edildi.
- [ ] AI/HIGGS dahil uzaktan kategori ekleme testi yapıldı.
- [ ] Cihaz yeniden başlatma ve uygulama güncellemesi sonrası topic/iş kontrolü test edildi.

## 4. App Links

- [x] Preview `assetlinks.json` kaydı mevcut ve preview sertifikasıyla eşleşiyor.
- [x] `assetlinks.json` production paket `com.bilgilimedia.mobile` için mevcut production sertifikasıyla kayıt içeriyor.
- [ ] Production SHA-256 değeri Play Console > App integrity > **App signing certificate** ile birebir eşleşiyor.
- [ ] Play App Signing birden fazla dağıtım sertifikası gösteriyorsa gerekli tüm fingerprint'ler API sağlayıcılarında ve assetlinks'te kaydedildi.
- [x] QA üzerinde app-first içerik paylaşımı ve web fallback gerçek cihazda doğrulandı.
- [ ] Final Play-signed production build üzerinde doğrulanmış App Link doğrudan açılışı test edildi.

## 5. İmza ve güncelleme sürekliliği

- [x] Mevcut üretim imza yedeğinin sertifikası doğrulandı: `F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`.
- [x] Eski production AAB'nin sertifikası aynı fingerprint ile eşleşiyor.
- [ ] Google Play App Signing ilk release'ten önce mevcut production app signing key'i kullanacak şekilde yapılandırıldı; böylece sideload edilmiş eski `com.bilgilimedia.mobile` kurulumlarıyla imza sürekliliği korunuyor.
- [ ] App signing key ile upload key ayrıldı ve upload key yedeği güvenli yerde tutuluyor.
- [x] Özel anahtar/parola GitHub veya herkese açık dosyalara eklenmedi.

## 6. Gizlilik / Data Safety

- [x] Herkese açık gizlilik politikası güncel.
- [x] Gizlilik metni FCM/Firebase Installations FID kullanımını açıklıyor.
- [x] Gizlilik metni Firebase Hosting standart ağ/IP işleme bilgisini açıklıyor.
- [x] Gizlilik metni normal Android arka plan izinlerini açıklıyor.
- [x] Referans APK'da Analytics, Crashlytics ve AdMob yok.
- [ ] Final Play Data Safety formunda **Device or other IDs** (FID) toplama beyan edildi.
- [ ] Amaç final formda uygulama işlevselliğiyle uyumlu seçildi.
- [ ] Firebase hizmet sağlayıcı istisnasına göre “sharing” cevabı final form metniyle doğrulandı.
- [ ] “Data encrypted in transit” final AAB ve form metniyle doğrulandı.
- [x] Uygulamada kullanıcı hesabı yok; account-deletion zorunluluğu mevcut davranışta uygulanmıyor.
- [x] Sponsor reklam sistemi kapalı.
- [ ] Sponsor sistemi açılmadan önce Contains ads + privacy + Data Safety yeniden güncellenecek.

## 7. Play Console / geliştirici hesabı

- [ ] Geliştirici kimlik doğrulaması tamamlandı. (Kimlik belgesi bekleniyor.)
- [ ] `com.bilgilimedia.mobile` Play package adı 30 Eylül 2026 sonrası paket kayıt gerekliliklerine göre Play Console'da kayıtlı/doğrulanmış görünüyor.
- [ ] Uygulama oluşturma ekranında doğru uygulama adı, varsayılan dil ve uygulama türü seçildi.
- [ ] Uygulama erişimi: giriş gerekmiyor şeklinde beyan edildi.
- [ ] Reklam beyanı: sponsorAds kapalı olduğu sürece “No”.
- [ ] İçerik derecelendirme IARC anketi gerçek final içerik üzerinden tamamlandı.
- [ ] Hedef kitle gerçek ürün kararıyla dolduruldu.
- [ ] Haber uygulaması olmadığı beyanı gerçek ürün davranışıyla uyumlu.
- [ ] Yeni kişisel geliştirici hesabı kuralına tabi olunuyorsa 12 tester / 14 kesintisiz gün kapalı test tamamlandı.

## 8. Mağaza varlıkları

- [x] 512×512 uygulama simgesi mevcut; final marka görseliyle yeniden doğrulanacak.
- [x] 1024×500 feature graphic mevcut; final marka mesajıyla yeniden doğrulanacak.
- [ ] Ekran görüntüleri **final production build** üzerinden yeniden çekildi.
- [ ] Eski 1.1.x ekran görüntüleri kullanılmıyor.
- [x] Güncel Türkçe mağaza metni repoya eklendi.
- [ ] Destek e-postası ve gizlilik URL'si Play Console'a girildi.
- [ ] Mağaza açıklaması final kategori/özelliklerle birebir eşleşiyor.

## 9. Fiziksel cihaz QA

- [ ] Temiz kurulum.
- [ ] Eski `com.bilgilimedia.mobile` sürümünün üzerine güncelleme.
- [ ] Android 8/9 veya desteklenen en eski API 26 cihaz/emülatör testi.
- [ ] Android 13+ bildirim izin testi.
- [ ] Android 16 / API 36 cihaz veya emülatör testi.
- [ ] 16 KB page-size ortamında test.
- [ ] Wi-Fi / mobil ağ / bağlantısız / yavaş ağ senaryoları.
- [x] Geri tuşu / çift geri çıkış davranışı QA cihazında doğrulandı.
- [ ] Yeniden açma ve ekran döndürme.
- [ ] Büyük yazı boyutu ve erişilebilirlik.
- [ ] Favoriler, son okunanlar, tema ve bildirim tercihlerinin güncellemede korunması.
- [ ] Sosyal bağlantı uygulama varsa app'e, yoksa HTTPS fallback'e final QA turunda doğrulandı.
- [x] QA app-first paylaşım bağlantısı doğru içeriği uygulamada açıyor; uygulama yoksa web fallback çalışıyor.
- [ ] Final Play-signed production App Links doğrulaması tamamlandı.
- [ ] AI/HIGGS ve yeni uzaktan kategoriler menü/bildirim tarafında doğrulanıyor.

## 10. Altyapı güvenliği

- [x] GitHub -> Google Cloud anahtarsız OIDC/WIF.
- [x] Eski `FIREBASE_SERVICE_ACCOUNT` secret kaldırıldı.
- [x] Eski kullanıcı tarafından yönetilen servis hesabı JSON anahtarı kaldırıldı.
- [x] GitHub Actions bağımlılıkları tam commit SHA'larına sabitlendi.
- [x] `package-lock.json` + `npm ci`.
- [x] Hosting CSP ve temel güvenlik başlıkları.
- [x] GitHub `main` ruleset aktif.
- [x] PR zorunlu + `validate` status check zorunlu.
- [x] Force push ve branch silme engelli.
