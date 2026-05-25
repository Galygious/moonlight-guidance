package org.arcanaforge.app.data.astrology

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.arcanaforge.app.core.database.dao.NatalChartAiMessageDao
import org.arcanaforge.app.core.database.entity.NatalChartAiMessageEntity
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole

interface NatalChartAiChatRepository {
    fun observeMessages(chartId: String): Flow<List<AiChatMessage>>
    suspend fun getMessages(chartId: String): List<AiChatMessage>
    suspend fun addMessage(chartId: String, role: AiChatRole, text: String): AiChatMessage
    suspend fun clearMessages(chartId: String)
}

class OfflineNatalChartAiChatRepository(
    private val natalChartAiMessageDao: NatalChartAiMessageDao,
) : NatalChartAiChatRepository {
    override fun observeMessages(chartId: String): Flow<List<AiChatMessage>> =
        natalChartAiMessageDao.observeMessages(chartId).map { messages ->
            messages.map { it.toDomain() }
        }

    override suspend fun getMessages(chartId: String): List<AiChatMessage> =
        natalChartAiMessageDao.getMessages(chartId).map { it.toDomain() }

    override suspend fun addMessage(
        chartId: String,
        role: AiChatRole,
        text: String,
    ): AiChatMessage {
        val message = NatalChartAiMessageEntity(
            id = UUID.randomUUID().toString(),
            chartId = chartId,
            role = role,
            text = text,
            createdAt = Instant.now(),
        )
        natalChartAiMessageDao.insert(message)
        return message.toDomain()
    }

    override suspend fun clearMessages(chartId: String) {
        natalChartAiMessageDao.deleteForChart(chartId)
    }

    private fun NatalChartAiMessageEntity.toDomain(): AiChatMessage =
        AiChatMessage(
            id = id,
            role = role,
            text = text,
            createdAt = createdAt,
        )
}
