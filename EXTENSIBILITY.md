# BilgiliMedia genişletilebilir uygulama yapısı

BilgiliMedia Android uygulaması, mümkün olduğunca APK güncellemesi gerektirmeden büyüyebilecek şekilde tasarlanmıştır.

## Yeni kategori ekleme

1. Canlı içerik akışında yeni kategorinin adıyla içerik yayımlanır.
2. `hosting/app-config.json` içindeki `categories` listesine kategori eklenir.
3. Kategori için benzersiz bir FCM topic adı verilir.

Örnek:

```json
{"name":"Havacılık","topic":"havacilik","enabled":true}
```

Uygulama:
- kategori rail/filter listesini canlı içerikten görür,
- bildirim ekranına kategoriyi otomatik ekler,
- topic eşleşmesini app-config üzerinden alır,
- GitHub Actions yeni içerik push'unu aynı topic'e yollar.

Kategori kapatılacaksa `enabled:false` yapılabilir. Uygulama eski FCM topic aboneliklerini temizleyebilir.

## Yeni "Daha" sayfası ekleme

Yeni bilgilendirme/yardım/kampanya sayfaları Firebase Hosting altında oluşturulur ve `extraPages` listesine eklenir.

Örnek:

```json
{
  "id":"yardim",
  "title":"Yardım Merkezi",
  "subtitle":"Sık sorulan sorular",
  "type":"web",
  "url":"https://bilgilimedia-60d6f.web.app/pages/yardim",
  "enabled":true
}
```

Uygulama bu öğeyi "Daha" ekranına APK güncellemesi olmadan ekleyebilir.

Güvenlik nedeniyle uzaktan menüye yalnızca BilgiliMedia'nın `bilgilimedia-60d6f.web.app` alanındaki HTTPS sayfaları kabul edilir.

## APK güncellemesi gerektiren değişiklikler

Şunlar gerçek Android kodu değişikliği olduğundan yeni sürüm gerektirir:
- yeni cihaz izni,
- kamera/mikrofon/konum gibi donanım özellikleri,
- tamamen yeni native ekran türü,
- alt navigasyonun temel yapısını değiştirmek,
- yeni Android SDK/kitaplık entegrasyonu,
- ödeme, hesap, giriş gibi yeni sistemler.

## Teknik tanılama

Normal kullanıcı Bildirimler ekranında FCM token/topic teknik kontrolleri gösterilmez.
Preview uygulamasında Hakkında ekranındaki "Sürüm" satırına uzun basıldığında gizli geliştirici tanılama ekranı açılır.
