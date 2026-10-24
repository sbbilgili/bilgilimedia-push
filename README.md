# BilgiliMedia Android altyapısı

Bu repository, BilgiliMedia Android uygulamasının **uzaktan içerik, Firebase Hosting, App Links ve FCM bildirim altyapısını** içerir.

> Native Android uygulamasının tam kaynak projesi bu repository'nin parçası değildir. Google Play'e gidecek APK/AAB ayrıca derlenir ve imzalanır.

## Ana bileşenler

- `content/feed.json`: uygulamanın içerik kaynağı
- `hosting/app-config.json`: kategori/topic ve uzaktan özellik yapılandırması
- `hosting/.well-known/assetlinks.json`: Android App Links doğrulaması
- `hosting/privacy/`: herkese açık gizlilik politikası
- `scripts/check-feed.js`: yeni içerik tespiti ve FCM bildirimi
- `.github/workflows/`: anahtarsız OIDC tabanlı dağıtım ve bildirim otomasyonu

## Güvenlik

- GitHub Actions -> Google Cloud bağlantısı Workload Identity Federation/OIDC kullanır.
- Repository'de servis hesabı JSON anahtarı tutulmaz.
- Workflow action bağımlılıkları tam commit SHA'larına sabitlenir.
- Hosting yanıtlarında temel tarayıcı güvenlik başlıkları uygulanır.
- Sponsor reklam sistemi varsayılan olarak kapalıdır.

## Yayın

Google Play üretim sürümünden önce `PLAY_RELEASE_CHECKLIST.md` kontrol edilmelidir.
