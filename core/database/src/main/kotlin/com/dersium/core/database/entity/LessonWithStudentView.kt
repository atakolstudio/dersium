package com.dersium.core.database.entity

import androidx.room.DatabaseView

@DatabaseView(
    """
    SELECT 
        l.id,
        l.studentId,
        s.name || ' ' || s.surname AS studentName,
        s.avatarColor AS studentAvatarColor,
        l.date,
        l.startTimeMinutes,
        l.durationMinutes,
        l.fee,
        l.topic,
        l.notes,
        l.paymentStatus,
        l.isRecurring,
        l.recurringDayOfWeek,
        l.seasonId,
        l.createdAt
    FROM lessons l
    INNER JOIN students s ON l.studentId = s.id
    """,
    viewName = "lesson_with_student"
)
data class LessonWithStudentView(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val studentAvatarColor: String,
    val date: Long,
    val startTimeMinutes: Int,
    val durationMinutes: Int,
    val fee: Double,
    val topic: String,
    val notes: String,
    val paymentStatus: String,
    val isRecurring: Boolean,
    val recurringDayOfWeek: Int?,
    val seasonId: Long,
    val createdAt: Long,
)
