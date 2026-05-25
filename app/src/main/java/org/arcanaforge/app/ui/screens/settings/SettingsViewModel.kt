package org.arcanaforge.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.arcanaforge.app.core.database.entity.AIProviderConfigEntity
import org.arcanaforge.app.data.ai.AiProviderRepository
import org.arcanaforge.app.domain.ai.AiAuthMode
import org.arcanaforge.app.domain.ai.AiProviderType

data class SettingsUiState(
    val aiConfigs: List<AIProviderConfigEntity> = emptyList(),
    val apiKeyName: String = "OpenAI API key",
    val apiKeyProviderType: AiProviderType = AiProviderType.OpenAi,
    val apiKeyBaseUrl: String = "",
    val apiKeyValue: String = "",
    val apiKeyImageModel: String = "",
    val apiKeyTextModel: String = "",
    val isSavingApiKey: Boolean = false,
    val isConnectingOAuth: Boolean = false,
    val authUrlToOpen: String? = null,
    val message: String? = null,
)

class SettingsViewModel(
    private val aiProviderRepository: AiProviderRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        aiProviderRepository.observeConfigs(),
        formState,
    ) { configs, form ->
        form.copy(aiConfigs = configs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun updateApiKeyName(value: String) {
        formState.update { it.copy(apiKeyName = value) }
    }

    fun updateApiKeyProviderType(value: AiProviderType) {
        formState.update {
            it.copy(
                apiKeyProviderType = value,
                apiKeyName = when (value) {
                    AiProviderType.OpenAi -> "OpenAI API key"
                    AiProviderType.CustomOpenAiCompatible -> "OpenAI-compatible API"
                    else -> it.apiKeyName
                },
            )
        }
    }

    fun updateApiKeyBaseUrl(value: String) {
        formState.update { it.copy(apiKeyBaseUrl = value) }
    }

    fun updateApiKeyValue(value: String) {
        formState.update { it.copy(apiKeyValue = value) }
    }

    fun updateApiKeyImageModel(value: String) {
        formState.update { it.copy(apiKeyImageModel = value) }
    }

    fun updateApiKeyTextModel(value: String) {
        formState.update { it.copy(apiKeyTextModel = value) }
    }

    fun saveApiKeyConfig() {
        val current = formState.value
        if (current.apiKeyValue.isBlank()) {
            formState.update { it.copy(message = "Enter an API key before saving.") }
            return
        }
        viewModelScope.launch {
            formState.update { it.copy(isSavingApiKey = true, message = null) }
            runCatching {
                aiProviderRepository.saveApiKeyConfig(
                    id = null,
                    name = current.apiKeyName,
                    providerType = current.apiKeyProviderType,
                    baseUrl = current.apiKeyBaseUrl,
                    apiKey = current.apiKeyValue,
                    imageModel = current.apiKeyImageModel,
                    textModel = current.apiKeyTextModel,
                    enabled = true,
                )
            }.onSuccess {
                formState.update {
                    it.copy(
                        apiKeyValue = "",
                        isSavingApiKey = false,
                        message = "AI provider saved.",
                    )
                }
            }.onFailure { error ->
                formState.update {
                    it.copy(
                        isSavingApiKey = false,
                        message = error.message ?: "Could not save AI provider.",
                    )
                }
            }
        }
    }

    fun connectOpenAiAccount() {
        if (formState.value.isConnectingOAuth) {
            return
        }
        viewModelScope.launch {
            formState.update { it.copy(isConnectingOAuth = true, message = null) }
            val session = runCatching {
                aiProviderRepository.startOpenAiCodexOAuth()
            }.getOrElse { error ->
                formState.update {
                    it.copy(
                        isConnectingOAuth = false,
                        message = error.message ?: "Could not start OpenAI account sign-in.",
                    )
                }
                return@launch
            }
            formState.update { it.copy(authUrlToOpen = session.authorizationUrl) }
            runCatching {
                withTimeout(OAUTH_TIMEOUT_MILLIS) {
                    aiProviderRepository.finishOpenAiCodexOAuth(session)
                }
            }.onSuccess {
                formState.update {
                    it.copy(
                        isConnectingOAuth = false,
                        authUrlToOpen = null,
                        message = "OpenAI account connected.",
                    )
                }
            }.onFailure { error ->
                session.close()
                formState.update {
                    it.copy(
                        isConnectingOAuth = false,
                        authUrlToOpen = null,
                        message = error.message ?: "OpenAI account sign-in did not finish.",
                    )
                }
            }
        }
    }

    fun authUrlOpened() {
        formState.update { it.copy(authUrlToOpen = null) }
    }

    fun setProviderEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch {
            aiProviderRepository.setEnabled(id, enabled)
        }
    }

    fun deleteProvider(id: String) {
        viewModelScope.launch {
            aiProviderRepository.delete(id)
        }
    }

    fun clearMessage() {
        formState.update { it.copy(message = null) }
    }

    companion object {
        private const val OAUTH_TIMEOUT_MILLIS = 5 * 60 * 1000L
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

        fun AIProviderConfigEntity.displaySubtitle(): String {
            val authLabel = when (authMode) {
                AiAuthMode.ApiKey -> "API key"
                AiAuthMode.OpenAiCodexOAuth -> "OpenAI account"
            }
            val modelLabel = listOfNotNull(textModel, imageModel).joinToString(" / ")
            return listOf(
                authLabel,
                oauthAccountLabel,
                oauthExpiresAt?.atZone(ZoneId.systemDefault())?.format(dateTimeFormatter)
                    ?.let { "expires $it" },
                modelLabel.ifBlank { null },
            ).joinToString(" - ")
        }
    }
}
