package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.arcanaforge.app.domain.ai.AiProviderType

@Entity(
    tableName = "ai_provider_configs",
    indices = [
        Index(value = ["provider_type"]),
        Index(value = ["enabled"]),
    ],
)
data class AIProviderConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "provider_type") val providerType: AiProviderType,
    @ColumnInfo(name = "base_url") val baseUrl: String? = null,
    @ColumnInfo(name = "api_key_encrypted") val apiKeyEncrypted: String? = null,
    @ColumnInfo(name = "image_model") val imageModel: String? = null,
    @ColumnInfo(name = "text_model") val textModel: String? = null,
    val enabled: Boolean = false,
)
