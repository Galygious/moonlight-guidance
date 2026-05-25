package org.arcanaforge.app.data.schedule

import android.content.Context
import java.time.Instant
import java.time.LocalTime
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.ScheduledReadingDao
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity
import org.arcanaforge.app.core.notifications.ScheduleWorkScheduler
import org.arcanaforge.app.domain.schedule.ScheduleRuleCodec

interface ScheduleRepository {
    fun observeSchedules(): Flow<List<ScheduledReadingEntity>>
    fun observeNextEnabledSchedule(): Flow<ScheduledReadingEntity?>
    suspend fun createSchedule(
        title: String,
        deckId: String,
        layoutId: String,
        questionTemplate: String,
        scheduleRule: String,
        reminderTime: LocalTime,
    ): ScheduledReadingEntity
    suspend fun setEnabled(schedule: ScheduledReadingEntity, enabled: Boolean)
    suspend fun deleteSchedule(schedule: ScheduledReadingEntity)
}

class OfflineScheduleRepository(
    private val context: Context,
    private val scheduledReadingDao: ScheduledReadingDao,
) : ScheduleRepository {
    override fun observeSchedules(): Flow<List<ScheduledReadingEntity>> =
        scheduledReadingDao.observeSchedules()

    override fun observeNextEnabledSchedule(): Flow<ScheduledReadingEntity?> =
        scheduledReadingDao.observeNextEnabledSchedule()

    override suspend fun createSchedule(
        title: String,
        deckId: String,
        layoutId: String,
        questionTemplate: String,
        scheduleRule: String,
        reminderTime: LocalTime,
    ): ScheduledReadingEntity {
        val now = Instant.now()
        val schedule = ScheduledReadingEntity(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Scheduled Reading" },
            deckId = deckId,
            layoutId = layoutId,
            questionTemplate = questionTemplate.trim(),
            scheduleRule = when (scheduleRule) {
                ScheduleRuleCodec.Weekly -> ScheduleRuleCodec.Weekly
                ScheduleRuleCodec.Monthly -> ScheduleRuleCodec.Monthly
                else -> ScheduleRuleCodec.Daily
            },
            reminderTime = reminderTime.toString(),
            enabled = true,
            autoCreateReading = false,
            createdAt = now,
            updatedAt = now,
        )
        scheduledReadingDao.insert(schedule)
        ScheduleWorkScheduler.enqueue(context, schedule)
        return schedule
    }

    override suspend fun setEnabled(schedule: ScheduledReadingEntity, enabled: Boolean) {
        val updatedSchedule = schedule.copy(enabled = enabled, updatedAt = Instant.now())
        scheduledReadingDao.upsert(updatedSchedule)
        ScheduleWorkScheduler.enqueue(context, updatedSchedule)
    }

    override suspend fun deleteSchedule(schedule: ScheduledReadingEntity) {
        scheduledReadingDao.deleteById(schedule.id)
        ScheduleWorkScheduler.cancel(context, schedule.id)
    }
}
