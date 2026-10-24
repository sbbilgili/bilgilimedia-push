# BilgiliMedia production Android build prerequisites

This file records the non-secret inputs required before the Google Play production AAB is built.

## Production identity

- Package: `com.bilgilimedia.mobile`
- Existing production signing certificate SHA-256:
  `F6:84:6E:CB:EC:19:CC:99:DB:68:2A:37:4F:47:8F:D1:9B:59:E6:96:54:2E:C5:0F:16:B5:33:0E:DD:DF:CF:6A`
- Recommended next versionCode: `23` or higher.
- Target SDK: API 36 or the newer level required by Google Play on release day.

The private signing key and passwords must never be committed to this repository.

## Firebase production registration

The currently recovered `google-services.json` belongs to the preview package:
`com.bilgilimedia.mobile.media.preview`.

Before the production AAB is built, Firebase Console must contain a separate Android app registration for:
`com.bilgilimedia.mobile`.

The production `google-services.json` from that Firebase registration is required for the final build. It must not be replaced with the preview configuration.

## App Links

`assetlinks.json` contains:
- preview package + preview certificate
- production package + existing production signing certificate

When Play App Signing is configured, compare the **App signing certificate SHA-256** in Play Console with the production fingerprint above. If Google Play uses an additional/different app-signing certificate, add that fingerprint to the production target before release.

## Golden UI reference

The approved original-interface preview APK is:
- versionName `1.4.8`
- versionCode `22`
- package `com.bilgilimedia.mobile.media.preview`
- SHA-256 `dca81609a1df783de5606ee646af4bd672121aed0188a84886db1c0652ccae76`

The production app must preserve this UI/behavior unless a deliberate product change is approved.
