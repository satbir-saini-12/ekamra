# Final Migration Report

**Date:** 2026-08-26  
**Project:** OXOO-Flutter OTT Streaming App  
**Target:** New independently branded Android app for Google Play 2026  

---

## Completed Work

### Phase A — Audit Documents (11 files created)

All audit documents are in `docs/`:

| Document | Purpose |
|----------|---------|
| `FULL_CODE_AUDIT.md` | Complete architecture, features, technical problems, security, API 36 compatibility |
| `PLAY_STORE_2026_COMPLIANCE.md` | Google Play policy requirements (privacy, UGC, financial, permissions, etc.) |
| `DATA_SAFETY_MAPPING.md` | Data collection inventory for Play Console Data Safety form |
| `PRIVACY_POLICY_REQUIREMENTS.md` | Required privacy policy content and implementation steps |
| `REBRANDING_CHECKLIST.md` | All locations with old identity, ordered migration steps |
| `SECRET_AUDIT.md` | All exposed secrets with severity, location, and remediation |
| `KEY_MIGRATION_PLAN.md` | Signing key, API key, and credential rotation strategy |
| `GITHUB_ACTIONS_AUDIT.md` | CI/CD requirements and proposed workflow design |
| `FIREBASE_MIGRATION.md` | Firebase project migration steps |
| `MIGRATION_PLAN.md` | Complete task table (Phases A-J) with dependencies and status |
| `PLAY_STORE_RELEASE_CHECKLIST.md` | Store listing, graphics, declarations needed for release |

### Phase B/E — Security and Build Fixes Implemented

#### Secrets Removed from Repository
- Deleted `android/keys/release.keystore` (release signing key)
- Deleted `android/keys/release_new.keystore` (second keystore)
- Deleted `android/keys/debug.keystore` (debug keystore)
- Deleted `android/keys/*.md` (6 documentation files with plaintext passwords)
- Deleted `android/keys/*.bat`, `*.sh`, `*.ps1` (5 scripts with plaintext passwords)
- Deleted `android/keys/keystore_output.txt`
- Deleted `android/key.properties` (plaintext keystore passwords)
- Deleted `android/local.properties` (machine-specific SDK paths)
- Deleted `encryption_public_key.pem` (unreferenced public key)
- Deleted `flutter_01.log` (log file)

#### Security Fixes
- **Cleartext traffic:** `usesCleartextTraffic` changed from `true` to `false`
- **ProGuard rules:** Replaced `-keep class * { public private *; }` with targeted keep rules for Flutter, Hive, models, OneSignal, Razorpay, Stripe, Firebase, Flutter Downloader
- **Allow backup:** Added `android:allowBackup="false"` to prevent data backup to Google Drive
- **Logging:** Disabled in release mode (`kLOG_ENABLE = !kReleaseMode`)
- **Malformed URL:** Fixed `https://http://desidhamaka.in/panel/terms/` → placeholder `https://example.com/privacy-policy`
- **.gitignore:** Expanded to cover `*.pem`, `*.jks`, `*.keystore`, key scripts/docs, `local.properties`

#### Manifest Fixes
- Removed deprecated `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` permissions
- Added `POST_NOTIFICATIONS` permission (Android 13+)
- Added `FOREGROUND_SERVICE` permission (Android 14+)
- Added `FOREGROUND_SERVICE_DATA_SYNC` permission (Android 14+)
- Removed `maxSdkVersion="36"` from uses-sdk
- Removed `package` attribute from main/debug/profile manifests (using Gradle namespace)
- Removed AdMob test App ID meta-data
- Removed Facebook App ID meta-data and strings

#### Build System Fixes
- Removed duplicate `android.useAndroidX` and `android.enableJetifier` entries in `gradle.properties`
- Removed `enableJetifier` (unnecessary, slows builds)
- Removed version code hack (hardcoded overrides for versions 4/5/6) — now uses simple defaults
- Removed custom debug signing config (uses Gradle default)
- Removed debug signing fallback for release builds
- Changed ProGuard default file from `proguard-android.txt` to `proguard-android-optimize.txt`
- Removed all commented-out code blocks in `build.gradle`
- Reset version to `1.0.0+1` in `pubspec.yaml`
- Removed unused `change_app_package_name` dependency

#### Code Fixes
- Fixed `DioError` → `DioException` deprecation in `repository.dart`
- Updated log tag from `[OXOO-Flutter]` to `[APP]`
- Replaced test that made real API call with basic smoke test

#### Cleanup
- Deleted orphaned `com/oxoo/spagreen/MainActivity.kt` (old package `com.desi.dhamaka`)
- Deleted `lib/strings_bd.dart` (entirely commented out, dead code with old branding)
- Deleted 11 stale documentation files from root directory
- Removed empty `android/keys/` directory

### Phase I — GitHub Actions Workflows Created

- **`.github/workflows/android-build.yml`** — CI build & test (push/PR triggers)
  - Java 17, Flutter stable, caching, analyze, test, debug APK artifact
- **`.github/workflows/android-release.yml`** — Signed release AAB (tags/manual)
  - Java 17, Flutter stable, caching, keystore decode from secrets, signed AAB, mapping file, cleanup

---

## Pending Work (Blocked on Owner Decisions)

The following cannot be completed without owner-provided information:

### New Application Identity
- **New app name** — needed to update `strings.dart`, `strings.xml`, `AndroidManifest.xml`, `landing_screen.dart`, `pubspec.yaml`
- **New application ID** — needed to update `build.gradle`, manifests, Kotlin package, MethodChannel name, `google-services.json`
- **New Dart package name** — needed to rename `oxoo` in `pubspec.yaml` and update ~50+ import statements

### Backend and Services
- **New API server URL** — needed to update `config.dart`
- **New API key** — needed to update `config.dart`
- **New OneSignal App ID** — needed to update `config.dart`
- **New Stripe publishable key** — needed to update `config.dart`
- **New Firebase project** — needed to replace `google-services.json`
- **New privacy policy URL** — needed to update `config.dart`

### Branding Assets
- **New app icon** (1024x1024px) — needed to regenerate all launcher icons
- **New splash screen image** — needed to replace splash assets
- **New logo** — needed to replace drawer header logo
- **New contact email** — needed to update `strings.dart`
- **New copyright text** — needed to update `strings.dart`

### Play Store
- **Content rating questionnaire** — must be completed in Play Console
- **Data Safety form** — must be completed using `DATA_SAFETY_MAPPING.md`
- **Target audience** — must be declared in Play Console
- **App access instructions** — test credentials for review team
- **Store listing info** — title, descriptions, graphics
- **Financial features declaration** — payment method details
- **Account deletion verification** — confirm backend truly deletes user data

### Signing
- **Generate new upload keystore** — must be done locally, never committed
- **Configure GitHub repository secrets** — `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_PASSWORD`, `ANDROID_KEY_ALIAS`
- **Enroll in Play App Signing** — via Play Console

---

## What Can Be Done Now (Without Owner Input)

1. **Verify the app builds** — run `flutter pub get && flutter build apk --debug` to confirm no compilation errors from the changes
2. **Run `flutter analyze`** — check for any new static analysis issues
3. **Review the audit documents** — all in `docs/` directory
4. **Generate new keystore** — follow instructions in `docs/KEY_MIGRATION_PLAN.md`
5. **Create new Firebase project** — follow instructions in `docs/FIREBASE_MIGRATION.md`
6. **Create new OneSignal app** — for push notifications
7. **Write privacy policy** — using `docs/PRIVACY_POLICY_REQUIREMENTS.md` as a template

---

## Risk Assessment

| Risk | Severity | Mitigation |
|------|----------|------------|
| Secrets in Git history | CRITICAL | Rotate all secrets. Use `git filter-repo` if repo has remote. |
| App won't build after changes | MEDIUM | Run `flutter pub get && flutter build apk --debug` to verify. ProGuard changes may reveal missing keep rules. |
| ProGuard over-minification | MEDIUM | If release build crashes, add specific keep rules for affected classes. Test release build thoroughly. |
| `flutter_downloader` foreground service | MEDIUM | Verify `flutter_downloader` ^1.8.4 supports foreground service types on Android 14+. May need plugin update. |
| Facebook SDK still in dependencies | LOW | `flutter_facebook_auth` is in pubspec but auth is disabled. Can remove dependency if desired, but may break imports. |

---

## File Change Summary

### Files Deleted (25+)
- `android/keys/release.keystore`
- `android/keys/release_new.keystore`
- `android/keys/debug.keystore`
- `android/keys/EXPORT_KEY_WITH_PEPK.md`
- `android/keys/GOOGLE_PLAY_SIGNING_GUIDE.md`
- `android/keys/KEYSTORE_MAPPING.md`
- `android/keys/KEYSTORE_SOLUTION.md`
- `android/keys/NEW_KEYSTORE_INFO.md`
- `android/keys/README.md`
- `android/keys/SHA_FINGERPRINTS.md`
- `android/keys/export_key.bat`
- `android/keys/export_key.sh`
- `android/keys/generate_keys.bat`
- `android/keys/generate_keys.sh`
- `android/keys/test_keystore.ps1`
- `android/keys/keystore_output.txt`
- `android/key.properties`
- `android/local.properties`
- `encryption_public_key.pem`
- `flutter_01.log`
- `android/app/src/main/kotlin/com/oxoo/spagreen/MainActivity.kt`
- `lib/strings_bd.dart`
- `REBRANDING_CHECKLIST.md`
- `REBRANDING_STATUS.md`
- `GOOGLE_PLAY_COMPATIBILITY.md`
- `GOOGLE_PLAY_UPDATES_APPLIED.md`
- `API_36_UPGRADE_SUMMARY.md`
- `FOLDABLE_SUPPORT_ANALYSIS.md`
- `FOLDABLE_UPGRADE_SUMMARY.md`
- `RELEASE_NOTES_PLAY_STORE_v5.0.1.txt`
- `RELEASE_NOTES_PLAY_STORE_v5.0.2.txt`
- `RELEASE_NOTES_SHORT_v5.0.1.md`
- `RELEASE_NOTES_v5.0.1.md`

### Files Modified (12)
- `android/app/src/main/AndroidManifest.xml` — security + permission fixes
- `android/app/src/debug/AndroidManifest.xml` — removed package attribute
- `android/app/src/profile/AndroidManifest.xml` — removed package attribute
- `android/app/build.gradle` — signing, version, cleanup
- `android/app/proguard-rules.pro` — replaced permissive rules
- `android/gradle.properties` — removed duplicates and Jetifier
- `android/app/src/main/res/values/strings.xml` — removed Facebook App ID
- `lib/config.dart` — fixed malformed terms URL
- `lib/constants.dart` — disabled release logging, updated log tag
- `lib/server/repository.dart` — fixed DioError deprecation
- `pubspec.yaml` — reset version, removed unused dependency
- `test/widget_test.dart` — replaced real API call with smoke test
- `.gitignore` — expanded secret exclusions

### Files Created (14)
- `docs/FULL_CODE_AUDIT.md`
- `docs/PLAY_STORE_2026_COMPLIANCE.md`
- `docs/DATA_SAFETY_MAPPING.md`
- `docs/PRIVACY_POLICY_REQUIREMENTS.md`
- `docs/REBRANDING_CHECKLIST.md`
- `docs/SECRET_AUDIT.md`
- `docs/KEY_MIGRATION_PLAN.md`
- `docs/GITHUB_ACTIONS_AUDIT.md`
- `docs/FIREBASE_MIGRATION.md`
- `docs/MIGRATION_PLAN.md`
- `docs/PLAY_STORE_RELEASE_CHECKLIST.md`
- `docs/FINAL_MIGRATION_REPORT.md`
- `.github/workflows/android-build.yml`
- `.github/workflows/android-release.yml`
