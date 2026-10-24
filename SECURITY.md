# Security Policy

## Secrets

BilgiliMedia GitHub Actions, Google Cloud'a Workload Identity Federation/OIDC ile bağlanır. Uzun ömürlü servis hesabı JSON anahtarları repository'ye veya GitHub Actions secrets alanına eklenmemelidir.

## Supported production path

- GitHub repository: `sbbilgili/bilgilimedia-push`
- Allowed branch for Google Cloud federation: `main`
- Workload Identity Pool: `github-actions`
- Provider: `bilgilimedia-push`

## Reporting

Güvenlik açığı şüphesinde ayrıntıları herkese açık issue'ya yazmak yerine uygulamanın gizlilik sayfasındaki iletişim adresi üzerinden bildirin.

Bir kimlik bilgisi yanlışlıkla açığa çıkarsa:
1. Anahtarı/token'ı derhal iptal edin.
2. İlgili GitHub workflow ve IAM izinlerini kontrol edin.
3. GitHub Actions ve Google Cloud loglarını inceleyin.
4. Gerekirse Workload Identity provider koşullarını daraltın.
