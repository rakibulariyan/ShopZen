# ShopZen — Development Rules

## 1. General engineering
- Inspect before modifying; prefer focused changes over rewrites.
- Preserve existing working behavior unless the task explicitly changes it.
- Follow existing naming, formatting and dependency-injection conventions.
- Keep functions and composables focused.
- Avoid duplicate business logic and unnecessary abstractions.
- Do not introduce a dependency when the platform or existing stack already solves the problem.
- Do not invent files, APIs, SDK methods or Gradle tasks. Verify against the project and official documentation when version-sensitive.

## 2. Architecture
- Compose UI renders state and emits user events; it must not access Room directly.
- ViewModels coordinate UI state and use cases/repositories.
- Repository interfaces belong at the appropriate domain boundary; implementations belong in the data layer.
- Room entities and DAOs stay in the data layer.
- Keep domain calculations independent of Android UI.
- Use Kotlin coroutines and Flow consistently; avoid blocking the main thread.
- Avoid a second database, duplicate repository layer or parallel source of truth.

## 3. Kotlin and Compose
- Prefer immutable UI state and unidirectional data flow.
- Use stable keys in lazy lists.
- Keep composables side-effect free; use appropriate Compose effect APIs only when needed.
- Handle lifecycle-aware collection of flows.
- Provide previews where practical, without making previews a substitute for tests.
- Use string resources for user-facing text.
- Use theme tokens rather than scattering literal colors and dimensions.
- Provide accessible labels and meaningful semantics.

## 4. Data and money
- Room is the MVP source of truth.
- Store money as integer minor currency units (`Long`), never binary floating point.
- Use `BigDecimal` or exact integer arithmetic for quantity/price calculations; define and test rounding rules.
- Respect currency fraction digits (for example, some currencies have zero minor digits).
- Store ISO 4217 currency code on each list.
- Do not perform currency conversion in MVP.
- Store quantities in a representation that avoids floating-point precision loss; document parsing and scale rules.
- Use database transactions for multi-table operations such as archiving a list and creating immutable history snapshots.
- Add explicit Room migrations; do not use destructive migrations in production.
- Preserve history snapshots when active lists change or are deleted.

## 5. UX behavior
- Follow `docs/DESIGN.md`.
- Every screen must handle relevant loading, empty, success, error and validation states.
- Confirm destructive actions.
- Provide undo for reversible purchase toggles where specified.
- Market Mode must be usable one-handed and must not show ads.
- Do not show ads on Market Mode, Add/Edit Item, Budget screen or purchase screen.
- Avoid blocking users with non-critical budget warnings.
- Do not show fake loading or success states.

## 6. Validation and errors
- Validate required names and numeric values at the boundary and domain layer as appropriate.
- Prevent invalid or negative quantities/prices unless a requirement explicitly permits them.
- Handle missing, deleted or stale list/item IDs safely.
- Surface actionable errors without exposing stack traces or sensitive data.
- Avoid swallowing exceptions silently.

## 7. Security and privacy
- Request only permissions required for an approved feature.
- Do not log shopping-list contents, personal notes, purchase details, tokens or identifiers.
- Do not hardcode secrets, signing credentials, API keys or billing verification secrets.
- Keep signing files and local secrets out of Git.
- Disclose third-party SDK data practices accurately.
- Follow `docs/SECURITY.md`.

## 8. Dependencies and platform
- Use versions compatible with the installed Android Gradle Plugin, Kotlin and compile SDK.
- Do not blindly copy version numbers from old prompts.
- Verify current Play target API requirements before release.
- Keep minimum SDK at API 29 unless the product owner approves a change.
- Avoid adding Firebase or a backend merely to support optional future functionality.

## 9. Testing and Git
- Add unit tests for business rules and money calculations.
- Add DAO/repository tests for persistence and transactions.
- Add Compose/UI tests for critical user journeys when practical.
- Run relevant tests and build verification after changes.
- Inspect `git diff` and `git status` before reporting.
- Never overwrite user changes or reset files without permission.
- Prefer small, descriptive commits after a verified milestone.
