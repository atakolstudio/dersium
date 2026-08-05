package com.dersium.core.data.sync

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anonymous Firebase Auth just to satisfy Firestore security rules (every device needs
 * *some* identity to read/write a shared workspace) — no sign-in screen, no email/password,
 * nothing the user ever sees or has to manage.
 */
@Singleton
class WorkspaceAuth @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    suspend fun ensureSignedIn() {
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously().await()
        }
    }
}
