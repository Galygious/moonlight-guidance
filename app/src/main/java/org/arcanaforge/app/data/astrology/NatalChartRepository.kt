package org.arcanaforge.app.data.astrology

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.arcanaforge.app.core.database.dao.NatalChartDao
import org.arcanaforge.app.core.database.entity.NatalChartEntity
import org.arcanaforge.app.domain.astrology.NatalChartInput
import org.arcanaforge.app.domain.astrology.NatalChartSnapshot

data class NatalChartRecord(
    val entity: NatalChartEntity,
    val snapshot: NatalChartSnapshot,
)

interface NatalChartRepository {
    fun observeCharts(): Flow<List<NatalChartRecord>>
    suspend fun getChart(id: String): NatalChartRecord?
    suspend fun createChart(input: NatalChartInput): String
    suspend fun updateNotes(id: String, notes: String)
    suspend fun updateFavorite(id: String, isFavorite: Boolean)
    suspend fun delete(id: String)
}

class OfflineNatalChartRepository(
    private val natalChartDao: NatalChartDao,
    private val natalChartCalculator: NatalChartCalculator,
) : NatalChartRepository {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    override fun observeCharts(): Flow<List<NatalChartRecord>> =
        natalChartDao.observeCharts().map { charts ->
            charts.map { it.toRecord() }
        }

    override suspend fun getChart(id: String): NatalChartRecord? =
        natalChartDao.getById(id)?.toRecord()

    override suspend fun createChart(input: NatalChartInput): String {
        val now = Instant.now()
        val id = UUID.randomUUID().toString()
        val snapshot = natalChartCalculator.calculate(input)
        val subject = input.subjectName.trim().ifBlank { "Untitled" }
        val label = input.label.trim().ifBlank { "$subject Natal Chart" }
        natalChartDao.insert(
            NatalChartEntity(
                id = id,
                label = label,
                subjectName = subject,
                birthDate = input.birthDate,
                birthTime = input.birthTime,
                timeKnown = input.timeKnown,
                zoneId = input.zoneId,
                locationName = input.locationName.trim(),
                latitude = input.latitude,
                longitude = input.longitude,
                houseSystem = snapshot.houseSystem,
                chartJson = json.encodeToString(NatalChartSnapshot.serializer(), snapshot),
                createdAt = now,
                updatedAt = now,
            ),
        )
        return id
    }

    override suspend fun updateNotes(id: String, notes: String) {
        natalChartDao.updateNotes(id, notes, Instant.now())
    }

    override suspend fun updateFavorite(id: String, isFavorite: Boolean) {
        natalChartDao.updateFavorite(id, isFavorite, Instant.now())
    }

    override suspend fun delete(id: String) {
        natalChartDao.deleteById(id)
    }

    private fun NatalChartEntity.toRecord(): NatalChartRecord =
        NatalChartRecord(
            entity = this,
            snapshot = json.decodeFromString(NatalChartSnapshot.serializer(), chartJson),
        )
}

