package org.arcanaforge.app.domain.reading

import java.security.SecureRandom
import java.util.UUID
import org.arcanaforge.app.core.database.entity.CardEntity
import org.arcanaforge.app.core.database.entity.LayoutSlotEntity
import org.arcanaforge.app.core.database.entity.ReadingCardEntity

object ReadingDrawEngine {
    fun draw(
        cards: List<CardEntity>,
        slots: List<LayoutSlotEntity>,
        reversalsEnabled: Boolean,
        random: SecureRandom = SecureRandom(),
    ): List<ReadingCardEntity> {
        require(slots.size <= cards.size) {
            "Layout requires ${slots.size} cards, but this deck only has ${cards.size}."
        }

        val pool = cards.toMutableList()
        return slots.sortedBy { it.drawOrder }.map { slot ->
            val selected = pool.removeAt(random.nextInt(pool.size))
            val canReverse = reversalsEnabled && slot.reversedAllowed
            ReadingCardEntity(
                id = UUID.randomUUID().toString(),
                readingId = "",
                slotId = slot.id,
                cardId = selected.id,
                orientation = if (canReverse && random.nextBoolean()) {
                    ReadingOrientation.Reversed
                } else {
                    ReadingOrientation.Upright
                },
            )
        }
    }
}
