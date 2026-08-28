# Migration Plan

**Audit Date:** 2026-08-26  
**Target:** Convert existing OXOO-Flutter OTT app into a new independently branded Android application for Google Play 2026  

---

## Phase A — Audit (COMPLETE)

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| A1 | Full codebase audit | HIGH | LOW | All | None | COMPLETE |
| A2 | Play Store compliance audit | HIGH | LOW | All | A1 | COMPLETE |
| A3 | Data safety mapping | HIGH | LOW | All | A1 | COMPLETE |
| A4 | Privacy policy requirements | HIGH | LOW | All | A1 | COMPLETE |
| A5 | Rebranding checklist | HIGH | LOW | All | A1 | COMPLETE |
| A6 | Secret audit | CRITICAL | HIGH | All | A1 | COMPLETE |
| A7 | Key migration plan | CRITICAL | HIGH | All | A6 | COMPLETE |
| A8 | GitHub Actions audit | HIGH | MEDIUM | .github/ | A1 | COMPLETE |
| A9 | Firebase migration guide | MEDIUM | MEDIUM | google-services.json | A1 | COMPLETE |

---

## Phase B — Build System Modernization

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| B1 | Remove duplicate gradle.properties entries | LOW | LOW | `android/gradle.properties` | None | TODO |
| B2 | Remove `enableJetifier` | LOW | LOW | `android/gradle.properties` | B1 | TODO |
| B3 | Fix version code override hack | MEDIUM | MEDIUM | `android/app/build.gradle` | None | TODO |
| B4 | Create GitHub Actions build workflow | HIGH | MEDIUM | `.github/workflows/android-build.yml` | A8 | TODO |
| B5 | Create GitHub Actions release workflow | HIGH | MEDIUM | `.github/workflows/android-release.yml` | B4 | TODO |

---

## Phase C — API 36 Migration (Already Complete)

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| C1 | Set compileSdk = 36 | HIGH | LOW | `android/app/build.gradle` | None | COMPLETE |
| C2 | Set targetSdk = 36 | HIGH | LOW | `android/app/build.gradle` | None | COMPLETE |
| C3 | Set NDK r28 for 16KB page size | HIGH | LOW | `android/app/build.gradle` | None | COMPLETE |
| C4 | Enable 16KB page size flag | MEDIUM | LOW | `android/gradle.properties` | None | COMPLETE |

---

## Phase D — Dependency Upgrades

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| D1 | Verify all dependencies API 36 compatible | MEDIUM | MEDIUM | `pubspec.yaml` | C1 | TODO |
| D2 | Remove unused `change_app_package_name` dep | LOW | LOW | `pubspec.yaml` | None | TODO |
| D3 | Remove `flutter_facebook_auth` if staying disabled | MEDIUM | LOW | `pubspec.yaml`, `AndroidManifest.xml` | None | TODO |
| D4 | Fix `DioError` → `DioException` deprecation | LOW | LOW | `lib/server/repository.dart` | None | TODO |

---

## Phase E — Policy/Security Fixes

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| E1 | Remove committed keystores from repo | CRITICAL | HIGH | `android/keys/*` | None | TODO |
| E2 | Remove `key.properties` with plaintext passwords | CRITICAL | HIGH | `android/key.properties` | None | TODO |
| E3 | Remove all keystore docs/scripts with passwords | CRITICAL | HIGH | `android/keys/*.md,*.bat,*.sh,*.ps1` | E1 | TODO |
| E4 | Remove `local.properties` from repo | HIGH | MEDIUM | `android/local.properties` | None | TODO |
| E5 | Remove `encryption_public_key.pem` | MEDIUM | LOW | `encryption_public_key.pem` | None | TODO |
| E6 | Remove `flutter_01.log` | LOW | LOW | `flutter_01.log` | None | TODO |
| E7 | Fix `usesCleartextTraffic="true"` → false | HIGH | MEDIUM | `AndroidManifest.xml` | None | TODO |
| E8 | Fix ProGuard rules (currently keeps everything) | HIGH | MEDIUM | `proguard-rules.pro` | None | TODO |
| E9 | Disable logging in release | MEDIUM | LOW | `lib/constants.dart` | None | TODO |
| E10 | Add `POST_NOTIFICATIONS` permission | HIGH | LOW | `AndroidManifest.xml` | None | TODO |
| E11 | Add `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` permissions | HIGH | LOW | `AndroidManifest.xml` | None | TODO |
| E12 | Remove deprecated `READ/WRITE_EXTERNAL_STORAGE` | HIGH | MEDIUM | `AndroidManifest.xml` | None | TODO |
| E13 | Remove `maxSdkVersion` from uses-sdk | MEDIUM | LOW | `AndroidManifest.xml` | None | TODO |
| E14 | Set `android:allowBackup="false"` | MEDIUM | LOW | `AndroidManifest.xml` | None | TODO |
| E15 | Remove AdMob test App ID from manifest | MEDIUM | LOW | `AndroidManifest.xml` | None | TODO |
| E16 | Remove Facebook App ID (if staying disabled) | MEDIUM | LOW | `strings.xml`, `AndroidManifest.xml` | D3 | TODO |
| E17 | Fix malformed terms URL | HIGH | LOW | `lib/config.dart` | None | TODO |
| E18 | Move hardcoded API credentials to build-time injection | HIGH | MEDIUM | `lib/config.dart`, `lib/network/api_configuration.dart` | None | TODO |

---

## Phase F — New Application Identity

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| F1 | Change Dart package name in pubspec.yaml | HIGH | HIGH | `pubspec.yaml`, all imports | OWNER: new name | BLOCKED |
| F2 | Change Android applicationId and namespace | HIGH | HIGH | `app/build.gradle`, manifests | F1 | BLOCKED |
| F3 | Move MainActivity.kt to new package directory | HIGH | MEDIUM | `kotlin/` | F2 | BLOCKED |
| F4 | Delete orphaned `com/oxoo/spagreen/MainActivity.kt` | MEDIUM | LOW | `kotlin/com/oxoo/spagreen/` | F2 | TODO |
| F5 | Replace `google-services.json` with new Firebase config | HIGH | MEDIUM | `google-services.json` | F2, OWNER: Firebase | BLOCKED |
| F6 | Update Hive box name from 'oxooUser' | LOW | LOW | `lib/service/authentication_service.dart`, `lib/main.dart` | F1 | TODO |
| F7 | Update log tag | LOW | LOW | `lib/constants.dart` | F1 | TODO |
| F8 | Update in-app purchase product ID | LOW | LOW | `lib/screen/landing_screen.dart` | F1 | TODO |
| F9 | Update MethodChannel name in MainActivity.kt | MEDIUM | LOW | `MainActivity.kt` | F2 | TODO |

---

## Phase G — Branding Replacement

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| G1 | Update app name in strings.dart | HIGH | LOW | `lib/strings.dart` | OWNER: new name | BLOCKED |
| G2 | Update app name in AndroidManifest.xml | HIGH | LOW | `AndroidManifest.xml` | G1 | BLOCKED |
| G3 | Update app name in strings.xml | HIGH | LOW | `strings.xml` | G1 | BLOCKED |
| G4 | Update app name in landing_screen.dart | HIGH | LOW | `lib/screen/landing_screen.dart` | G1 | BLOCKED |
| G5 | Update contact email and copyright | MEDIUM | LOW | `lib/strings.dart` | OWNER: new info | BLOCKED |
| G6 | Replace app icon source image | HIGH | LOW | `assets/android_launcher_icon.jpg` | OWNER: icon | BLOCKED |
| G7 | Regenerate launcher icons | HIGH | LOW | `res/mipmap-*/` | G6 | BLOCKED |
| G8 | Replace splash screen image | MEDIUM | LOW | `assets/splash_image.png`, `res/mipmap/launch_image.png` | OWNER: splash | BLOCKED |
| G9 | Replace logo image | MEDIUM | LOW | `assets/logo.png` | OWNER: logo | BLOCKED |
| G10 | Update splash gradient colors | LOW | LOW | `res/drawable/splash_gradient.xml` | G1 | BLOCKED |
| G11 | Delete stale documentation files | LOW | LOW | Root `.md` files | None | TODO |
| G12 | Delete `strings_bd.dart` (dead code) | LOW | LOW | `lib/strings_bd.dart` | None | TODO |
| G13 | Update pubspec.yaml description and version | MEDIUM | LOW | `pubspec.yaml` | G1 | BLOCKED |
| G14 | Update API server URL and key | HIGH | MEDIUM | `lib/config.dart` | OWNER: new backend | BLOCKED |
| G15 | Update OneSignal App ID | HIGH | MEDIUM | `lib/config.dart` | OWNER: new OneSignal | BLOCKED |
| G16 | Update Stripe publishable key | MEDIUM | MEDIUM | `lib/config.dart` | OWNER: new Stripe | BLOCKED |
| G17 | Update terms/privacy policy URL | HIGH | LOW | `lib/config.dart` | OWNER: new URL | BLOCKED |

---

## Phase H — Signing and Secret Migration

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| H1 | Generate new upload keystore (locally, not committed) | CRITICAL | HIGH | N/A (local) | None | TODO |
| H2 | Configure GitHub repository secrets | CRITICAL | HIGH | GitHub settings | H1 | TODO |
| H3 | Update signing config in build.gradle to use env vars | HIGH | MEDIUM | `app/build.gradle` | H1 | TODO |
| H4 | Remove custom debug signing config | MEDIUM | LOW | `app/build.gradle` | None | TODO |

---

## Phase I — GitHub Actions Build

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| I1 | Create android-build.yml workflow | HIGH | MEDIUM | `.github/workflows/android-build.yml` | B4 | TODO |
| I2 | Create android-release.yml workflow | HIGH | MEDIUM | `.github/workflows/android-release.yml` | B5, H2 | TODO |
| I3 | Verify build is reproducible on clean runner | HIGH | MEDIUM | N/A | I1, I2 | TODO |

---

## Phase J — Testing and Release Verification

| ID | Task | Severity | Risk | Files | Dependency | Status |
| -- | ---- | -------- | ---- | ----- | ---------- | ------ |
| J1 | Fix unit test (currently makes real API call) | MEDIUM | LOW | `test/widget_test.dart` | None | TODO |
| J2 | Verify clean install on API 36 emulator | HIGH | MEDIUM | N/A | All phases | TODO |
| J3 | Verify app startup and navigation | HIGH | MEDIUM | N/A | J2 | TODO |
| J4 | Verify authentication flows | HIGH | MEDIUM | N/A | J2 | TODO |
| J5 | Verify streaming/media playback | HIGH | MEDIUM | N/A | J2 | TODO |
| J6 | Verify push notifications | MEDIUM | MEDIUM | N/A | J2 | TODO |
| J7 | Verify payment flows | MEDIUM | HIGH | N/A | J2 | TODO |
| J8 | Verify signed AAB builds in CI | HIGH | MEDIUM | N/A | I2 | TODO |
| J9 | Create Play Store release checklist | HIGH | LOW | `docs/PLAY_STORE_RELEASE_CHECKLIST.md` | All | TODO |
| J10 | Create final migration report | MEDIUM | LOW | `docs/FINAL_MIGRATION_REPORT.md` | All | TODO |

---

## Blocked Items Summary

The following items are **BLOCKED** pending owner decisions:

1. **New app name** — blocks F1, G1-G4, G13
2. **New application ID / package name** — blocks F1-F5, F8
3. **New Firebase project** — blocks F5
4. **New API backend URL and key** — blocks G14
5. **New OneSignal App ID** — blocks G15
6. **New Stripe key** — blocks G16
7. **New privacy policy URL** — blocks G17
8. **New app icon** — blocks G6-G7
9. **New splash/logo images** — blocks G8-G9
10. **New contact email and copyright** — blocks G5

**Items that can proceed without owner input:** All Phase B, C, D, E items (build system, security fixes, dependency cleanup, policy fixes).
