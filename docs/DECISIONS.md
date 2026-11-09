# ShopZen — Decision Log

Record durable product and technical decisions here. Use newest entries at the bottom. If a decision changes, mark the old entry as superseded and add a new one; do not silently rewrite history.

## ADR-001 — Native Android application
- **Status:** Accepted
- **Decision:** Build ShopZen as a native Android application using Kotlin and Jetpack Compose.
- **Reason:** The initial product is Android-first and requires a focused native shopping experience.
- **Consequences:** Android-specific UI, lifecycle and release workflows are used.

## ADR-002 — Offline-first MVP
- **Status:** Accepted
- **Decision:** Core MVP functionality works offline, with Room as the local source of truth.
- **Reason:** Shopping should remain usable in markets with unreliable connectivity.
- **Consequences:** No backend is required for the MVP. Cloud sync is deferred.

## ADR-003 — No mandatory authentication
- **Status:** Accepted
- **Decision:** Users can use core MVP features without creating an account.
- **Reason:** Reduce onboarding friction and avoid unnecessary backend/user-data complexity.
- **Consequences:** Cross-device sync and account-based backup are not MVP features.

## ADR-004 — Room / SQLite persistence
- **Status:** Accepted
- **Decision:** Use Room for lists, items, categories and shopping history.
- **Reason:** Relational data, observable queries, transactions and offline persistence are needed.
- **Consequences:** Schema migrations and database tests are required.

## ADR-005 — MVVM and repository boundaries
- **Status:** Accepted
- **Decision:** Use MVVM with repository boundaries and separated domain rules.
- **Reason:** Keep UI, business rules and persistence independently testable.
- **Consequences:** Avoid direct database access from composables.

## ADR-006 — Money stored in minor units
- **Status:** Accepted
- **Decision:** Store monetary amounts as integer minor currency units (`Long`), with currency-specific fraction digits.
- **Reason:** Avoid binary floating-point errors in financial calculations.
- **Consequences:** Exact conversion, formatting and rounding rules must be tested.

## ADR-007 — Currency per shopping list
- **Status:** Accepted
- **Decision:** Each shopping list stores its own ISO 4217 currency code.
- **Reason:** Users may create lists for different currencies.
- **Consequences:** No currency conversion or mixed-currency totals in MVP.

## ADR-008 — Templates are deferred from MVP
- **Status:** Accepted
- **Decision:** Built-in/custom templates are V1 candidates, not a required MVP feature.
- **Reason:** Prioritize the core create-list, shop, budget and history journey.
- **Consequences:** Do not show a dead Templates tab in MVP. Move templates into MVP only with explicit approval.

## ADR-009 — Ads excluded from critical shopping screens
- **Status:** Accepted
- **Decision:** No ads in Market Mode, Add/Edit Item, Budget screen or purchase screen.
- **Reason:** Avoid interruption and accidental interaction during core tasks.
- **Consequences:** Ad placement is limited to approved non-critical surfaces.

## ADR-010 — Remove Ads subscription
- **Status:** Proposed / implementation gated
- **Decision:** A yearly Remove Ads subscription is planned, with localized pricing fetched from Google Play.
- **Reason:** Monetization without hardcoding regional prices.
- **Consequences:** Verify current Play Billing policies and SDK behavior before implementation.

## ADR-011 — No MEMORY.md
- **Status:** Accepted
- **Decision:** Do not create or maintain `MEMORY.md`.
- **Reason:** Project state is tracked in `TASKS.md`; durable decisions in this file; change history in `CHANGELOG.md` if needed.
- **Consequences:** Agent must keep task status accurate and read project docs before each task.

## ADR-012 — Application identity
- **Status:** Accepted
- **Decision:** Display name is ShopZen and application ID is `com.barakwebsoft.shopzen`.
- **Reason:** Verified against existing `app/build.gradle.kts` (namespace and applicationId both set to `com.barakwebsoft.shopzen`).
- **Consequences:** Stable application identity confirmed across build configuration, PRD and codebase. Once published, changing application ID creates a different Play app.

## ADR-013 — Room 2.7.2 with KSP2 and Schema Export
- **Status:** Accepted
- **Decision:** Use Room `2.7.2` with KSP `2.2.10-2.0.2` and export schemas to `app/schemas/`.
- **Reason:** AGP 9.3 and Kotlin 2.2 require KSP2 analysis API; Room 2.7.2 resolves KSP2 void return signature compilation issues, provides full SQLite foreign key / index code generation, and exports immutable schema JSON artifacts for version verification.
- **Consequences:** Room annotations use `BINARY` retention. Schema integrity and migrations are verified against exported JSON schema files. Incremental compilation cache is stabilized via `kotlin.incremental=false` on Windows.

## ADR-014 — Data export and import deferred from MVP to V1
- **Status:** Accepted
- **Decision:** Data export and import (e.g. JSON/CSV backup, file picker export) are deferred to V1 per PRD Section 7 ("V1 candidates: Sharing and export improvements").
- **Reason:** Maintain strict MVP offline-first boundary, avoid unapproved file storage permissions or dead controls, and prioritize core list creation, market shopping, budgeting, and history snapshots.
- **Consequences:** Task SZ-044 is resolved as deferred per approved product scope. No placeholder or incomplete export/import UI is rendered in MVP.
