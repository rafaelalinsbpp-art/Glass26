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

echo "Assigning Glass26 as the Android Home role holder..."
adb shell cmd role add-role-holder android.app.role.HOME com.glass26.app
adb shell cmd role get-role-holders android.app.role.HOME | grep -q 'com.glass26.app'

echo "Launching launcher through the actual Android HOME intent..."
adb logcat -c || true
adb shell input keyevent KEYCODE_HOME
sleep 7
adb shell pidof com.glass26.app | grep -Eq '[0-9]+'
adb shell screencap -p /sdcard/glass26.png
adb pull /sdcard/glass26.png glass26-emulador.png
test -s glass26-emulador.png

echo "Testing swipe to the second launcher page..."
adb shell input swipe 900 1040 170 1040 490
sleep 4
adb shell uiautomator dump /sdcard/glass26-workspace.xml >/dev/null
adb pull /sdcard/glass26-workspace.xml glass26-workspace.xml >/dev/null
python3 - <<'PY'
import xml.etree.ElementTree as ET
root=ET.parse("glass26-workspace.xml").getroot()
texts=[node.attrib.get("text","") for node in root.iter()]
print("UI after swipe:",texts[:50])
assert any("Biblioteca de aplicativos" in x for x in texts), "Horizontal swipe did not change the launcher page"
assert any("Página 2" in x for x in texts), "Second page marker missing"
PY
adb shell screencap -p /sdcard/glass26-pagina2.png
adb pull /sdcard/glass26-pagina2.png glass26-pagina2.png

echo "Testing HOME key brings the default launcher back..."
adb shell am start -W -a android.settings.SETTINGS
sleep 3
adb shell input keyevent KEYCODE_HOME
sleep 3
adb shell pidof com.glass26.app | grep -Eq '[0-9]+'
adb shell dumpsys activity activities | grep -E 'mResumedActivity|topResumedActivity|ResumedActivity' | tail -6

echo "Testing a tap on a real launcher icon..."
adb shell input swipe 160 1040 900 1040 490
sleep 3
adb shell uiautomator dump /sdcard/glass26-icons.xml >/dev/null
adb pull /sdcard/glass26-icons.xml glass26-icons.xml >/dev/null
python3 - <<'PY'
import xml.etree.ElementTree as ET,re,subprocess,time
root=ET.parse("glass26-icons.xml").getroot()
names=("Config.","Configurações","Settings","Câmera","Camera")
for name in names:
    nodes=[n for n in root.iter() if n.attrib.get("text","")==name]
    if not nodes: continue
    bounds=nodes[0].attrib.get("bounds","")
    points=[int(v) for v in re.findall(r"\d+",bounds)]
    if len(points)==4:
        x=(points[0]+points[2])//2
        y=(points[1]+points[3])//2
        print("Tapping icon label:",name,x,y,flush=True)
        subprocess.check_call(["adb","shell","input","tap",str(x),str(y)])
        time.sleep(3)
        activities=subprocess.check_output(["adb","shell","dumpsys","activity","activities"],text=True)
        resumed=[line for line in activities.splitlines() if "ResumedActivity" in line or "topResumedActivity" in line or "mResumedActivity" in line]
        print("After tap:",resumed[-5:],flush=True)
        if "com.glass26.app/.HomeActivity" in "\n".join(resumed):
            raise AssertionError("App icon tap did not open its Android app")
        break
else:
    raise AssertionError("No known native application icon found in launcher")
PY

echo "Testing the Glass26 keyboard as the selected Android input method..."
echo "Allowing software keyboard while emulator exposes a hardware keyboard..."
adb shell settings put secure show_ime_with_hard_keyboard 1
adb shell settings get secure show_ime_with_hard_keyboard
adb shell ime enable com.glass26.app/.GlassKeyboardService
adb shell ime set com.glass26.app/.GlassKeyboardService
adb shell am start -W -n com.glass26.app/.KeyboardTestActivity
sleep 3
adb shell uiautomator dump /sdcard/glass26-keyboard-field.xml >/dev/null
adb pull /sdcard/glass26-keyboard-field.xml glass26-keyboard-field.xml >/dev/null
python3 - <<'PY'
import xml.etree.ElementTree as ET,re,subprocess
root=ET.parse("glass26-keyboard-field.xml").getroot()
inputs=[node for node in root.iter() if node.attrib.get("class","").endswith("EditText")]
assert inputs, "Glass26 keyboard test EditText was not visible"
bounds=inputs[0].attrib.get("bounds","")
points=[int(v) for v in re.findall(r"\d+",bounds)]
assert len(points)==4,bounds
x=(points[0]+points[2])//2
y=(points[1]+points[3])//2
print("Focusing IME test field",x,y,flush=True)
subprocess.check_call(["adb","shell","input","tap",str(x),str(y)])
buttons=[n for n in root.iter() if "3. Escrever no campo de teste" in n.attrib.get("text","")]
if buttons:
    area=[int(v) for v in re.findall(r"\d+",buttons[0].attrib.get("bounds",""))]
    if len(area)==4:
        bx=(area[0]+area[2])//2
        by=(area[1]+area[3])//2
        print("Explicitly requesting keyboard from test activity",bx,by,flush=True)
        subprocess.check_call(["adb","shell","input","tap",str(bx),str(by)])
PY
sleep 5
echo "Android input method diagnostics:"
adb shell dumpsys input_method > glass26-ime-status.txt
grep -E 'mCurId=|mInputShown|mIsInputViewShown|mServedView' glass26-ime-status.txt | tail -14 || true
grep -q 'mCurId=com.glass26.app/.GlassKeyboardService' glass26-ime-status.txt
grep -q 'mIsInputViewShown=true' glass26-ime-status.txt

echo "Capturing actual software-keyboard pixels before any accessibility inspection..."
adb shell screencap -p /sdcard/glass26-teclado.png
adb pull /sdcard/glass26-teclado.png glass26-teclado.png
test -s glass26-teclado.png
adb shell uiautomator dump /sdcard/glass26-ime-keys.xml >/dev/null
adb pull /sdcard/glass26-ime-keys.xml glass26-ime-keys.xml >/dev/null
echo "Testing real on-screen taps (Android accessibility hierarchy may omit IME windows)..."
python3 - <<'PY'
import subprocess,xml.etree.ElementTree as ET,struct,time,re
with open("glass26-teclado.png","rb") as file:
    raw=file.read(24)
assert raw.startswith(b"\x89PNG\r\n\x1a\n"),"Missing screenshot PNG header"
width,height=struct.unpack(">II",raw[16:24])
density_output=subprocess.check_output(["adb","shell","wm","density"],text=True)
matches=re.findall(r"(\d+)",density_output)
density=(int(matches[-1])/160) if matches else 2.75
print("IME screenshot dimensions:",width,height,"density:",density,flush=True)
x=round(width/18)
placeholder="Toque aqui e digite com Glass26"
def edit_contents():
    subprocess.check_call(["adb","shell","uiautomator","dump","/sdcard/glass26-ime-result.xml"],stdout=subprocess.DEVNULL)
    subprocess.check_call(["adb","pull","/sdcard/glass26-ime-result.xml","glass26-ime-result.xml"],stdout=subprocess.DEVNULL)
    root=ET.parse("glass26-ime-result.xml").getroot()
    return [n.attrib.get("text","") for n in root.iter() if n.attrib.get("class","").endswith("EditText")]
for distance_dp in (150,165,180,195,210,225,135,245):
    y=height-round(distance_dp*density)
    print("Tapping software-keyboard candidate:",x,y,"distance dp:",distance_dp,flush=True)
    subprocess.check_call(["adb","shell","input","tap",str(x),str(y)])
    time.sleep(0.8)
    texts=edit_contents()
    print("Text after tap:",texts,flush=True)
    if any(value and value!=placeholder for value in texts):
        print("PASS: selected Glass26 IME typed into Android EditText",flush=True)
        break
else:
    raise AssertionError("IME is shown according to Android, but physical screen taps did not insert text")
PY
adb shell screencap -p /sdcard/glass26-teclado-digitando.png
adb pull /sdcard/glass26-teclado-digitando.png glass26-teclado-digitando.png

echo "Testing lock-screen style preview, leaving Samsung secure keyguard untouched..."
adb shell am start -W -n com.glass26.app/.LockPreviewActivity
sleep 3
adb shell screencap -p /sdcard/glass26-bloqueio.png
adb pull /sdcard/glass26-bloqueio.png glass26-bloqueio.png
adb shell uiautomator dump /sdcard/glass26-lock.xml >/dev/null
adb pull /sdcard/glass26-lock.xml glass26-lock.xml >/dev/null
python3 - <<'PY'
import xml.etree.ElementTree as ET
root=ET.parse("glass26-lock.xml").getroot()
texts=[node.attrib.get("text","") for node in root.iter()]
assert any("Prévia visual" in text for text in texts), "Glass26 lock-screen preview was not rendered"
print("Lock preview shown without overriding system keyguard.",flush=True)
PY
adb shell input keyevent KEYCODE_HOME
sleep 3
adb logcat -d -s AndroidRuntime:E > android-runtime.txt

if grep -q 'FATAL EXCEPTION' android-runtime.txt; then
  echo "A fatal exception was found in emulator logcat."
  cat android-runtime.txt
  exit 1
fi

echo "Glass26 emulator smoke test PASSED."
