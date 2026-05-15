package com.dersium.core.domain.repository

import com.dersium.core.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun getAllStudents(seasonId: Long): Flow<List<Student>>
    fun getActiveStudents(seasonId: Long): Flow<List<Student>>
    fun getStudentById(id: Long): Flow<Student?>
    suspend fun insertStudent(student: Student): Long
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(student: Student)
    suspend fun getStudentCount(seasonId: Long): Int
}
