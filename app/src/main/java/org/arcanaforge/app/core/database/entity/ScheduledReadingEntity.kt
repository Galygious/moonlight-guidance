package org.arcanaforge.app.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "scheduled_readings",
    indices = [
        Index(value = ["enabled"]),
        Index(value = ["deck_id"]),
        Index(value = ["layout_id"]),
    ],
)
data class ScheduledReadingEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "deck_id") val deckId: String,
    @ColumnInfo(name = "layout_id") val layoutId: String,
    @ColumnInfo(name = "question_template") val questionTemplate: String = "",
    @ColumnInfo(name = "schedule_rule") val scheduleRule: String,
    @ColumnInfo(name = "reminder_time") val reminderTime: String,
    val enabled: Boolean = true,
    @ColumnInfo(name = "auto_create_reading") val autoCreateReading: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
)
