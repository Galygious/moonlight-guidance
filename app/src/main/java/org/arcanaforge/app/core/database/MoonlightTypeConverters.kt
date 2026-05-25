package org.arcanaforge.app.core.database

import androidx.room.TypeConverter
import java.time.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.arcanaforge.app.domain.ai.AiAuthMode
import org.arcanaforge.app.domain.ai.AiChatRole
import org.arcanaforge.app.domain.ai.AiProviderType
import org.arcanaforge.app.domain.correspondence.CardCorrespondences
import org.arcanaforge.app.domain.deck.DeckType
import org.arcanaforge.app.domain.image.ImageSource
import org.arcanaforge.app.domain.reading.ReadingOrientation

class MoonlightTypeConverters {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun stringListToJson(value: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), value)

    @TypeConverter
    fun jsonToStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) {
            emptyList()
        } else {
            json.decodeFromString(ListSerializer(String.serializer()), value)
        }

    @TypeConverter
    fun stringListMapToJson(value: Map<String, List<String>>): String =
        json.encodeToString(
            MapSerializer(String.serializer(), ListSerializer(String.serializer())),
            value,
        )

    @TypeConverter
    fun jsonToStringListMap(value: String?): Map<String, List<String>> =
        if (value.isNullOrBlank()) {
            emptyMap()
        } else {
            json.decodeFromString(
                MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                value,
            )
        }

    @TypeConverter
    fun correspondencesToJson(value: CardCorrespondences): String =
        json.encodeToString(CardCorrespondences.serializer(), value)

    @TypeConverter
    fun jsonToCorrespondences(value: String?): CardCorrespondences =
        if (value.isNullOrBlank()) {
            CardCorrespondences()
        } else {
            json.decodeFromString(CardCorrespondences.serializer(), value)
        }

    @TypeConverter
    fun deckTypeToString(value: DeckType): String = value.name

    @TypeConverter
    fun stringToDeckType(value: String): DeckType = enumValueOf(value)

    @TypeConverter
    fun imageSourceToString(value: ImageSource): String = value.name

    @TypeConverter
    fun stringToImageSource(value: String): ImageSource = enumValueOf(value)

    @TypeConverter
    fun readingOrientationToString(value: ReadingOrientation): String = value.name

    @TypeConverter
    fun stringToReadingOrientation(value: String): ReadingOrientation = enumValueOf(value)

    @TypeConverter
    fun aiProviderTypeToString(value: AiProviderType): String = value.name

    @TypeConverter
    fun stringToAiProviderType(value: String): AiProviderType = enumValueOf(value)

    @TypeConverter
    fun aiAuthModeToString(value: AiAuthMode): String = value.name

    @TypeConverter
    fun stringToAiAuthMode(value: String): AiAuthMode = enumValueOf(value)

    @TypeConverter
    fun aiChatRoleToString(value: AiChatRole): String = value.name

    @TypeConverter
    fun stringToAiChatRole(value: String): AiChatRole = enumValueOf(value)
}
