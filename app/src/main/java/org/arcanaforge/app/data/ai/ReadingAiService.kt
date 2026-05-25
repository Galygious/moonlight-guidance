package org.arcanaforge.app.data.ai

import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.arcanaforge.app.core.ai.OpenAiCodexOAuthClient
import org.arcanaforge.app.core.database.dao.AIProviderConfigDao
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity
import org.arcanaforge.app.core.security.SecureStringStore
import org.arcanaforge.app.domain.ai.AiAuthMode
import org.arcanaforge.app.domain.ai.AiChatMessage
import org.arcanaforge.app.domain.ai.AiChatRole
import org.arcanaforge.app.domain.ai.AiProviderType
import org.json.JSONArray
import org.json.JSONObject

interface ReadingAiService {
    suspend fun askAboutReading(
        readingContext: String,
        history: List<AiChatMessage>,
        question: String,
    ): String
}

class OpenAiReadingAiService(
    private val aiProviderConfigDao: AIProviderConfigDao,
    private val secureStringStore: SecureStringStore,
    private val openAiCodexOAuthClient: OpenAiCodexOAuthClient,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) : ReadingAiService {
    override suspend fun askAboutReading(
        readingContext: String,
        history: List<AiChatMessage>,
        question: String,
    ): String = withContext(Dispatchers.IO) {
        val config = aiProviderConfigDao.getEnabledConfig()
            ?: error("Connect or enable an AI provider in Settings first.")
        when (config.authMode) {
            AiAuthMode.ApiKey -> askWithApiKey(
                config = config,
                readingContext = readingContext,
                history = history,
                question = question,
            )

            AiAuthMode.OpenAiCodexOAuth -> askWithOpenAiCodex(
                config = config,
                readingContext = readingContext,
                history = history,
                question = question,
            )
        }
    }

    private fun askWithApiKey(
        config: AIProviderConfigEntity,
        readingContext: String,
        history: List<AiChatMessage>,
        question: String,
    ): String {
        val encryptedApiKey = config.apiKeyEncrypted
            ?: error("The selected AI provider does not have an API key saved.")
        val apiKey = secureStringStore.decrypt(encryptedApiKey)
        val body = buildResponseBody(
            model = config.textModel?.ifBlank { null }
                ?: error("Choose a text model for the selected AI provider in Settings."),
            readingContext = readingContext,
            history = history,
            question = question,
            stream = false,
        )
        val endpoint = resolveOpenAiResponsesUrl(config)
        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", JSON_MEDIA_TYPE)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("AI request failed (${response.code}): ${responseBody.take(500)}")
            }
            return extractResponseText(JSONObject(responseBody))
                ?: error("AI response did not include text.")
        }
    }

    private suspend fun askWithOpenAiCodex(
        config: AIProviderConfigEntity,
        readingContext: String,
        history: List<AiChatMessage>,
        question: String,
    ): String {
        val freshConfig = refreshCodexIfNeeded(config)
        val accessToken = freshConfig.oauthAccessTokenEncrypted
            ?.let(secureStringStore::decrypt)
            ?: error("OpenAI account token is missing. Reconnect the account in Settings.")
        val accountId = freshConfig.oauthAccountId
            ?: error("OpenAI account id is missing. Reconnect the account in Settings.")
        val body = buildResponseBody(
            model = freshConfig.textModel?.ifBlank { null }
                ?: error("Choose a text model for the OpenAI account in Settings."),
            readingContext = readingContext,
            history = history,
            question = question,
            stream = true,
        ).apply {
            put("text", JSONObject().put("verbosity", "low"))
            put("prompt_cache_key", "moonlight_reading_chat")
        }
        val request = Request.Builder()
            .url(CODEX_RESPONSES_URL)
            .header("Authorization", "Bearer $accessToken")
            .header("chatgpt-account-id", accountId)
            .header("originator", "moonlight-guidance")
            .header("OpenAI-Beta", "responses=experimental")
            .header("Accept", "text/event-stream")
            .header("Content-Type", JSON_MEDIA_TYPE)
            .post(body.toString().toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("OpenAI account request failed (${response.code}): ${responseBody.take(500)}")
            }
            return extractSseResponseText(responseBody)
                ?: error("OpenAI account response did not include text.")
        }
    }

    private suspend fun refreshCodexIfNeeded(config: AIProviderConfigEntity): AIProviderConfigEntity {
        val expiresAt = config.oauthExpiresAt
        if (expiresAt == null || expiresAt.isAfter(Instant.now().plusSeconds(60))) {
            return config
        }
        val refreshToken = config.oauthRefreshTokenEncrypted
            ?.let(secureStringStore::decrypt)
            ?: return config
        val tokens = openAiCodexOAuthClient.refresh(refreshToken)
        val next = config.copy(
            oauthAccessTokenEncrypted = secureStringStore.encrypt(tokens.accessToken),
            oauthRefreshTokenEncrypted = secureStringStore.encrypt(tokens.refreshToken),
            oauthExpiresAt = tokens.expiresAt,
            oauthAccountId = tokens.accountId,
            oauthAccountLabel = tokens.email ?: tokens.accountId,
        )
        aiProviderConfigDao.upsert(next)
        return next
    }

    private fun buildResponseBody(
        model: String,
        readingContext: String,
        history: List<AiChatMessage>,
        question: String,
        stream: Boolean,
    ): JSONObject {
        val input = JSONArray()
            .put(
                JSONObject()
                    .put("role", "user")
                    .put("content", "Reading context:\n$readingContext"),
            )
        history.takeLast(MAX_HISTORY_MESSAGES).forEach { message ->
            input.put(
                JSONObject()
                    .put("role", message.role.apiRole())
                    .put("content", message.text),
            )
        }
        input.put(
            JSONObject()
                .put("role", "user")
                .put("content", question),
        )
        return JSONObject()
            .put("model", model)
            .put("store", false)
            .put("stream", stream)
            .put("instructions", READING_CHAT_INSTRUCTIONS)
            .put("input", input)
    }

    private fun resolveOpenAiResponsesUrl(config: AIProviderConfigEntity): String {
        val baseUrl = config.baseUrl?.trim()?.trimEnd('/')
        return when {
            config.providerType == AiProviderType.CustomOpenAiCompatible && !baseUrl.isNullOrBlank() ->
                if (baseUrl.endsWith("/responses")) baseUrl else "$baseUrl/responses"

            else -> OPENAI_RESPONSES_URL
        }
    }

    private fun extractResponseText(response: JSONObject): String? {
        val output = response.optJSONArray("output") ?: return null
        val parts = buildList {
            for (i in 0 until output.length()) {
                val item = output.optJSONObject(i) ?: continue
                val content = item.optJSONArray("content") ?: continue
                for (j in 0 until content.length()) {
                    val contentItem = content.optJSONObject(j) ?: continue
                    if (contentItem.optString("type") == "output_text") {
                        val text = contentItem.optString("text")
                        if (text.isNotBlank()) add(text)
                    }
                }
            }
        }
        return parts.joinToString("\n\n").ifBlank { null }
    }

    private fun extractSseResponseText(responseBody: String): String? {
        val completedTexts = mutableListOf<String>()
        val deltas = StringBuilder()
        responseBody.lineSequence()
            .filter { it.startsWith("data:") }
            .map { it.removePrefix("data:").trim() }
            .filter { it.isNotBlank() && it != "[DONE]" }
            .forEach { data ->
                val event = runCatching { JSONObject(data) }.getOrNull() ?: return@forEach
                when (event.optString("type")) {
                    "response.output_text.delta" -> deltas.append(event.optString("delta"))
                    "response.output_text.done" -> {
                        val text = event.optString("text")
                        if (text.isNotBlank()) completedTexts.add(text)
                    }
                    "response.completed", "response.done" -> {
                        extractResponseText(event.optJSONObject("response") ?: JSONObject())
                            ?.let(completedTexts::add)
                    }
                    "response.failed", "error" -> {
                        error(event.toString())
                    }
                }
            }
        return completedTexts.firstOrNull { it.isNotBlank() } ?: deltas.toString().ifBlank { null }
    }

    private fun AiChatRole.apiRole(): String =
        when (this) {
            AiChatRole.User -> "user"
            AiChatRole.Assistant -> "assistant"
        }

    private companion object {
        const val OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses"
        const val CODEX_RESPONSES_URL = "https://chatgpt.com/backend-api/codex/responses"
        const val JSON_MEDIA_TYPE = "application/json"
        const val MAX_HISTORY_MESSAGES = 8
        const val READING_CHAT_INSTRUCTIONS = """
You are a reflective tarot, oracle, and custom deck reading companion inside Moonlight Guidance.
Use the user's deck meanings, slot meanings, card orientations, notes, and correspondences as the primary source of context.
Answer conversationally and help the user explore possible symbolic connections.
Do not claim certainty, predict fixed future outcomes, or tell the user what must happen.
Do not provide medical, legal, financial, or mental health diagnosis or instructions.
Use chakra, crystal, herb, and energy correspondences only as symbolic reflection tools, not as medical advice.
When the user shares a real-life event, discuss how it might resonate with the reading while making clear that the reading does not prove causation.
        """
    }
}
