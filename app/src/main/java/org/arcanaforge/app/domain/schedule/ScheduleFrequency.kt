package org.arcanaforge.app.domain.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ScheduleFrequency {
    @SerialName("daily")
    Daily,

    @SerialName("weekly")
    Weekly,

    @SerialName("monthly")
    Monthly,

    @SerialName("specific_weekdays")
    SpecificWeekdays,
}
