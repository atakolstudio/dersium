package com.dersium.core.data.repository

import com.dersium.core.data.mapper.toDomain
import com.dersium.core.data.mapper.toEntity
import com.dersium.core.database.dao.LessonDao
import com.dersium.core.domain.model.Lesson
import com.dersium.core.domain.model.PaymentStatus
import com.dersium.core.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
) : LessonRepository {

    override fun getAllLessons(seasonId: Long): Flow<List<Lesson>> =
        lessonDao.getAllLessons(seasonId).map { it.map { e -> e.toDomain() } }

    override fun getLessonsByStudent(studentId: Long): Flow<List<Lesson>> =
        lessonDao.getLessonsByStudent(studentId).map { it.map { e -> e.toDomain() } }

    override fun getLessonsByDate(date: LocalDate): Flow<List<Lesson>> {
        val epochMilli = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return lessonDao.getLessonsByDate(epochMilli).map { it.map { e -> e.toDomain() } }
    }

    override fun getLessonsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Lesson>> {
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return lessonDao.getLessonsByDateRange(start, end).map { it.map { e -> e.toDomain() } }
    }

    override fun getPendingLessons(seasonId: Long): Flow<List<Lesson>> =
        lessonDao.getPendingLessons(seasonId).map { it.map { e -> e.toDomain() } }

    override fun getLessonById(id: Long): Flow<Lesson?> =
        lessonDao.getLessonById(id).map { it?.toDomain() }

    override suspend fun insertLesson(lesson: Lesson): Long =
        lessonDao.insertLesson(lesson.toEntity())

    override suspend fun updateLesson(lesson: Lesson) {
        val entity = lessonDao.getLessonEntityById(lesson.id) ?: lesson.toEntity()
        lessonDao.updateLesson(lesson.toEntity().copy(createdAt = entity.createdAt))
    }

    override suspend fun deleteLesson(lesson: Lesson) =
        lessonDao.deleteLesson(lesson.toEntity())

    override suspend fun updatePaymentStatus(lessonId: Long, status: PaymentStatus) =
        lessonDao.updatePaymentStatus(lessonId, status.name)

    override fun getMonthlyLessonStats(seasonId: Long): Flow<Map<String, Pair<Int, Double>>> =
        lessonDao.getMonthlyStats(seasonId).map { rows ->
            rows.associate { it.month to Pair(it.lessonCount, it.totalPaid) }
        }
}
