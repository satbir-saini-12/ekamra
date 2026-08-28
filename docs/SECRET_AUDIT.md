# Secret Audit

**Audit Date:** 2026-08-26  
**⚠️ CRITICAL: Multiple secrets are committed to the repository in plaintext**  

---

## Summary

| Severity | Count | Status |
|----------|-------|--------|
| CRITICAL | 6 | ROTATION REQUIRED |
| HIGH | 4 | ROTATION REQUIRED |
| MEDIUM | 3 | REVIEW REQUIRED |
| LOW | 2 | INFORMATIONAL |

---

## CRITICAL — Secrets Committed to Git

### S1. Release Keystore Passwords

| Field | Value |
|-------|-------|
| **Location** | `android/key.properties` |
| **Type** | Signing identity — store/key passwords |
| **Value** | `aeroplay123` (masked: `aero****123`) |
| **Status** | **ROTATION REQUIRED** — committed to repository |
| **Action** | Generate new keystore with new passwords. Never commit `key.properties` with real values. Use GitHub Secrets for CI. |

### S2. Release Keystore File

| Field | Value |
|-------|-------|
| **Location** | `android/keys/release.keystore` |
| **Type** | Signing identity — private keystore |
| **Status** | **ROTATION REQUIRED** — private key committed to repository |
| **Action** | Generate new upload keystore. Delete old keystore from repo. Use GitHub Secrets to store keystore base64. |

### S3. Second Release Keystore File

| Field | Value |
|-------|-------|
| **Location** | `android/keys/release_new.keystore` |
| **Type** | Signing identity — private keystore |
| **Status** | **ROTATION REQUIRED** — second keystore also committed |
| **Action** | Delete from repo. |

### S4. Debug Keystore File

| Field | Value |
|-------|-------|
| **Location** | `android/keys/debug.keystore` |
| **Type** | Debug signing key |
| **Status** | **ROTATION REQUIRED** — should not be in repository |
| **Action** | Delete from repo. Use Flutter/Gradle default debug signing. |

### S5. Keystore Documentation with Plaintext Passwords

| Field | Value |
|-------|-------|
| **Location** | `android/keys/EXPORT_KEY_WITH_PEPK.md`, `android/keys/KEYSTORE_MAPPING.md`, `android/keys/NEW_KEYSTORE_INFO.md`, `android/keys/README.md`, `android/keys/SHA_FINGERPRINTS.md` |
| **Type** | Documentation containing plaintext passwords |
| **Exposed values** | `aeroplay123` (store password), `aeroplay123` (key password), `aeroplay` (alias) |
| **Status** | **ROTATION REQUIRED** — passwords exposed in documentation |
| **Action** | Delete all files in `android/keys/` directory. |

### S6. Keystore Generation Scripts with Plaintext Passwords

| Field | Value |
|-------|-------|
| **Location** | `android/keys/generate_keys.bat`, `android/keys/generate_keys.sh`, `android/keys/export_key.bat`, `android/keys/export_key.sh`, `android/keys/test_keystore.ps1` |
| **Type** | Scripts containing plaintext passwords |
| **Exposed values** | `aeroplay123`, `aeroplay`, `CN=AeroPlay` |
| **Status** | **ROTATION REQUIRED** — passwords in scripts |
| **Action** | Delete all scripts. |

---

## HIGH — API Credentials Hardcoded in Source

### S7. API Basic Auth Credentials

| Field | Value |
|-------|-------|
| **Location** | `lib/network/api_configuration.dart:11-13` |
| **Type** | API credential — Basic Auth username/password |
| **Value** | `admin` / `****` (masked: `1***`) |
| **Status** | **ROTATION REQUIRED** — hardcoded in source |
| **Action** | Move to environment configuration or backend. At minimum, change credentials on the new backend. |

### S8. API Key

| Field | Value |
|-------|-------|
| **Location** | `lib/config.dart:6` |
| **Type** | API credential — API key header |
| **Value** | `nzmux****` (masked: `n****`) |
| **Status** | **ROTATION REQUIRED** — hardcoded in source |
| **Action** | Generate new API key for new backend. Consider using `--dart-define` for build-time injection. |

### S9. OneSignal App ID

| Field | Value |
|-------|-------|
| **Location** | `lib/config.dart:9` |
| **Type** | Public configuration identifier — OneSignal app ID |
| **Value** | `36fe0ed****` (masked: `36fe****`) |
| **Status** | **ROTATION REQUIRED** — tied to old OneSignal account |
| **Action** | Create new OneSignal app for new application. |

### S10. Stripe Publishable Key

| Field | Value |
|-------|-------|
| **Location** | `lib/config.dart:13` |
| **Type** | API credential — Stripe publishable key (test mode) |
| **Value** | `pk_test_51M25oy****` (masked: `pk_test****`) |
| **Status** | **ROTATION REQUIRED** — test key committed, tied to old Stripe account |
| **Action** | Generate new Stripe key for new account. Use `--dart-define` for build-time injection. |

---

## MEDIUM — Configuration Identifiers

### S11. Firebase API Key

| Field | Value |
|-------|-------|
| **Location** | `android/app/google-services.json:47` |
| **Type** | Public configuration identifier — Firebase API key |
| **Value** | `AIzaSyBkVp****` (masked: `AIzaS****`) |
| **Status** | REVIEW REQUIRED — Firebase API keys are public by design, but tied to old project |
| **Action** | Replace with new Firebase project's `google-services.json`. |

### S12. Firebase OAuth Client IDs

| Field | Value |
|-------|-------|
| **Location** | `android/app/google-services.json:17,25,33,41` |
| **Type** | Public configuration identifier — OAuth client IDs |
| **Value** | `1020367860153-****` (masked) |
| **Status** | REVIEW REQUIRED — tied to old Firebase project |
| **Action** | Replace with new Firebase project config. |

### S13. Facebook App ID

| Field | Value |
|-------|-------|
| **Location** | `android/app/src/main/res/values/strings.xml:4` |
| **Type** | Public configuration identifier — Facebook App ID |
| **Value** | `442816973525148` |
| **Status** | REVIEW REQUIRED — tied to old Facebook app, auth disabled |
| **Action** | Remove from strings and manifest if Facebook auth stays disabled. |

---

## LOW — Informational

### S14. RSA Public Key

| Field | Value |
|-------|-------|
| **Location** | `encryption_public_key.pem` |
| **Type** | Public key (not secret) |
| **Status** | INFORMATIONAL — public keys are not secrets |
| **Action** | Determine if this key is used for encryption. If tied to old backend, may need replacement. |

### S15. Play Store Public Key (Base64)

| Field | Value |
|-------|-------|
| **Location** | `lib/config.dart:28` |
| **Type** | Public key for in-app purchase verification |
| **Value** | `MIIBIjANBgkq****` (masked) |
| **Status** | INFORMATIONAL — public key, but IAP is disabled |
| **Action** | Remove if IAP stays disabled. Replace with new key if re-enabled. |

---

## Git History Warning

**All secrets listed above may exist in Git history even after deletion from the working tree.**

- If this repository has been pushed to any remote (GitHub, GitLab, etc.), the secrets are potentially exposed
- Simply deleting files does NOT remove them from Git history
- **Recommended action:**
  1. Rotate ALL secrets listed above (generate new keystores, new API keys, new passwords)
  2. Use `git filter-repo` or BFG Repo-Cleaner to purge secrets from history (if remote exists)
  3. Force-push cleaned history (if remote exists)
  4. Notify anyone with repository access about the rotation

---

## Files to Delete from Repository

```
android/key.properties
android/keys/release.keystore
android/keys/release_new.keystore
android/keys/debug.keystore
android/keys/EXPORT_KEY_WITH_PEPK.md
android/keys/GOOGLE_PLAY_SIGNING_GUIDE.md
android/keys/KEYSTORE_MAPPING.md
android/keys/KEYSTORE_SOLUTION.md
android/keys/NEW_KEYSTORE_INFO.md
android/keys/README.md
android/keys/SHA_FINGERPRINTS.md
android/keys/export_key.bat
android/keys/export_key.sh
android/keys/generate_keys.bat
android/keys/generate_keys.sh
android/keys/keystore_output.txt
android/keys/test_keystore.ps1
encryption_public_key.pem
flutter_01.log
android/local.properties
```

**Note:** `android/key.properties` and `android/local.properties` are in `.gitignore` but may still be tracked in Git history. Verify with `git log --all --diff-filter=A -- android/key.properties`.
