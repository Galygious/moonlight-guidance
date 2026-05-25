package org.arcanaforge.app.domain.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AiProviderType {
    @SerialName("openai")
    OpenAi,

    @SerialName("openai_codex")
    OpenAiCodex,

    @SerialName("stability")
    Stability,

    @SerialName("replicate")
    Replicate,

    @SerialName("custom_openai_compatible")
    CustomOpenAiCompatible,

    @SerialName("comfyui")
    ComfyUi,

    @SerialName("local")
    Local,
}
