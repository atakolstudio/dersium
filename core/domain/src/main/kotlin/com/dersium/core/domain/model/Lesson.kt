package com.dersium.core.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Lesson(
    val id: Long = 0,
    val studentId: Long,
    val studentName: String = "",
    val studentAvatarColor: String = "#6366F1",
    val date: LocalDate,
    val startTime: LocalTime,
    val durationMinutes: Int = 60,
    val fee: Double,
    val topic: String = "",
    val notes: String = "",
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val isRecurring: Boolean = false,
    val recurringDayOfWeek: Int? = null,
    val seasonId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    val endTime: LocalTime get() = startTime.plusMinutes(durationMinutes.toLong())
    val isPaid: Boolean get() = paymentStatus == PaymentStatus.PAID
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Bekleyen"),
    PAID("Ödendi"),
    CANCELLED("İptal");

    companion object {
        fun fromName(name: String) = entries.find { it.name == name } ?: PENDING
    }
}
