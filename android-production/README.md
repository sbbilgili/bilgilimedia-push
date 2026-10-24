# BilgiliMedia production Android source

Bu proje, orijinal 1.4.8 önizleme APK'sından doğrulanan ekran/işlev sözleşmesini yeniden üretilebilir kaynak projeye taşır.

## Kimlik
- Production applicationId: `com.bilgilimedia.mobile`
- QA debug applicationId: `com.bilgilimedia.mobile.qa`
- Version: 1.4.9 / versionCode 23
- minSdk 26
- target/compileSdk 36

## Tasarım ilkesi
Orijinal 1.4.8'in programatik native Android arayüz yapısı korunur. Alt navigasyon: Ana Sayfa, Ara, Kayıtlar, Hesaplar, Daha.

## Firebase
Firebase client configuration uygulamanın istemci kimliğidir; servis hesabı/private key değildir. Backend yetkisi GitHub OIDC + Workload Identity Federation kullanır. Signing private key ve parolalar bu projeye eklenmez.

## Build
CI, repository'deki `content/feed.json` ve `hosting/app-config.json` dosyalarını offline fallback asset olarak kopyalar.

Production AAB, imza anahtarı repoya konmadan CI dışında güvenli ortamda imzalanır.
