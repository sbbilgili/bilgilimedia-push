# BilgiliMedia genişletilebilir uygulama yapısı

BilgiliMedia, içerik ve menü yapısının mümkün olduğunca APK güncellemesi gerektirmeden büyüyebilmesi için **geriye dönük uyumlu uzaktan yapılandırma** kullanır.

## Yapılandırma sürümü

`hosting/app-config.json` artık Schema v2 kullanır.

- `categories`: mevcut 1.4.8 istemcilerinin kullandığı geriye dönük uyumlu düz kategori listesi.
- `sections`: kategorileri mantıksal bölümlere ayırır.
- `navigation`: sonraki istemcilerin okuyacağı çok dallı menü ağacı.
- `extraPages`: BilgiliMedia Hosting altında açılabilecek uzaktan sayfalar.
- `features`: özellik bayrakları.
- `compatibility`: eski istemcilerin güvenli biçimde çalışmaya devam etmesi için uyumluluk bilgisi.

Mevcut 1.4.8, bilmediği Schema v2 alanlarını yok sayıp `categories` ile çalışmaya devam eder. Yeni istemciler `navigation` ağacını kullanabilir.

## Sabit kategori kimliği

Her kategori üç ayrı kimliğe sahiptir:

- `id`: değişmemesi gereken teknik kimlik. Örn. `higgs`.
- `name`: kullanıcıya gösterilen ad. İleride değişebilir.
- `topic`: FCM abonelik adı. Mümkün olduğunca sabit tutulur.

Örnek:

```json
{
  "id": "higgs",
  "name": "HIGGS",
  "topic": "higgs",
  "sectionId": "yaratici",
  "order": 40,
  "enabled": true
}
```

Görünen ad değişse bile `id` ve mümkünse `topic` değiştirilmez. Böylece içerik referansları ve bildirim abonelikleri kırılmaz.

## Yeni kategori ekleme

1. `sections` içinde uygun bölüm seçilir veya yeni bölüm eklenir.
2. `categories` listesine benzersiz `id`, `name` ve `topic` ile kategori eklenir.
3. `navigation` içinde uygun dala kategori referansı eklenir.
4. İçerik yayımlanırken `category` alanında görünen ad korunur. Yeni üreticiler ek olarak `categoryId` yazabilir.
5. Pull request'teki `validate` kontrolü geçmeden main'e alınamaz.

Mevcut örnek ek kategoriler:
- AI -> `id: ai`, topic `ai`
- HIGGS -> `id: higgs`, topic `higgs`

## Çok dallı menü

`navigation.items` bir ağaçtır. Desteklenen düğümler:

- `group`: alt menü; başka group/category/screen/page öğeleri içerebilir.
- `category`: bir kategoriye `categoryId` ile bağlanır.
- `screen`: uygulamanın yerel ekranına bağlanır.
- `page`: `extraPages` içindeki güvenli bir BilgiliMedia sayfasına bağlanır.

Örnek:

```text
Ana Sayfa
İçerikler
  Bilgi & Belgesel
    Genel
    Belgesel
    Tarih
    Doğa
    Teknoloji
  Yaşam
    Yemek
  Yaratıcı & Dijital
    Animasyon
    Çizgi Film
    AI
    HIGGS
Kütüphane
  Favoriler
  Son Okunanlar
Sosyal Hesaplar
Bildirimler
Daha
  Hakkında
```

Menü kimlikleri benzersiz olmalı ve yapı `maxDepth` sınırını aşmamalıdır. Doğrulama scripti bilinmeyen ekran, bozuk kategori referansı, tekrar eden kimlik veya güvensiz sayfa adresini reddeder.

## Yeni uzaktan sayfa ekleme

Yeni bilgilendirme/yardım/kampanya sayfaları Firebase Hosting altında oluşturulur ve `extraPages` listesine eklenir.

```json
{
  "id": "yardim",
  "title": "Yardım Merkezi",
  "subtitle": "Sık sorulan sorular",
  "type": "web",
  "url": "https://bilgilimedia-60d6f.web.app/pages/yardim",
  "enabled": true
}
```

Ardından navigation ağacına:

```json
{"id":"nav-yardim","type":"page","pageId":"yardim","title":"Yardım","order":20,"enabled":true}
```

eklenebilir.

Güvenlik nedeniyle uzaktan menü sayfaları yalnızca `https://bilgilimedia-60d6f.web.app` alanından kabul edilir.

## APK güncellemesi gerektiren değişiklikler

Şunlar gerçek Android kodu değişikliği olduğundan yeni sürüm gerektirir:

- yeni cihaz izni,
- kamera/mikrofon/konum gibi donanım özellikleri,
- tamamen yeni native ekran türü,
- ödeme, hesap veya giriş sistemi,
- yeni Android SDK/kitaplık entegrasyonu,
- mevcut istemcinin henüz desteklemediği yeni navigation düğüm türü.

Kategori, bölüm, sıralama, FCM topic eşleşmesi ve desteklenen uzaktan sayfalar için amaç APK güncellemesini gerektirmemektir.

## Teknik tanılama

Normal kullanıcı Bildirimler ekranında FCM token/topic teknik kontrolleri gösterilmez.
Preview uygulamasında Hakkında ekranındaki sürüm satırına uzun basıldığında gizli geliştirici tanılama ekranı açılır.
