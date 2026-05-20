package com.dersium.core.common

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object DersiumAnalytics {

    private var analytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    // Ders olayları
    fun logLessonAdded(studentName: String, fee: Double) {
        log("lesson_added", Bundle().apply {
            putString("student_name", studentName)
            putDouble("fee", fee)
        })
    }

    fun logLessonPaid(fee: Double) {
        log("lesson_paid", Bundle().apply {
            putDouble("fee", fee)
        })
    }

    // Öğrenci olayları
    fun logStudentAdded(paymentType: String) {
        log("student_added", Bundle().apply {
            putString("payment_type", paymentType)
        })
    }

    // Rapor olayları
    fun logPdfGenerated() {
        log("pdf_generated")
    }

    fun logBackupCreated() {
        log("backup_created")
    }

    // WhatsApp olayları
    fun logWhatsAppReminderSent(parentType: String) {
        log("whatsapp_reminder_sent", Bundle().apply {
            putString("parent_type", parentType)
        })
    }

    // Ekran görüntüleme
    fun logScreenView(screenName: String) {
        log(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    private fun log(event: String, params: Bundle? = null) {
        try {
            analytics?.logEvent(event, params)
        } catch (_: Exception) {}
    }
}
