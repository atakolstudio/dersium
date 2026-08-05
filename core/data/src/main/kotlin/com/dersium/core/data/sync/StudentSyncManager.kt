package com.dersium.core.data.sync

import com.dersium.core.database.dao.StudentDao
import com.dersium.core.database.entity.StudentEntity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mirrors student records between the local Room table and a shared Firestore workspace so
 * two (or more) devices paired on the same workspace code see each other's changes.
 *
 * Deliberately NOT syncing seasonId as-is: each device generates its own local Season rows
 * with its own auto-increment ids, so the same numeric seasonId can mean a different season
 * on two devices. A record arriving from a teammate is instead filed under *this* device's
 * own currently-active season — correct for the common case (both people actively working
 * the same current term together) even though it means the very first sync of seasons
 * themselves is a followup, not part of this pass.
 */
@Singleton
class StudentSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val workspaceAuth: WorkspaceAuth,
    private val studentDao: StudentDao,
) {
    private var listener: ListenerRegistration? = null
    // syncIds this device just wrote locally because of an incoming remote change — skip
    // re-pushing them back to Firestore immediately after (would otherwise loop forever).
    private val justAppliedFromRemote = mutableSetOf<String>()

    private fun collection(workspaceId: String) =
        firestore.collection("workspaces").document(workspaceId).collection("students")

    fun attach(workspaceId: String, scope: CoroutineScope, currentSeasonId: suspend () -> Long) {
        detach()
        listener = collection(workspaceId).addSnapshotListener { snapshot, _ ->
            val changes = snapshot?.documentChanges ?: return@addSnapshotListener
            scope.launch {
                for (change in changes) {
                    val syncId = change.document.id
                    when (change.type) {
                        DocumentChange.Type.REMOVED -> {
                            justAppliedFromRemote.add(syncId)
                            studentDao.deleteStudentBySyncId(syncId)
                        }
                        else -> {
                            val data = change.document.data
                            val remoteUpdatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                            val local = studentDao.getStudentBySyncId(syncId)
                            // Last-write-wins: only apply the remote copy if it's actually newer
                            // than what we already have, so we don't clobber a local edit that
                            // hasn't been pushed yet with a stale remote snapshot.
                            if (local == null || remoteUpdatedAt >= local.updatedAt) {
                                justAppliedFromRemote.add(syncId)
                                studentDao.insertStudent(mapToEntity(syncId, data, local?.id ?: 0, currentSeasonId()))
                            }
                        }
                    }
                }
            }
        }
    }

    fun detach() {
        listener?.remove()
        listener = null
    }

    suspend fun push(entity: StudentEntity, workspaceId: String) {
        if (justAppliedFromRemote.remove(entity.syncId)) return
        workspaceAuth.ensureSignedIn()
        collection(workspaceId).document(entity.syncId).set(entityToMap(entity)).await()
    }

    suspend fun pushDelete(syncId: String, workspaceId: String) {
        justAppliedFromRemote.remove(syncId)
        workspaceAuth.ensureSignedIn()
        collection(workspaceId).document(syncId).delete().await()
    }

    private fun entityToMap(e: StudentEntity): Map<String, Any?> = mapOf(
        "name" to e.name, "surname" to e.surname, "avatarColor" to e.avatarColor,
        "lessonFee" to e.lessonFee, "paymentType" to e.paymentType,
        "lessonCountForPayment" to e.lessonCountForPayment,
        "school" to e.school, "grade" to e.grade,
        "motherName" to e.motherName, "motherPhone" to e.motherPhone,
        "fatherName" to e.fatherName, "fatherPhone" to e.fatherPhone,
        "phone" to e.phone, "notes" to e.notes, "isActive" to e.isActive,
        "createdAt" to e.createdAt, "updatedAt" to e.updatedAt,
        "scheduleSlots" to e.scheduleSlots,
    )

    private fun mapToEntity(syncId: String, data: Map<String, Any?>, localId: Long, seasonId: Long): StudentEntity = StudentEntity(
        id = localId, syncId = syncId,
        name = data["name"] as? String ?: "",
        surname = data["surname"] as? String ?: "",
        avatarColor = data["avatarColor"] as? String ?: "#6366F1",
        lessonFee = (data["lessonFee"] as? Number)?.toDouble() ?: 0.0,
        paymentType = data["paymentType"] as? String ?: "UPFRONT",
        lessonCountForPayment = (data["lessonCountForPayment"] as? Number)?.toInt() ?: 1,
        school = data["school"] as? String ?: "",
        grade = data["grade"] as? String ?: "",
        motherName = data["motherName"] as? String ?: "",
        motherPhone = data["motherPhone"] as? String ?: "",
        fatherName = data["fatherName"] as? String ?: "",
        fatherPhone = data["fatherPhone"] as? String ?: "",
        phone = data["phone"] as? String ?: "",
        notes = data["notes"] as? String ?: "",
        isActive = data["isActive"] as? Boolean ?: true,
        seasonId = seasonId,
        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        scheduleSlots = data["scheduleSlots"] as? String ?: "",
    )
}
