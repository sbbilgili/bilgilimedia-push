#!/usr/bin/env bash
set -euo pipefail

APK="${1:-android-production/app/build/outputs/apk/release/app-release-unsigned.apk}"
AAB="${2:-android-production/app/build/outputs/bundle/release/app-release.aab}"

fail() { echo "::error::$*"; exit 1; }
note() { echo "::notice::$*"; }

test -s "$APK" || fail "Release APK not found: $APK"
test -s "$AAB" || fail "Release AAB not found: $AAB"
unzip -t "$APK" >/dev/null || fail "Release APK ZIP integrity failed"
unzip -t "$AAB" >/dev/null || fail "Release AAB ZIP integrity failed"

APKANALYZER="$(find "${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}" -type f -name apkanalyzer 2>/dev/null | sort -V | tail -1 || true)"
test -x "$APKANALYZER" || fail "apkanalyzer not found in Android SDK"

ZIPALIGN="$(find "${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}" -type f -name zipalign 2>/dev/null | sort -V | tail -1 || true)"
test -x "$ZIPALIGN" || fail "zipalign not found in Android SDK"

APP_ID="$("$APKANALYZER" manifest application-id "$APK" | tr -d '\r')"
VERSION_CODE="$("$APKANALYZER" manifest version-code "$APK" | tr -d '\r')"
VERSION_NAME="$("$APKANALYZER" manifest version-name "$APK" | tr -d '\r')"
MIN_SDK="$("$APKANALYZER" manifest min-sdk "$APK" | tr -d '\r')"
TARGET_SDK="$("$APKANALYZER" manifest target-sdk "$APK" | tr -d '\r')"
MANIFEST="$("$APKANALYZER" manifest print "$APK")"
PERMISSIONS="$("$APKANALYZER" manifest permissions "$APK")"

[[ "$APP_ID" == "com.bilgilimedia.mobile" ]] || fail "Unexpected production applicationId: $APP_ID"
[[ "$VERSION_CODE" =~ ^[0-9]+$ ]] || fail "Invalid versionCode: $VERSION_CODE"
(( VERSION_CODE >= 23 )) || fail "versionCode must be >= 23, got $VERSION_CODE"
[[ "$MIN_SDK" == "26" ]] || fail "minSdk must be 26, got $MIN_SDK"
[[ "$TARGET_SDK" == "36" ]] || fail "targetSdk must be 36, got $TARGET_SDK"

grep -q 'android:allowBackup="false"' <<<"$MANIFEST" || fail "allowBackup=false missing"
grep -q 'android:usesCleartextTraffic="false"' <<<"$MANIFEST" || fail "usesCleartextTraffic=false missing"
grep -q 'android.permission.POST_NOTIFICATIONS' <<<"$PERMISSIONS" || fail "POST_NOTIFICATIONS missing"
grep -q 'android.permission.RECEIVE_BOOT_COMPLETED' <<<"$PERMISSIONS" || fail "RECEIVE_BOOT_COMPLETED missing"

FORBIDDEN=(
  android.permission.ACCESS_FINE_LOCATION
  android.permission.ACCESS_COARSE_LOCATION
  android.permission.CAMERA
  android.permission.RECORD_AUDIO
  android.permission.READ_CONTACTS
  android.permission.WRITE_CONTACTS
  android.permission.READ_SMS
  android.permission.SEND_SMS
  android.permission.READ_PHONE_STATE
  android.permission.MANAGE_EXTERNAL_STORAGE
  android.permission.REQUEST_INSTALL_PACKAGES
)
for p in "${FORBIDDEN[@]}"; do
  if grep -q "$p" <<<"$PERMISSIONS"; then
    fail "Forbidden/sensitive permission present: $p"
  fi
done

"$ZIPALIGN" -c -P 16 -v 4 "$APK" >/dev/null || fail "APK zip alignment / 16 KB page alignment check failed"

mkdir -p android-production/app/build/reports/release-audit
{
  echo "applicationId=$APP_ID"
  echo "versionCode=$VERSION_CODE"
  echo "versionName=$VERSION_NAME"
  echo "minSdk=$MIN_SDK"
  echo "targetSdk=$TARGET_SDK"
  sha256sum "$APK"
  sha256sum "$AAB"
} | tee android-production/app/build/reports/release-audit/summary.txt

note "Android release artifact audit passed for $APP_ID v$VERSION_NAME ($VERSION_CODE)."
