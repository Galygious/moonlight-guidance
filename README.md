# Moonlight Guidance

Moonlight Guidance is a free, open-source, local-first Android app for tarot, oracle, custom decks, custom layouts, readings, journaling, scheduled reminders, and optional BYOK AI features.

The Android package is `org.arcanaforge.app`.

## What is Arcana Forge?

Arcana Forge is the open-source project namespace behind Moonlight Guidance. The app is deck-agnostic and is designed around user-owned decks, images, layouts, readings, notes, schedules, and settings.

## Features

Phase 0 and Phase 1 currently include:

- Native Android project using Kotlin, Gradle Kotlin DSL, Jetpack Compose, and Material 3.
- App shell with Home, Deck Library, Layout Library, Readings, Schedule, Settings, and Import/Export routes.
- Room database with initial deck, card, image, layout, reading, schedule, and AI provider entities.
- DataStore settings foundation for theme preferences.
- Seed data for one sample oracle deck, a one-card layout, a three-card layout, and a seven-chakra layout.
- Home and Deck Library screens that read from and write to the database.

## Screenshots

Screenshot placeholders live in `screenshots/`. Final screenshots will be added after the MVP UI is complete.

## Install/build from source

Requirements:

- JDK 17 or newer.
- Android SDK with API 35 installed.
- Android SDK path set through `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or a local `local.properties` file.

Create `local.properties` when needed:

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

Build:

```powershell
.\gradlew.bat assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat testDebugUnitTest
```

Run instrumentation tests with a connected device or emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Install on a connected device or emulator:

```powershell
.\gradlew.bat installDebug
```

## Data privacy

The app is designed to work offline. User-created decks, cards, images, layouts, readings, notes, settings, and schedules are stored locally by default. AI is optional and BYOK-only. API keys are not part of Phase 1 behavior and must not be exported by default in later phases.

Chakra, crystal, herb, and energy correspondences are provided for reflection and spiritual practice. They are not medical advice or a substitute for professional care.

## BYOK AI setup

BYOK AI configuration is planned for Phase 8. The database entity and dependency foundation exist, but provider setup and encrypted key storage are not implemented yet.

## Import/export format

The planned deck and backup formats are documented in `docs/IMPORT_EXPORT.md`.

## Development setup

Open the project root in Android Studio, sync Gradle, and run the `app` configuration. The app currently uses manual dependency wiring through `AppContainer`.

## Architecture overview

Architecture notes live in `docs/ARCHITECTURE.md`.

## Roadmap

- Phase 2: deck editor, card builder, Photo Picker image import, local image storage, thumbnails.
- Phase 3: correspondence editing and display.
- Phase 4: built-in layouts, reading creation, draw logic, reversals, saved history.
- Phase 5: reading detail and journaling.
- Phase 6: custom layout canvas editor.
- Phase 7: scheduling with WorkManager notifications.
- Phase 8: BYOK AI provider foundation.
- Phase 9: import/export.
- Phase 10: polish, accessibility, release readiness.

## Contributing

Use small, focused changes. Keep core behavior local-first and deck-agnostic. Do not add required cloud services.

## License

MIT. See `LICENSE`.
