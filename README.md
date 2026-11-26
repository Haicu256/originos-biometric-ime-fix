# OriginOS Biometric IME Fix

Lightweight Xposed/LSPosed/Vector module (`~13 KB`) that fixes the **OriginOS 5 `BiometricPrompt` third-party keyboard bug**, allowing **all third-party keyboards** (Gboard, SwiftKey, Laban Key, HeliBoard, etc.) to type PIN/passwords normally without enabling vivo's built-in Secure Keyboard (`vivo_secure_input_method`).

---

## Root Cause / Nguyên nhân gốc

On vivo / iQOO devices running **OriginOS 5 (Android 15)**, `/system/priv-app/SystemUI/SystemUI.apk` has a duplicate view inflation bug inside `com.android.systemui.biometrics.AuthContainerView`:

1. `AuthContainerView.showPrompt()` calls `addCredentialView(boolean, boolean)` → inflates `CredentialPasswordView` **#1** (`lockPassword`) and adds it to `mLayout`.
2. Immediately afterward, `AuthContainerView.onAttachedToWindow()` calls `addCredentialView(boolean, boolean)` **a second time** without checking whether `mCredentialView` is already attached → inflates `CredentialPasswordView` **#2** directly on top of #1.
3. Third-party keyboards connect to View #1 (hidden underneath), while View #2 covers the screen with an empty `EditText`, making keys appear unresponsive even though they are typing into the hidden field.

### Tiếng Việt
Trên máy vivo / iQOO chạy **OriginOS 5 (Android 15)**, `SystemUI` gọi trùng hàm `AuthContainerView.addCredentialView(boolean, boolean)` 2 lần liên tiếp (từ `showPrompt()` và `onAttachedToWindow()`), tạo 2 ô `lockPassword` chồng đè lên nhau. Bàn phím bên thứ 3 (Gboard, Laban Key, SwiftKey…) gõ chữ vào ô bị che ở dưới nên màn hình không hiện dấu chấm mật khẩu.

---

## Fix / Cách hoạt động

- Hooks **only** `com.android.systemui` (`AuthContainerView.addCredentialView(boolean, boolean)`).
- If `mCredentialView` is already non-null and attached to a parent (`getParent() != null`), skips the duplicate inflation call cleanly.
- Zero system file modifications, zero background daemons, zero battery overhead, compatible with KernelSU `late-load` Temp Root + Vector / LSPosed.

---

## Installation / Cài đặt

1. Download `OriginOS_Biometric_IME_Fix_v1.0.apk` from [Releases](../../releases).
2. Install the APK.
3. Open **LSPosed / Vector Manager** → enable **OriginOS Biometric IME Fix** → ensure scope **`System UI` (`com.android.systemui`)** is checked.
4. Restart **System UI** (or Soft Reboot / Reboot).
5. Turn off **Secure Keyboard** in OriginOS Settings if desired — any third-party keyboard now works on `BiometricPrompt`.

---

## Build from Source

Requires JDK (`javac`), Android SDK (`android.jar`, `d8`, `zipalign`), `aapt2`, and `apksigner`:

```bash
./build.sh
```

Unit tests (`CredentialGuardTest`) run automatically before packaging.

## License

MIT
