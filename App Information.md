## Chakra, crystal, and correspondence metadata

Add optional spiritual/correspondence metadata to cards, decks, layouts, and readings. This supports decks that include chakra information, crystal associations, elemental correspondences, zodiac signs, planets, herbs, colors, or other symbolic systems.

This should be optional and user-defined, not hardcoded as required tarot structure.

### Supported correspondence types

Initial supported fields:

* Chakras
* Helpful crystals
* Elements
* Zodiac signs
* Planets
* Colors
* Herbs/plants
* Keywords
* Custom correspondences

Examples:

```json
{
  "chakras": ["Heart", "Throat"],
  "crystals": ["Rose Quartz", "Aquamarine"],
  "elements": ["Water"],
  "zodiac": ["Cancer", "Pisces"],
  "colors": ["Pink", "Blue"],
  "custom": {
    "Affirmation": "I speak my feelings clearly.",
    "Body area": "Chest and throat"
  }
}
```

### Card-level metadata

Each card can optionally include correspondence data.

Use cases:

* A chakra oracle deck where every card maps to one or more chakras
* A crystal deck where each card has associated stones
* A tarot deck where The Empress links to Venus, Earth, rose quartz, and the heart chakra
* A custom deck where users define their own symbolic categories

Suggested card fields:

```ts
type CardCorrespondences = {
  chakras?: string[]
  crystals?: string[]
  elements?: string[]
  zodiacSigns?: string[]
  planets?: string[]
  colors?: string[]
  herbs?: string[]
  custom?: Record<string, string | string[]>
}
```

Add to `Card`:

```ts
correspondences?: CardCorrespondences
```

### Deck-level metadata

Decks can define which correspondence systems they use.

Example:

```ts
type Deck = {
  id: string
  name: string
  correspondenceSystems: string[]
}
```

Example values:

```json
["chakras", "crystals", "elements", "custom"]
```

This lets the UI show only relevant metadata fields for that deck.

### Reading-level usage

During a reading, the app can surface relevant correspondences alongside the card meaning.

Example reading display:

```text
Slot: What supports me today
Card: Heart Renewal

Meaning:
Open yourself to receiving care without minimizing your needs.

Chakras:
Heart, Throat

Helpful crystals:
Rose Quartz, Aquamarine

Reflection:
Where am I making it harder than necessary to receive support?
```

### Layout-level suggestions

Layouts can optionally include a correspondence focus.

Examples:

* Chakra alignment spread
* Crystal guidance spread
* Elemental balance spread
* Mind/body/spirit energy check
* Seven-chakra reading

Example layout slots:

```json
[
  {
    "title": "Root Chakra",
    "description": "Grounding, safety, body, survival needs."
  },
  {
    "title": "Sacral Chakra",
    "description": "Creativity, pleasure, emotion, desire."
  },
  {
    "title": "Solar Plexus Chakra",
    "description": "Confidence, willpower, agency, boundaries."
  }
]
```

### Built-in optional layouts

Add optional starter layouts:

* Seven Chakra Check-In
* Crystal Support Reading
* Elemental Balance Spread
* Energy Block / Energy Support / Integration
* Body / Heart / Mind / Spirit

### AI prompt support

When generating card art or interpretations, include correspondence metadata when available.

Example:

```text
Card: Heart Renewal
Keywords: receiving, softness, emotional honesty
Chakras: Heart, Throat
Helpful crystals: Rose Quartz, Aquamarine
Element: Water

Generate vertical oracle card art with no text.
Focus on the symbolic feeling of emotional openness, communication, and gentle healing.
```

For interpretation:

```text
Use the card's chakra and crystal correspondences only as symbolic reflection tools.
Do not present crystal or chakra associations as medical advice.
```

### Safety note

The app should treat chakra, crystal, herb, and energy information as reflective/spiritual metadata, not health guidance.

Suggested disclaimer:

```text
Chakra, crystal, herb, and energy correspondences are provided for reflection and spiritual practice. They are not medical advice or a substitute for professional care.
```

### MVP implementation

For MVP, add a flexible `correspondences` JSON field to cards rather than creating many database tables immediately.

Room field option:

```kotlin
data class CardCorrespondences(
    val chakras: List<String> = emptyList(),
    val crystals: List<String> = emptyList(),
    val elements: List<String> = emptyList(),
    val zodiacSigns: List<String> = emptyList(),
    val planets: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val herbs: List<String> = emptyList(),
    val custom: Map<String, List<String>> = emptyMap()
)
```

Store it as serialized JSON.

Later, if search/filtering becomes important, promote common fields like chakras and crystals into indexed tables.
