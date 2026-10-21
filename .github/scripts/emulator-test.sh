#!/usr/bin/env bash
# Glass26: install and smoke-test the signed debug APK on an Android emulator.
set -euo pipefail

APK="app/build/outputs/apk/debug/app-debug.apk"
test -s "$APK"
echo "Waiting for Android PackageManager..."
ready=0
for attempt in $(seq 1 36); do
  if adb shell pm path android 2>/dev/null | grep -q '^package:'; then
    ready=1
    break
  fi
  echo "PackageManager not ready yet ($attempt/36)"
  sleep 5
done
test "$ready" -eq 1

echo "Installing Glass26 using non-streaming ADB..."
installed=0
for attempt in $(seq 1 6); do
  if adb install --no-streaming -r "$APK"; then
    installed=1
    break
  fi
  echo "ADB install retry ($attempt/6)"
  sleep 10
done
test "$installed" -eq 1

echo "Checking package and keyboard registration..."
adb shell pm path com.glass26.app | grep -q '^package:'
adb shell ime list -s | grep -q 'com.glass26.app'

echo "Launching Glass26 home..."
adb logcat -c || true
adb shell am start -W -n com.glass26.app/.HomeActivity
sleep 10
adb shell pidof com.glass26.app | grep -Eq '[0-9]+'
adb shell screencap -p /sdcard/glass26.png
adb pull /sdcard/glass26.png glass26-emulador.png
test -s glass26-emulador.png
adb logcat -d -s AndroidRuntime:E > android-runtime.txt

if grep -q 'FATAL EXCEPTION' android-runtime.txt; then
  echo "A fatal exception was found in emulator logcat."
  cat android-runtime.txt
  exit 1
fi

echo "Glass26 emulator smoke test PASSED."
