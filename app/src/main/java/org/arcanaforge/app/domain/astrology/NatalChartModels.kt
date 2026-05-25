package org.arcanaforge.app.domain.astrology

import kotlinx.serialization.Serializable

enum class ChartBody(val displayName: String) {
    Sun("Sun"),
    Moon("Moon"),
    Mercury("Mercury"),
    Venus("Venus"),
    Mars("Mars"),
    Jupiter("Jupiter"),
    Saturn("Saturn"),
    Uranus("Uranus"),
    Neptune("Neptune"),
    Pluto("Pluto"),
    Ascendant("Ascendant"),
    Midheaven("Midheaven"),
}

enum class ZodiacSign(val displayName: String) {
    Aries("Aries"),
    Taurus("Taurus"),
    Gemini("Gemini"),
    Cancer("Cancer"),
    Leo("Leo"),
    Virgo("Virgo"),
    Libra("Libra"),
    Scorpio("Scorpio"),
    Sagittarius("Sagittarius"),
    Capricorn("Capricorn"),
    Aquarius("Aquarius"),
    Pisces("Pisces"),
}

enum class ChartAspectType(
    val displayName: String,
    val angle: Double,
) {
    Conjunction("Conjunction", 0.0),
    Sextile("Sextile", 60.0),
    Square("Square", 90.0),
    Trine("Trine", 120.0),
    Opposition("Opposition", 180.0),
}

@Serializable
data class NatalChartSnapshot(
    val calculatedAtUtc: String,
    val birthInstantUtc: String,
    val timeKnown: Boolean,
    val locationName: String,
    val latitude: Double?,
    val longitude: Double?,
    val houseSystem: String = "Whole Sign",
    val placements: List<ChartPlacement> = emptyList(),
    val aspects: List<ChartAspect> = emptyList(),
)

@Serializable
data class ChartPlacement(
    val body: ChartBody,
    val longitude: Double,
    val sign: ZodiacSign,
    val degreeInSign: Double,
    val house: Int? = null,
    val retrograde: Boolean = false,
)

@Serializable
data class ChartAspect(
    val first: ChartBody,
    val second: ChartBody,
    val type: ChartAspectType,
    val orb: Double,
)

data class NatalChartInput(
    val label: String,
    val subjectName: String,
    val birthDate: String,
    val birthTime: String,
    val timeKnown: Boolean,
    val zoneId: String,
    val locationName: String,
    val latitude: Double?,
    val longitude: Double?,
)

fun zodiacSignFor(longitude: Double): ZodiacSign {
    val normalized = normalizeDegrees(longitude)
    return ZodiacSign.entries[(normalized / 30.0).toInt().coerceIn(0, 11)]
}

fun degreeInSign(longitude: Double): Double = normalizeDegrees(longitude) % 30.0

fun normalizeDegrees(value: Double): Double {
    val normalized = value % 360.0
    return if (normalized < 0.0) normalized + 360.0 else normalized
}

fun signedDegreeDelta(from: Double, to: Double): Double {
    var delta = normalizeDegrees(to) - normalizeDegrees(from)
    if (delta > 180.0) delta -= 360.0
    if (delta < -180.0) delta += 360.0
    return delta
}

