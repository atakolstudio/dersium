package com.dersium.core.data.repository

import com.dersium.core.data.mapper.toDomain
import com.dersium.core.data.mapper.toEntity
import com.dersium.core.data.sync.StudentSyncManager
import com.dersium.core.database.dao.StudentDao
import com.dersium.core.domain.model.Student
import com.dersium.core.domain.repository.StudentRepository
import com.dersium.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncManager: StudentSyncManager,
) : StudentRepository {

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Whenever the paired workspace changes (paired, switched, or left), re-attach the
        // Firestore listener so remote changes for the *current* workspace flow into Room.
        repoScope.launch {
            userPreferencesRepository.userPreferences
                .map { it.workspaceId }
                .distinctUntilChanged()
                .collect { workspaceId ->
                    if (workspaceId != null) {
                        syncManager.attach(workspaceId, repoScope) {
                            // Incoming records land in whatever season is active on THIS device.
                            userPreferencesRepository.userPreferences.first().activeSeasonId
                        }
                    } else {
                        syncManager.detach()
                    }
                }
        }
    }

    private suspend fun pushIfPaired(entitySyncId: String) {
        val workspaceId = userPreferencesRepository.userPreferences.first().workspaceId ?: return
        val entity = studentDao.getStudentBySyncId(entitySyncId) ?: return
        syncManager.push(entity, workspaceId)
    }

    override fun getAllStudents(seasonId: Long): Flow<List<Student>> =
        studentDao.getAllStudents(seasonId).map { list -> list.map { it.toDomain() } }

    override fun getActiveStudents(seasonId: Long): Flow<List<Student>> =
        studentDao.getActiveStudents(seasonId).map { list -> list.map { it.toDomain() } }

    override fun getStudentById(id: Long): Flow<Student?> =
        studentDao.getStudentById(id).map { it?.toDomain() }

    override suspend fun insertStudent(student: Student): Long {
        val id = studentDao.insertStudent(student.toEntity())
        pushIfPaired(student.syncId)
        return id
    }

    override suspend fun updateStudent(student: Student) {
        val updated = student.copy(updatedAt = System.currentTimeMillis())
        studentDao.updateStudent(updated.toEntity())
        pushIfPaired(updated.syncId)
    }

    override suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student.toEntity())
        val workspaceId = userPreferencesRepository.userPreferences.first().workspaceId
        if (workspaceId != null) syncManager.pushDelete(student.syncId, workspaceId)
    }

    override suspend fun getStudentCount(seasonId: Long): Int =
        studentDao.getStudentCount(seasonId)
}
