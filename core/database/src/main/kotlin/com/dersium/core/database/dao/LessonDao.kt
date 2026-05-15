package com.dersium.core.database.dao

import androidx.room.*
import com.dersium.core.database.entity.LessonEntity
import com.dersium.core.database.entity.LessonWithStudentView
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lesson_with_student WHERE seasonId = :seasonId ORDER BY date DESC, startTime ASC")
    fun getAllLessons(seasonId: Long): Flow<List<LessonWithStudentView>>

    @Query("SELECT * FROM lesson_with_student ORDER BY date DESC, startTime ASC")
    fun getAllLessonsAllSeasons(): Flow<List<LessonWithStudentView>>

    @Query("SELECT * FROM lesson_with_student WHERE studentId = :studentId ORDER BY date DESC")
    fun getLessonsByStudent(studentId: Long): Flow<List<LessonWithStudentView>>

    @Query("SELECT * FROM lesson_with_student WHERE date = :dateEpoch ORDER BY startTime ASC")
    fun getLessonsByDate(dateEpoch: Long): Flow<List<LessonWithStudentView>>

    @Query("SELECT * FROM lesson_with_student WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, startTime ASC")
    fun getLessonsByDateRange(startDate: Long, endDate: Long): Flow<List<LessonWithStudentView>>

    @Query("SELECT * FROM lesson_with_student WHERE paymentStatus = 'PENDING' AND seasonId = :seasonId ORDER BY date DESC")
    fun getPendingLessons(seasonId: Long): Flow<List<LessonWithStudentView>>

    @Query("SELECT * FROM lesson_with_student WHERE id = :id")
    fun getLessonById(id: Long): Flow<LessonWithStudentView?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long

    @Update
    suspend fun updateLesson(lesson: LessonEntity)

    @Delete
    suspend fun deleteLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET paymentStatus = :status WHERE id = :lessonId")
    suspend fun updatePaymentStatus(lessonId: Long, status: String)

    @Query("""
        SELECT strftime('%Y-%m', date/1000, 'unixepoch') as month,
               COUNT(*) as lessonCount,
               SUM(CASE WHEN paymentStatus = 'PAID' THEN fee ELSE 0 END) as totalPaid
        FROM lessons
        WHERE seasonId = :seasonId
        GROUP BY month
        ORDER BY month DESC
    """)
    fun getMonthlyStats(seasonId: Long): Flow<List<MonthlyStatRow>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonEntityById(id: Long): LessonEntity?
}

data class MonthlyStatRow(
    val month: String,
    val lessonCount: Int,
    val totalPaid: Double,
)
