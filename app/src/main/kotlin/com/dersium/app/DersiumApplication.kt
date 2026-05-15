package com.dersium.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DersiumApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        listOf(
            NotificationChannel(
                CHANNEL_LESSONS,
                "Ders Hatırlatmaları",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Yaklaşan ders bildirimleri" },
            NotificationChannel(
                CHANNEL_PAYMENTS,
                "Ödeme Hatırlatmaları",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Bekleyen ödeme bildirimleri" },
            NotificationChannel(
                CHANNEL_GENERAL,
                "Genel",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Genel uygulama bildirimleri" },
        ).forEach { nm.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_LESSONS  = "dersium_lessons"
        const val CHANNEL_PAYMENTS = "dersium_payments"
        const val CHANNEL_GENERAL  = "dersium_general"
    }
}
