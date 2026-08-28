# Firebase Migration Guide

**Audit Date:** 2026-08-26  

---

## Current Firebase Configuration

| Field | Current Value |
|-------|--------------|
| Project ID | `aeroplayott` |
| Project Number | `1020367860153` |
| Storage Bucket | `aeroplayott.firebasestorage.app` |
| Mobile SDK App ID | `1:1020367860153:android:bccf172aaea389d43b8668` |
| API Key | `AIzaSyBk****` (masked) |
| Package Name | `com.aero.play` |
| OAuth Client IDs | 4 clients (3 Android, 1 Web) |
| Certificate Hashes | 3 SHA-1 fingerprints registered |

**Config file:** `android/app/google-services.json`

---

## Firebase Services Used

| Service | Used? | Evidence |
|---------|-------|----------|
| Firebase Authentication | ✅ Yes | `firebase_auth` package, email/phone/Google auth |
| Firestore | ❌ No | No `cloud_firestore` dependency |
| Realtime Database | ❌ No | No `firebase_database` dependency |
| Cloud Storage | ❌ No | No `firebase_storage` dependency |
| Cloud Messaging (FCM) | ❌ Indirect | OneSignal handles FCM; no direct FCM usage |
| Crashlytics | ❌ No | No `firebase_crashlytics` dependency |
| Analytics | ❌ No | No `firebase_analytics` dependency |
| Remote Config | ❌ No | No `firebase_remote_config` dependency |
| App Check | ❌ No | No `firebase_app_check` dependency |

**Only Firebase Authentication is actively used.**

---

## Migration Steps

### Step 1: Create New Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project (e.g., `newapp-prod`)
3. Enable **Authentication** → Sign-in methods:
   - Email/Password
   - Phone
   - Google
   - (Apple — if iOS support needed)

### Step 2: Register Android App

1. In Firebase Console → Project Settings → Add app → Android
2. Enter new package name (e.g., `com.yourcompany.appname`)
3. Enter app nickname
4. Add SHA-1 certificate fingerprint:
   - **Debug key:** Run `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   - **Upload key:** Run `keytool -list -v -keystore upload-keystore.jks -alias upload -storepass [PASSWORD]`
5. Download `google-services.json`
6. Replace `android/app/google-services.json` with the new file

### Step 3: Configure Google Sign-In

1. In Firebase Console → Authentication → Sign-in method → Google
2. Enable Google Sign-In
3. Ensure the Web SDK configuration has the correct OAuth client
4. The new `google-services.json` will contain the updated OAuth client IDs

### Step 4: Configure Phone Auth

1. In Firebase Console → Authentication → Sign-in method → Phone
2. Enable Phone authentication
3. Configure phone number verification settings
4. Add test phone numbers if needed for development

### Step 5: Update OneSignal (if using FCM)

1. Create new OneSignal app at [onesignal.com](https://onesignal.com)
2. Configure OneSignal to use the new Firebase project's FCM credentials
3. Update `Config.oneSignalID` in `lib/config.dart` with new OneSignal App ID
4. In Firebase Console → Project Settings → Cloud Messaging → copy Server Key and Sender ID
5. Enter these in OneSignal app settings

### Step 6: Update App Code

1. Replace `android/app/google-services.json` with new file
2. Update `lib/config.dart` with new OneSignal App ID
3. No other Firebase configuration changes needed in Dart code (Firebase uses `google-services.json` automatically)

---

## SHA-1 Certificate Fingerprints

The new app will need new SHA-1 fingerprints registered in Firebase:

### Debug Certificate
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Upload Certificate (NEW keystore)
```bash
keytool -list -v -keystore upload-keystore.jks -alias upload -storepass [PASSWORD]
```

### Play App Signing Certificate
After enrolling in Play App Signing, Google Play will provide the app signing key certificate. Add this SHA-1 to Firebase as well.

---

## What Does NOT Need Migration

- **No Firestore data** to migrate
- **No Realtime Database** data to migrate
- **No Storage** files to migrate
- **No Crashlytics** configuration to migrate
- **No Analytics** configuration to migrate

Only **Authentication** users exist in Firebase. Since this is a **new** app with a **new** package ID, existing users from the old Firebase project will NOT carry over. Users will need to create new accounts.

**OWNER ACTION REQUIRED:** If user migration is needed, export users from old Firebase Auth and import to new Firebase Auth using the Firebase CLI or Admin SDK.

---

## OAuth Provider Console Changes

### Google Cloud Console
1. Create new OAuth 2.0 Client IDs for the new package name
2. Update API restrictions for the new Android app
3. Configure OAuth consent screen for the new app name

### Apple Developer Console (if iOS)
1. Configure Sign in with Apple for new Bundle ID
2. Update Firebase with Apple Services ID and Team ID

### Facebook Developer Console (if re-enabling)
1. Create new Facebook App or update existing
2. Configure Android settings with new package name and key hash
3. Update `strings.xml` with new Facebook App ID
