#!/usr/bin/env bash
set -euo pipefail

INPUT_AAB="${1:-android-production/app/build/outputs/bundle/release/app-release.aab}"
OUTPUT_AAB="${2:-android-production/app/build/outputs/bundle/release/BilgiliMedia-1.4.9-upload-signed.aab}"

required_env=(
  BILGILIMEDIA_KEYSTORE_FILE
  BILGILIMEDIA_KEY_ALIAS
  BILGILIMEDIA_STORE_PASSWORD
  BILGILIMEDIA_KEY_PASSWORD
)

for name in "${required_env[@]}"; do
  if [[ -z "${!name:-}" ]]; then
    echo "Missing required environment variable: $name" >&2
    exit 2
  fi
done

test -s "$INPUT_AAB"
test -s "$BILGILIMEDIA_KEYSTORE_FILE"
command -v jarsigner >/dev/null
command -v keytool >/dev/null
command -v sha256sum >/dev/null

mkdir -p "$(dirname "$OUTPUT_AAB")"
cp "$INPUT_AAB" "$OUTPUT_AAB"

jarsigner   -keystore "$BILGILIMEDIA_KEYSTORE_FILE"   -storepass:env BILGILIMEDIA_STORE_PASSWORD   -keypass:env BILGILIMEDIA_KEY_PASSWORD   -sigalg SHA256withRSA   -digestalg SHA-256   "$OUTPUT_AAB"   "$BILGILIMEDIA_KEY_ALIAS"

jarsigner -verify -strict -verbose "$OUTPUT_AAB" >/dev/null

actual_sha256="$(
  keytool -list -v     -keystore "$BILGILIMEDIA_KEYSTORE_FILE"     -storepass:env BILGILIMEDIA_STORE_PASSWORD     -alias "$BILGILIMEDIA_KEY_ALIAS" |
    awk -F': ' '/SHA256:/{print $2; exit}'
)"

if [[ -z "$actual_sha256" ]]; then
  echo "Could not read signing certificate SHA-256 fingerprint." >&2
  exit 3
fi

normalize_fingerprint() {
  printf '%s' "$1" | tr '[:lower:]' '[:upper:]' | tr -d ':[:space:]'
}

if [[ -n "${BILGILIMEDIA_EXPECTED_CERT_SHA256:-}" ]]; then
  expected="$(normalize_fingerprint "$BILGILIMEDIA_EXPECTED_CERT_SHA256")"
  actual="$(normalize_fingerprint "$actual_sha256")"
  if [[ "$expected" != "$actual" ]]; then
    echo "Signing certificate fingerprint mismatch." >&2
    echo "Expected: $BILGILIMEDIA_EXPECTED_CERT_SHA256" >&2
    echo "Actual:   $actual_sha256" >&2
    rm -f "$OUTPUT_AAB"
    exit 4
  fi
fi

echo "Signed AAB: $OUTPUT_AAB"
echo "Signing certificate SHA-256: $actual_sha256"
sha256sum "$OUTPUT_AAB"
