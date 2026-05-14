package org.arcanaforge.app.data.schedule

import kotlinx.coroutines.flow.Flow
import org.arcanaforge.app.core.database.dao.ScheduledReadingDao
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity

interface ScheduleRepository {
    fun observeNextEnabledSchedule(): Flow<ScheduledReadingEntity?>
}

class OfflineScheduleRepository(
    private val scheduledReadingDao: ScheduledReadingDao,
) : ScheduleRepository {
    override fun observeNextEnabledSchedule(): Flow<ScheduledReadingEntity?> =
        scheduledReadingDao.observeNextEnabledSchedule()
}
