package com.dersium.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dersium.core.common.DersiumAnalytics
import com.dersium.core.common.NotificationHelper
import com.dersium.core.data.worker.ReminderScheduler
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DersiumApp : Application(), Configuration.Provider {

    // Required for WorkManager to be able to instantiate @HiltWorker classes (which take
    // injected dependencies beyond the Context/WorkerParameters pair). Without this,
    // WorkManager falls back to its default factory, which doesn't know how to construct
    // a Hilt worker and crashes at run time the moment one is enqueued — which is exactly
    // what happened before and is why the previous boot-reschedule logic was ripped out.
    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject lateinit var reminderScheduler: ReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        DersiumAnalytics.init(this)
        NotificationHelper.createChannel(this)
        reminderScheduler.scheduleDailyReminder()
    }
}
