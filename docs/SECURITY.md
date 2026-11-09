# ShopZen — Security and Privacy Requirements

## 1. Security posture
MVP is local-first and does not require a user account or backend. Keep the attack surface small and avoid collecting data that is not needed.

## 2. Data minimization
- Store only information required for shopping lists, items, preferences and history.
- Do not collect names, contact details or account information unless a future approved feature requires it.
- Do not log shopping list contents, item notes, prices, purchase history or other user data.
- Avoid analytics events containing item names, notes or exact purchase details.

## 3. Local storage
- Use Room for structured app data and DataStore for preferences.
- Do not store secrets or payment credentials in the database.
- Use platform-appropriate app-private storage.
- Review backup behavior and Android backup/data extraction rules; document the chosen policy.
- Consider user expectations before excluding or including shopping data in device backups.
- Do not implement custom encryption without a defined threat model and appropriate key management.

## 4. Permissions
- Request only permissions required by an approved feature.
- Core MVP list functionality should not require contacts, location, camera, microphone or storage permissions.
- Add permissions only with documented purpose and user-facing explanation.

## 5. Third-party SDKs
- Ads, consent and billing SDKs are planned for monetization.
- Verify current official SDK documentation, data collection disclosures and Play policies before integration.
- Initialize ads only after required consent handling.
- Do not pass shopping-list content or personal notes to ad networks.
- Keep SDK failures from blocking offline list functionality.
- Maintain accurate privacy policy and Google Play Data safety disclosures.

## 6. Billing
- Never store card details or payment credentials.
- Use Google Play Billing for the planned subscription.
- Do not trust a local Boolean as proof of purchase.
- Query/verify purchase state using supported Play mechanisms.
- Handle pending, cancelled, expired, refunded and restored states.
- Protect signing keys and any future backend verification credentials.

## 7. Secrets and repository hygiene
- Never commit signing keystores, passwords, API secrets or private credentials.
- Keep local secrets outside version control.
- Ensure `.gitignore` excludes local signing/configuration files.
- Use placeholders only in example configuration.
- Review Git diff before committing.

## 8. Input and data integrity
- Validate list/item names, quantities, units and prices.
- Use exact numeric representations for money and quantities.
- Use Room transactions for multi-step state changes.
- Handle malformed, stale or missing identifiers safely.
- Do not expose stack traces or internal database details to users.

## 9. Release and compliance
- Review current Google Play target API requirements before release.
- Review privacy policy, Data safety, ads declaration, subscription terms and consent requirements.
- Keep app permissions and store declarations consistent with actual SDK behavior.
- Conduct a dependency and manifest review before production.

## 10. Incident handling
If a privacy/security issue is found:
1. Stop the affected release/task.
2. Identify data and users potentially affected.
3. Avoid destructive remediation without preserving necessary evidence.
4. Document root cause and corrective action.
5. Update this file and relevant tests.

## 11. Google Play Data Safety Declaration

| Data Category | Data Type | Collected by ShopZen? | Shared with Third Parties? | Purpose / Handling | Ephemeral / Stored | User Deletion Mechanism |
|---|---|---|---|---|---|---|
| **Personal Info** | Name, Email, Phone, Address, User IDs | **No** | **No** | N/A (No user account or login in MVP) | N/A | N/A |
| **Financial Info** | Credit/Debit Card, Bank Details, Purchase History | **No** | **No** | Payments are processed exclusively by Google Play Core service; ShopZen never sees or stores card numbers | N/A | Google Play account management |
| **App Activity** | Shopping lists, item names, prices, categories, budget metrics | **No** | **No** | Stored 100% locally on device in Room SQLite (`shopzen.db`). Never transmitted to any cloud or remote server | Stored locally | In-app list/history deletion or Android "Clear Storage" |
| **Device or other IDs** | Advertising ID (`AD_ID`), IP Address | Yes (by Google Mobile Ads SDK only) | Yes (by Google for AdMob) | Ad serving, frequency capping, fraud prevention, and regional consent management via UMP | Managed by Google SDK | Reset Advertising ID in Android OS Settings or revoke consent |
| **Diagnostics** | Crash logs, performance metrics | No (ShopZen has no analytics SDK) | SDK internal only | Handled by Google Play Services / AdMob SDK if an unhandled crash occurs | Managed by Google | N/A |

### Security Practices
- **Data encrypted in transit:** Yes (AdMob SDK and Play Billing communicate strictly via HTTPS/TLS).
- **Data deletion request:** Yes (All user shopping data is strictly local; user can delete any list, clear history, or wipe app data completely in Android Settings).
- **Committed to Play Families Policy:** Ads are family-safe and consent-gated; no children targeted.

## 12. Android Manifest Permissions Audit
- `android.permission.INTERNET`: Required for Google Mobile Ads banner requests and Google Play Billing product details retrieval.
- `android.permission.ACCESS_NETWORK_STATE`: Required to monitor network connectivity for UMP consent forms and Play Billing retry flows.
- `com.google.android.gms.permission.AD_ID`: Required by Google Mobile Ads SDK for Android 13+ advertising ID access (subject to UMP consent).
- `com.android.vending.BILLING`: Merged by Google Play Billing Library for in-app subscription processing.
- **Dangerous permissions requested:** **ZERO** (Verified by `PrivacyAndPermissionsSecurityTest`). No camera, contacts, microphone, coarse/fine location, or external storage permissions exist in the manifest.
