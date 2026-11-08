# ShopZen — Agent Operating Instructions

## Mission
Build ShopZen, an Android-first, offline-first shopping list and grocery budget planner. Work incrementally in the existing Android Studio project. The source code is the implementation truth; documentation must reflect verified implementation, not intended work.

## Required reading before every task
1. `AGENTS.md`
2. `RULES.md`
3. `TASKS.md`
4. `README.md`
5. `android_ui_system.md`
6. Relevant files in `docs/`
7. Relevant source code, tests, Gradle configuration and Git status

Do not create, read, reference, or maintain `MEMORY.md`.

## Product baseline
- Product: ShopZen
- Application ID target: `com.barakwebsoft.shopzen` (confirm actual Gradle namespace/applicationId before changing)
- Platform: native Android
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Architecture: MVVM with repository boundaries and clean separation of concerns
- Persistence: Room / SQLite
- Minimum Android: API 29 (Android 10)
- Offline-first; no backend or mandatory account in MVP
- Currency is selected per shopping list using ISO 4217 currency codes
- No currency conversion in MVP
- Monetary values use integer minor units (`Long`); never use `Float` or `Double` for money

## Required workflow
For each task:
1. Read the required project context.
2. Inspect current implementation, Git status and relevant tests.
3. Identify scope, dependencies, risks and acceptance criteria.
4. Present a concise plan for substantial changes.
5. Implement the smallest complete change.
6. Add/update tests.
7. Run appropriate verification commands.
8. Review the diff for regressions, unrelated edits and security issues.
9. Update only the documentation affected by the change.
10. Update `TASKS.md` only when the task status is supported by evidence.
11. Report changed files, tests actually run, results, known issues and next task.

Use: INSPECT → PLAN → IMPLEMENT → TEST → REVIEW → DOCUMENT → REPORT.

## Documentation responsibilities
- `docs/PRD.md`: product requirements, scope and acceptance outcomes.
- `docs/ARCHITECTURE.md`: actual architecture, package responsibilities, persistence and data flow.
- `docs/DESIGN.md`: design tokens, navigation, screen behavior and accessibility.
- `docs/android_ui_system`:navigation, screen behavior and accessibility. ui system.
- `docs/TEST_PLAN.md`: test strategy, cases, execution status and known gaps.
- `docs/SECURITY.md`: privacy, permissions, local data, third-party SDK and release security.
- `docs/DECISIONS.md`: significant decisions, rationale, status and superseded decisions.
- `TASKS.md`: ordered work breakdown and verified task status.
- `README.md`: human-facing project setup, build and contribution guidance.
- `CHANGELOG.md` is optional; do not create unless the project needs a release/change history.

After a task, update only files whose content changed. Never claim a test passed unless it was executed and its result observed. Never mark a feature complete because code was merely generated.

## Scope controls
Do not add backend services, authentication, cloud sync, AI, retailer integrations, grocery delivery, barcode scanning, live price comparison or currency conversion to MVP without explicit approval.
Do not silently change product requirements or application identity.
Do not add placeholder controls that appear functional but do nothing.
Do not remove working features without approval.

## Build and verification
Inspect available Gradle tasks and project configuration before choosing commands. Common commands may include:
- Windows: `.\gradlew.bat test`
- Windows: `.\gradlew.bat lint`
- Windows: `.\gradlew.bat assembleDebug`
- Unix-like: `./gradlew test`, `./gradlew lint`, `./gradlew assembleDebug`

Run instrumentation tests only when a suitable emulator/device is available. Distinguish pre-existing failures, introduced failures and environment failures.

## Completion report
Include:
1. Task ID and title
2. Implemented behavior
3. Files created/modified
4. Database/API changes, if any
5. Tests/commands actually executed and results
6. Documentation updated
7. Known issues and unimplemented scope
8. Recommended next task
