# BilgiliMedia Android altyapısı

Bu repository, BilgiliMedia Android uygulamasının **uzaktan içerik, genişletilebilir navigasyon, Firebase Hosting, App Links ve FCM bildirim altyapısını** içerir.

> Kullanımdaki 1.4.8 istemcisi geriye dönük uyumlu `categories` listesini okur. Schema v2 ayrıca sonraki istemciler için çok dallı `navigation` ağacı sağlar.

## Ana bileşenler

- `content/feed.json`: uygulamanın içerik kaynağı
- `hosting/app-config.json`: Schema v2 kategori, bölüm, menü ağacı, topic ve özellik yapılandırması
- `hosting/.well-known/assetlinks.json`: Android App Links doğrulaması
- `hosting/privacy/`: herkese açık gizlilik politikası
- `scripts/check-feed.js`: yeni içerik tespiti ve FCM bildirimi
- `scripts/validate-content.js`: içerik, kategori, nested navigation, App Links ve hosting güvenlik doğrulaması
- `.github/workflows/`: anahtarsız OIDC tabanlı dağıtım, bildirim ve PR doğrulama otomasyonu

## Genişletilebilirlik

Kategori adı kullanıcı arayüzü içindir; teknik ilişkiler sabit `category.id` ile kurulabilir. Yeni menüler `navigation` ağacında group/category/screen/page düğümleriyle tanımlanır. Bu model, tek düz kategori satırından daha büyük ve çok dallı bir BilgiliMedia uygulamasına geçiş için temel oluşturur.

Mevcut uzaktan kategoriler arasında AI ve HIGGS de bulunur.

Ayrıntılar için `EXTENSIBILITY.md` dosyasına bakın.

## Güvenlik

- GitHub Actions -> Google Cloud bağlantısı Workload Identity Federation/OIDC kullanır.
- Repository'de servis hesabı JSON anahtarı tutulmaz.
- Workflow action bağımlılıkları tam commit SHA'larına sabitlenir.
- `main` dalı PR + zorunlu `validate` kontrolü ile korunur.
- Hosting yanıtlarında temel tarayıcı güvenlik başlıkları uygulanır.
- Uzaktan web menüleri yalnız BilgiliMedia Firebase Hosting alanına yönlendirilebilir.
- Sponsor reklam sistemi varsayılan olarak kapalıdır.

## Yayın

Google Play üretim sürümünden önce `PLAY_RELEASE_CHECKLIST.md` kontrol edilmelidir.
