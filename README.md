# Moonlight Guidance

Moonlight Guidance is a free, open-source, local-first Android app for tarot, oracle, custom decks, custom layouts, readings, natal charts, journaling, scheduled reminders, and optional BYOK AI features.

The Android package is `org.arcanaforge.app`.

## What is Arcana Forge?

Arcana Forge is the open-source project namespace behind Moonlight Guidance. The app is deck-agnostic and is designed around user-owned decks, images, layouts, readings, natal charts, notes, schedules, and settings.

## Features

Current implemented scope includes Phase 0, Phase 1, a Phase 2 MVP slice, the first Phase 3 correspondence-editing pass, a Phase 4 reading MVP slice, the first Phase 5 journaling pass, an initial Phase 6 layout-library/editor slice, a Phase 7 scheduling MVP, initial Phase 8 AI provider/chat support, and a first natal-chart MVP:

- Native Android project using Kotlin, Gradle Kotlin DSL, Jetpack Compose, and Material 3.
- App shell with Home, Deck Library, Layout Library, Readings, Schedule, Settings, and Import/Export routes.
- Room database with initial deck, card, image, layout, reading, schedule, and AI provider entities.
- DataStore settings foundation for theme preferences.
- Seed data for one sample oracle deck, a complete 78-card Standard Tarot deck, a one-card layout, a three-card layout, and a seven-chakra layout.
- Home and Deck Library screens that read from and write to the database.
- Deck editor for name, description, author, deck type, tags, correspondence systems, and reversals.
- Deck editor card rows show attached card thumbnails beside the delete action.
- Card editor for title, subtitle, suit, group, keywords, meanings, notes, correspondence metadata, AI prompt metadata, and image attachment.
- Android Photo Picker import into app-controlled storage with generated card thumbnails.
- Correspondence editing for chakras, crystals, elements, zodiac signs, planets, colors, herbs/plants, and custom key/value lists.
- Reading creation from a selected deck and layout using SecureRandom, drawing without replacement.
- Manual physical-card reading entry for recording externally drawn cards by slot and orientation.
- Saved reading history and reading detail views with card images, slot meaning, card meaning, orientation, and correspondence display.
- Reading detail journaling with full reading notes, per-card notes, larger tapped-card detail, and favorite toggling.
- Reading image sharing from Reading Detail through an Android share sheet.
- Scheduled reading reminders with daily, weekly, and monthly options, notification permission handling, WorkManager delivery, enable/disable, delete confirmation, and notification tap-through into prepared reading creation.
- BYOK OpenAI/OpenAI-compatible providers plus OpenAI account OAuth, encrypted local credential storage, and persisted AI chats on reading details.
- Natal chart creation with offline planet-position calculation, manual location coordinates, Whole Sign house assignment when time/location are available, saved notes, favorite/delete, text sharing, and persisted AI chart chat.
- Layout Library screen for browsing seeded layouts and creating custom layouts.
- Layout editor for custom layout metadata, canvas dimensions, scaled preview, drag-to-position slots, slot creation, slot duplication, slot editing, slot deletion, and persisted slot counts.
- Guarded delete flows for saved readings, custom layouts, and decks using a typed confirmation step.

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

The app is designed to work offline. User-created decks, cards, images, layouts, readings, natal charts, notes, settings, and schedules are stored locally by default. AI is optional. API keys and OpenAI OAuth tokens are stored encrypted on-device and must not be exported by default in later phases.

Chakra, crystal, herb, and energy correspondences are provided for reflection and spiritual practice. They are not medical advice or a substitute for professional care.

## BYOK AI setup

Settings supports BYOK OpenAI/OpenAI-compatible providers and OpenAI account OAuth. Reading and natal-chart detail screens can use the enabled provider for reflective chat.

## Import/export format

The planned deck and backup formats are documented in `docs/IMPORT_EXPORT.md`.

## Development setup

Open the project root in Android Studio, sync Gradle, and run the `app` configuration. The app currently uses manual dependency wiring through `AppContainer`.

## Architecture overview

Architecture notes live in `docs/ARCHITECTURE.md`.

## Roadmap

- Phase 2 follow-up: richer deck templates, card reordering, card duplication, card delete confirmation, image crop/replace.
- Phase 3 follow-up: richer correspondence display in card detail.
- Phase 4 follow-up: reveal modes, reading filters, and visual canvas reading display.
- Phase 5 follow-up: richer journal workflows, share/export reading text, more share image templates, and accessibility fallback polish.
- Phase 6 follow-up: resize handles, grid snapping, and richer custom-layout reading-flow polish.
- Phase 7 follow-up: specific weekdays, editable existing schedules, exact-alarm option where appropriate, and richer schedule status text.
- Phase 8 follow-up: AI image generation, model discovery, richer prompt templates, and provider-specific diagnostics.
- Natal chart follow-up: location search, chart wheel rendering, more house systems, transits, synastry/composite charts, and shareable chart images.
- Phase 9: import/export.
- Phase 10: polish, accessibility, release readiness.

## Contributing

Use small, focused changes. Keep core behavior local-first and deck-agnostic. Do not add required cloud services.

## License

MIT. See `LICENSE`.
