package org.arcanaforge.app.domain.image

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ImageSource {
    @SerialName("uploaded")
    Uploaded,

    @SerialName("generated")
    Generated,

    @SerialName("imported")
    Imported,
}
