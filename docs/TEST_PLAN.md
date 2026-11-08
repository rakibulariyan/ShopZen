# ShopZen — Test Plan

## 1. Testing objectives
Verify core offline shopping workflows, data integrity, money calculations, history snapshots, UI states and release behavior.

## 2. Test levels
- Unit tests: domain rules, validators, calculations and use cases.
- Database tests: Room DAOs, relations, transactions and migrations.
- Repository tests: mapping, error handling and source-of-truth behavior.
- Compose UI tests: critical user flows, accessibility semantics and state rendering.
- Manual/device tests: offline behavior, different screen sizes, theme and real device interaction.
- Release tests: billing, ads, consent, signing and Play compliance when monetization is integrated.

## 3. Critical functional cases

### Lists
- Create list with valid name.
- Reject blank/invalid name.
- Edit list name, currency and budget.
- Delete list after confirmation.
- Archive list.
- Duplicate list creates new IDs.
- Duplicated items reset purchase state and actual prices.
- List data survives process/app restart.

### Items
- Add, edit and delete item.
- Validate item name and quantity.
- Validate non-negative price values.
- Item belongs to correct list.
- Deleting or missing parent list is handled safely.
- Category deletion/reference behavior follows database constraints.
- Item ordering persists.

### Market Mode
- Toggle item to purchased.
- Toggle item back to pending.
- Undo accidental purchase toggle.
- Actual price can be entered and edited.
- Purchased and pending counts are correct.
- Works with no network connection.
- Touch targets and layout remain usable on small screens.
- No ads appear.

### Budget and money
- Estimated totals are separate from actual totals.
- Only applicable purchased items contribute to known actual spend.
- Unpriced purchased items are disclosed and counted.
- Over-budget warning is correct and non-blocking.
- Zero-decimal currency behavior is correct.
- Two-decimal and other fraction-digit currencies format correctly.
- Quantity multiplication and rounding follow documented rules.
- Large values do not overflow silently.
- No monetary calculations use Float/Double.

### History
- Archive creates history header and item snapshots atomically.
- Transaction failure rolls back partial snapshot creation.
- Snapshot remains unchanged after original list/item edits or deletion.
- Historical details display the original currency and values.
- Empty history state is correct.

### Preferences and UI
- Light, dark and system theme.
- Currency defaults from locale where possible and can be changed per list.
- Empty, loading, error and success states.
- Accessibility labels for icon-only controls.
- Small-screen and large-font behavior.

### Monetization (when implemented)
- Consent flow is correct for applicable regions.
- Ads are absent from protected screens.
- Ad loading failure does not break core functionality.
- Billing localized price is displayed from Play.
- Pending purchase is not treated as active entitlement.
- Restore/re-query purchase state works.
- Expired/cancelled entitlement restores ads according to current product rules.
- Test with Play license/test accounts and official test tracks.

## 4. Migration and integrity
- Test every Room migration from each supported prior schema version.
- Verify foreign keys and indices.
- Verify archive transaction atomicity.
- Never rely on destructive migration in production.

## 5. Test execution record
For each run, record:
- Date
- Task/commit
- Device/emulator and Android version where applicable
- Command or test suite
- Passed/failed/skipped counts
- Failure details and whether pre-existing, introduced or environment-related

### Baseline verification: 2026-11-09 (Task SZ-001 / SZ-002)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (24 actionable tasks executed)
- **Passed:** 1 unit test (`ExampleUnitTest.addition_isCorrect`)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. Existing unit test suite passes cleanly.

### Navigation and Architecture Foundation: 2026-11-09 (Task SZ-003)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (24 actionable tasks executed)
- **Passed:** 7 unit tests (including `ShopZenRoutesTest` route/destination validations)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. Added Navigation Compose, ViewModel Compose, and Material Icons Extended cleanly.

Do not pre-mark test cases as passed. This file defines expected tests, not evidence of execution.

## 6. Release gate
Do not declare release-ready until critical journeys, migrations, money edge cases, privacy disclosures, ads/billing restrictions, accessibility basics and release build have been verified.
