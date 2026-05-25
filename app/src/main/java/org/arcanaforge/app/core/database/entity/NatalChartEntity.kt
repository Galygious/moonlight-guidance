package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "natal_charts",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["is_favorite"]),
    ],
)
data class NatalChartEntity(
    @PrimaryKey val id: String,
    val label: String,
    @ColumnInfo(name = "subject_name") val subjectName: String,
    @ColumnInfo(name = "birth_date") val birthDate: String,
    @ColumnInfo(name = "birth_time") val birthTime: String,
    @ColumnInfo(name = "time_known") val timeKnown: Boolean,
    @ColumnInfo(name = "zone_id") val zoneId: String,
    @ColumnInfo(name = "location_name") val locationName: String,
    val latitude: Double?,
    val longitude: Double?,
    @ColumnInfo(name = "house_system") val houseSystem: String,
    @ColumnInfo(name = "chart_json") val chartJson: String,
    val notes: String = "",
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)

