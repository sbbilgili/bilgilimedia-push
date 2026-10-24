# BilgiliMedia — Google Play kapalı test planı

Bu plan, yeni kişisel geliştirici hesabı test şartına tabi olunması halinde kullanılacaktır. Google Play'in güncel şartı, en az **12 test kullanıcısının 14 gün kesintisiz opted-in** kalmasıdır.

## Test başlamadan önce

- Finale yakın production AAB kullanılmalı.
- Package: `com.bilgilimedia.mobile`.
- Sürüm kodu production hattında monoton artmalı.
- Gizlilik URL'si ve destek e-postası çalışmalı.
- Store listing taslağı final davranışla uyumlu olmalı.
- Test kullanıcıları mümkünse farklı Android sürümleri ve ekran boyutlarına dağılmalı.

## Tester hedef dağılımı

Minimum 12 kişi yerine 15-20 kişi davet etmek önerilir; opt-out riski azaltılır.

Önerilen cihaz çeşitliliği:
- 2+ kullanıcı: Android 8-10
- 3+ kullanıcı: Android 11-12
- 4+ kullanıcı: Android 13-15
- 3+ kullanıcı: Android 16
- Farklı üreticiler: Samsung, Xiaomi/Redmi, Oppo/Realme, Pixel vb.

## Test kullanıcılarının yapacağı görevler

Her tester en az şu akışları denemeli:

1. Play üzerinden kurulum ve ilk açılış.
2. Ana Sayfa içerik yüklenmesi.
3. Kategori geçişleri.
4. Arama.
5. İçerik açma, kaynak ve video bağlantısı.
6. Favoriye ekleme/çıkarma.
7. Son Okunanlar.
8. Sosyal hesap bağlantıları.
9. Bildirim izni ve kategori tercihleri.
10. En az bir gerçek FCM bildiriminin alınması ve doğru içeriği açması.
11. Uygulamayı kapatıp yeniden açma.
12. Wi-Fi ile kullanım.
13. Mobil veri ile kullanım.
14. Kısa süreli internet kesintisi/offline davranışı.
15. Ekran döndürme ve büyük yazı boyutu.
16. Paylaşım bağlantısının açılması.
17. Gizlilik sayfasının açılması.

## Geri bildirim kanalı

Tester geri bildirimi şu başlıklarla toplanmalı:

- Telefon modeli
- Android sürümü
- Uygulama sürümü
- Denenen ekran/özellik
- Beklenen davranış
- Gerçek davranış
- Tekrarlanabilir mi?
- Ekran görüntüsü/video
- Önem derecesi: Blocker / High / Medium / Low

## 14 günlük süre boyunca

- Testerların opted-in durumları korunmalı.
- Büyük hata düzeltilirse yeni AAB kapalı teste verilebilir; testerların testten çıkmaması gerekir.
- Crash, ANR, bildirim teslimi ve kullanıcı geri bildirimi günlük/iki günlük kontrol edilmeli.
- Yapılan değişiklikler release note ile kaydedilmeli.

## Production access başvurusuna hazırlık

Google'ın soracağı alanlar için test sonunda şu özet hazırlanmalı:

- Tester bulma yöntemi
- Testerların ne kadar aktif olduğu
- Hangi özelliklerin test edildiği
- Gelen ana geri bildirimler
- Bu geri bildirimlerle yapılan değişiklikler
- Uygulamanın production'a hazır olduğuna hangi ölçütlerle karar verildiği

## Çıkış kriteri

Aşağıdakiler sağlanmadan production access başvurusu yapılmamalı:

- 12+ tester 14 gün kesintisiz opted-in.
- Blocker/High açık hata yok.
- FCM ve App Links gerçek cihazda doğrulanmış.
- Temiz kurulum ve upgrade senaryosu geçmiş.
- Store listing ve Data Safety final build ile uyumlu.
- Privacy URL erişilebilir.
