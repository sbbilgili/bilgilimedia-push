# BilgiliMedia Automatic Push

BilgiliMedia bildirim akışı, GitHub deposundaki `content/feed.json` değişiklikleri üzerinden çalışır.

## Çalışma şekli

- Workflow yalnızca ilgili dosyalar `main` dalına geldiğinde veya elle çalıştırıldığında başlar.
- Zamanlanmış 15 dakikalık polling yoktur.
- Push olayında mevcut `feed.json`, olaydan önceki commit'teki `feed.json` ile karşılaştırılır.
- Yalnızca yeni eklenen içerik ID'leri bildirim adayı olur; geçmiş içerikler yeniden gönderilmez.
- Kategori -> FCM topic eşleşmesi `hosting/app-config.json` dosyasından alınır.
- Schema v2 ile önce sabit `categoryId`, yoksa geriye dönük uyumluluk için görünen `category` adı kullanılır.
- Kapalı veya eşleşmemiş kategoriler için bildirim gönderilmez.
- Workflow her çalışmada FCM erişimini dry-run ile doğrular; bu kontrol gerçek bildirim teslim etmez.
- Android uygulaması data-only yüksek öncelikli FCM mesajını alır ve ilgili içeriği açacak yerel bildirimi oluşturur.

## Kimlik doğrulama

GitHub Actions, Google Cloud'a uzun ömürlü JSON anahtarıyla bağlanmaz.

Kullanılan yöntem:

`GitHub OIDC -> Google Workload Identity Federation -> service account impersonation`

- Pool: `github-actions`
- Provider: `bilgilimedia-push`
- Service account: `firebase-adminsdk-fbsvc@bilgilimedia-60d6f.iam.gserviceaccount.com`
- Yetki yalnızca `sbbilgili/bilgilimedia-push` deposunun `main` dalı için sınırlandırılmıştır.
- Repository secret olarak `FIREBASE_SERVICE_ACCOUNT` kullanılmaz ve yeniden oluşturulmamalıdır.

## Topic eşleşmeleri

- Genel -> `genel`
- Belgesel -> `belgesel`
- Tarih -> `tarih`
- Doğa -> `doga`
- Yemek -> `yemek`
- Teknoloji -> `teknoloji`
- Animasyon -> `animasyon`
- Çizgi Film -> `cizgifilm`
- AI -> `ai`
- HIGGS -> `higgs`

Yeni kategori eklenirken `app-config.json` içinde benzersiz ve geçerli bir sabit `id` ve FCM `topic` tanımlanmalıdır.
