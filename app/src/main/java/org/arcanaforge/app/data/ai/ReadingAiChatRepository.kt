package org.arcanaforge.app.data.ai

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.arcanaforge.app.core.database.dao.ReadingAiMessageDao
import org.arcanaforge.app.core.database.entity.ReadingAiMessageEntity
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole

interface ReadingAiChatRepository {
    fun observeMessages(readingId: String): Flow<List<AiChatMessage>>
    suspend fun getMessages(readingId: String): List<AiChatMessage>
    suspend fun addMessage(readingId: String, role: AiChatRole, text: String): AiChatMessage
    suspend fun clearMessages(readingId: String)
}

class OfflineReadingAiChatRepository(
    private val readingAiMessageDao: ReadingAiMessageDao,
) : ReadingAiChatRepository {
    override fun observeMessages(readingId: String): Flow<List<AiChatMessage>> =
        readingAiMessageDao.observeMessages(readingId).map { messages ->
            messages.map { it.toDomain() }
        }

    override suspend fun getMessages(readingId: String): List<AiChatMessage> =
        readingAiMessageDao.getMessages(readingId).map { it.toDomain() }

    override suspend fun addMessage(
        readingId: String,
        role: AiChatRole,
        text: String,
    ): AiChatMessage {
        val message = ReadingAiMessageEntity(
            id = UUID.randomUUID().toString(),
            readingId = readingId,
            role = role,
            text = text,
            createdAt = Instant.now(),
        )
        readingAiMessageDao.insert(message)
        return message.toDomain()
    }

    override suspend fun clearMessages(readingId: String) {
        readingAiMessageDao.deleteForReading(readingId)
    }

    private fun ReadingAiMessageEntity.toDomain(): AiChatMessage =
        AiChatMessage(
            id = id,
            role = role,
            text = text,
            createdAt = createdAt,
        )
}
