package org.arcanaforge.app.core.database

import org.junit.Assert.assertTrue
import org.junit.Test

class StandardTarotMeaningsTest {
    @Test
    fun everyStandardTarotCardHasSeedMeaning() {
        standardTarotCardIds().forEach { cardId ->
            val meaning = StandardTarotMeanings.forCard(cardId)

            assertTrue("$cardId is missing keywords", meaning?.keywords?.isNotEmpty() == true)
            assertTrue("$cardId is missing upright meaning", meaning?.uprightMeaning?.isNotBlank() == true)
            assertTrue("$cardId is missing reversed meaning", meaning?.reversedMeaning?.isNotBlank() == true)
        }
    }

    private fun standardTarotCardIds(): List<String> {
        val majorArcana = (0..21).map { index ->
            "standard-tarot-major-${index.toString().padStart(2, '0')}"
        }
        val ranks = listOf(
            "ace",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "ten",
            "page",
            "knight",
            "queen",
            "king",
        )
        val suits = listOf("wands", "cups", "swords", "pentacles")
        val minorArcana = suits.flatMap { suit ->
            ranks.map { rank -> "standard-tarot-$suit-$rank" }
        }
        return majorArcana + minorArcana
    }
}
