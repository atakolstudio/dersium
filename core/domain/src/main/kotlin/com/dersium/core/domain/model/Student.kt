package com.dersium.core.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class ScheduleSlot(
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val durationMinutes: Int = 60,
)

data class Student(
    val id: Long = 0,
    val name: String,
    val surname: String,
    val avatarColor: String = "#6366F1",
    val lessonFee: Double,
    val paymentType: PaymentType,
    val lessonCountForPayment: Int = 1,
    val school: String = "",
    val grade: String = "",
    val motherName: String = "",
    val motherPhone: String = "",
    val fatherName: String = "",
    val fatherPhone: String = "",
    val phone: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val seasonId: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduleSlots: List<ScheduleSlot> = emptyList(),
) {
    val fullName: String get() = "$name $surname".trim()
    val initials: String get() = buildString {
        name.firstOrNull()?.let { append(it.uppercaseChar()) }
        surname.firstOrNull()?.let { append(it.uppercaseChar()) }
    }
    val parentName: String get() = motherName.ifEmpty { fatherName }
    val parentPhone: String get() = motherPhone.ifEmpty { fatherPhone }
}

enum class PaymentType(val displayName: String) {
    UPFRONT("Peşin Ödeme"),
    AFTER_LESSON("Ders Sonrası"),
    AFTER_CERTAIN_LESSONS("Belirli Ders Sonrası"),
    MONTHLY("Aylık Ödeme");
    companion object { fun fromName(name: String) = entries.find { it.name == name } ?: UPFRONT }
}
