package com.dersium.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dersium.core.data.worker.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Re-arms the daily reminder after a reboot. WorkManager's own periodic-work storage
 * normally survives a restart on its own, but a couple of OEM battery managers (and a
 * factory reset of app data) can drop it, so re-enqueuing here costs nothing: KEEP policy
 * means it's a no-op if the work is already scheduled.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            reminderScheduler.scheduleDailyReminder()
        }
    }
}
