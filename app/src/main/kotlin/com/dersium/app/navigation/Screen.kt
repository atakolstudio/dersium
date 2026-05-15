package com.dersium.app.navigation

sealed class Screen(val route: String) {
    // Auth
    data object Auth : Screen("auth")

    // Main tabs
    data object Home      : Screen("home")
    data object Students  : Screen("students")
    data object Lessons   : Screen("lessons")
    data object Calendar  : Screen("calendar")
    data object Financial : Screen("financial")
    data object Reports   : Screen("reports")

    // Detail / add screens (no bottom bar)
    data object Settings : Screen("settings")
    data object Export   : Screen("export")

    data object StudentDetail : Screen("student/{studentId}") {
        fun createRoute(studentId: Long) = "student/$studentId"
    }
    data object AddEditStudent : Screen("student/edit?studentId={studentId}") {
        fun createRoute(studentId: Long? = null) = if (studentId != null) "student/edit?studentId=$studentId" else "student/edit?studentId=-1"
    }
    data object AddEditLesson : Screen("lesson/edit?lessonId={lessonId}&studentId={studentId}") {
        fun createRoute(lessonId: Long? = null, studentId: Long? = null) =
            "lesson/edit?lessonId=${lessonId ?: -1L}&studentId=${studentId ?: -1L}"
    }
}
