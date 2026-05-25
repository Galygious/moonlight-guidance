package org.arcanaforge.app.data.astrology

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.rotationEqjEqd
import io.github.cosinekitty.astronomy.siderealTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan
import org.arcanaforge.app.domain.astrology.ChartAspect
import org.arcanaforge.app.domain.astrology.ChartAspectType
import org.arcanaforge.app.domain.astrology.ChartBody
import org.arcanaforge.app.domain.astrology.ChartPlacement
import org.arcanaforge.app.domain.astrology.NatalChartInput
import org.arcanaforge.app.domain.astrology.NatalChartSnapshot
import org.arcanaforge.app.domain.astrology.degreeInSign
import org.arcanaforge.app.domain.astrology.normalizeDegrees
import org.arcanaforge.app.domain.astrology.signedDegreeDelta
import org.arcanaforge.app.domain.astrology.zodiacSignFor

interface NatalChartCalculator {
    fun calculate(input: NatalChartInput): NatalChartSnapshot
}

class AstronomyEngineNatalChartCalculator : NatalChartCalculator {
    override fun calculate(input: NatalChartInput): NatalChartSnapshot {
        val birthInstant = input.toBirthInstant()
        val time = Time.fromMillisecondsSince1970(birthInstant.toEpochMilli())
        val ascendantLongitude = if (input.timeKnown && input.latitude != null && input.longitude != null) {
            approximateAscendantLongitude(time, input.latitude, input.longitude)
        } else {
            null
        }
        val midheavenLongitude = if (input.timeKnown && input.longitude != null) {
            approximateMidheavenLongitude(time, input.longitude)
        } else {
            null
        }
        val ascendantSign = ascendantLongitude?.let(::zodiacSignFor)
        val placements = buildList {
            planetBodies.forEach { (chartBody, astronomyBody) ->
                val longitude = eclipticLongitudeOfDate(astronomyBody, time)
                add(
                    ChartPlacement(
                        body = chartBody,
                        longitude = longitude,
                        sign = zodiacSignFor(longitude),
                        degreeInSign = degreeInSign(longitude),
                        house = ascendantSign?.let { wholeSignHouse(zodiacSignFor(longitude).ordinal, it.ordinal) },
                        retrograde = isRetrograde(chartBody, astronomyBody, time),
                    ),
                )
            }
            if (ascendantLongitude != null) {
                add(
                    ChartPlacement(
                        body = ChartBody.Ascendant,
                        longitude = ascendantLongitude,
                        sign = zodiacSignFor(ascendantLongitude),
                        degreeInSign = degreeInSign(ascendantLongitude),
                        house = 1,
                    ),
                )
            }
            if (midheavenLongitude != null) {
                add(
                    ChartPlacement(
                        body = ChartBody.Midheaven,
                        longitude = midheavenLongitude,
                        sign = zodiacSignFor(midheavenLongitude),
                        degreeInSign = degreeInSign(midheavenLongitude),
                    ),
                )
            }
        }

        return NatalChartSnapshot(
            calculatedAtUtc = Instant.now().toString(),
            birthInstantUtc = birthInstant.toString(),
            timeKnown = input.timeKnown,
            locationName = input.locationName,
            latitude = input.latitude,
            longitude = input.longitude,
            placements = placements,
            aspects = calculateAspects(placements.filter { it.body !in angleOnlyBodies }),
        )
    }

    private fun NatalChartInput.toBirthInstant(): Instant {
        val date = LocalDate.parse(birthDate)
        val time = if (timeKnown) LocalTime.parse(birthTime) else LocalTime.NOON
        return date.atTime(time).atZone(ZoneId.of(zoneId)).toInstant()
    }

    private fun eclipticLongitudeOfDate(body: Body, time: Time): Double {
        val eqj = geoVector(body, time, Aberration.Corrected)
        val eqd = rotationEqjEqd(time).rotate(eqj)
        return normalizeDegrees(eclipticLongitudeFromEquatorOfDate(eqd.x, eqd.y, eqd.z, time))
    }

    private fun eclipticLongitudeFromEquatorOfDate(x: Double, y: Double, z: Double, time: Time): Double {
        val obliquity = meanObliquityRadians(time)
        val eclipticY = y * cos(obliquity) + z * sin(obliquity)
        return Math.toDegrees(atan2(eclipticY, x))
    }

    private fun meanObliquityRadians(time: Time): Double {
        val jd = time.ut + 2451545.0
        val t = (jd - 2451545.0) / 36525.0
        val seconds = 84381.448 - 46.8150 * t - 0.00059 * t * t + 0.001813 * t * t * t
        return Math.toRadians(seconds / 3600.0)
    }

    private fun isRetrograde(chartBody: ChartBody, body: Body, time: Time): Boolean {
        if (chartBody == ChartBody.Sun || chartBody == ChartBody.Moon) {
            return false
        }
        val before = eclipticLongitudeOfDate(body, time.addDays(-0.5))
        val after = eclipticLongitudeOfDate(body, time.addDays(0.5))
        return signedDegreeDelta(before, after) < 0.0
    }

    private fun calculateAspects(placements: List<ChartPlacement>): List<ChartAspect> =
        buildList {
            placements.forEachIndexed { index, first ->
                placements.drop(index + 1).forEach { second ->
                    val separation = kotlin.math.abs(signedDegreeDelta(first.longitude, second.longitude))
                    val aspect = ChartAspectType.entries.minBy { kotlin.math.abs(separation - it.angle) }
                    val orb = kotlin.math.abs(separation - aspect.angle)
                    if (orb <= DEFAULT_ASPECT_ORB) {
                        add(
                            ChartAspect(
                                first = first.body,
                                second = second.body,
                                type = aspect,
                                orb = orb,
                            ),
                        )
                    }
                }
            }
        }.sortedWith(compareBy<ChartAspect> { it.type.angle }.thenBy { it.orb })

    private fun wholeSignHouse(signOrdinal: Int, ascendantSignOrdinal: Int): Int =
        ((signOrdinal - ascendantSignOrdinal + 12) % 12) + 1

    private fun approximateAscendantLongitude(time: Time, latitude: Double, longitude: Double): Double {
        val siderealRadians = localSiderealRadians(time, longitude)
        val obliquity = meanObliquityRadians(time)
        val latitudeRadians = Math.toRadians(latitude)
        val y = -cos(siderealRadians)
        val x = sin(siderealRadians) * cos(obliquity) + tan(latitudeRadians) * sin(obliquity)
        return normalizeDegrees(Math.toDegrees(atan2(y, x)) + 180.0)
    }

    private fun approximateMidheavenLongitude(time: Time, longitude: Double): Double {
        val siderealRadians = localSiderealRadians(time, longitude)
        val obliquity = meanObliquityRadians(time)
        return normalizeDegrees(Math.toDegrees(atan2(sin(siderealRadians), cos(siderealRadians) * cos(obliquity))))
    }

    private fun localSiderealRadians(time: Time, longitude: Double): Double {
        val degrees = normalizeDegrees(siderealTime(time) * 15.0 + longitude)
        return degrees * PI / 180.0
    }

    private companion object {
        const val DEFAULT_ASPECT_ORB = 6.0
        val angleOnlyBodies = setOf(ChartBody.Ascendant, ChartBody.Midheaven)
        val planetBodies = listOf(
            ChartBody.Sun to Body.Sun,
            ChartBody.Moon to Body.Moon,
            ChartBody.Mercury to Body.Mercury,
            ChartBody.Venus to Body.Venus,
            ChartBody.Mars to Body.Mars,
            ChartBody.Jupiter to Body.Jupiter,
            ChartBody.Saturn to Body.Saturn,
            ChartBody.Uranus to Body.Uranus,
            ChartBody.Neptune to Body.Neptune,
            ChartBody.Pluto to Body.Pluto,
        )
    }
}
