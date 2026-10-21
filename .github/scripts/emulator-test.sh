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

echo "Checking package registration..."
registered=0
for attempt in $(seq 1 12); do
  adb shell pm path com.glass26.app > glass26-package.txt 2>&1 || true
  echo "Package check $attempt: $(cat glass26-package.txt)"
  if grep -q 'package:' glass26-package.txt; then
    registered=1
    break
  fi
  sleep 3
done
test "$registered" -eq 1

echo "Checking keyboard (IME) registration..."
keyboard=0
for attempt in $(seq 1 12); do
  adb shell ime list -a -s > glass26-keyboards.txt 2>&1 || true
  echo "IME check $attempt: $(cat glass26-keyboards.txt)"
  if grep -q 'com.glass26.app' glass26-keyboards.txt; then
    keyboard=1
    break
  fi
  sleep 3
done
test "$keyboard" -eq 1

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
