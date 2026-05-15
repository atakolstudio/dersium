package com.dersium.core.database.dao

import androidx.room.*
import com.dersium.core.database.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students WHERE seasonId = :seasonId ORDER BY name ASC")
    fun getAllStudents(seasonId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE seasonId = :seasonId AND isActive = 1 ORDER BY name ASC")
    fun getActiveStudents(seasonId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Long): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT COUNT(*) FROM students WHERE seasonId = :seasonId")
    suspend fun getStudentCount(seasonId: Long): Int

    @Query("SELECT COUNT(*) FROM students WHERE seasonId = :seasonId AND isActive = 1")
    suspend fun getActiveStudentCount(seasonId: Long): Int
}
