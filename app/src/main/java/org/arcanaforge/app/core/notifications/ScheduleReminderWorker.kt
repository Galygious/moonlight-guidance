package org.arcanaforge.app.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.arcanaforge.app.core.database.AppDatabase

class ScheduleReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val scheduleId = inputData.getString(ScheduleWorkScheduler.KEY_SCHEDULE_ID)
            ?: return Result.success()
        val database = AppDatabase.build(applicationContext)
        return runCatching {
            val schedule = database.scheduledReadingDao().getSchedule(scheduleId)
                ?: return@runCatching
            if (!schedule.enabled) return@runCatching

            val deck = database.deckDao().getDeck(schedule.deckId)
            val layout = database.layoutDao().getLayout(schedule.layoutId)
            if (deck == null || layout == null) {
                ScheduleNotificationHelper.showScheduleError(
                    context = applicationContext,
                    scheduleId = schedule.id,
                    title = schedule.title,
                )
            } else {
                ScheduleNotificationHelper.showReadingReminder(
                    context = applicationContext,
                    scheduleId = schedule.id,
                    title = schedule.title,
                    deckId = deck.id,
                    layoutId = layout.id,
                    question = schedule.questionTemplate,
                    deckName = deck.name,
                    layoutName = layout.name,
                )
            }

            ScheduleWorkScheduler.enqueue(applicationContext, schedule)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        ).also {
            database.close()
        }
    }
}
