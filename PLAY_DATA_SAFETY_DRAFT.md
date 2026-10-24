# BilgiliMedia — Google Play Data Safety çalışma taslağı

Bu dosya, Play Console'daki Veri Güvenliği formunu doldururken teknik gerçekleri kaybetmemek için hazırlanmış çalışma notudur. Google Play formunun güncel soru metni esas alınmalı; burada yazanlar körlemesine kopyalanmamalıdır.

## Mevcut uygulama davranışı

Bilinen mevcut yapı:

- Kullanıcı hesabı/üyelik yok.
- Konum, kamera, mikrofon veya rehber kullanılmıyor.
- Ayrı bir analytics SDK'sı yok.
- Ayrı bir reklam SDK'sı yok.
- Sponsor reklam sistemi uzaktan yapılandırılabilir ancak şu anda kapalı.
- Kaydedilenler, geçmiş, tema ve bildirim tercihleri cihazda yerel tutuluyor.
- Firebase Cloud Messaging (FCM) bildirimler için kullanılıyor.
- Firebase Installations, FCM ile birlikte kurulum başına Firebase Installation ID (FID) kullanıyor.
- BilgiliMedia kişisel verileri satmıyor.

## Firebase nedeniyle dikkate alınacak teknik veriler

Firebase'in Android veri açıklamasına göre FCM/Firebase Installations aşağıdaki teknik verileri otomatik işleyebilir:

- Uygulama sürümü.
- Firebase user agent bilgileri (ör. OS/sürüm ve cihaz/app/SDK hakkında teknik metadata).
- Kurulum başına Firebase Installation ID (FID).
- Bildirim teslimi/topic aboneliği için gerekli FCM teknik kimlik bilgileri.

Bu nedenle eski Play taslağındaki "uygulama hiçbir veri toplamıyor" yaklaşımı artık kullanılmamalıdır.

## Play Console doldurulurken doğrulanacak noktalar

- [ ] FCM/FID nedeniyle Google Play'in güncel veri türü sınıflandırmasında hangi kutuların işaretleneceği mevcut form metnine göre kontrol edildi.
- [ ] Bu verilerin temel kullanım amacı "App functionality / bildirimlerin çalışması" ile uyumlu şekilde beyan edildi.
- [ ] Verilerin aktarım sırasında şifrelendiği Firebase dokümantasyonuyla uyumlu şekilde değerlendirildi.
- [ ] Google/Firebase'in hizmet sağlayıcı olarak işlediği veriler için Play Console'un güncel "sharing" tanımı ve istisnaları kontrol edildi.
- [ ] Uygulamada Google Analytics, Crashlytics, Performance Monitoring, reklam SDK'sı veya başka veri toplayan SDK bulunmadığı son üretim AAB üzerinde yeniden doğrulandı.
- [ ] Sponsor reklam sistemi etkinleştirilmeden önce "Contains ads", gizlilik politikası ve Veri Güvenliği formu yeniden değerlendirildi.
- [ ] Üçüncü taraf sosyal medya/kaynak bağlantılarının uygulama dışına çıkabileceği gizlilik politikasında açıklandı.

## Yayın öncesi son kaynak

Kesin beyan hazırlanırken Firebase'in güncel "Prepare for Google Play's data disclosure requirements" sayfası ve Play Console'da o gün görünen Veri Güvenliği soruları birlikte kontrol edilmelidir.
