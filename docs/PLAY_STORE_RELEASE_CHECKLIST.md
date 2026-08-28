# Play Store Release Checklist

**Audit Date:** 2026-08-26  

---

## Store Listing Information

| Item | Status | Value / Action |
|------|--------|----------------|
| App title | **OWNER ACTION REQUIRED** | Provide new app name (max 30 characters) |
| Short description | **OWNER ACTION REQUIRED** | Provide short description (max 80 characters) |
| Full description | **OWNER ACTION REQUIRED** | Provide full description (max 4000 characters) |
| App category | **OWNER ACTION REQUIRED** | Likely "Entertainment" or "Video Players & Editors" |
| Developer email | **OWNER ACTION REQUIRED** | Provide support email |
| Developer website | **OWNER ACTION REQUIRED** | Provide website URL (if any) |
| Privacy Policy URL | **MISSING** | Must be created and hosted. Update `Config.termsPolicyUrl` |

---

## Graphics

| Item | Status | Dimensions / Action |
|------|--------|---------------------|
| App icon | **MISSING** | 512x512px PNG, 32-bit, no alpha |
| Feature graphic | **MISSING** | 1024x500px PNG/JPG |
| Phone screenshots | **MISSING** | Min 2, max 8. 16:9 or 9:16, min 320px, max 3840px |
| Tablet screenshots | **NOT APPLICABLE** | Optional — provide if tablet optimization is claimed |
| Promo video | **OPTIONAL** | YouTube URL |

---

## Content Rating

| Item | Status | Action |
|------|--------|--------|
| IARC questionnaire | **OWNER ACTION REQUIRED** | Complete content rating questionnaire in Play Console |
| Likely rating | **UNKNOWN** | Depends on content available on the platform |

---

## Data Safety

| Item | Status | Action |
|------|--------|--------|
| Data Safety form | **MISSING** | Complete using `docs/DATA_SAFETY_MAPPING.md` |
| Data types declared | **MISSING** | Email, name, phone, photos, purchase history, comments, device ID |
| Encryption declared | **MISSING** | Yes — all data encrypted in transit (HTTPS) |
| Data deletion declared | **MISSING** | Yes — account deactivation exists (verify true deletion) |

---

## Ads Declaration

| Item | Status | Action |
|------|--------|--------|
| Contains ads? | **NO** | AdMob is disabled. Remove test App ID from manifest. |
| Ads ID used? | **NO** | No advertising ID collected |

---

## App Access Instructions

| Item | Status | Action |
|------|--------|--------|
| App access instructions | **OWNER ACTION REQUIRED** | If app requires login, provide test credentials for Play review team |

---

## Target Audience

| Item | Status | Action |
|------|--------|--------|
| Target audience age | **OWNER ACTION REQUIRED** | Select target audience in Play Console |
| Families policy | **OWNER ACTION REQUIRED** | Determine if app targets children |

---

## News Declaration

| Item | Status | Action |
|------|--------|--------|
| Is this a news app? | **NO** | Not a news application |

---

## Account Deletion

| Item | Status | Action |
|------|--------|--------|
| Account creation supported? | **YES** | Email, Google, Phone auth |
| Account deletion mechanism? | **PARTIAL** | Deactivation exists, verify true deletion with backend |
| Deletion accessible in-app? | **YES** | Profile screen → Deactivate Account |
| Play Console declaration | **MISSING** | Declare account deletion URL/mechanism in Play Console |

---

## Permissions Declarations

| Permission | Declaration Required | Status |
|-----------|---------------------|--------|
| INTERNET | No | ✅ |
| POST_NOTIFICATIONS (to add) | Yes | **MISSING** — add permission, declare in Play Console |
| FOREGROUND_SERVICE (to add) | Yes | **MISSING** — add permission, declare in Play Console |
| FOREGROUND_SERVICE_DATA_SYNC (to add) | Yes | **MISSING** — add permission, declare in Play Console |
| READ_EXTERNAL_STORAGE (to remove) | Was required | Will be removed |
| WRITE_EXTERNAL_STORAGE (to remove) | Was required | Will be removed |

---

## Financial Features

| Item | Status | Action |
|------|--------|--------|
| Payment methods used | **YES** | Stripe, PayPal, Razorpay |
| Google Play Billing | **DISABLED** | `inAppPurchaseActivated = false` |
| Financial declaration | **OWNER ACTION REQUIRED** | Declare payment features in Play Console |
| Play Billing compliance | **OWNER ACTION REQUIRED** | If selling digital goods, must use Google Play Billing per Play policy |

---

## Government Apps

| Item | Status | Action |
|------|--------|--------|
| Is this a government app? | **NO** | N/A |

---

## Sensitive Permissions

| Permission | Status | Action |
|-----------|--------|--------|
| CAMERA | **NOT DECLARED** | `image_picker` may request camera. Declare if used. |
| ACCESS_FINE_LOCATION | **NOT USED** | N/A |
| RECORD_AUDIO | **NOT USED** | N/A |
| READ_CONTACTS | **NOT USED** | N/A |

---

## Release Artifact

| Item | Status | Action |
|------|--------|--------|
| Release AAB | **MISSING** | Must be built via GitHub Actions `android-release.yml` |
| Signing | **MISSING** | New upload keystore must be generated, enrolled in Play App Signing |
| versionCode | **CURRENT: 7** | Reset to 1 for new app |
| versionName | **CURRENT: 5.0.2** | Reset to 1.0.0 for new app |

---

## Pre-Launch Report

| Item | Status | Action |
|------|--------|--------|
| Pre-launch report | **PENDING** | Run after uploading AAB to Play Console internal testing track |

---

## Summary

| Category | Ready | Missing | Owner Action | N/A |
|----------|-------|---------|--------------|-----|
| Store listing | 0 | 3 | 4 | 0 |
| Graphics | 0 | 3 | 0 | 2 |
| Content rating | 0 | 0 | 1 | 0 |
| Data safety | 0 | 3 | 0 | 0 |
| Ads | 2 | 0 | 0 | 0 |
| App access | 0 | 0 | 1 | 0 |
| Target audience | 0 | 0 | 1 | 0 |
| Account deletion | 2 | 1 | 0 | 0 |
| Permissions | 1 | 3 | 0 | 2 |
| Financial | 1 | 0 | 2 | 0 |
| Release artifact | 0 | 3 | 0 | 0 |
| **Total** | **8** | **13** | **9** | **4** |
