# Privacy Policy Requirements

**Audit Date:** 2026-08-26  

---

## Current State

- **Privacy Policy URL in app:** `Config.termsPolicyUrl = "https://http://desidhamaka.in/panel/terms/"` — **MALFORMED** (double protocol, old domain)
- **In-app access:** Settings → Terms & Policies (WebView loads the URL)
- **No dedicated privacy policy** — only a general "terms & policies" page

---

## Required Privacy Policy Content

Based on code-level analysis, the privacy policy must disclose:

### 1. Data Collection and Use

The policy must clearly state that the app collects:

- **Email address** — for account registration, authentication, and password reset
- **Full name** — for user profile and display
- **Phone number** — for phone-based authentication (optional)
- **Profile photo** — for user profile display (optional)
- **Password** — for authentication (stored hashed on server)
- **Comments and replies** — user-generated content visible to other users
- **Subscription/purchase history** — for managing premium content access
- **Rental history** — for pay-per-view content access tracking
- **Device identifier** — collected by OneSignal for push notifications
- **Firebase instance ID** — collected by Firebase for authentication
- **IP address** — automatically collected by server during API communication

### 2. Data Sharing

The policy must disclose that data is shared with:

- **Backend API server** (currently `ekamraott.com` — will change with rebranding)
- **Firebase** (Google LLC) — for authentication services
- **OneSignal** — for push notification delivery
- **Google Sign-In** (Google LLC) — for authentication
- **Stripe** — for payment processing (if Stripe payments are used)
- **PayPal** — for payment processing (if PayPal payments are used)
- **Razorpay** — for payment processing (if Razorpay payments are used)

### 3. Data Encryption

- **In transit:** All API communication uses HTTPS
- **At rest (local):** Hive stores user data locally without encryption
- **At rest (server):** Depends on backend implementation — **OWNER ACTION REQUIRED** to confirm

### 4. Data Retention and Deletion

- **Account deletion:** App provides account deactivation (`/deactivate_account` endpoint)
- **Policy must state:** How long data is retained after account deletion
- **Policy must explain:** How users can request data deletion
- **OWNER ACTION REQUIRED:** Confirm actual data retention policy with backend team

### 5. Third-Party Services

The policy must list third-party services with links to their privacy policies:

- Firebase: https://firebase.google.com/support/privacy
- OneSignal: https://onesignal.com/privacy
- Google Sign-In: https://policies.google.com/privacy
- Stripe: https://stripe.com/privacy
- PayPal: https://www.paypal.com/legal/privacy-policy
- Razorpay: https://razorpay.com/privacy/

### 6. Children's Privacy

- The app is not directed at children under 13 (or applicable age in jurisdiction)
- The policy must state that the app is not intended for children
- **OWNER ACTION REQUIRED:** Confirm target audience age

### 7. User Rights

The policy should describe user rights regarding:
- Access to their data
- Correction of their data
- Deletion of their data
- Withdrawal of consent
- Data portability

### 8. Changes to Privacy Policy

- Must state how users will be notified of policy changes
- Must state effective date

### 9. Contact Information

- Must provide contact email or form for privacy inquiries
- **OWNER ACTION REQUIRED:** Provide contact email for privacy concerns

---

## Implementation Requirements

1. **Create a privacy policy document** hosted at a public URL
2. **Update `Config.termsPolicyUrl`** in `lib/config.dart` to point to the valid privacy policy URL
3. **Ensure in-app access** to the privacy policy from:
   - Settings screen (currently exists via Terms & Policies)
   - Registration screen (should link to privacy policy)
   - Login screen (should link to privacy policy)
4. **Add privacy policy URL to Play Console** listing

---

## Current Code References

- `lib/config.dart:18-19` — `termsPolicyUrl` (malformed URL, needs fix)
- `lib/screen/terms_polices.dart` — WebView that loads the terms/privacy URL
- `lib/screen/settings_screen.dart:200-212` — Settings link to Terms & Policies
- `lib/strings.dart:108` — "By tapping continue, you agree to Terms and Conditions and Privacy Policy of $appName"

---

## OWNER ACTION REQUIRED

The following information is needed to complete the privacy policy:

1. **Privacy Policy URL** — Where will the privacy policy be hosted?
2. **Contact email** — What email should users use for privacy inquiries?
3. **Data retention period** — How long is user data retained after account deletion?
4. **Backend data deletion confirmation** — Does `/deactivate_account` truly delete user data?
5. **Target audience** — Is the app intended for users under 18?
6. **Data processing location** — Where is the backend server located (for GDPR compliance)?
