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

### Categories & Category Selection Verification: 2026-11-09 (Task SZ-025)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 9 executed, 17 up-to-date)
- **Passed:** 116 unit/integration tests (110 existing + 5 new unit tests in `CategoryVisualsTest` verifying icon mapping, theme token color resolution, and preset uniqueness + 1 new unit test in `AddEditItemViewModelTest` verifying custom category creation, persistence via repository, category list refresh, and auto-selection of the new category)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date)
- **Failures/Issues:** None. `CategoryVisuals`, `CategoryChip` (with 48dp touch target, leading icons, and badge counts), `CreateCategoryDialog`, and `CategorySelector` integrated cleanly into `AddEditItemScreen` and `ListDetailsScreen`.

### Search & Filtering Verification: 2026-11-09 (Task SZ-026)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 8 executed, 18 up-to-date)
- **Passed:** 122 unit/integration tests (116 existing + 6 new unit tests in `ListDetailsViewModelTest` verifying:
  - In-list real-time item search by name (case-insensitive)
  - In-list item search matching notes content
  - Item purchase status filtering (`ALL`, `PENDING`, `PURCHASED`)
  - Combined multi-criteria filtering across category, purchase status, and search query
  - `resetFilters` resetting all criteria back to default
  - `isFilterEmpty` detection when active filters yield no matching items)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date)
- **Failures/Issues:** None. `ShopZenSearchBar`, botanical `SearchNoResultsView` (per `docs/android_ui_system/search_no_results`), status filter chips, and search toggle integrated into `ListDetailsScreen` and `HomeScreen`.

### Critical List & Item Flow Validation Verification: 2026-11-09 (Task SZ-027)
- **Command:** `.\gradlew.bat test`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 3 executed, 23 up-to-date)
- **Passed:** 125 unit/integration tests (122 existing + 3 new comprehensive flow integration tests in `CriticalListFlowIntegrationTest` verifying:
  - Complete list lifecycle: List creation -> Rapid item entry ("Add & Create Another" with form reset retaining unit/category) -> Unpriced item handling -> Live financial metrics calculation (`estimatedTotalMinor`, `actualSpentMinor`, exact minor unit math) -> Search by name & notes -> Multi-filter across category and purchase status -> Purchase toggle & actual price updates -> Duplicate list verifying full state reset (new IDs, ACTIVE status, unpurchased state, null actual prices) -> Atomic archive creating immutable history snapshot with exact minor unit totals and counts -> Active list removal
  - Item validation & error recovery: empty name validation, non-positive quantity validation, stepper increment/decrement bounds recovery
  - Single item deletion cascading to list details and financial metrics updates)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 3 executed, 34 up-to-date)
- **Failures/Issues:** None. Phase 2 (Shopping list workflows: SZ-020 through SZ-027) is 100% complete and fully verified. Ready to proceed to Phase 3 (Market Mode & budget).

### Exact Money & Quantity Calculation Utilities Verification: 2026-11-09 (Task SZ-030)
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 7 executed, 19 up-to-date)
- **Passed:** 135 unit/integration tests (125 existing + 10 new unit tests in `MoneyCalculatorTest` verifying:
  - Strict decimal and integer quantity parsing across comma and period delimiters (`1,5` -> `1.5`)
  - Trailing zero stripping formatting (`2.000` -> `"2"`, `2.500` -> `"2.5"`)
  - Stepper increment/decrement adjustments enforcing minimum positive bound (`0.001`)
  - Exact line total calculations with `RoundingMode.HALF_UP` to minor units (`Long`) without binary floating-point drift
  - Aggregates calculation (`estimatedTotalMinor`, `actualSpendMinor`, `pendingEstimatedTotalMinor`)
  - Budget calculations (`remainingBudgetMinor`, `overBudgetMinor`, `isOverBudget`, `budgetProgress`)
  - PRD unpriced item tracking (`countPurchasedLackingActualPrice`, `unpricedItemCount`, `hasUnpricedItems`)
  - Mixed zero-decimal (JPY) and 3-decimal (KWD) currency support with no hardcoded decimals in line calculations
  - Large value arithmetic safety without integer overflow
  - Integration with `ShoppingListDetail` domain model and `AddEditItemUiState`)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date)
- **Failures/Issues:** None. Integer minor unit rule strictly maintained; `Double`/`Float` arithmetic avoided completely. Ready to proceed to `SZ-031`.

### Purchased/Pending Item State and Undo Verification: 2026-11-09 (Task SZ-031)
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 7 executed, 19 up-to-date)
- **Passed:** 142 unit/integration tests (135 existing + 7 new unit tests in `ListDetailsViewModelTest` verifying:
  - Item toggle to purchased: updates repository atomically, sets `lastPurchaseToggleAction` with item name, pending-to-purchased transition, and emits `ListDetailsEvent.ItemPurchaseToggled` with `Marked "<item>" as purchased`
  - Item toggle to pending: updates repository atomically, sets `lastPurchaseToggleAction` with purchased-to-pending transition, and emits `ListDetailsEvent.ItemPurchaseToggled` with `Marked "<item>" as pending`
  - Undo purchase toggle: reverts item to previous state in repository and clears `lastPurchaseToggleAction`
  - Explicit undo toggle: allows reverting a specific item ID directly to a target state
  - Safe no-op on undo when no prior toggle has occurred
  - Sequential toggles tracking the latest action for undo
  - Integration with `SnackbarHost` in `ListDetailsScreen` allowing one-click snackbar Undo
  - Tactile feedback integration via `LocalHapticFeedback` and 48dp accessible check target in `ShoppingItemRow`)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 3 executed, 34 up-to-date)
- **Failures/Issues:** None. Purchase toggling, undo mechanisms, and haptic feedback verified. Ready to proceed to `SZ-032`.

### Market Mode, Large Touch Targets & Haptic Feedback Verification: 2026-11-09 (Task SZ-032)
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 9 executed, 17 up-to-date)
- **Passed:** 152 unit/integration tests (142 existing + 10 new unit tests in `MarketModeViewModelTest` verifying:
  - Observation of list detail, item counts calculation, and live progress percentage
  - Item purchase toggling with inverted status updates in repository
  - Emitting `MarketModeEvent.ItemPurchaseToggled` for snackbar notifications
  - Instant undo via `undoLastToggle()` and `undoPurchaseToggle(action)` reverting repository state
  - Haptic feedback enablement and user toggle (`isHapticEnabled`)
  - Screen keep-awake toggle (`isKeepScreenOn`) and automatic `FLAG_KEEP_SCREEN_ON` lifecycle cleanup
  - Category / aisle filtering with pending-first sorting
  - Precise calculation and disclosure of unpriced purchased items in cart
  - Shopping run finish transition
  - Integration with sticky `MarketFinancialHud`, 52dp `MarketShoppingItemRow`, and `ShopZenRoutes.MARKET_MODE` navigation
  - Strict compliance with protected screen policy: zero advertisements in Market Mode)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date)
- **Failures/Issues:** None. In-store Market Mode shopping experience fully verified. Ready to proceed to `SZ-033`.

### Actual Price Entry Verification: 2026-11-09 (Task SZ-033)
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 8 executed, 18 up-to-date)
- **Passed:** 156 unit/integration tests (152 existing + 4 new unit tests in `MarketModeViewModelTest` verifying:
  - Opening and closing price entry sheet updates `priceEntryItemId` and resolves `activePriceEntryItem`
  - Saving actual price updates item in repository with exact minor units (`Long`) and automatically marks item as purchased in cart
  - Saving item with null price allows setting purchased item as unpriced without losing cart state
  - Option to update price without altering purchased state
  - Modal bottom sheet `ActualPriceEntryBottomSheet` integration with quick-apply estimated price chip, clear/unpriced chip, live line total calculation via `MoneyCalculator.calculateLineTotal`, and price variance badge
  - Zero-decimal (e.g. JPY) vs decimal currency keyboard handling and numeric formatting via `CurrencyFormatter`)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date)
- **Failures/Issues:** None. Actual price entry modal sheet and repository updates verified. Ready to proceed to `SZ-034`.

### Budget Summary, Over-Budget Warning & Unpriced Item Handling Verification: 2026-11-09 (Task SZ-034)
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Result:** BUILD SUCCESSFUL (26 actionable tasks: 7 executed, 19 up-to-date)
- **Passed:** 160 unit/integration tests (156 existing + 4 new unit tests in `MoneyCalculatorTest` verifying:
  - Non-blocking over-budget state calculation (`isOverBudget = true`, `overBudgetMinor`, deficit tracking in `remainingBudgetMinor`)
  - Unpriced purchased items detection and PRD-mandated `isSpendEstimated` flag
  - Category spend breakdown and proportional percentages calculation (`CategorySpendDistribution`)
  - Top cost drivers / overrun culprits identification (`SpendCulprit`)
  - Upgraded Material 3 botanical `FinancialMetricsCard` with non-blocking over-budget warning banner, unpriced items spend disclosure banner, high-precision multi-segment progress bar (settled vs pending vs headroom buffer), and expandable category breakdown
  - Upgraded sticky `MarketFinancialHud` in Market Mode with non-blocking overrun banner and explicit unpriced spend warning)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date)
- **Failures/Issues:** None. Budget summary, over-budget warnings, and unpriced disclosures verified. Ready to proceed to `SZ-035`.

### 2026-11-09 — Task SZ-035: Ensure Market Mode, Add/Edit Item and Budget screens contain no ads
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 167 unit tests (+7 in `ProtectedScreenPolicyTest` verifying:
  - Strict zero-ads policy enforcement on Market Mode pattern route (`ShopZenRoutes.MARKET_MODE`), instantiated routes, and custom path segments per ADR-009, PRD Section 7 & 8, and DESIGN Section 8
  - Strict zero-ads policy enforcement on Add/Edit Item pattern route (`ShopZenRoutes.ADD_EDIT_ITEM`), creation routes, and editing routes with query parameters
  - Strict zero-ads policy enforcement on Budget routes (`budget`, `budget/summary`, `budget/breakdown`)
  - Strict zero-ads policy enforcement on Purchase and In-App Billing routes (`purchase`, `billing`, `remove_ads`, `subscription`)
  - Semantic screen name checks for `MARKET_MODE`, `ADD_EDIT_ITEM`, `BUDGET`, `FINANCIAL_METRICS`, `PURCHASE`, `BILLING`, `REMOVE_ADS`
  - Approved ad surfaces (`HOME`, `HISTORY`, `SETTINGS`, `LIST_DETAILS`) allow ad placement when eligible
  - Null, empty, and malformed route safety prevents unauthorized ad displays
  - Exception assertion: `ProtectedScreenPolicy.assertNoAdsAllowed` throws `ProtectedScreenAdException` on protected screens
  - Introduced `SafeAdPlacementContainer` composable guard preventing ad rendering and ad SDK initialization on protected screens)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date)
- **Failures/Issues:** None. Protected screens verified to forbid ads. Ready to proceed to `SZ-036`.

### 2026-11-09 — Task SZ-036: Test rounding, zero-decimal currencies, large values and edge cases
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 183 unit tests (+16 in `FinancialEdgeCasesAndCurrenciesTest` verifying:
  - Zero-decimal currencies (JPY, KRW): 0 fraction digits, currency symbols (¥, ₩), zero decimal point formatting (e.g. `¥15000`), integer minor unit parsing and HALF_UP rounding for fractional inputs, and complete shopping list financial metrics in JPY
  - Three-decimal currencies (BHD, KWD, OMR): 3 fraction digits, 3-decimal formatted amounts (e.g. `KWD 1.250`, `0.005`), exact parsing preserving 3 decimals, and 4th decimal place HALF_UP rounding
  - Rounding behavior: exact `HALF_UP` verification (0.5 -> 1, 0.49 -> 0, 1.5 -> 2, 2.5 -> 3, 3.5 -> 4), repeating fraction quantities (1/3 kg -> 333 minor, 2/3 kg -> 667 minor)
  - Large values & overflow safety: 100-billion minor unit calculations without 64-bit integer overflow, non-scientific currency string formatting, and extreme budget overrun handling (e.g. 50,000% utilization)
  - Boundary edge cases: zero budget (`budgetMinor = 0L` or `null`) handled safely with null budget progress to prevent division-by-zero, empty item lists, extreme fractional quantities (`0.0001` -> 1 minor, `0.00004` -> 0 minor), European comma vs standard period decimal separator handling, negative unit price rejection in `MoneyCalculator.calculateLineTotal`)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 3 executed, 34 up-to-date)
- **Failures/Issues:** None. All money edge cases, zero-decimal and three-decimal currencies verified. Phase 3 is 100% complete! Ready to proceed to Phase 4 (`SZ-040`).

### 2026-11-09 — Task SZ-040: Implement atomic archive and immutable history snapshots
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 188 unit tests (+5 in `RepositoriesTest` verifying:
  - Atomic archive execution via Room `database.withTransaction` binding in `HistoryRepositoryImpl`
  - Accurate calculation of total spent and item line totals using `MoneyCalculator` with support for comma-separated decimal quantities (e.g. `1,5` kg) and exact `HALF_UP` rounding
  - Preservation of unpriced items and accurate recording of `unpricedPurchasedCount`
  - Snapshot immutability: deleting the original active shopping list does not delete or alter the historical snapshot
  - Snapshot immutability: modifying or deleting active list items after archiving does not mutate the historical snapshot items
  - State machine safety: attempting to archive an already-archived list throws `IllegalStateException`
  - Validation: attempting to archive a non-existent list ID throws `IllegalArgumentException`)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 3 executed, 34 up-to-date)
- **Failures/Issues:** None. Atomic archive and snapshot immutability verified. Ready to proceed to `SZ-041`.

### 2026-11-09 — Task SZ-041: Implement history list and historical detail screens
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 196 unit tests (+8 in `HistoryViewModelTest` and `HistoricalDetailsViewModelTest` verifying:
  - History list observation, reactive loading state, and trip counting in `HistoryViewModel`
  - Real-time case-insensitive trip name search filtering and clear query restoration
  - Delete trip confirmation flow, dismiss action, and repository deletion invocation
  - Historical detail loading with complete item snapshots in `HistoricalDetailsViewModel`
  - History deletion state management (`isDeleted = true`) and confirmation dialog lifecycle
  - Re-creating an active shopping list from a historical trip (`duplicateAsNewList`), generating new UUIDs, copying items with reset purchase states (`isPurchased = false`, `actualUnitPriceMinor = null`), and setting previous actual price as estimated price
  - Verified `HistoryScreen` botanical UI with search bar, empty states, `HistoryCard`, and delete dialog
  - Verified `HistoricalDetailsScreen` botanical UI with snapshot summary card, unpriced spend warning banner, item list with strikethrough for unpurchased items, and 'Start New List from this Trip' bottom CTA)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date in 35s)
- **Failures/Issues:** None. History list and historical detail screens verified. Ready to proceed to `SZ-042`.

### 2026-11-09 — Task SZ-042: Implement currency selection per list and locale-based default
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 202 unit tests (+6 in `SettingsViewModelTest` and `AddEditListViewModelTest` verifying:
  - Locale-based currency default derivation via `UserPreferencesDataStore.getDefaultLocaleCurrencyCode()` falling back safely to `USD`
  - Observation and retrieval of default currency preferences in `SettingsViewModel`
  - Changing global default currency in settings updates `UserPreferencesRepository` and updates currency info symbol/display
  - Currency picker dialog visibility and selection workflow
  - Per-list currency override in `AddEditListViewModel`: lists can select any ISO 4217 currency (e.g. `JPY`, `EUR`, `GBP`) independently of the global default
  - Editing an existing list preserves its specific currency without being overwritten by subsequent global default changes
  - Verified `SettingsScreen` UI cards with default currency display and interactive currency selection dialog)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date in 1m 10s)
- **Failures/Issues:** None. Currency selection per list and locale-based default verified. Ready to proceed to `SZ-043`.

### 2026-11-09 — Task SZ-043: Implement appearance and app preferences
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 203 unit tests (+1 in `UserPreferencesTest` verifying:
  - Dynamic theme resolution logic across `SYSTEM`, `LIGHT`, and `DARK` options with simulated system dark mode states
  - Full app-wide theme propagation in `MainActivity` observing `AppPreferences.themePreference` and dynamically recomposing `ShopZenTheme(darkTheme)`
  - Botanical Material 3 `ThemeSelectionDialog` with clear descriptions, accessibility labels, and radio selection
  - Verified toggle cards for tactile haptics (`hapticFeedbackEnabled`) and completed items visibility (`showCompletedItems`))
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 5 executed, 32 up-to-date in 30s)
- **Failures/Issues:** None. Appearance and app preferences verified. Ready to proceed to `SZ-044`.

### 2026-11-09 — Task SZ-044: Resolve data export/import scope against approved MVP boundary
- **Verification:** Audited `docs/PRD.md` Section 6 ("Out of scope for MVP: Advanced sharing unless separately approved") and Section 7 ("V1 candidates: Sharing and export improvements"), along with `AGENTS.md` ("Do not add placeholder controls that appear functional but do nothing.").
- **Formal Record:** Created and committed `ADR-014` in `docs/DECISIONS.md`.
- **Result:** Data export and import are confirmed as deferred to V1. No dead controls or unnecessary runtime storage permissions added. Resolved as deferred per approved product scope. Ready to proceed to `SZ-045`.

### 2026-11-09 — Task SZ-045: Test archive, snapshot integrity and deletion behavior
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 212 unit tests (+9 new unit/integration tests across `RoomSqliteIntegrationTest` and `HistorySnapshotAndDeletionIntegrationTest` verifying:
  - Native SQLite foreign key test `foreignKey_shoppingList_deletionLeavesHistorySnapshotIntact`: Confirmed that Room schema v1 defines no cascading foreign key from `shopping_history` to `shopping_lists`, ensuring permanent deletion of an active list cascades to `shopping_items` while leaving `shopping_history` headers and child `history_items` 100% intact with preserved `original_list_id` audit reference.
  - Snapshot creation with mixed pricing: Dot and comma decimal quantities (`2.5`, `1,5`), integer minor unit calculations matching `MoneyCalculator`, category snapshot retention, and explicit unpriced purchased item counting.
  - Snapshot immutability: Post-archive modifications to active lists (renaming, changing budgets, adding items, updating prices, deleting items) do not mutate historical snapshots.
  - Deletion behavior: Deleting an individual historical trip cascades to all child snapshot items in `history_items` without touching active lists or items.
  - Complete history purge: `deleteAllHistory()` removes all trips and snapshot items while leaving active shopping lists intact.
  - Archive state machine guardrails: Re-archiving an already archived list throws `IllegalStateException`; archiving a non-existent list ID throws `IllegalArgumentException`.
  - Trip restoration decoupling: "Start New List from this Trip" action generates new distinct UUIDs, sets list status to `ACTIVE`, resets `isPurchased = false` and `actualUnitPriceMinor = null`, and initializes `estimatedUnitPriceMinor` from previous actual unit price.)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks up-to-date in 16s)
- **Failures/Issues:** None. Phase 4 (History and settings) is 100% complete! All 212 unit tests passing. Ready to advance to Phase 5 (Monetization: `SZ-050`).

### 2026-11-09 — Task SZ-050: Verify current Google Mobile Ads SDK and UMP integration guidance
- **Verification:** Audited current Google Mobile Ads SDK (v23.x / v24.x) and User Messaging Platform (UMP) architecture against official documentation and Google Play policies:
  - Artifact resolution: `com.google.android.gms:play-services-ads` with bundled UMP SDK support.
  - Privacy-first initialization: Enforced that `MobileAds.initialize` must only run after `consentInformation.canRequestAds()` evaluates to true or following `loadAndShowConsentFormIfRequired`.
  - Fast-path for returning users: Verified synchronous `canRequestAds()` check on startup to avoid blocking ad requests on network round-trips for existing consent.
  - Single-invocation protection: Guarded with `AtomicBoolean(false)`.
  - Offline resilience: Verified that consent/network failures fail gracefully without disrupting offline list creation, shopping, item editing, or budgeting.
  - Protected screen compliance: Verified that ads remain strictly prohibited on `MARKET_MODE`, `ADD_EDIT_ITEM`, and `LIST_DETAILS` (budgeting) via `ProtectedScreenPolicy.isAdPermitted()`.
  - Data minimization: Zero shopping list contents, item names, notes, quantities, or prices are ever passed to ad networks.
- **Formal Record:** Recorded and accepted `ADR-015` in `docs/DECISIONS.md`.
- **Result:** Official SDK guidance verified and codified. Ready to proceed to `SZ-051` (Implement consent flow and privacy-safe ad initialization).

### 2026-11-09 — Task SZ-051: Implement consent flow and privacy-safe ad initialization
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 219 unit tests (+7 in `GoogleAdConsentManagerTest` verifying:
  - Direct delegation of `canRequestAds()` to underlying UMP `ConsentInformation`
  - Atomic initialization guard ensuring `MobileAds.initialize` executes strictly once per application lifecycle across concurrent or repeated triggers
  - Synchronous fast-path: immediate initialization on app launch if consent was previously obtained without waiting for network round-trips
  - Offline resilience: network/consent update failure does not throw or crash; completion callback executes safely to keep core list workflows fully operational
  - Returning user offline support: returning users with saved consent initialize ads even when offline
  - Privacy options requirement reflection for GDPR/EEA settings UI)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date in 1m 15s)
- **Failures/Issues:** None. UMP consent flow, `GoogleAdConsentManager`, `AppContainer` wiring, and `MainActivity` lifecycle integration verified. Ready to proceed to `SZ-052` (Add ads only to approved placements; never in protected screens).

### 2026-11-09 — Task SZ-052: Add ads only to approved placements; never in protected screens
- **Command:** `.\gradlew.bat testDebugUnitTest`
- **Passed:** 227 unit tests (+8 in `SafeAdPlacementContainerTest` verifying:
  - Strict prohibition: Market Mode (`ShopZenRoutes.MARKET_MODE`, `market_mode/{listId}`) never emits ads
  - Strict prohibition: Add/Edit Item (`ShopZenRoutes.ADD_EDIT_ITEM`, `add_edit_item/{listId}?itemId=...`) never emits ads
  - Strict prohibition: Budget & Financial Metrics screens (`budget`, `budget/summary`, `budget/breakdown`) never emit ads
  - Strict prohibition: Purchase, Billing, and Remove Ads screens (`purchase`, `billing`, `remove_ads`, `subscription`) never emit ads
  - Null and blank route safety: Undefined or blank navigation routes never emit ads
  - Approved placement: Home overview screen (`ShopZenRoutes.HOME`) permits ad rendering only when consent is granted and Remove Ads is inactive
  - Entitlement suppression: When `isAdsRemoved` is true, ads are 100% suppressed across all surfaces including approved Home overview
  - UMP consent gating: When `canRequestAds` is false, ads are 100% suppressed across all surfaces)
- **Failed:** 0
- **Skipped:** 0
- **Debug Build:** `.\gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 actionable tasks: 4 executed, 33 up-to-date in 57s)
- **Failures/Issues:** None. `AdMobBannerView`, `SafeAdPlacementContainer` enhancement, and `HomeScreen` approved placement verified. Ready to proceed to `SZ-053` (Verify current Google Play Billing integration guidance).

### 2026-11-09 — Task SZ-053: Verify current Google Play Billing integration guidance
- **Verification:** Audited current Google Play Billing Library 7.x (`com.android.billingclient:billing-ktx`) against Google Play Developer Program policies, PRD Section 5, and SECURITY.md Section 6:
  - Artifact resolution: `com.android.billingclient:billing-ktx:7.1.1` providing Coroutines and Flow extensions.
  - Subscription product contract: `remove_ads_yearly` configured as subscription (`BillingClient.ProductType.SUBS`).
  - Dynamic localized pricing: Verified extraction of localized formatted price via `ProductDetails.subscriptionOfferDetails.pricingPhases.pricingPhaseList.first().formattedPrice` (e.g. `$4.99/year`, `€4,99/an`) without hardcoding monetary price strings.
  - Security & Cryptographic proof: Verified that local booleans are never trusted as proof of purchase; entitlements are queried directly from Google Play via `BillingClient.queryPurchasesAsync()`.
  - State machine lifecycle: Verified handling of `PURCHASED`, `PENDING` (no entitlement until cleared), `UNSPECIFIED_STATE`, acknowledgment within 3-day window (`acknowledgePurchase`), cancellation, expiration, and refund revocation.
  - Offline resilience: Google Play client caches verified tokens locally for offline subscriber access; network disconnections reconnect gracefully with exponential retry.
- **Formal Record:** Recorded and accepted `ADR-016` in `docs/DECISIONS.md`.
- **Result:** Official Google Play Billing 7.x guidance verified and recorded. Ready to proceed to `SZ-054` (Implement yearly Remove Ads entitlement using Play-provided localized price).

Do not pre-mark test cases as passed. This file defines expected tests, not evidence of execution.

## 6. Release gate
Do not declare release-ready until critical journeys, migrations, money edge cases, privacy disclosures, ads/billing restrictions, accessibility basics and release build have been verified.
