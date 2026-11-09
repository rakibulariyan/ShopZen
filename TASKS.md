# ShopZen — Development Tasks

> Status values: `NOT STARTED`, `IN PROGRESS`, `BLOCKED`, `DONE`. Mark `DONE` only after implementation and the stated verification have been completed. Update this file as tasks are verified.

## Phase 0 — Project inspection and foundation
- [x] SZ-001 Inspect existing Android Studio project, Gradle configuration, package IDs and Git status.
- [x] SZ-002 Confirm application ID/namespace, SDK configuration and compatible toolchain versions.
- [x] SZ-003 Establish package/module structure and dependency conventions without unnecessary modules.
- [x] SZ-004 Implement Material 3 theme tokens, typography and light/dark/system theme foundation.
- [x] SZ-005 Establish navigation shell and app-level UI state handling.
- [x] SZ-006 Verify debug build and document actual setup.

## Phase 1 — Local data foundation
- [x] SZ-010 Define Room entities, relationships, indices and schema export.
- [x] SZ-011 Implement DAOs and observable queries.
- [x] SZ-012 Implement repositories and domain models/mappers.
- [x] SZ-013 Implement database migrations and migration tests.
- [x] SZ-014 Implement DataStore preferences.
- [x] SZ-015 Test CRUD, foreign keys, transactions and persistence.

## Phase 2 — Shopping list workflows
- [x] SZ-020 Home list overview and empty state.
- [x] SZ-021 Create, edit, archive and delete shopping lists.
- [x] SZ-022 Duplicate list with new IDs and reset purchase state/prices.
- [x] SZ-023 List details and item ordering.
- [x] SZ-024 Add, edit and delete shopping items.
- [x] SZ-025 Categories and category selection.
- [x] SZ-026 Search and filtering.
- [x] SZ-027 Validate critical list/item flows with tests.

## Phase 3 — Market Mode and budget
- [x] SZ-030 Implement exact money and quantity calculation utilities.
- [x] SZ-031 Implement purchased/pending item state and undo.
- [x] SZ-032 Implement Market Mode, large touch targets and haptic feedback.
- [x] SZ-033 Implement actual price entry.
- [ ] SZ-034 Implement budget summary, over-budget warning and unpriced-item handling.
- [ ] SZ-035 Ensure Market Mode, Add/Edit Item and Budget screens contain no ads.
- [ ] SZ-036 Test rounding, zero-decimal currencies, large values and edge cases.

## Phase 4 — History and settings
- [ ] SZ-040 Implement atomic archive and immutable history snapshots.
- [ ] SZ-041 Implement history list and historical detail screens.
- [ ] SZ-042 Implement currency selection per list and locale-based default.
- [ ] SZ-043 Implement appearance and app preferences.
- [ ] SZ-044 Implement data export/import only if confirmed in the approved MVP scope.
- [ ] SZ-045 Test archive, snapshot integrity and deletion behavior.

## Phase 5 — Monetization
- [ ] SZ-050 Verify current Google Mobile Ads SDK and UMP integration guidance.
- [ ] SZ-051 Implement consent flow and privacy-safe ad initialization.
- [ ] SZ-052 Add ads only to approved placements; never in protected screens.
- [ ] SZ-053 Verify current Google Play Billing integration guidance.
- [ ] SZ-054 Implement yearly Remove Ads entitlement using Play-provided localized price.
- [ ] SZ-055 Handle pending, purchased, cancelled, expired, restored and unavailable states.
- [ ] SZ-056 Test billing/ad behavior; do not trust a local boolean as purchase proof.

## Phase 6 — Release readiness
- [ ] SZ-060 Complete critical journey and regression tests.
- [ ] SZ-061 Review accessibility, small screens, theme and performance.
- [ ] SZ-062 Review privacy disclosures, permissions and Data safety declarations.
- [ ] SZ-063 Verify current Play target API and store requirements.
- [ ] SZ-064 Configure release signing securely and verify AAB.
- [ ] SZ-065 Prepare store listing assets and release notes.
- [ ] SZ-066 Complete production checklist and release handover.

## Deferred / not MVP
- [ ] Future: widgets, voice item entry, templates, sharing/export enhancements.
- [ ] Future: family real-time sync and cloud backup.
- [ ] Future: barcode scanning and pantry/low-stock management.
- [ ] Explicitly avoid initially: AI assistant, grocery delivery marketplace, live retailer price comparison, retailer integrations and currency conversion.

## Current task
- **Current:** SZ-034 — implement budget summary, over-budget warning and unpriced-item handling.
- **Next:** SZ-035 — ensure Market Mode, Add/Edit Item and Budget screens contain no ads.
- **Blockers:** None. SZ-033 complete with 156 passing tests and clean debug APK build.
