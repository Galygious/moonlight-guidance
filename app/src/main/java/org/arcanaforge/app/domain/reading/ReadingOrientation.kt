package org.arcanaforge.app.domain.reading

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReadingOrientation {
    @SerialName("upright")
    Upright,

    @SerialName("reversed")
    Reversed,
}
