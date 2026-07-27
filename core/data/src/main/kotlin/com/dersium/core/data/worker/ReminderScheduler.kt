package com.dersium.core.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily lesson/payment reminder notification via WorkManager. Kept as its
 * own small class so both [com.dersium.app.DersiumApp] (app start) and the boot receiver
 * (device restart) call the exact same scheduling logic — WorkManager's own
 * ExistingPeriodicWorkPolicy.KEEP makes both calls safely idempotent, so calling this on
 * every app launch never resets or duplicates the timer.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scheduleDailyReminder() {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(REMINDER_HOUR, 0)
        if (!now.isBefore(next)) next = next.plusDays(1)
        val initialDelayMinutes = ChronoUnit.MINUTES.between(now, next).coerceAtLeast(1)

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(false).build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelDailyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        private const val REMINDER_HOUR = 8
        const val UNIQUE_WORK_NAME = "daily_lesson_reminder"
    }
}
