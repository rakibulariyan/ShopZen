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

### Material 3 Design System Tokens: 2026-11-09 (Task SZ-004)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (24 actionable tasks executed)
- **Passed:** 8 unit tests (including `ThemeColorTokensTest` design token validation)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. Brand botanical theme tokens, typography scale, and light/dark palette verified.

### Navigation Shell & App UI State: 2026-11-09 (Task SZ-005)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (24 actionable tasks executed)
- **Passed:** 9 unit tests (including `ShopZenNavigationTest` top-level route validation)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. Navigation shell with BottomBar for Home, History, and Settings verified.

### Debug Build Verification: 2026-11-09 (Task SZ-006)
- **Command:** `.\gradlew.bat assembleDebug`
- **Result:** BUILD SUCCESSFUL (36 actionable tasks executed, output `app/build/outputs/apk/debug/app-debug.apk`)
- **Passed:** Debug APK produced with zero compilation or dexing errors.
- **Failures/Issues:** None. Phase 0 foundation complete.

### Room Entities & Schema Verification: 2026-11-09 (Task SZ-010)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 17 unit tests (including `RoomEntityStructureTest` verifying 5 entities, indices, foreign keys, cascade delete constraints, and exported schema `1.json`)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. Room 2.7.2 and KSP 2.3.9 compiler processing verified. Schema successfully exported to `app/schemas`.

### Room DAOs & Observable Queries Verification: 2026-11-09 (Task SZ-011)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 23 unit tests (including `RoomDaoStructureTest` verifying 4 DAOs, Flow observable queries, atomic `@Transaction` archive execution, KSP generated `_Impl` implementations, default categories seeding, and `ShopZenDatabase` DAO accessors)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. Room KSP compiler generated all DAO implementations (`ShoppingListDao_Impl`, `ShoppingItemDao_Impl`, `CategoryDao_Impl`, `HistoryDao_Impl`, `ShopZenDatabase_Impl`) and verified all SQL queries against the SQLite schema. Debug build verified via `.\gradlew.bat assembleDebug`.

### Repositories, Domain Models & Mappers Verification: 2026-11-09 (Task SZ-012)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 36 unit tests (including `MappersTest` with 6 tests verifying bidirectional domain/entity mapping and aggregate counts, and `RepositoriesTest` with 7 tests verifying list CRUD/duplication/reset logic, item sorting, category default seeding, and atomic history snapshot creation with exact minor unit calculations)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. All 4 domain repositories (`ShoppingListRepository`, `ShoppingItemRepository`, `CategoryRepository`, `HistoryRepository`), domain models, and mappers verified and integrated. Debug build verified via `.\gradlew.bat assembleDebug`.

### Database Migrations & Schema Integrity Verification: 2026-11-09 (Task SZ-013)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 43 unit tests (including `RoomMigrationTest` with 7 tests verifying schema version 1 format, identity hash, full table createSql templates, foreign key cascade constraints, indices, migration registry registration on `ShopZenDatabase`, prevention of destructive migrations in production, and simulated forward migration execution)
- **Failed:** 0
- **Skipped:** 0
- **Failures/Issues:** None. `Migrations.ALL_MIGRATIONS` registry created and hooked into Room database builder without destructive migration fallback. Debug build verified via `.\gradlew.bat assembleDebug`.

### DataStore Preferences Verification: 2026-11-09 (Task SZ-014)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 47 unit tests (including `UserPreferencesTest` with 4 tests verifying `ThemePreference` enum serialization, `AppPreferences` default values, locale-based currency fallback resolution, and `UserPreferencesRepositoryImpl` reactive Flow emissions and edit methods via DataStore preferences)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 18 executed, 19 up-to-date)
- **Failures/Issues:** None. AndroidX DataStore preferences 1.1.3 integrated and verified. Non-relational preferences (theme mode, default currency, haptic feedback, and completed items visibility) fully operational.

Do not pre-mark test cases as passed. This file defines expected tests, not evidence of execution.

## 6. Release gate
Do not declare release-ready until critical journeys, migrations, money edge cases, privacy disclosures, ads/billing restrictions, accessibility basics and release build have been verified.
