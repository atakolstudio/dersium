package com.dersium.core.data.repository

import com.dersium.core.data.mapper.toDomain
import com.dersium.core.data.mapper.toEntity
import com.dersium.core.database.dao.StudentDao
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
) : StudentRepository {

    override fun getAllStudents(seasonId: Long): Flow<List<Student>> =
        studentDao.getAllStudents(seasonId).map { list -> list.map { it.toDomain() } }

    override fun getActiveStudents(seasonId: Long): Flow<List<Student>> =
        studentDao.getActiveStudents(seasonId).map { list -> list.map { it.toDomain() } }

    override fun getStudentById(id: Long): Flow<Student?> =
        studentDao.getStudentById(id).map { it?.toDomain() }

    override suspend fun insertStudent(student: Student): Long =
        studentDao.insertStudent(student.toEntity())

    override suspend fun updateStudent(student: Student) =
        studentDao.updateStudent(student.toEntity())

    override suspend fun deleteStudent(student: Student) =
        studentDao.deleteStudent(student.toEntity())

    override suspend fun getStudentCount(seasonId: Long): Int =
        studentDao.getStudentCount(seasonId)
}
