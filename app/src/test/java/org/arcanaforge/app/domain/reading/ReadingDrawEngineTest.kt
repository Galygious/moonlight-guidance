package org.arcanaforge.app.domain.reading

import java.time.Instant
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingDrawEngineTest {
    @Test
    fun drawUsesSlotsWithoutReplacingCards() {
        val cards = listOf(
            card("card-1", 0),
            card("card-2", 1),
            card("card-3", 2),
        )
        val slots = listOf(slot("slot-1", 0), slot("slot-2", 1))

        val drawnCards = ReadingDrawEngine.draw(
            cards = cards,
            slots = slots,
            reversalsEnabled = false,
        )

        assertEquals(2, drawnCards.size)
        assertEquals(listOf("slot-1", "slot-2"), drawnCards.map { it.slotId })
        assertEquals(2, drawnCards.map { it.cardId }.toSet().size)
        assertTrue(drawnCards.all { it.orientation == ReadingOrientation.Upright })
    }

    @Test(expected = IllegalArgumentException::class)
    fun drawRejectsLayoutsThatNeedMoreCardsThanDeckHas() {
        ReadingDrawEngine.draw(
            cards = listOf(card("card-1", 0)),
            slots = listOf(slot("slot-1", 0), slot("slot-2", 1)),
            reversalsEnabled = true,
        )
    }

    private fun card(id: String, orderIndex: Int): CardEntity {
        val now = Instant.EPOCH
        return CardEntity(
            id = id,
            deckId = "deck",
            title = id,
            orderIndex = orderIndex,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun slot(id: String, drawOrder: Int): LayoutSlotEntity =
        LayoutSlotEntity(
            id = id,
            layoutId = "layout",
            title = id,
            x = 0f,
            y = 0f,
            width = 100f,
            height = 160f,
            drawOrder = drawOrder,
        )
}
