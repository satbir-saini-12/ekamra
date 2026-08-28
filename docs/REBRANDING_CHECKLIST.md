# Rebranding Checklist — New Application Identity

**Audit Date:** 2026-08-26  
**Current Identity:** "AeroPlay" / `com.aero.play` (previously "OXOO" / `com.oxoo.spagreen`, "DesiDhamaka" / `com.desi.dhamaka`)  

---

## OWNER ACTION REQUIRED

Before implementation, the following must be provided:

- [ ] **New App Name** — e.g., "StreamX", "MediaHub", etc.
- [ ] **New Application ID** — e.g., `com.yourcompany.appname`
- [ ] **New API Server URL** — backend URL for the new app instance
- [ ] **New API Key** — API key for the new backend
- [ ] **New OneSignal App ID** — create new OneSignal app
- [ ] **New Firebase Project** — create new Firebase project, download new `google-services.json`
- [ ] **New Stripe Publishable Key** — if Stripe is used
- [ ] **New PayPal credentials** — if PayPal is used
- [ ] **New Razorpay Key ID** — if Razorpay is used
- [ ] **New Privacy Policy URL** — hosted privacy policy
- [ ] **New Terms & Conditions URL** — hosted terms page
- [ ] **New Support Email** — contact email for users
- [ ] **New Company/Copyright Name** — for settings screen
- [ ] **New App Icon** — 1024x1024px source icon
- [ ] **New Splash Screen Image** — if different from icon
- [ ] **New Logo** — for drawer header
- [ ] **New AdMob App ID** — if ads are re-enabled (currently disabled)

---

## All Locations Where Old Identity Appears

### 1. Application Name

| Location | Current Value | Action |
|----------|--------------|--------|
| `lib/strings.dart:2` | `appName = "AeroPlay"` | Replace with new name |
| `lib/strings.dart:42` | `"aeroplay.live"` in reset note text | Replace with new domain |
| `lib/strings.dart:43` | `"Watching live on $appName"` | Auto-updated when appName changes |
| `lib/strings.dart:123` | `infoAddress = "Info@aeroplay.live"` | Replace with new support email |
| `lib/strings.dart:125` | `copyrightText = "Copyright @ AEROPLAY"` | Replace with new copyright |
| `lib/screen/landing_screen.dart:84` | `"AeroPlay"` in `_widgetTitle` | Replace with new name |
| `android/app/src/main/AndroidManifest.xml:13` | `android:label="AeroPlay"` | Replace with new name |
| `android/app/src/main/res/values/strings.xml:3` | `<string name="app_name">AeroPlay</string>` | Replace with new name |
| `pubspec.yaml:2` | `description: A new Flutter application.` | Update description |
| `pubspec.yaml:18` | `version: 5.0.2+7` | Reset to `1.0.0+1` for new app |

### 2. Application ID / Package Name

| Location | Current Value | Action |
|----------|--------------|--------|
| `android/app/build.gradle:41` | `namespace "com.aero.play"` | Replace with new application ID |
| `android/app/build.gradle:69` | `applicationId "com.aero.play"` | Replace with new application ID |
| `android/app/src/main/AndroidManifest.xml:2` | `package="com.aero.play"` | Replace with new package |
| `android/app/src/debug/AndroidManifest.xml:2` | `package="com.aero.play"` | Replace with new package |
| `android/app/src/profile/AndroidManifest.xml:2` | `package="com.aero.play"` | Replace with new package |
| `android/app/src/main/kotlin/com/aero/play/MainActivity.kt:1` | `package com.aero.play` | Move file to new package dir, update declaration |
| `android/app/src/main/kotlin/com/aero/play/MainActivity.kt:15` | `CHANNEL = "com.aero.play/foldable"` | Update channel name |
| `android/app/src/main/kotlin/com/oxoo/spagreen/MainActivity.kt:1` | `package com.desi.dhamaka` | DELETE this orphaned file |
| `android/app/google-services.json` | `"package_name": "com.aero.play"` | Replace entire file with new Firebase config |

### 3. Dart Package Name

| Location | Current Value | Action |
|----------|--------------|--------|
| `pubspec.yaml:1` | `name: oxoo` | Change to new Dart package name |
| All `import 'package:oxoo/...'` statements | ~50+ files | Update all imports |

**Note:** Changing the Dart package name is a large refactor. All imports across the codebase use `package:oxoo/...`. This should be done with a controlled find-and-replace, not manually.

### 4. API Configuration

| Location | Current Value | Action |
|----------|--------------|--------|
| `lib/config.dart:3` | `apiServerUrl = "https://aeroplay.live/panel/rest-api/"` | Replace with new backend URL |
| `lib/config.dart:6` | `apiKey = "nzmuxnghyd8u9xgvk3so8ilm"` | Replace with new API key |
| `lib/config.dart:9` | `oneSignalID = "36fe0ed2-84e7-4ef0-8a1d-11c889ce993f"` | Replace with new OneSignal ID |
| `lib/config.dart:13` | Stripe publishable key (test key) | Replace with new Stripe key |
| `lib/config.dart:19` | `termsPolicyUrl` (malformed URL) | Replace with valid URL |
| `lib/network/api_configuration.dart:11-13` | Basic Auth `admin:1234` | Replace with new credentials or remove |

### 5. Firebase Configuration

| Location | Current Value | Action |
|----------|--------------|--------|
| `android/app/google-services.json` | Project: `aeroplayott`, package: `com.aero.play` | Replace with new Firebase project config |
| `lib/main.dart:20` | `Firebase.initializeApp()` | Uses default config from google-services.json — will work with new config |

### 6. OAuth / Login Configuration

| Location | Current Value | Action |
|----------|--------------|--------|
| `android/app/google-services.json` | OAuth client IDs for `com.aero.play` | New OAuth clients needed in Firebase/Google Cloud Console |
| `android/app/src/main/res/values/strings.xml:4` | `facebook_app_id = "442816973525148"` | Replace or remove (Facebook auth disabled) |
| `android/app/src/main/res/values/strings.xml:5` | `fb_login_protocol_scheme = "fb442816973525148"` | Replace or remove |
| `android/app/src/main/AndroidManifest.xml:21-22` | Facebook App ID meta-data | Remove if Facebook auth stays disabled |

### 7. AdMob Configuration

| Location | Current Value | Action |
|----------|--------------|--------|
| `android/app/src/main/AndroidManifest.xml:18-19` | `ca-app-pub-3940256099942544~3347511713` (test ID) | Remove from manifest (ads disabled) |

### 8. In-App Purchase Product ID

| Location | Current Value | Action |
|----------|--------------|--------|
| `lib/screen/landing_screen.dart:61` | `com.oxoo.flutter.allaccess` | Replace with new product ID (if IAP is re-enabled) |
| `lib/config.dart:28` | `publicKeyBase64` (Play Store public key) | Replace with new key or remove (IAP disabled) |

### 9. Signing Identity

| Location | Current Value | Action |
|----------|--------------|--------|
| `android/key.properties` | `aeroplay123` passwords, `aeroplay` alias | Generate NEW keystore, use GitHub Secrets |
| `android/keys/release.keystore` | Old release keystore | DELETE from repo, generate new |
| `android/keys/release_new.keystore` | Another keystore | DELETE from repo |
| `android/keys/debug.keystore` | Debug keystore | DELETE from repo, use default debug signing |
| `android/keys/*.md` | Docs with plaintext passwords | DELETE all |
| `android/keys/*.bat`, `*.sh`, `*.ps1` | Scripts with passwords | DELETE all |

### 10. Hive Box Name

| Location | Current Value | Action |
|----------|--------------|--------|
| `lib/service/authentication_service.dart:6` | `Hive.box<AuthUser>('oxooUser')` | Rename box (causes data migration issue for existing users — acceptable for new app) |
| `lib/main.dart:40` | `Hive.openBox<AuthUser>('oxooUser')` | Update to match |

### 11. Log Tag

| Location | Current Value | Action |
|----------|--------------|--------|
| `lib/constants.dart:1` | `kLOG_TAG = "[OXOO-Flutter]"` | Update to new app name |

### 12. Splash Screen

| Location | Current Value | Action |
|----------|--------------|--------|
| `assets/splash_image.png` | Old splash image (282KB) | Replace with new splash image |
| `assets/splash.png` | Old splash (1.4MB) | Replace or remove |
| `android/app/src/main/res/drawable/launch_background.xml` | References `@mipmap/launch_image` | Update if splash changes |
| `android/app/src/main/res/drawable/splash_gradient.xml` | Gold/orange gradient | Update colors to match new brand |

### 13. App Icons

| Location | Action |
|----------|--------|
| `assets/android_launcher_icon.jpg` | Replace with new icon source |
| `assets/logo.png` | Replace with new logo |
| `android/app/src/main/res/mipmap-*/ic_launcher.png` | Regenerate via `flutter_launcher_icons` |
| `android/app/src/main/res/mipmap-*/ic_launcher_round.png` | Regenerate |
| `android/app/src/main/res/mipmap-*/ic_launcher_foreground.png` | Regenerate |
| `android/app/src/main/res/mipmap-*/ic_launcher_background.png` | Regenerate |
| `android/app/src/main/res/mipmap-*/ic_launcher_monochrome.png` | Regenerate |
| `android/app/src/main/res/mipmap-*/launcher_icon.png` | Regenerate |
| `android/app/src/main/res/mipmap-*/launch_image.png` | Replace with new splash |
| `android/app/src/main/res/ic_launcher-web.png` | Replace |
| `android/app/src/main/res/playstore-icon.png` | Replace |

### 14. Bengali Strings (Commented Out)

| Location | Current Value | Action |
|----------|--------------|--------|
| `lib/strings_bd.dart` | Entirely commented out, contains "OXOO", "Spagreen", "aeroplay.live" | DELETE file (dead code) |

### 15. Existing Documentation (Root Level)

| Location | Action |
|----------|--------|
| `REBRANDING_CHECKLIST.md` | DELETE (stale, references old "DesiDhamaka" identity) |
| `REBRANDING_STATUS.md` | DELETE (stale, references "AeroPlay" migration) |
| `GOOGLE_PLAY_COMPATIBILITY.md` | DELETE (stale, references API 35) |
| `GOOGLE_PLAY_UPDATES_APPLIED.md` | DELETE (stale) |
| `API_36_UPGRADE_SUMMARY.md` | DELETE (stale) |
| `FOLDABLE_SUPPORT_ANALYSIS.md` | DELETE (stale) |
| `FOLDABLE_UPGRADE_SUMMARY.md` | DELETE (stale) |
| `RELEASE_NOTES_*.txt` / `RELEASE_NOTES_*.md` | DELETE (old release notes) |

---

## Migration Steps (Ordered)

1. **Get OWNER decisions** on new name, package ID, URLs, keys
2. **Change Dart package name** in `pubspec.yaml` and all imports
3. **Change Android application ID** in Gradle, manifests, Kotlin package
4. **Replace Firebase config** (`google-services.json`)
5. **Update API configuration** (`config.dart`, `api_configuration.dart`)
6. **Update app strings** (`strings.dart`, `strings.xml`, manifest label)
7. **Replace app icons** (source image → `flutter_launcher_icons`)
8. **Replace splash screen assets**
9. **Generate new signing keystore** (do NOT commit to repo)
10. **Remove old keystores and key docs** from repository
11. **Remove orphaned Kotlin file** (`com/oxoo/spagreen/MainActivity.kt`)
12. **Delete stale documentation** files
13. **Update Hive box name** (acceptable for new app — no existing users)
14. **Update log tag**
15. **Remove AdMob test ID** from manifest
16. **Remove Facebook App ID** from strings/manifest (if staying disabled)
17. **Clean and rebuild**
