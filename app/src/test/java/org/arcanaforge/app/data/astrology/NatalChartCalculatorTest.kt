package org.arcanaforge.app.data.astrology

import org.arcanaforge.app.domain.astrology.ChartBody
import org.arcanaforge.app.domain.astrology.NatalChartInput
import org.arcanaforge.app.domain.astrology.ZodiacSign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NatalChartCalculatorTest {
    private val calculator = AstronomyEngineNatalChartCalculator()

    @Test
    fun calculatesCorePlacementsAndWholeSignHouses() {
        val chart = calculator.calculate(
            NatalChartInput(
                label = "Test Chart",
                subjectName = "Tester",
                birthDate = "1990-04-12",
                birthTime = "20:42",
                timeKnown = true,
                zoneId = "America/Chicago",
                locationName = "Chicago",
                latitude = 41.8781,
                longitude = -87.6298,
            ),
        )

        val sun = chart.placements.first { it.body == ChartBody.Sun }
        val moon = chart.placements.first { it.body == ChartBody.Moon }
        val ascendant = chart.placements.first { it.body == ChartBody.Ascendant }

        assertEquals(ZodiacSign.Aries, sun.sign)
        assertTrue(moon.longitude in 0.0..360.0)
        assertNotNull(ascendant.house)
        assertTrue(chart.placements.size >= 12)
        assertTrue(chart.placements.filter { it.body != ChartBody.Midheaven }.all { it.house != null })
        assertFalse(chart.aspects.isEmpty())
    }

    @Test
    fun omitsAnglesAndHousesWhenBirthTimeIsUnknown() {
        val chart = calculator.calculate(
            NatalChartInput(
                label = "Unknown Time",
                subjectName = "Tester",
                birthDate = "1990-04-12",
                birthTime = "12:00",
                timeKnown = false,
                zoneId = "America/Chicago",
                locationName = "",
                latitude = null,
                longitude = null,
            ),
        )

        assertTrue(chart.placements.none { it.body == ChartBody.Ascendant })
        assertTrue(chart.placements.none { it.body == ChartBody.Midheaven })
        assertTrue(chart.placements.all { it.house == null })
    }

    @Test
    fun matchesKnownCafeAstrologyReferenceChartWithinMvpTolerance() {
        val chart = calculator.calculate(
            NatalChartInput(
                label = "Cafe Reference",
                subjectName = "Shawn",
                birthDate = "1996-10-03",
                birthTime = "21:45",
                timeKnown = true,
                zoneId = "America/New_York",
                locationName = "Andrews Air Force Base, MD",
                latitude = 38.82,
                longitude = -76.90,
            ),
        )

        val sun = chart.placements.first { it.body == ChartBody.Sun }
        val moon = chart.placements.first { it.body == ChartBody.Moon }
        val ascendant = chart.placements.first { it.body == ChartBody.Ascendant }
        val midheaven = chart.placements.first { it.body == ChartBody.Midheaven }

        assertEquals(ZodiacSign.Libra, sun.sign)
        assertEquals(ZodiacSign.Cancer, moon.sign)
        assertEquals(ZodiacSign.Gemini, ascendant.sign)
        assertEquals(ZodiacSign.Aquarius, midheaven.sign)
        assertTrue(kotlin.math.abs(sun.degreeInSign - 11.12) < 0.2)
        assertTrue(kotlin.math.abs(moon.degreeInSign - 6.32) < 0.4)
        assertTrue(kotlin.math.abs(ascendant.degreeInSign - 13.17) < 0.8)
        assertTrue(kotlin.math.abs(midheaven.degreeInSign - 19.97) < 0.8)
    }
}
