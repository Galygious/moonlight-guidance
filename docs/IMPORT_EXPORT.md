# Import and Export Formats

Import/export is planned for Phase 9. This document defines the target formats so earlier schema choices remain compatible.

## Deck export

File extension:

```text
.arcana-deck.zip
```

Planned structure:

```text
deck.json
cards.json
images/
  card_001.webp
  card_002.webp
  back.webp
```

`deck.json` and `cards.json` must include a schema version. Imported IDs must be validated and remapped when they collide with existing local IDs. Import as copy is the default behavior.

## Full backup

File extension:

```text
.arcana-backup.zip
```

Planned structure:

```text
backup_manifest.json
decks/
layouts/
readings/
schedules/
images/
settings.json
```

Encrypted API keys must not be exported by default. Any future API key export must require an explicit user opt-in.
