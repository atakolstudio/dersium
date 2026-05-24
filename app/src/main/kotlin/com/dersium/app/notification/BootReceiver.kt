package com.dersium.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // WorkManager yerine sadece log - crash önlenir
            android.util.Log.d("BootReceiver", "Boot completed - Dersium")
        }
    }
}
