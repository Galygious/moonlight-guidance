# Architecture

Moonlight Guidance uses a small layered architecture that can grow without adding a required backend.

## Layers

- `core`: platform services such as Room, DataStore, future image storage, notifications, AI transport, import/export, security, and utilities.
- `domain`: deck-agnostic models and enums used across the app.
- `data`: repositories that mediate between UI and persistence.
- `ui`: Compose screens, navigation, theme, and reusable components.

## Current dependency flow

```text
Compose screen -> ViewModel -> Repository -> Room DAO -> Room database
```

The app currently uses manual dependency construction in `AppContainer`. This keeps Phase 1 transparent and avoids introducing a DI framework before the object graph needs it.

## Persistence

Room is the source of truth for user data. All first-pass entities use string IDs so imports can preserve or remap IDs safely later. Flexible fields such as tags, correspondence systems, custom correspondences, and schedule metadata are serialized through Kotlin Serialization.

Room migrations start at version 1 in `AppDatabase.MIGRATION_1_2_PLACEHOLDER`. Future schema changes must add explicit migrations instead of destructive fallback.

## Local-first principle

No account or cloud backend is required. AI provider configuration exists as schema only in Phase 1. Future AI calls must be explicit user actions and must never silently upload decks, readings, notes, images, or API keys.

## UI state

Screen state is kept in ViewModels. Repositories expose `Flow` values from Room, and Compose observes those flows through lifecycle-aware collection.
