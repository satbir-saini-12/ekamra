# Google Play 2026 Compliance Audit

**Audit Date:** 2026-08-26  
**Target:** New Google Play listing as a separate application  
**Target SDK:** 36 (Android 16)  

---

## 1. Privacy Policy

**Status: REQUIRED**

The app collects user data (email, phone, profile info, device identifiers via Firebase/OneSignal) and transmits it to external servers. Google Play requires a Privacy Policy URL for all apps that collect personal data.

- **Current state:** App has a terms & policies WebView screen, but the URL is malformed (`https://http://desidhamaka.in/panel/terms/`)
- **Action needed:** Create a valid Privacy Policy hosted at a public URL, update `Config.termsPolicyUrl` in `lib/config.dart`
- **In-app access:** Privacy Policy must be accessible from within the app (currently via Settings → Terms & Policies)

---

## 2. Data Safety Form

**Status: REQUIRED**

The app collects and transmits user data. The Play Console Data Safety form must be completed accurately based on `docs/DATA_SAFETY_MAPPING.md`.

Key data types collected:
- Email, name, phone number
- Profile photo
- Device identifier (via OneSignal)
- Purchase/subscription history
- Comments/user-generated content

---

## 3. Account Deletion

**Status: REQUIRED**

The app supports account creation (email signup, Google Sign-In, phone auth). Google Play requires an accessible account deletion mechanism.

- **Current state:** Account **deactivation** exists (`/deactivate_account` API endpoint in `lib/server/repository.dart:608-627`)
- **Assessment:** Deactivation may not satisfy Google Play's account **deletion** requirement. Deactivation typically disables an account without removing data. True deletion must remove user data from the backend.
- **Action needed:** 
  - Verify with backend whether `/deactivate_account` actually deletes user data or merely disables the account
  - If it only deactivates, implement a true deletion endpoint on the backend
  - Ensure the in-app flow is clearly labeled as "Delete Account" (not "Deactivate Account")
  - **OWNER ACTION REQUIRED:** Confirm backend deletion behavior

---

## 4. Content Rating

**Status: REQUIRED**

The app streams movies, TV series, and live content. Content rating must be completed via the Play Console Content Rating questionnaire (IARC).

- **Likely rating:** Based on streaming content with user comments, likely "Teen" or "Mature 17+"
- **Action needed:** Complete IARC questionnaire in Play Console
- **OWNER ACTION REQUIRED:** Content rating depends on actual content available on the platform

---

## 5. Advertising Declaration

**Status: NOT REQUIRED (currently)**

- AdMob SDK is commented out in `pubspec.yaml`
- AdMob test App ID exists in manifest (`ca-app-pub-3940256099942544~3347511713`) but no ads are shown
- **If ads are re-enabled:** Advertising declaration and Ads ID declaration will become REQUIRED
- **Action needed:** Remove AdMob test App ID from manifest to avoid confusion

---

## 6. Ads ID Declaration

**Status: NOT REQUIRED**

No advertising SDK is active. No advertising ID is used.

---

## 7. Permissions Declaration

**Status: REQUIRED**

Google Play requires declarations for certain sensitive permissions. Based on the current manifest:

| Permission | Declaration Required? | Notes |
|-----------|----------------------|-------|
| `INTERNET` | No | Standard permission |
| `READ_EXTERNAL_STORAGE` | ⚠️ Yes (if kept) | Should be replaced with scoped storage / media permissions |
| `WRITE_EXTERNAL_STORAGE` | ⚠️ Yes (if kept) | Should be removed; use scoped storage |
| `POST_NOTIFICATIONS` (needed) | Yes | Must be added for Android 13+ |
| `FOREGROUND_SERVICE` (needed) | Yes | Must be added for `flutter_downloader` |
| `FOREGROUND_SERVICE_DATA_SYNC` (needed) | Yes | Foreground service type for downloads |

**Action needed:** Remove deprecated storage permissions, add required Android 13/14 permissions, declare all sensitive permissions in Play Console.

---

## 8. Sensitive Permissions Declaration

**Status: NEEDS OWNER INFORMATION**

- **Camera:** `image_picker` may request camera access for profile photos. If camera is used, `CAMERA` permission declaration is required.
- **Microphone:** Not used
- **Location:** Not used
- **Contacts:** Not used
- **SMS:** Not used
- **Call logs:** Not used
- **Phone state:** Not used

**Current manifest does NOT declare CAMERA, but `image_picker` can request it at runtime.** The Play Console may flag this.

---

## 9. Financial Features Declaration

**Status: REQUIRED**

The app includes payment integrations:
- Stripe (credit card payments)
- PayPal
- Razorpay (India)
- In-App Purchase (Google Play Billing — currently disabled)

Google Play requires declaration of financial features. If payments are for digital content subscriptions within the app, Google Play Billing may be the required payment method (per Play Payments Policy).

- **Action needed:** Declare payment features in Play Console
- **OWNER ACTION REQUIRED:** Determine whether Stripe/PayPal/Razorpay are used for digital goods (requires Google Play Billing) or physical goods/services (external payments allowed)

---

## 10. Health Declaration

**Status: NOT REQUIRED**

No health-related features, no health data collection.

---

## 11. News Declaration

**Status: NOT REQUIRED**

The app is an OTT streaming platform, not a news application. The "LATEST NEWS" tab label in `landing_screen.dart` appears to be a mislabeled TV Series tab.

---

## 12. User-Generated Content Controls

**Status: REQUIRED**

The app allows users to:
- Post comments on videos
- Post replies to comments
- Upload profile photos

Google Play requires apps with UGC to have:
- **Content moderation policy:** Must be documented and enforced
- **In-app reporting/blocking:** Users should be able to report inappropriate content
- **User blocking:** Should allow blocking other users

- **Current state:** Comments can be added but no reporting/blocking mechanism is visible in the code
- **Action needed:** Implement content reporting and user blocking features
- **OWNER ACTION REQUIRED:** Define content moderation policy

---

## 13. Child Safety / Families Policy

**Status: NEEDS OWNER INFORMATION**

- The app streams movies/TV content which may include mature content
- If target audience includes children, Families Policy applies with strict requirements
- **OWNER ACTION REQUIRED:** Declare target audience in Play Console

---

## 14. Location Declaration

**Status: NOT REQUIRED**

No location permissions requested, no location data collected.

---

## 15. Background Location Declaration

**Status: NOT REQUIRED**

No background location access.

---

## 16. Photo/Video Access Declaration

**Status: REQUIRED**

The app uses `image_picker` for profile photo selection, which accesses the photo gallery.

- **Action needed:** Declare photo access in Play Console
- **Recommendation:** Migrate to Android Photo Picker (API 33+) to avoid requiring broad photo access permissions

---

## 17. Exact Alarm Declaration

**Status: NOT REQUIRED**

No `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` permission used.

---

## 18. Foreground Service Declarations

**Status: REQUIRED**

`flutter_downloader` uses a foreground service for download operations.

- **Action needed:**
  - Add `FOREGROUND_SERVICE` permission to manifest
  - Add `FOREGROUND_SERVICE_DATA_SYNC` permission (Android 14+)
  - Declare foreground service type in Play Console
  - Ensure `flutter_downloader` is updated to support foreground service types

---

## 19. Data Export / Portability

**Status: NOT REQUIRED (but recommended)**

Google Play doesn't explicitly require data export, but account deletion (which is required) should handle data removal.

---

## 20. Background Execution / Battery

**Status: LOW RISK**

- `flutter_downloader` uses foreground service for downloads — compliant
- No background location tracking
- No background media playback service (PiP is foreground)
- OneSignal push notifications are received via FCM — compliant

---

## Summary Table

| Requirement | Status | Action |
|-------------|--------|--------|
| Privacy Policy | REQUIRED | Create and host privacy policy, fix URL |
| Data Safety Form | REQUIRED | Complete using DATA_SAFETY_MAPPING.md |
| Account Deletion | REQUIRED | Verify/upgrade deactivation to true deletion |
| Content Rating | REQUIRED | Complete IARC questionnaire |
| Advertising Declaration | NOT REQUIRED | Remove AdMob test ID from manifest |
| Ads ID Declaration | NOT REQUIRED | No ads active |
| Permissions Declaration | REQUIRED | Declare storage, notification, foreground service |
| Sensitive Permissions | NEEDS OWNER INFO | Verify camera usage |
| Financial Features | REQUIRED | Declare payment methods, verify Play Billing compliance |
| Health Declaration | NOT REQUIRED | No health features |
| News Declaration | NOT REQUIRED | Not a news app |
| UGC Controls | REQUIRED | Add content reporting/blocking |
| Families Policy | NEEDS OWNER INFO | Determine target audience |
| Location Declaration | NOT REQUIRED | No location access |
| Background Location | NOT REQUIRED | No background location |
| Photo/Video Access | REQUIRED | Declare photo access for profile images |
| Exact Alarm | NOT REQUIRED | No exact alarms |
| Foreground Service | REQUIRED | Add permissions, declare in Play Console |
