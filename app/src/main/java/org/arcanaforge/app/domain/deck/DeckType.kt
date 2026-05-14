package org.arcanaforge.app.domain.deck

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DeckType {
    @SerialName("tarot")
    Tarot,

    @SerialName("oracle")
    Oracle,

    @SerialName("playing_cards")
    PlayingCards,

    @SerialName("affirmation")
    Affirmation,

    @SerialName("chakra")
    Chakra,

    @SerialName("crystal")
    Crystal,

    @SerialName("custom")
    Custom,
}

fun DeckType.displayName(): String = when (this) {
    DeckType.Tarot -> "Tarot"
    DeckType.Oracle -> "Oracle"
    DeckType.PlayingCards -> "Playing cards"
    DeckType.Affirmation -> "Affirmation"
    DeckType.Chakra -> "Chakra"
    DeckType.Crystal -> "Crystal"
    DeckType.Custom -> "Custom"
}
