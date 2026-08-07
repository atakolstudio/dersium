package com.dersium.core.data.repository

import com.dersium.core.data.sync.WorkspaceAuth
import com.dersium.core.domain.repository.WorkspaceRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val workspaceAuth: WorkspaceAuth,
) : WorkspaceRepository {

    private fun workspaceDoc(code: String) = firestore.collection("workspaces").document(code)

    override suspend fun claimWorkspaceCode(code: String): Boolean {
        workspaceAuth.ensureSignedIn()
        // Firestore transactions fail atomically if the doc already exists between the read
        // and the write, so two devices racing to claim the same freshly-generated code can't
        // both "win" — the loser's transaction throws and createWorkspace() just retries with
        // a new code.
        return try {
            firestore.runTransaction { transaction ->
                val doc = workspaceDoc(code)
                val snapshot = transaction.get(doc)
                if (snapshot.exists()) {
                    throw WorkspaceCodeTakenException()
                }
                transaction.set(doc, mapOf("createdAt" to System.currentTimeMillis()))
            }.await()
            true
        } catch (_: WorkspaceCodeTakenException) {
            false
        }
    }

    override suspend fun workspaceExists(code: String): Boolean {
        workspaceAuth.ensureSignedIn()
        return workspaceDoc(code).get().await().exists()
    }

    private class WorkspaceCodeTakenException : Exception()
}
