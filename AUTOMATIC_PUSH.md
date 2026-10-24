# BilgiliMedia Automatic Push

This repository checks the BilgiliMedia live content feed every 15 minutes with GitHub Actions.

## Behavior

- Existing articles are stored in `state/known.json` and do not trigger historical notifications.
- New articles are detected by their `id`.
- Each new article is sent to its category FCM topic.
- The Android app receives a data-only high-priority FCM message and creates its own notification so tapping it can open the related article.
- State advances only after Firebase credentials are available and the push run succeeds.

## Topic mapping

- Genel -> `genel`
- Belgesel -> `belgesel`
- Tarih -> `tarih`
- Doğa -> `doga`
- Yemek -> `yemek`
- Teknoloji -> `teknoloji`
- Animasyon -> `animasyon`
- Çizgi Film -> `cizgifilm`

## Required GitHub secret

Create one repository secret named exactly:

`FIREBASE_SERVICE_ACCOUNT`

Its value must be the complete Firebase service account JSON for the BilgiliMedia Firebase project.

Never commit that JSON file to this repository.
