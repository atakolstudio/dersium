package com.dersium.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dersium.core.data.worker.RescheduleNotificationsWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val rescheduleWork = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>().build()
            WorkManager.getInstance(context).enqueue(rescheduleWork)
        }
    }
}
