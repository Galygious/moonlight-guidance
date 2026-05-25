package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import org.arcanaforge.app.domain.ai.AiAuthMode
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
    @ColumnInfo(name = "auth_mode") val authMode: AiAuthMode = AiAuthMode.ApiKey,
    @ColumnInfo(name = "base_url") val baseUrl: String? = null,
    @ColumnInfo(name = "api_key_encrypted") val apiKeyEncrypted: String? = null,
    @ColumnInfo(name = "oauth_access_token_encrypted") val oauthAccessTokenEncrypted: String? = null,
    @ColumnInfo(name = "oauth_refresh_token_encrypted") val oauthRefreshTokenEncrypted: String? = null,
    @ColumnInfo(name = "oauth_expires_at") val oauthExpiresAt: Instant? = null,
    @ColumnInfo(name = "oauth_account_id") val oauthAccountId: String? = null,
    @ColumnInfo(name = "oauth_account_label") val oauthAccountLabel: String? = null,
    @ColumnInfo(name = "image_model") val imageModel: String? = null,
    @ColumnInfo(name = "text_model") val textModel: String? = null,
    val enabled: Boolean = false,
)
