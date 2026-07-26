package com.dersium.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations (Navigation-Compose 2.8+ / kotlinx.serialization).
 * Replaces the old hand-rolled string-route + Bundle-argument approach: every screen
 * and its arguments are now a plain, compiler-checked Kotlin type instead of a
 * hand-formatted URL string, so a typo or a missing argument is now caught by the
 * Kotlin compiler at build time instead of crashing at runtime.
 */
sealed interface Screen {
    @Serializable data object Auth : Screen

    // Main tabs
    @Serializable data object Home : Screen
    @Serializable data object Students : Screen
    @Serializable data object Lessons : Screen
    @Serializable data object Calendar : Screen
    @Serializable data object Financial : Screen
    @Serializable data object Reports : Screen

    // Detail / add screens (no bottom bar)
    @Serializable data object Settings : Screen
    @Serializable data object Export : Screen
    @Serializable data object PrivacyPolicy : Screen

    @Serializable data class StudentDetail(val studentId: Long) : Screen
    @Serializable data class AddEditStudent(val studentId: Long? = null) : Screen
    @Serializable data class AddEditLesson(val lessonId: Long? = null, val studentId: Long? = null) : Screen
}
