package org.arcanaforge.app.data.ai

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.ai.OpenAiCodexOAuthClient
import org.arcanaforge.app.core.database.dao.AIProviderConfigDao
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity
import org.arcanaforge.app.core.security.SecureStringStore
import org.arcanaforge.app.domain.ai.AiAuthMode
import org.arcanaforge.app.domain.ai.AiProviderType

interface AiProviderRepository {
    fun observeConfigs(): Flow<List<AIProviderConfigEntity>>
    suspend fun saveApiKeyConfig(
        id: String?,
        name: String,
        providerType: AiProviderType,
        baseUrl: String?,
        apiKey: String?,
        imageModel: String?,
        textModel: String?,
        enabled: Boolean,
    )

    suspend fun startOpenAiCodexOAuth(): OpenAiCodexOAuthClient.AuthorizationSession
    suspend fun finishOpenAiCodexOAuth(session: OpenAiCodexOAuthClient.AuthorizationSession)
    suspend fun finishOpenAiCodexOAuthWithInput(
        session: OpenAiCodexOAuthClient.AuthorizationSession,
        input: String,
    )
    suspend fun setEnabled(id: String, enabled: Boolean)
    suspend fun delete(id: String)
}

class OfflineAiProviderRepository(
    private val aiProviderConfigDao: AIProviderConfigDao,
    private val secureStringStore: SecureStringStore,
    private val openAiCodexOAuthClient: OpenAiCodexOAuthClient,
) : AiProviderRepository {
    override fun observeConfigs(): Flow<List<AIProviderConfigEntity>> =
        aiProviderConfigDao.observeConfigs()

    override suspend fun saveApiKeyConfig(
        id: String?,
        name: String,
        providerType: AiProviderType,
        baseUrl: String?,
        apiKey: String?,
        imageModel: String?,
        textModel: String?,
        enabled: Boolean,
    ) {
        val existing = id?.let { aiProviderConfigDao.getById(it) }
        val encryptedApiKey = when {
            apiKey.isNullOrBlank() -> existing?.apiKeyEncrypted
            else -> secureStringStore.encrypt(apiKey.trim())
        }
        val entity = AIProviderConfigEntity(
            id = existing?.id ?: id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            name = name.trim().ifBlank { providerType.defaultName() },
            providerType = providerType,
            authMode = AiAuthMode.ApiKey,
            baseUrl = baseUrl?.trim()?.ifBlank { null },
            apiKeyEncrypted = encryptedApiKey,
            imageModel = imageModel?.trim()?.ifBlank { null },
            textModel = textModel?.trim()?.ifBlank { null },
            enabled = enabled,
        )
        aiProviderConfigDao.upsert(entity)
    }

    override suspend fun startOpenAiCodexOAuth(): OpenAiCodexOAuthClient.AuthorizationSession =
        openAiCodexOAuthClient.createAuthorizationSession()

    override suspend fun finishOpenAiCodexOAuth(session: OpenAiCodexOAuthClient.AuthorizationSession) {
        try {
            val code = session.awaitCode()
            exchangeAndSaveOpenAiCodex(session = session, code = code)
        } finally {
            session.close()
        }
    }

    override suspend fun finishOpenAiCodexOAuthWithInput(
        session: OpenAiCodexOAuthClient.AuthorizationSession,
        input: String,
    ) {
        try {
            val code = openAiCodexOAuthClient.parseAuthorizationInput(
                input = input,
                expectedState = session.state,
            )
            exchangeAndSaveOpenAiCodex(session = session, code = code)
        } finally {
            session.close()
        }
    }

    private suspend fun exchangeAndSaveOpenAiCodex(
        session: OpenAiCodexOAuthClient.AuthorizationSession,
        code: String,
    ) {
        val tokens = openAiCodexOAuthClient.exchangeAuthorizationCode(
            code = code,
            verifier = session.verifier,
        )
        val label = tokens.email ?: tokens.accountId
        aiProviderConfigDao.upsert(
            AIProviderConfigEntity(
                id = OPENAI_CODEX_CONFIG_ID,
                name = "OpenAI account",
                providerType = AiProviderType.OpenAiCodex,
                authMode = AiAuthMode.OpenAiCodexOAuth,
                oauthAccessTokenEncrypted = secureStringStore.encrypt(tokens.accessToken),
                oauthRefreshTokenEncrypted = secureStringStore.encrypt(tokens.refreshToken),
                oauthExpiresAt = tokens.expiresAt,
                oauthAccountId = tokens.accountId,
                oauthAccountLabel = label,
                enabled = true,
            ),
        )
    }

    override suspend fun setEnabled(id: String, enabled: Boolean) {
        val existing = aiProviderConfigDao.getById(id) ?: return
        aiProviderConfigDao.upsert(existing.copy(enabled = enabled))
    }

    override suspend fun delete(id: String) {
        aiProviderConfigDao.deleteById(id)
    }

    private fun AiProviderType.defaultName(): String =
        when (this) {
            AiProviderType.OpenAi -> "OpenAI API key"
            AiProviderType.OpenAiCodex -> "OpenAI account"
            AiProviderType.CustomOpenAiCompatible -> "OpenAI-compatible API"
            AiProviderType.Stability -> "Stability AI"
            AiProviderType.Replicate -> "Replicate"
            AiProviderType.ComfyUi -> "ComfyUI"
            AiProviderType.Local -> "Local provider"
        }

    private companion object {
        const val OPENAI_CODEX_CONFIG_ID = "openai-codex:default"
    }
}
