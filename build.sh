#!/usr/bin/env bash
set -euo pipefail

PROJ="$(cd "$(dirname "$0")" && pwd)"
ANDROID_SDK="${ANDROID_HOME:-$HOME/Android/Sdk}"
ANDROID_JAR="${ANDROID_JAR:-$ANDROID_SDK/platforms/android-34/android.jar}"
D8="${D8:-$ANDROID_SDK/build-tools/34.0.0/d8}"
ZIPALIGN="${ZIPALIGN:-$ANDROID_SDK/build-tools/34.0.0/zipalign}"
OUT_APK="$PROJ/OriginOS_Biometric_IME_Fix_v1.0.apk"

rm -rf "$PROJ/build"
mkdir -p "$PROJ/build/test_cls" "$PROJ/build/stubs_cls" "$PROJ/build/app_cls" "$PROJ/build/res_flat"

echo "=== 1. Unit tests ==="
javac --release 8 -Xlint:-options -d "$PROJ/build/test_cls" \
  "$PROJ/java/com/originos/biometric/imefix/CredentialGuard.java" \
  "$PROJ/test/CredentialGuardTest.java"
java -cp "$PROJ/build/test_cls" CredentialGuardTest

echo "=== 2. Compile ==="
javac --release 8 -Xlint:-options -cp "$ANDROID_JAR" -d "$PROJ/build/stubs_cls" \
  $(find "$PROJ/stubs" -name "*.java")
javac --release 8 -Xlint:-options -cp "$ANDROID_JAR:$PROJ/build/stubs_cls" -d "$PROJ/build/app_cls" \
  "$PROJ/java/com/originos/biometric/imefix/CredentialGuard.java" \
  "$PROJ/java/com/originos/biometric/imefix/BiometricImeFixHook.java"

echo "=== 3. Dex ==="
"$D8" --min-api 29 --lib "$ANDROID_JAR" --classpath "$PROJ/build/stubs_cls" \
  --output "$PROJ/build" $(find "$PROJ/build/app_cls" -name "*.class")

echo "=== 4. Package ==="
aapt2 compile --dir "$PROJ/res" -o "$PROJ/build/res_flat/res.zip"
aapt2 link -I "$ANDROID_JAR" --manifest "$PROJ/AndroidManifest.xml" \
  "$PROJ/build/res_flat/res.zip" -A "$PROJ/assets" \
  --min-sdk-version 34 --target-sdk-version 35 \
  -o "$PROJ/build/unsigned.apk"
(cd "$PROJ/build" && zip -q -u unsigned.apk classes.dex)
"$ZIPALIGN" -f 4 "$PROJ/build/unsigned.apk" "$PROJ/build/aligned.apk"

echo "=== 5. Sign & Verify ==="
KS="${KEYSTORE_PATH:-$PROJ/release.jks}"
KS_PASS="${KEYSTORE_PASS:-imefix123}"
KS_ALIAS="${KEYSTORE_ALIAS:-imefix}"
if [ ! -f "$KS" ]; then
  keytool -genkeypair -v -keystore "$KS" -alias "$KS_ALIAS" \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -storepass "$KS_PASS" -keypass "$KS_PASS" \
    -dname "CN=Haicu256, OU=OriginOS, O=OpenSource, C=VN" >/dev/null 2>&1
fi
apksigner sign --ks "$KS" --ks-key-alias "$KS_ALIAS" \
  --ks-pass "pass:$KS_PASS" --key-pass "pass:$KS_PASS" \
  --out "$OUT_APK" "$PROJ/build/aligned.apk"
apksigner verify --print-certs "$OUT_APK"
sha256sum "$OUT_APK"
