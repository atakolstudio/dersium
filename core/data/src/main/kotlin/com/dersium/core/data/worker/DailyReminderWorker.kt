package com.dersium.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dersium.core.common.NotificationHelper
import com.dersium.core.domain.repository.FinancialRepository
import com.dersium.core.domain.repository.LessonRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Runs once a day (scheduled by [ReminderScheduler]) and shows, at most, two local
 * notifications: today's lesson digest and — if anything is unpaid — a pending-payment
 * digest for the active season. Everything here is read-only and local (Room + DataStore
 * via the repositories), no network involved, so there is nothing that can meaningfully
 * fail besides a transient DB read — hence the retry() on unexpected exceptions rather
 * than letting WorkManager mark the whole periodic series as failed.
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val lessonRepository: LessonRepository,
    private val financialRepository: FinancialRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = userPreferencesRepository.userPreferences.first()
            if (!prefs.dailyReminderEnabled) return Result.success()

            val seasonId = prefs.activeSeasonId
            val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

            val todayLessons = lessonRepository.getLessonsByDate(LocalDate.now()).first()
                .filter { it.seasonId == seasonId }
                .sortedBy { it.startTime }

            if (todayLessons.isNotEmpty()) {
                val first = todayLessons.first()
                val message = if (todayLessons.size == 1) {
                    "${first.studentName} ile bugün saat ${first.startTime.format(timeFmt)}'de dersiniz var."
                } else {
                    "Bugün ${todayLessons.size} dersiniz var. İlki: ${first.studentName}, saat ${first.startTime.format(timeFmt)}."
                }
                NotificationHelper.showDigestNotification(
                    applicationContext,
                    title = "Bugünkü Dersler",
                    message = message,
                    notificationId = DAILY_LESSONS_NOTIFICATION_ID,
                )
            }

            val summary = financialRepository.getFinancialSummary(seasonId).first()
            if (summary.pendingAmount > 0) {
                val amount = summary.pendingAmount.toInt()
                NotificationHelper.showDigestNotification(
                    applicationContext,
                    title = "Bekleyen Ödemeler",
                    message = "${summary.pendingLessons} ders için toplam $amount ${prefs.currency} ödeme bekliyor.",
                    notificationId = PENDING_PAYMENTS_NOTIFICATION_ID,
                )
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val DAILY_LESSONS_NOTIFICATION_ID = 101
        private const val PENDING_PAYMENTS_NOTIFICATION_ID = 102
    }
}
