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
