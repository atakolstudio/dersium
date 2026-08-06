package com.dersium.core.domain.repository

interface WorkspaceRepository {
    /**
     * Atomically claims [code] as a brand-new workspace. Returns false (without claiming
     * anything) if that code is already taken by someone else — the caller should generate
     * a different code and retry rather than reuse a colliding one, or two unrelated pairs
     * could end up sharing a single Firestore workspace and seeing each other's data.
     */
    suspend fun claimWorkspaceCode(code: String): Boolean

    /** True if [code] refers to a workspace that already exists (used by the "join" flow). */
    suspend fun workspaceExists(code: String): Boolean
}
