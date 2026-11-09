# ShopZen — Architecture

## 1. Architectural goals
- Native Android application.
- Offline-first with Room as the MVP source of truth.
- Clear separation of UI, domain rules and persistence.
- Testable business logic.
- Minimal infrastructure and dependencies.

## 2. Technology baseline
- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Repository pattern
- Room / SQLite
- DataStore for preferences
- Kotlin coroutines and Flow

Exact versions must be selected based on the actual Android Studio, AGP, Kotlin and SDK configuration. Verify compatibility before adding/upgrading dependencies.

## 3. Suggested package organization
Adapt to the existing project rather than moving files unnecessarily.

```text
com.barakwebsoft.shopzen/
├── core/
│   ├── common/
│   ├── designsystem/
│   ├── navigation/
│   └── util/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   ├── mapper/
│   ├── preferences/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   ├── usecase/
│   └── calculator/
├── feature/
│   ├── home/
│   ├── lists/
│   ├── items/
│   ├── market/
│   ├── budget/
│   ├── history/
│   ├── templates/
│   └── settings/
└── MainActivity.kt
```

Do not create every package empty up front. Add structure when a real feature requires it.

## 4. Data flow
Compose screen → ViewModel → use case/domain service → repository interface → repository implementation → Room DAO → SQLite.

Room `Flow` observations propagate changes back through repository/ViewModel to Compose UI. UI composables must not call DAOs directly.

## 5. Data model proposal

### ShoppingList
- `id`: UUID string
- `name`: required
- `description`: optional
- `currencyCode`: ISO 4217 code
- `budgetMinor`: nullable Long
- `status`: ACTIVE / COMPLETED / ARCHIVED (final lifecycle must be consistently defined)
- `createdAt`, `updatedAt`: timestamps
- `completedAt`: nullable timestamp

### ShoppingItem
- `id`: UUID string
- `listId`: foreign key to ShoppingList
- `name`: required
- `quantity`: exact decimal representation; choose and document a canonical scale/serialization approach
- `unit`: standard or custom unit
- `categoryId`: nullable category reference
- `estimatedUnitPriceMinor`: nullable Long
- `actualUnitPriceMinor`: nullable Long
- `isPurchased`: Boolean
- `notes`: optional
- `sortOrder`: integer
- timestamps

### Category
- `id`, `name`, `icon`, optional color token, `isDefault`, `sortOrder`

### ShoppingHistory
- `id`
- `originalListId`
- `listName`
- `currencyCode`
- `budgetMinor`
- `totalSpentMinor`
- `purchasedCount`
- `unpricedPurchasedCount`
- `completedAt`

### HistoryItem
- `id`
- `historyId`
- `originalItemId` (optional)
- `itemName`
- `quantity`
- `unit`
- `categoryName` snapshot
- `actualUnitPriceMinor`
- `lineTotalMinor`
- `isPurchased`

History rows are snapshots and must not depend on mutable active-list rows.

## 6. Persistence rules
- Use foreign keys and indices for common list/item queries.
- Deleting a list must not delete its already-created history snapshots.
- Use a Room transaction to create history/header/item snapshots and update list lifecycle atomically.
- Duplicate operation creates new IDs, resets purchased state and clears actual prices.
- Implement explicit migrations and export Room schemas.
- Do not use destructive migration fallback in production.
- Use DataStore for non-relational app preferences.

## 7. Money and quantity rules
- Store money as `Long` minor units.
- Resolve fraction digits using the selected currency; do not assume every currency has two decimal places.
- Use exact decimal arithmetic for quantity × unit price and define rounding behavior.
- Never use `Float` or `Double` for money.
- Estimated and actual totals must remain distinct.
- Unknown actual prices must be counted and disclosed rather than silently counted as zero.
- No currency conversion in MVP.

## 8. Domain services/use cases
Implement only when needed:
- Create/update/delete/duplicate/observe lists.
- Add/update/delete/toggle shopping items.
- Calculate estimated and actual budget summaries.
- Archive list and create history snapshot.
- Observe history and load historical details.
- Format money for the selected currency.

## 9. State and navigation
- ViewModels expose immutable UI state.
- Use lifecycle-aware Flow collection.
- Navigation routes should pass stable IDs rather than entire database entities.
- Handle missing/deleted IDs with an explicit error or safe return path.
- Keep navigation consistent with the approved screen design.

## 10. Backend and network
No backend is required for MVP. Do not add a server, remote database, mandatory authentication or cloud sync without explicit product approval.

## 11. Monetization integration
Ads and billing are isolated behind small interfaces/services where this improves testability. Follow current official SDK guidance. Do not let ad SDK initialization block core offline functionality. Never place ads on protected screens. Billing entitlement must reflect Play purchase state and restoration behavior.

## 12. App Launch & Splash Screen Lifecycle
- **Official Splash Library:** Uses `androidx.core:core-splashscreen` (`androidx-core-splashscreen:1.0.1`).
- **Zero-Flicker Launch:** Splash screen initialization is performed via `installSplashScreen()` in `MainActivity.onCreate()` prior to `super.onCreate(savedInstanceState)`.
- **System Backporting:** Provides backward compatibility from Android 10 (API 29) through Android 15+. On Android 12+, the platform native window splash screen is configured via window attributes (`windowSplashScreenBackground`, `windowSplashScreenAnimatedIcon`).
- **Post-Splash Theme Swap:** `Theme.ShopZen.Starting` handles initial cold-start window drawing, then immediately delegates to `postSplashScreenTheme` (`@style/Theme.ShopZen`) as soon as Compose content begins drawing. No artificial delays or redundant splash Activities are introduced.
