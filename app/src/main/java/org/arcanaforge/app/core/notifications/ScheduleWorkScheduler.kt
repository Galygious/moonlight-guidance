package org.arcanaforge.app.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import org.arcanaforge.app.core.database.entity.ScheduledReadingEntity
import org.arcanaforge.app.domain.schedule.ScheduleRuleCodec

object ScheduleWorkScheduler {
    const val KEY_SCHEDULE_ID = "schedule_id"

    fun enqueue(context: Context, schedule: ScheduledReadingEntity) {
        if (!schedule.enabled) {
            cancel(context, schedule.id)
            return
        }

        val delayMillis = ScheduleRuleCodec.nextDelayMillis(
            rule = schedule.scheduleRule,
            reminderTime = schedule.reminderTime,
        )
        val request = OneTimeWorkRequestBuilder<ScheduleReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(KEY_SCHEDULE_ID, schedule.id)
                    .build(),
            )
            .addTag(workName(schedule.id))
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            workName(schedule.id),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(context: Context, scheduleId: String) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(workName(scheduleId))
    }

    private fun workName(scheduleId: String): String = "scheduled-reading-$scheduleId"
}
