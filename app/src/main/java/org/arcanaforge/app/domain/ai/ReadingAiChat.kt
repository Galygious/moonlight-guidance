package org.arcanaforge.app.domain.ai

import java.time.Instant
import java.util.UUID

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: AiChatRole,
    val text: String,
    val createdAt: Instant = Instant.now(),
)

enum class AiChatRole {
    User,
    Assistant,
}
