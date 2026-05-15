package com.dersium.core.domain.repository

import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LessonRepository {
    fun getAllLessons(seasonId: Long): Flow<List<Lesson>>
    fun getLessonsByStudent(studentId: Long): Flow<List<Lesson>>
    fun getLessonsByDate(date: LocalDate): Flow<List<Lesson>>
    fun getLessonsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Lesson>>
    fun getPendingLessons(seasonId: Long): Flow<List<Lesson>>
    fun getLessonById(id: Long): Flow<Lesson?>
    suspend fun insertLesson(lesson: Lesson): Long
    suspend fun updateLesson(lesson: Lesson)
    suspend fun deleteLesson(lesson: Lesson)
    suspend fun updatePaymentStatus(lessonId: Long, status: PaymentStatus)
    fun getMonthlyLessonStats(seasonId: Long): Flow<Map<String, Pair<Int, Double>>>
}
