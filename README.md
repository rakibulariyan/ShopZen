# ShopZen

ShopZen is an Android-first, offline-first shopping list and grocery budget planner. It helps users prepare shopping lists, organize items, track purchases and monitor spending while shopping.

## Product principles
- Fast, simple and usable one-handed in a busy market.
- Core functionality works offline.
- No mandatory account in the MVP.
- Local Room database is the source of truth.
- Each list has its own currency; no currency conversion in MVP.
- No ads in Market Mode, Add/Edit Item, Budget or purchase screens.

## Technology
- Kotlin
- Android / Jetpack Compose
- Material 3
- MVVM and repository boundaries
- Room / SQLite
- DataStore for app preferences

Exact dependency versions must be verified against the project's Android Gradle Plugin, Kotlin and SDK configuration. Do not assume versions in this document are a dependency lockfile.

## Requirements
- Android Studio compatible with the project's Gradle/AGP versions.
- JDK version required by the configured Android Gradle Plugin.
- Android SDK and platform tools for the configured compile SDK.
- Git recommended.

## Open and run
1. Open the `shopzen` project root in Android Studio.
2. Allow Gradle sync to complete.
3. Review any SDK/JDK prompts and use versions compatible with the project.
4. Run the `app` configuration on an Android 10 (API 29) or newer emulator/device.

Windows command examples:
```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

Unix-like command examples:
```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Run only commands supported by the current Gradle project. Instrumentation tests require a configured emulator or connected device.

## Project documentation
- `AGENTS.md` — AI agent operating instructions.
- `RULES.md` — coding and implementation standards.
- `TASKS.md` — development roadmap and task status.
- `docs/PRD.md` — product requirements.
- `docs/ARCHITECTURE.md` — architecture and data model.
- `docs/DESIGN.md` — visual and interaction system.
- `docs/TEST_PLAN.md` — test strategy and cases.
- `docs/SECURITY.md` — security and privacy requirements.
- `docs/DECISIONS.md` — architecture/product decision record.

There is intentionally no `MEMORY.md`. Current work status belongs in `TASKS.md`; durable decisions belong in `DECISIONS.md`.

## Scope
The MVP is a local-first shopping list and budget planner. Backend, mandatory authentication, cloud sync, AI, retailer integrations and delivery marketplace features are out of scope unless explicitly approved.

## Development workflow
Inspect → Plan → Implement → Test → Review → Document → Report.

Read `AGENTS.md` and the relevant documentation before making changes. Keep documentation aligned with verified source code.
