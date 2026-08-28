# Key Migration Plan

**Audit Date:** 2026-08-26  

---

## Key Classification

### A. Signing Identity

| Key | Location | Current Value (Masked) | Action |
|-----|----------|----------------------|--------|
| Release keystore | `android/keys/release.keystore` | `[BINARY]` | **GENERATE NEW** — old keystore committed to repo. Create new upload keystore using `keytool`. Store as GitHub Secret (`ANDROID_KEYSTORE_BASE64`). Do NOT commit. |
| Release keystore password | `android/key.properties` | `aero****123` | **GENERATE NEW** — use strong password. Store as GitHub Secret (`ANDROID_KEYSTORE_PASSWORD`). |
| Release key password | `android/key.properties` | `aero****123` | **GENERATE NEW** — use strong password. Store as GitHub Secret (`ANDROID_KEY_PASSWORD`). |
| Release key alias | `android/key.properties` | `aeroplay` | **GENERATE NEW** — use new alias. Store as GitHub Secret (`ANDROID_KEY_ALIAS`). |
| Debug keystore | `android/keys/debug.keystore` | `[BINARY]` | **DELETE** — use Flutter/Gradle default debug signing. |

**Migration impact:** New app = new Play Store listing = new upload key. No existing users to impact. Old signing identity is irrelevant for the new app.

**Google Play App Signing:** Enroll the new app in Play App Signing. Generate upload key locally, use it to sign AABs. Google Play will use its own app signing key for distribution.

---

### B. API Credentials

| Key | Location | Current Value (Masked) | Action |
|-----|----------|----------------------|--------|
| API Basic Auth username | `lib/network/api_configuration.dart:11` | `admin` | **ROTATE** — create new credentials on new backend. |
| API Basic Auth password | `lib/network/api_configuration.dart:12` | `****` (masked) | **ROTATE** — use strong password on new backend. |
| API Key | `lib/config.dart:6` | `nzmux****` | **ROTATE** — generate new API key for new backend. |
| OneSignal App ID | `lib/config.dart:9` | `36fe0ed****` | **CREATE NEW** — new OneSignal app for new application. |
| Stripe Publishable Key | `lib/config.dart:13` | `pk_test****` | **CREATE NEW** — new Stripe account or new key for existing account. |
| PayPal Secret Key | `lib/config.dart:15` | (empty) | **CONFIGURE** — set up PayPal credentials for new app. |
| Razorpay Key ID | Backend-driven (PaymentConfig) | N/A | **CONFIGURE** — set up Razorpay for new app if used. |

**Migration impact:** New backend instance = new credentials. No existing user data dependency. All credentials should be injected at build time using `--dart-define` or environment variables where possible.

---

### C. Public Configuration Identifiers

| Key | Location | Current Value (Masked) | Action |
|-----|----------|----------------------|--------|
| Firebase API Key | `google-services.json:47` | `AIzaS****` | **REPLACE** — new Firebase project config. |
| Firebase Project ID | `google-services.json:4` | `aeroplayott` | **REPLACE** — new Firebase project. |
| Firebase App ID | `google-services.json:10` | `1:1020...` | **REPLACE** — new Firebase app registration. |
| Firebase OAuth Client IDs | `google-services.json:17,25,33,41` | `1020...-****` | **REPLACE** — new OAuth clients. |
| Facebook App ID | `strings.xml:4` | `442816973525148` | **REMOVE** — Facebook auth disabled. Remove from strings and manifest. |
| AdMob App ID | `AndroidManifest.xml:18` | `ca-app-pub-3940...` | **REMOVE** — AdMob disabled. Remove from manifest. |

**Migration impact:** These are public identifiers, not secrets. They must match the new application's registrations in respective consoles. No user data dependency.

---

### D. Internal Cryptographic Key

| Key | Location | Current Value (Masked) | Action |
|-----|----------|----------------------|--------|
| RSA Public Key | `encryption_public_key.pem` | `[PEM PUBLIC KEY]` | **ANALYZE** — determine if this key is used for encrypting user data. |

**Analysis:**
- The file is a **public** key (not private) — it cannot decrypt data by itself
- No code reference found that imports or uses this key in the Dart source
- This appears to be a leftover from the original OXOO template
- **Risk assessment:** LOW — public key alone cannot compromise data
- **Action:** Determine if the backend uses the corresponding private key. If the new backend uses a different key pair, remove this file. If the backend still uses this key pair, keep it but verify.

**Migration impact:** If the backend uses this key pair for encrypting sensitive data (e.g., payment info), changing it could make existing encrypted data unreadable. Since this is a **new** app with no existing users, this is not a concern. **OWNER ACTION REQUIRED:** Confirm with backend team whether this key is in use.

---

### E. Hardcoded Secrets

| Key | Location | Current Value (Masked) | Action |
|-----|----------|----------------------|--------|
| Keystore passwords in scripts | `android/keys/*.bat`, `*.sh`, `*.ps1` | `aero****123` | **REMOVE + ROTATE** — delete files, rotate keystore. |
| Keystore passwords in docs | `android/keys/*.md` | `aero****123` | **REMOVE + ROTATE** — delete files, rotate keystore. |
| Basic Auth credentials | `lib/network/api_configuration.dart` | `admin:****` | **REMOVE + ROTATE** — move to build-time injection, rotate on backend. |
| API key | `lib/config.dart:6` | `nzmux****` | **REMOVE + ROTATE** — move to build-time injection, rotate on backend. |
| Stripe key | `lib/config.dart:13` | `pk_test****` | **REMOVE + ROTATE** — move to build-time injection, rotate on Stripe. |

**Migration impact:** All hardcoded secrets must be removed from source code and replaced with build-time injection (`--dart-define`) or environment-based configuration. Since this is a new app, rotation is straightforward.

---

## Build-Time Secret Injection Strategy

For the new application, sensitive values should be injected at build time:

```bash
flutter build appbundle --release \
  --dart-define=API_SERVER_URL=https://newbackend.com/rest-api/ \
  --dart-define=API_KEY=new_api_key \
  --dart-define=ONE_SIGNAL_ID=new_onesignal_id \
  --dart-define=STRIPE_PUBLISHABLE_KEY=pk_live_new_key
```

In Dart code:
```dart
const apiServerUrl = String.fromEnvironment('API_SERVER_URL', defaultValue: '');
```

For GitHub Actions, these values come from repository secrets:
```yaml
env:
  API_SERVER_URL: ${{ secrets.API_SERVER_URL }}
  API_KEY: ${{ secrets.API_KEY }}
```

---

## Signing Key Generation Instructions

### Generate New Upload Keystore

```bash
keytool -genkey -v \
  -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload \
  -dname "CN=NewAppName,OU=Development,O=YourCompany,L=City,S=State,C=US" \
  -storepass [STRONG_PASSWORD] \
  -keypass [STRONG_PASSWORD]
```

### Base64 Encode for GitHub Secrets

**Linux/macOS:**
```bash
base64 -i upload-keystore.jks | pbcopy   # macOS
base64 -w 0 upload-keystore.jks          # Linux
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload-keystore.jks"))
```

### GitHub Secrets to Configure

| Secret Name | Value |
|-------------|-------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded keystore file |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore store password |
| `ANDROID_KEY_PASSWORD` | Key password |
| `ANDROID_KEY_ALIAS` | Key alias (e.g., `upload`) |

---

## Summary

| Category | Count | Action |
|----------|-------|--------|
| A. Signing identity | 5 items | Generate new keystore, use GitHub Secrets |
| B. API credentials | 7 items | Rotate all, use build-time injection |
| C. Public identifiers | 6 items | Replace with new project registrations |
| D. Internal crypto key | 1 item | Analyze backend usage, likely remove |
| E. Hardcoded secrets | 5 items | Remove from source, rotate all |
