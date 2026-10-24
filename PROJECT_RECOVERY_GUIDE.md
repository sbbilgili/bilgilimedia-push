# BilgiliMedia Project Recovery Guide

Bu dosya, sohbet geçmişi kaybolsa bile projeye güvenli biçimde devam edebilmek için hazırlanmıştır.

## Yetkili kaynaklar

### 1. GitHub
Repository: `sbbilgili/bilgilimedia-push`

Yetkili dal: `main`

`main` backend, hosting, FCM/OIDC, güvenlik, Google Play hazırlık dokümanları ve uzaktan yapılandırma için ana kaynaktır.

Önemli dosyalar:
- `GOLDEN_UI_1_4_8.md`
- `PLAY_BLOCKERS_CURRENT.md`
- `PLAY_RELEASE_CHECKLIST.md`
- `PLAY_DATA_SAFETY_DRAFT.md`
- `PLAY_STORE_LISTING_TR.md`
- `FIREBASE_PRODUCTION.md`
- `SECURITY.md`
- `EXTENSIBILITY.md`
- `content/feed.json`
- `hosting/app-config.json`
- `hosting/.well-known/assetlinks.json`
- `.github/workflows/`

GitHub Ruleset:
- `Protect main` aktif
- PR zorunlu
- `validate` zorunlu
- force push engelli
- branch silme engelli

### 2. Golden Android reference

Yetkili arayüz/işlev referansı:

- dosya: `BilgiliMedia_148_DIREKT.apk`
- versionName: `1.4.8`
- versionCode: `22`
- package: `com.bilgilimedia.mobile.media.preview`
- SHA-256: `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`

Bu APK ChatGPT Library'de:
`/BilgiliMedia/Recovery_2026-10-24/BilgiliMedia_148_DIREKT.apk`

ÇOK ÖNEMLİ:
- Orijinal 1.4.8 native kaynak projesi henüz birebir geri kazanılmış değildir.
- Bu APK + `GOLDEN_UI_1_4_8.md` birlikte altın referanstır.
- `android-production-rebuild-1-4-9` dalındaki QA yeniden yapımı görsel olarak reddedildi.
- PR #18 kapalıdır ve merge edilmemiştir.
- Bu dal final arayüz kaynağı olarak kullanılmamalıdır.

## Firebase

Firebase project:
- project id: `bilgilimedia-60d6f`
- project number: `190776938532`

Production:
- package: `com.bilgilimedia.mobile`
- app ID: `1:190776938532:android:714bfdfa9b2ab78c4788b1`

Preview:
- package: `com.bilgilimedia.mobile.media.preview`
- app ID: `1:190776938532:android:56fff2dc4359a7ac4788b1`

Production `google-services.json` ChatGPT Library'de:
`/BilgiliMedia/Recovery_2026-10-24/google-services-production.json`

## Signing

Production certificate SHA-256:
`F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`

Preview certificate SHA-256:
`E7:F2:BB:FC:73:E8:F9:00:40:E8:10:C6:15:9E:0F:1D:D3:BF:78:09:05:E6:78:90:B9:A4:82:DB:B3:5B:17:D6`

Private signing backup:
`BilgiliMedia_Android_Imza_Yedegi_2026-10-22.zip`

Bu özel dosya Library'de tutulur. GitHub'a, README'ye veya public dosyalara private key/parola konmaz.

## Tamamlanan güvenlik

- GitHub -> Google Cloud: OIDC / Workload Identity Federation
- uzun ömürlü service-account JSON key kaldırıldı
- eski GitHub `FIREBASE_SERVICE_ACCOUNT` secret kaldırıldı
- FCM dry-run doğrulandı
- Schema v2 / AI / HIGGS / genişletilebilir navigation
- Preview + Production App Links
- privacy/Data Safety/store listing hazırlıkları
- main branch protection
- PR + validate
- pinned GitHub Actions
- package-lock + npm ci
- Hosting CSP ve security headers

## Google Play için kalan ana blocker'lar

1. Geliştirici kimlik doğrulaması
2. Orijinal 1.4.8 kaynak kodunun birebir geri kazanımı
3. Aynı arayüzlü `com.bilgilimedia.mobile` production AAB
4. Play App Signing final fingerprint kontrolü
5. Final cihaz QA
6. Final ekran görüntüleri
7. Hesap uygunsa 12 tester / 14 gün closed test

## Sohbet kaybolursa yeni sohbette kullanılacak mesaj

> BilgiliMedia projemize devam edeceğiz. GitHub repository `sbbilgili/bilgilimedia-push`. Önce `PROJECT_RECOVERY_GUIDE.md`, `GOLDEN_UI_1_4_8.md`, `PLAY_BLOCKERS_CURRENT.md` ve `PLAY_RELEASE_CHECKLIST.md` dosyalarını incele. ChatGPT Library'deki `/BilgiliMedia/Recovery_2026-10-24/BilgiliMedia_148_DIREKT.apk` orijinal arayüzün altın referansıdır. `android-production-rebuild-1-4-9` dalını final kaynak olarak kullanma; PR #18 reddedilmiş QA denemesidir.

## Yedek stratejisi

En az üç kopya tut:
1. GitHub
2. ChatGPT Library
3. Kendi bilgisayarında/harici diskte offline kopya

İmza yedeğini ayrıca şifreli/offline sakla.
