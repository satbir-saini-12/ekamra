# Full Code Audit — OXOO-Flutter OTT Streaming App

**Audit Date:** 2026-08-26  
**Project Path:** `E:\Ekamra\OXOO Wrkng\OXOO-Flutter`  
**Auditor:** Cascade AI Agent  

---

## 1. Project Architecture

### Application Framework
- **Framework:** Flutter (Dart)
- **App Type:** OTT (Over-The-Top) streaming application — movies, live TV, TV series, radio, events
- **Origin:** Based on "OXOO" template by Spagreen, previously rebranded to "DesiDhamaka", then to "EKAMRA IPTV"

### Language
- **Primary:** Dart (Flutter)
- **Secondary:** Kotlin (Android native — `MainActivity.kt`)
- **No Java source files** (only Kotlin)

### Module Structure
```
OXOO-Flutter/
├── lib/                    # Flutter/Dart source
│   ├── app.dart            # Root widget, providers, OneSignal init
│   ├── config.dart         # API URLs, keys, feature flags
│   ├── constants.dart      # Logging constants
│   ├── main.dart           # Entry point, Hive init, Firebase init
│   ├── strings.dart        # English UI strings (appName = "AeroPlay")
│   ├── strings_bd.dart     # Bengali strings (entirely commented out)
│   ├── bloc/               # BLoC pattern (auth, home, movie, etc.)
│   ├── data/               # Data repository layer
│   ├── models/             # Data models (Hive adapters, JSON serialization)
│   ├── network/            # API configuration (headers, base URL)
│   ├── screen/             # UI screens (auth, movie, TV, profile, settings, etc.)
│   ├── server/             # Repository pattern (API calls via Dio)
│   ├── service/            # Services (auth, payment, config, navigation, discovery)
│   ├── style/              # Theme definitions
│   ├── utils/              # Routes, validators, widgets
│   └── widgets/            # Reusable widgets (players, shimmer, etc.)
├── android/                # Android project
│   ├── app/
│   │   ├── build.gradle    # App-level Gradle config
│   │   ├── google-services.json  # Firebase config
│   │   ├── proguard-rules.pro
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── kotlin/com/aero/play/MainActivity.kt  # Active
│   │       ├── kotlin/com/oxoo/spagreen/MainActivity.kt  # ORPHANED (old package)
│   │       └── res/        # Android resources
│   ├── build.gradle        # Root Gradle config
│   ├── settings.gradle     # Flutter plugin loader
│   ├── gradle.properties   # Gradle + AndroidX config
│   ├── key.properties      # ⚠️ HARDCODED PASSWORDS
│   └── keys/               # ⚠️ KEYSTORES + DOCS WITH PASSWORDS COMMITTED
├── assets/                 # Flutter assets (icons, splash, images)
├── pubspec.yaml            # Flutter dependencies
├── pubspec.lock            # Locked dependency versions
└── test/                   # Tests (single widget test)
```

### Build System
- **Build Tool:** Gradle (via Flutter)
- **Android Gradle Plugin (AGP):** 8.5.1
- **Gradle Version:** 8.12 (via `gradle-wrapper.properties`)
- **Kotlin Version:** 2.1.0
- **JDK Requirement:** Java 17 (configured in `compileOptions`)
- **Flutter SDK:** >=3.6.1 <4.0.0

### SDK Configuration
| Setting | Value | Status |
|---------|-------|--------|
| `compileSdk` | 36 | ✅ Android 16 / API 36 |
| `targetSdkVersion` | 36 | ✅ Android 16 / API 36 |
| `minSdkVersion` | 23 | ✅ Android 6.0 (reasonable for streaming app) |
| `ndkVersion` | 28.0.12674087 | ✅ NDK r28 (16KB page size support) |
| `multiDexEnabled` | true | ✅ |

### Dependency Management
- **Flutter:** `pubspec.yaml` / `pubspec.lock`
- **Android:** Gradle dependencies in `app/build.gradle`
- **Repositories:** `google()`, `mavenCentral()`, `gradlePluginPortal()`

### Native Dependencies
- **NDK:** Declared but no native `.so` libraries in the project source
- **16KB Page Size:** `android.experimental.enable16KbPageSize=true` in `gradle.properties`
- **No JNI/C/C++ code** in the project

### Networking
- **HTTP Client:** Dio (`^5.7.0`) + `http` package (`^1.2.2`)
- **API Base URL:** `https://ekamraott.com/panel/rest-api/` (hardcoded in `lib/config.dart`)
- **API Versioning:** Appends `v130` to base URL
- **Authentication:** Basic Auth (`admin:1234` base64 encoded) + API Key header
- **⚠️ CRITICAL:** Basic Auth credentials hardcoded in `lib/network/api_configuration.dart`

### Database / Storage
- **Local Storage:** Hive (`^2.2.3`) with `hive_flutter` (`^1.1.0`)
- **Hive Boxes:**
  - `configBox` — Configuration model
  - `appConfigbox` — App configuration
  - `adsConfigbox` — Ads configuration
  - `paymentConfigbox` — Payment configuration
  - `oxooUser` — Auth user data
  - `appModeBox` — App mode (dark/light, subscription status)
- **Code Generation:** `hive_generator` + `build_runner` for adapters
- **No SQLite / Room usage**

### Authentication
- **Email/Password:** Via backend API (`/login`, `/signup`)
- **Firebase Auth:** `firebase_auth` (`^5.3.4`) — used for Google Sign-In, Phone Auth
- **Google Sign-In:** `google_sign_in` (`^7.0.0`) — enabled
- **Facebook Auth:** `flutter_facebook_auth` (`^7.1.2`) — disabled in config (`enableFacebookAuth = false`)
- **Apple Sign-In:** `sign_in_with_apple` (`^7.0.1`) — enabled for iOS
- **Phone Auth:** Firebase phone authentication — enabled

### Background Services
- **flutter_downloader** (`^1.8.4`): Downloads video content — uses foreground service
- **No WorkManager / JobScheduler direct usage**
- **No custom Android Services** declared in manifest

### Media Functionality
- **Video Players:**
  - `video_player` (`^2.4.7`) — base Flutter video player
  - `chewie` (`^1.3.5`) — video player UI wrapper
  - `flick_video_player` (`^0.9.0`) — alternative video player
  - `youtube_player_flutter` (`^9.1.1`) — YouTube embed playback
- **Picture-in-Picture:** Implemented in `MainActivity.kt` (Android 8.0+)
- **Wakelock:** `wakelock_plus` (`^1.2.8`) — keeps screen on during playback
- **Casting:** `multicast_dns` (`^0.3.2+7`) — mDNS service discovery for TV connection

### Push Notifications
- **OneSignal:** `onesignal_flutter` (`^5.2.8`) — initialized in `app.dart`
- **OneSignal App ID:** `36fe0ed2-84e7-4ef0-8a1d-11c889ce993f` (hardcoded in `config.dart`)
- **Firebase Cloud Messaging:** Not directly used (OneSignal handles FCM)
- **Notification permission:** `OneSignal.Notifications.requestPermission(true)` called at init

### Analytics
- **No dedicated analytics SDK** (Google Analytics, Firebase Analytics not in dependencies)
- **OneSignal** provides basic analytics as a side effect

### Advertisements
- **AdMob:** `google_mobile_ads` is **commented out** in `pubspec.yaml`
- **AdMob App ID:** `ca-app-pub-3940256099942544~3347511713` (test App ID) in manifest
- **Banner ads widget exists** (`lib/widgets/banner_ads.dart`) but likely non-functional
- **Ads config model** exists in Hive (from backend configuration)

### Third-Party SDKs — Complete Inventory

| SDK | Current Version | Purpose | Play Store/Privacy Impact |
|-----|----------------|---------|--------------------------|
| `firebase_core` | ^3.8.1 | Firebase initialization | Collects device info, app instance ID |
| `firebase_auth` | ^5.3.4 | Authentication | Collects email, phone, user ID |
| `google_sign_in` | ^7.0.0 | Google OAuth login | Collects email, profile info |
| `flutter_facebook_auth` | ^7.1.2 | Facebook login (disabled) | Collects email, profile info |
| `sign_in_with_apple` | ^7.0.1 | Apple Sign-In (iOS) | Collects email, user ID |
| `onesignal_flutter` | ^5.2.8 | Push notifications | Collects device ID, push token |
| `dio` | ^5.7.0 | HTTP client | Transmits all API data |
| `http` | ^1.2.2 | HTTP client (secondary) | Transmits API data |
| `hive` / `hive_flutter` | ^2.2.3 / ^1.1.0 | Local storage | Stores user data locally |
| `flutter_downloader` | ^1.8.4 | File downloads | Foreground service, storage access |
| `webview_flutter` | ^4.9.0 | WebView (terms, Stripe) | Loads external web content |
| `image_picker` | ^1.1.2 | Profile image selection | Accesses camera/gallery |
| `permission_handler` | ^12.0.0+1 | Runtime permissions | Manages permission requests |
| `flutter_stripe` | ^11.3.0 | Stripe payments | Payment data |
| `flutter_paypal` | ^0.2.1 | PayPal payments | Payment data |
| `razorpay_flutter` | ^1.3.4 | Razorpay payments (India) | Payment data |
| `in_app_purchase` | ^3.2.3 | Google Play billing | Purchase data |
| `in_app_purchase_storekit` | ^0.4.2 | iOS in-app purchases | Purchase data |
| `package_info_plus` | ^8.1.1 | App version info | Reads package info |
| `in_app_review` | ^2.0.10 | App review prompt | No data collection |
| `share_plus` | ^11.0.0 | Share functionality | No data collection |
| `url_launcher` | ^6.1.6 | Open external URLs | No data collection |
| `carousel_slider` | ^5.0.0 | Image carousel | No data collection |
| `shimmer` | ^3.0.0 | Loading placeholders | No data collection |
| `flutter_svg` | ^2.0.16 | SVG rendering | No data collection |
| `flutter_spinkit` | ^5.1.0 | Loading spinners | No data collection |
| `flutter_pagewise` | ^2.0.4 | Pagination | No data collection |
| `flutter_bloc` | ^9.1.1 | State management | No data collection |
| `provider` | ^6.0.04 | DI / state | No data collection |
| `get_it` | ^8.0.3 | Service locator | No data collection |
| `equatable` | ^2.0.5 | Value equality | No data collection |
| `built_value` | ^8.4.1 | Immutable models | No data collection |
| `pinput` | ^5.0.0 | OTP input | No data collection |
| `country_code_picker` | ^3.1.0 | Country selection | No data collection |
| `cupertino_icons` | ^1.0.5 | iOS-style icons | No data collection |
| `wakelock_plus` | ^1.2.8 | Screen wakelock | No data collection |
| `visibility_detector` | ^0.4.0+2 | Widget visibility | No data collection |
| `modal_bottom_sheet` | ^3.0.0 | Bottom sheets | No data collection |
| `intl` | ^0.20.1 | Internationalization | No data collection |
| `multicast_dns` | ^0.3.2+7 | mDNS discovery | Network discovery |
| `change_app_package_name` | ^1.1.0 | Dev tool (package rename) | Dev only |

### Payment Integrations
1. **Stripe** — `flutter_stripe` + direct API calls to `api.stripe.com`
2. **PayPal** — `flutter_paypal` package
3. **Razorpay** — `razorpay_flutter` (India-focused)
4. **Google Play In-App Purchase** — `in_app_purchase` (disabled: `inAppPurchaseActivated = false`)
5. **Offline Payment** — Screen exists (`offline_payment_screen.dart`)

---

## 2. Existing Features — Complete Inventory

| Feature | Status | Notes |
|---------|--------|-------|
| Home screen with content sections | ✅ Working | Movies, Live TV, Series, Radio, Events |
| Movies listing & details | ✅ Working | Paginated, genre/star filtering |
| Live TV streaming | ✅ Working | Channel categories, details page |
| TV Series streaming | ✅ Working | Episode-based, details page |
| Radio streaming | ✅ Working | Listed in features, radio channels |
| Events listing & details | ✅ Working | Live events with comments |
| Search | ✅ Working | Search across content types |
| User registration (email) | ✅ Working | Via backend API |
| User login (email) | ✅ Working | Via backend API |
| Google Sign-In | ✅ Working | Firebase Auth + Google |
| Facebook Sign-In | ⚠️ Disabled | `enableFacebookAuth = false`, SDK still included |
| Apple Sign-In | ✅ Working (iOS) | `sign_in_with_apple` |
| Phone Auth (OTP) | ✅ Working | Firebase phone auth |
| Password reset | ✅ Working | Via backend API |
| Password set (after social login) | ✅ Working | Firebase auth users set password |
| User profile | ✅ Working | View/edit profile, upload photo |
| Profile image upload | ✅ Working | `image_picker` |
| Account deactivation | ✅ Working | Via backend API (`/deactivate_account`) |
| Favorites | ✅ Working | Add/remove/verify favorites |
| Comments on videos | ✅ Working | Add comments, view all comments |
| Replies to comments | ✅ Working | Add replies, view all replies |
| Content by genre | ✅ Working | Filter movies by genre |
| Content by country | ✅ Working | Filter content by country |
| Content by star/actor | ✅ Working | Filter by popular stars |
| Video player (multiple sources) | ✅ Working | MP4, YouTube, embed, HLS |
| Server selection | ✅ Working | Multiple streaming servers |
| Picture-in-Picture | ✅ Working | Android 8.0+, 16:9 aspect ratio |
| Download (offline) | ✅ Working | `flutter_downloader`, foreground service |
| Cast to TV | ⚠️ Partially | mDNS discovery, TV connection code |
| Subscription plans | ✅ Working | Backend-driven plans |
| Stripe payment | ✅ Working | Checkout sessions via API |
| PayPal payment | ✅ Working | PayPal checkout flow |
| Razorpay payment | ✅ Working | India-focused UPI/card payments |
| In-App Purchase (Play) | ⚠️ Disabled | `inAppPurchaseActivated = false` |
| Offline payment | ⚠️ Partially | Screen exists but minimal (405 bytes) |
| Rental / pay-per-view | ✅ Working | Rent history, rental access |
| Push notifications | ✅ Working | OneSignal integration |
| Dark mode | ✅ Working | Toggle in settings, default dark |
| Settings screen | ✅ Working | Notification toggle, contact, version, terms |
| Terms & Policies (WebView) | ✅ Working | Loads URL in WebView |
| Share app | ✅ Working | `share_plus` |
| In-app review | ✅ Working | `in_app_review` |
| Foldable support | ✅ Working | MethodChannel for fold state, PiP |
| Edge-to-edge display | ✅ Working | WindowCompat in MainActivity |
| Multi-window | ✅ Working | `resizeableActivity = true` |
| Bengali language | ❌ Obsolete | `strings_bd.dart` entirely commented out |
| AdMob banners | ❌ Disabled | SDK commented out, test App ID in manifest |
| Wakelock during playback | ✅ Working | `wakelock_plus` |

---

## 3. Technical Problems

### CRITICAL

| # | Problem | File(s) | Details |
|---|---------|---------|---------|
| C1 | **Hardcoded keystore passwords committed to Git** | `android/key.properties` | `storePassword=123456`, `keyPassword=123456` in plaintext |
| C2 | **Release keystore committed to Git** | `android/keys/release.keystore`, `android/keys/release_new.keystore` | Actual signing keys in repository |
| C3 | **Debug keystore committed to Git** | `android/keys/debug.keystore` | Debug signing key in repository |
| C4 | **All keystore documentation contains plaintext passwords** | `android/keys/*.md`, `android/keys/*.bat`, `android/keys/*.sh`, `android/keys/*.ps1` | Passwords visible in multiple files |
| C5 | **Hardcoded API Basic Auth credentials** | `lib/network/api_configuration.dart:11-13` | `username = 'admin'`, `password = '1234'` — base64 encoded |
| C6 | **Hardcoded API key** | `lib/config.dart:6` | `apiKey = "nzmuxnghyd8u9xgvk3so8ilm"` |
| C7 | **Hardcoded Stripe publishable key** | `lib/config.dart:13` | `pk_test_51M25oy...` (test key, but still hardcoded) |
| C8 | **Hardcoded OneSignal App ID** | `lib/config.dart:9` | `36fe0ed2-84e7-4ef0-8a1d-11c889ce993f` |
| C9 | **Hardcoded RSA public key** | `encryption_public_key.pem` | Public key committed (not secret, but needs review for new app) |
| C10 | **Hardcoded Play Store public key** | `lib/config.dart:28` | `publicKeyBase64` for in-app purchase verification |
| C11 | **`usesCleartextTraffic="true"`** | `AndroidManifest.xml:15` | Allows HTTP traffic, security risk |
| C12 | **Malformed terms URL** | `lib/config.dart:19` | `"https://http://desidhamaka.in/panel/terms/"` — double protocol, wrong domain |
| C13 | **ProGuard rules dangerously permissive** | `android/app/proguard-rules.pro` | `-keep class * { public private *; }` defeats minification |

### HIGH

| # | Problem | File(s) | Details |
|---|---------|---------|---------|
| H1 | **Deprecated storage permissions** | `AndroidManifest.xml:10-11` | `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` — deprecated for API 23+, should use scoped storage |
| H2 | **Orphaned MainActivity.kt with wrong package** | `android/app/src/main/kotlin/com/oxoo/spagreen/MainActivity.kt` | Package `com.desi.dhamaka` — old identity, causes confusion |
| H3 | **`maxSdkVersion="36"` in uses-sdk** | `AndroidManifest.xml:7` | Unusual restriction, could prevent future API updates |
| H4 | **Logging enabled in production** | `lib/constants.dart:2` | `kLOG_ENABLE = true` — prints debug info in release |
| H5 | **Test makes real API call** | `test/widget_test.dart:16` | Calls `Repository().passwordReset()` with real email — not a unit test |
| H6 | **No GitHub Actions workflows** | `.github/` | No CI/CD infrastructure exists |
| H7 | **`local.properties` may be committed** | `android/local.properties` | Contains machine-specific paths (`E:\\Android\\sdk`, `E:\\flutter`) |
| H8 | **`key.properties` in .gitignore but exists in repo** | `android/key.properties` | File is gitignored but present on disk — may be tracked in Git history |
| H9 | **Facebook SDK included but disabled** | `pubspec.yaml:44`, `AndroidManifest.xml:21-22` | `flutter_facebook_auth` dependency + Facebook App ID in manifest, but `enableFacebookAuth = false` |
| H10 | **AdMob test App ID in manifest** | `AndroidManifest.xml:18-19` | `ca-app-pub-3940256099942544~3347511713` — Google's test ID, should be removed or replaced |
| H11 | **Old product ID reference** | `lib/screen/landing_screen.dart:61` | `com.oxoo.flutter.allaccess` — old package name in in-app purchase product ID |
| H12 | **WebView JavaScript interface** | `lib/screen/terms_polices.dart:37` | `Toaster` JavaScript channel — potential XSS if terms page is compromised |

### MEDIUM

| # | Problem | File(s) | Details |
|---|---------|---------|---------|
| M1 | **Duplicate gradle.properties entries** | `android/gradle.properties:1,9,10` | `android.useAndroidX=true` and `android.enableJetifier=true` appear twice |
| M2 | **`enableJetifier=true` still enabled** | `android/gradle.properties:2` | Unnecessary if no old support libraries remain; slows builds |
| M3 | **`strings_bd.dart` entirely commented out** | `lib/strings_bd.dart` | 151 lines of commented Bengali translations — dead code with old branding |
| M4 | **Old log tag** | `lib/constants.dart:1` | `kLOG_TAG = "[OXOO-Flutter]"` — old branding |
| M5 | **Hive box name uses old branding** | `lib/service/authentication_service.dart:6` | Box name `'oxooUser'` — old identity |
| M6 | **Dart package name still `oxoo`** | `pubspec.yaml:1` | `name: oxoo` — all imports use `package:oxoo/...` |
| M7 | **App description generic** | `pubspec.yaml:2` | `description: A new Flutter application.` |
| M8 | **No `POST_NOTIFICATIONS` permission** | `AndroidManifest.xml` | Android 13+ requires `POST_NOTIFICATIONS` for notifications |
| M9 | **No `FOREGROUND_SERVICE` permission** | `AndroidManifest.xml` | `flutter_downloader` uses foreground service; Android 14+ requires typed foreground service permission |
| M10 | **No `android:foregroundServiceType`** | `AndroidManifest.xml` | Android 14+ requires foreground service type declaration |
| M11 | **Version code hack in build.gradle** | `android/app/build.gradle:19-26` | Hardcoded version override logic — fragile, could cause duplicate versionCode |
| M12 | **DioError deprecated** | `lib/server/repository.dart:772` | Uses `DioError` instead of `DioException` ( dio ^5.x renamed) |

### LOW

| # | Problem | File(s) | Details |
|---|---------|---------|---------|
| L1 | **Unused `change_app_package_name` dependency** | `pubspec.yaml:27` | Dev tool, not needed in production |
| L2 | **Splash screen uses large image** | `android/app/src/main/res/mipmap-hdpi/launch_image.png` | 282KB splash image in mipmap — should be in drawable |
| L3 | **`flutter_01.log` in project root** | `flutter_01.log` | Log file should not be in repo |
| L4 | **Existing audit docs are stale** | Root `.md` files | `REBRANDING_CHECKLIST.md` references old "DesiDhamaka" values, `GOOGLE_PLAY_COMPATIBILITY.md` references API 35 |
| L5 | **`ic_launcher_round.xml` missing monochrome** | `mipmap-anydpi-v26/ic_launcher_round.xml` | Round icon doesn't include monochrome variant (regular icon does) |
| L6 | **No `android:largeHeap="true"`** | `AndroidManifest.xml` | Streaming app may benefit from large heap for video buffers |

---

## 4. Android Behavior Changes Audit (API 12–16)

### Android 12 (API 31)
- **PendingIntent mutability:** ⚠️ Verify `flutter_downloader` and other plugins use `FLAG_IMMUTABLE` or `FLAG_MUTABLE` — no direct PendingIntent usage in app code
- **Approximate location:** Not applicable (no location permission)
- **Bluetooth permissions:** Not applicable
- **Exported components:** ✅ `MainActivity` has `android:exported="true"` with intent filter

### Android 13 (API 33)
- **POST_NOTIFICATIONS permission:** ⚠️ MISSING — OneSignal requests notification permission at runtime, but the manifest doesn't declare `POST_NOTIFICATIONS`. Required for Android 13+.
- **Scoped storage / media permissions:** ⚠️ Still uses `READ_EXTERNAL_STORAGE`/`WRITE_EXTERNAL_STORAGE` — should use `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO` for API 33+
- **Ad ID permission:** Not applicable (AdMob disabled)

### Android 14 (API 34)
- **Foreground service types:** ⚠️ MISSING — `flutter_downloader` uses foreground service but no `foregroundServiceType` declared. Required for Android 14+.
- **Foreground service permission:** ⚠️ MISSING — `FOREGROUND_SERVICE` permission not declared
- **Exact alarms:** Not applicable (no `SCHEDULE_EXACT_ALARM` usage)
- **Implicit intents to exported components:** No implicit intents used in app code

### Android 15 (API 35)
- **Edge-to-edge enforcement:** ✅ Already handled in `MainActivity.kt` (`WindowCompat.setDecorFitsSystemWindows(window, false)`)
- **16KB page size:** ✅ `android.experimental.enable16KbPageSize=true` set, NDK r28 declared
- **Foreground service timeout:** Not applicable for download service (data sync type)
- **Privacy manifest:** Not yet required by Flutter (no iOS-style privacy manifest on Android)

### Android 16 (API 36)
- **`targetSdk = 36`:** ✅ Already set
- **`compileSdk = 36`:** ✅ Already set
- **Adaptive icons:** ✅ `mipmap-anydpi-v26` with adaptive icon XML exists
- **Monochrome icon:** ✅ Present in `ic_launcher.xml` (missing from `ic_launcher_round.xml`)
- **Predictive back gesture:** ✅ `android:enableOnBackInvokedCallback="true"` set
- **Photo picker:** Should use Android photo picker instead of `READ_EXTERNAL_STORAGE` for image selection — `image_picker` plugin may handle this

---

## 5. Security Audit

### Cleartext Traffic
- **Status:** ⚠️ `android:usesCleartextTraffic="true"` in manifest
- **Risk:** Allows HTTP traffic, potential MITM attacks
- **Recommendation:** Set to `false` or use network security config to whitelist specific domains

### WebView Security
- **JavaScript mode:** `JavaScriptMode.unrestricted` in terms WebView — required for functionality
- **JavaScript interface:** `Toaster` channel — low risk if terms page is trusted
- **Navigation delegate:** ✅ Present, blocks YouTube navigation, handles Stripe callbacks
- **Risk:** MEDIUM — if terms URL is compromised, JavaScript could execute

### SSL/TLS
- **Dio:** No custom SSL configuration — uses defaults (should be fine)
- **No certificate pinning** — not critical but recommended for API calls
- **Basic Auth over HTTPS:** API uses Basic Auth (`admin:1234`) over HTTPS — credentials are hardcoded

### Exported Components
- **MainActivity:** `android:exported="true"` — required for launcher activity ✅
- **DownloadedFileProvider:** `android:exported="false"` — correct ✅
- **No other exported components**

### FileProvider Configuration
- **Provider:** `vn.hunghd.flutterdownloader.DownloadedFileProvider` — from `flutter_downloader`
- **Authority:** `${applicationId}.flutter_downloader.provider` — uses applicationId, will change with rebranding ✅
- **File paths:** References `@xml/provider_paths` — not found in res/xml/ (may be provided by plugin)

### SharedPreferences / Hive
- **Hive:** Stores auth user data, config, app mode — not encrypted
- **Risk:** LOW — Hive data is in app-private storage, but user auth tokens are stored unencrypted

### Debugging in Release
- **`debuggable`:** Not explicitly set — defaults to `false` in release build type ✅
- **Logging:** `kLOG_ENABLE = true` in `constants.dart` — prints debug info even in release ⚠️

### Backup Behavior
- **`android:allowBackup`:** Not set — defaults to `true`
- **Risk:** MEDIUM — Hive data could be backed up to Google Drive, potentially exposing user data
- **Recommendation:** Set `android:allowBackup="false"` or configure backup rules

---

## 6. Release Build Configuration

### Current Release Config
```gradle
release {
    signingConfig signingConfigs.release  // or debug fallback
    minifyEnabled true
    shrinkResources true
    proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
}
```

### Issues
1. **ProGuard rules too permissive** — `-keep class * { public private *; }` keeps everything, defeating R8
2. **Fallback to debug signing** — if `key.properties` not found, release builds use debug keys
3. **No mapping file upload configured** — needed for crash symbolication
4. **No native debug symbols** — not applicable (no native code)

### Signing Configuration
- **Release keystore:** `android/keys/release.keystore` (committed to repo — CRITICAL)
- **Keystore passwords:** In `key.properties` (committed — CRITICAL)
- **Key alias:** `key0`
- **Debug keystore:** Custom debug keystore at `android/keys/debug.keystore` (unusual)

---

## 7. Dependency Compatibility with API 36

| Dependency | Version | API 36 Compatible | Notes |
|-----------|---------|-------------------|-------|
| Flutter SDK | >=3.6.1 | ✅ | Stable channel supports API 36 |
| AGP | 8.5.1 | ✅ | Supports compileSdk 36 |
| Gradle | 8.12 | ✅ | Compatible with AGP 8.5.1 |
| Kotlin | 2.1.0 | ✅ | Latest stable |
| firebase_core | ^3.8.1 | ✅ | Recent Firebase SDK |
| firebase_auth | ^5.3.4 | ✅ | Recent Firebase SDK |
| google_sign_in | ^7.0.0 | ✅ | Recent |
| dio | ^5.7.0 | ✅ | Pure Dart, no platform issues |
| hive | ^2.2.3 | ✅ | Pure Dart |
| flutter_downloader | ^1.8.4 | ⚠️ | Needs foreground service type for Android 14+ |
| webview_flutter | ^4.9.0 | ✅ | Recent |
| image_picker | ^1.1.2 | ✅ | Uses photo picker on newer APIs |
| permission_handler | ^12.0.0+1 | ✅ | Recent |
| onesignal_flutter | ^5.2.8 | ✅ | Recent |
| video_player | ^2.4.7 | ✅ | Uses ExoPlayer/Media3 under the hood |
| chewie | ^1.3.5 | ✅ | UI wrapper |
| flick_video_player | ^0.9.0 | ⚠️ | May have ExoPlayer dependencies — verify |
| youtube_player_flutter | ^9.1.1 | ✅ | WebView-based |
| flutter_stripe | ^11.3.0 | ✅ | Recent |
| in_app_purchase | ^3.2.3 | ✅ | Google Play Billing |
| share_plus | ^11.0.0 | ✅ | Recent |
| url_launcher | ^6.1.6 | ✅ | Recent |
| flutter_bloc | ^9.1.1 | ✅ | Pure Dart |
| provider | ^6.0.04 | ✅ | Pure Dart |

---

## 8. Summary

This is a Flutter-based OTT streaming application originally built on the "OXOO" template by Spagreen. It has been through two previous rebranding attempts (DesiDhamaka → AeroPlay) but retains significant old branding artifacts. The app is functionally complete with streaming, auth, payments, and social features.

**Top priorities:**
1. Remove all committed secrets and keystores
2. Fix hardcoded credentials in API configuration
3. Add missing Android 13/14 permissions (POST_NOTIFICATIONS, FOREGROUND_SERVICE)
4. Create new application identity
5. Set up GitHub Actions for reproducible builds
6. Fix ProGuard rules
7. Disable cleartext traffic
