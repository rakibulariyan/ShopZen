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

### SQLite CRUD, Foreign Keys, Transactions & Persistence Verification: 2026-11-09 (Task SZ-015)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 59 unit tests (including `RoomSqliteIntegrationTest` with 12 end-to-end integration tests executing against SQLite engine with Room schema v1 DDL: schema and index creation, foreign key orphan rejection, cascade delete on list items, set null on category delete, cascade delete on history items, CRUD on lists/items/categories, transaction atomicity and rollback on archive failure, and cross-session file persistence across database reopens)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 18 executed, 19 up-to-date)
- **Failures/Issues:** None. Phase 1 (Local data foundation) fully implemented and verified against native SQLite. Ready for Phase 2 (Shopping list workflows).

### Home List Overview & Empty State Verification: 2026-11-09 (Task SZ-020)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 70 unit tests (59 existing + 5 `CurrencyFormatterTest` testing exact integer minor unit formatting, decimal places, JPY zero-decimal, and fallback symbols + 6 `HomeViewModelTest` verifying initial loading, true empty state transitions, active lists flow emission, search filtering, and query clearing)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 18 executed, 19 up-to-date)
- **Failures/Issues:** None. `ShopZenApplication` container and DI wiring, `CurrencyFormatter`, `ShoppingListCard` with progress and financial pills, `HomeEmptyState`, and `HomeScreen` fully operational.

### Create, Edit, Archive & Delete Lists Verification: 2026-11-09 (Task SZ-021)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 84 unit tests (70 existing + 4 new `CurrencyFormatterTest` tests covering exact `parseToMinor`, `toMajorUnitsString` and comma/zero-decimal conversions + 3 new `HomeViewModelTest` tests covering archive without copy, archive with duplicate copy, and delete + 7 new `AddEditListViewModelTest` tests covering create mode with default currency from preferences, edit mode field loading, required name validation, invalid budget validation, exact minor unit conversion on creation and update, and error clearing on input)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 6 executed, 31 up-to-date)
- **Failures/Issues:** None. `ArchiveListDialog` (with headroom saved/over-budget context and duplicate copy option), `DeleteListDialog`, `CurrencySelectionDialog`, `AddEditListScreen`, `AddEditListViewModel`, `CREATE_LIST` and `EDIT_LIST` navigation, and 3-dots card overflow actions fully operational.

### Duplicate List Workflow & State Reset Verification: 2026-11-09 (Task SZ-022)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 90 unit/integration tests (84 existing + 1 new native SQLite integration test in `RoomSqliteIntegrationTest` verifying atomic duplication, foreign key linkage, status reset to `ACTIVE`, and complete purchase state (`is_purchased = 0`) & actual price (`actual_unit_price_minor = null`) reset in SQLite + 4 new repository tests in `RepositoriesTest` verifying blank name fallback to `${name} (Copy)`, empty list duplication, non-existent list rejection with `IllegalArgumentException`, and archived list reactivation + 1 new ViewModel test in `HomeViewModelTest` verifying `duplicateList` invocation with custom name)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date)
- **Failures/Issues:** None. `DuplicateListDialog`, Home card 3-dots menu "Duplicate list" trigger, and atomic duplication logic fully operational.

### List Details & Item Ordering Verification: 2026-11-09 (Task SZ-023)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks executed)
- **Passed:** 100 unit/integration tests (90 existing + 9 new unit tests in `ListDetailsViewModelTest` verifying list detail observation, pending/purchased counts calculation, purchase toggle with inverted state, category filtering, sort orders including `NAME_ASC`, `PRICE_DESC`, and `PENDING_FIRST`, and lifecycle operations `archiveList`, `deleteList`, and `duplicateList` + 1 domain model test)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date)
- **Failures/Issues:** None. `FinancialMetricsCard`, `ShoppingItemRow` (with 48dp check toggle and unpriced disclosure badge), `ListDetailsEmptyState`, category filter chips, sort order dropdown menu, and `ShopZenRoutes.LIST_DETAILS` navigation fully operational.

### Add, Edit & Delete Shopping Items Verification: 2026-11-09 (Task SZ-024)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 6 executed, 20 up-to-date)
- **Passed:** 110 unit/integration tests (100 existing + 10 new unit tests in `AddEditItemViewModelTest` verifying:
  - Form initialization for add mode loading list currency and categories
  - Form initialization for edit mode pre-populating item details, category, unit, prices, and notes
  - Custom unit detection when unit is outside standard presets
  - Field validation with name error clearance on typing
  - Quantity stepper increment and decrement with lower bound at 1
  - Exact minor-unit line total calculation (`quantity * estimatedUnitPriceMinor` using `BigDecimal`)
  - Item creation persisting to repository and marking `isItemSaved`
  - "Add & Create Another" saving item, resetting form name/quantity/price while retaining selected category and unit for rapid data entry
  - Item updating in edit mode persisting modifications and actual unit price
  - Permanent item deletion with confirmation dialog support)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date)
- **Failures/Issues:** None. Material 3 `AddEditItemScreen`, `DeleteItemDialog`, and `ShopZenRoutes.ADD_EDIT_ITEM` navigation fully integrated.

Do not pre-mark test cases as passed. This file defines expected tests, not evidence of execution.

## 6. Release gate
Do not declare release-ready until critical journeys, migrations, money edge cases, privacy disclosures, ads/billing restrictions, accessibility basics and release build have been verified.
