package com.dersium.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dersium.core.ui.components.DersiumBottomBar
import com.dersium.feature.auth.AuthScreen
import com.dersium.feature.calendar.CalendarScreen
import com.dersium.feature.financial.FinancialScreen
import com.dersium.feature.home.HomeScreen
import com.dersium.feature.lessons.AddEditLessonScreen
import com.dersium.feature.lessons.LessonsScreen
import com.dersium.feature.reports.ReportsScreen
import com.dersium.feature.settings.SettingsScreen
import com.dersium.feature.students.AddEditStudentScreen
import com.dersium.feature.students.StudentDetailScreen
import com.dersium.feature.students.StudentsScreen

// Tabs with bottom bar visible
private val bottomBarRoutes = setOf(
    Screen.Home.route,
    Screen.Students.route,
    Screen.Lessons.route,
    Screen.Calendar.route,
    Screen.Financial.route,
    Screen.Reports.route,
)

@Composable
fun DersiumNavHost(
    startDestination: String = Screen.Home.route,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    // Helper to go to a main tab (save/restore state)
    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DersiumBottomBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    onNavigate = ::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition  = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            exitTransition   = { fadeOut(tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250)) },
            popEnterTransition = { fadeIn(tween(250)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) },
            popExitTransition  = { fadeOut(tween(250)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250)) },
        ) {
            // Auth
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                )
            }

            // ── Main tabs ─────────────────────────────────────────────────────
            composable(Screen.Home.route) {
                HomeScreen(
                    onAddLesson          = { navController.navigate(Screen.AddEditLesson.createRoute()) },
                    onNavigateToStudents = { navigateToTab(Screen.Students.route) },
                    onNavigateToReports  = { navigateToTab(Screen.Reports.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onStudentClick = { id -> navController.navigate(Screen.StudentDetail.createRoute(id)) },
                )
            }

            composable(Screen.Students.route) {
                StudentsScreen(
                    onStudentClick = { id -> navController.navigate(Screen.StudentDetail.createRoute(id)) },
                    onAddStudent   = { navController.navigate(Screen.AddEditStudent.createRoute()) },
                )
            }

            composable(Screen.Lessons.route) {
                LessonsScreen(
                    onAddLesson  = { navController.navigate(Screen.AddEditLesson.createRoute()) },
                    onEditLesson = { id -> navController.navigate(Screen.AddEditLesson.createRoute(lessonId = id)) },
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onAddLesson = { navController.navigate(Screen.AddEditLesson.createRoute()) },
                )
            }

            composable(Screen.Financial.route) { FinancialScreen() }
            composable(Screen.Reports.route)   { ReportsScreen()   }

            // ── Detail / edit screens ─────────────────────────────────────────
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Screen.StudentDetail.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType }),
            ) { back ->
                val studentId = back.arguments!!.getLong("studentId")
                StudentDetailScreen(
                    studentId  = studentId,
                    onBack     = { navController.popBackStack() },
                    onEdit     = { navController.navigate(Screen.AddEditStudent.createRoute(studentId)) },
                    onAddLesson = { navController.navigate(Screen.AddEditLesson.createRoute(studentId = studentId)) },
                )
            }

            composable(
                route = Screen.AddEditStudent.route,
                arguments = listOf(
                    navArgument("studentId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { back ->
                val id = back.arguments!!.getLong("studentId").takeIf { it != -1L }
                AddEditStudentScreen(
                    studentId = id,
                    onBack    = { navController.popBackStack() },
                )
            }

            composable(
                route = Screen.AddEditLesson.route,
                arguments = listOf(
                    navArgument("lessonId")  { type = NavType.LongType; defaultValue = -1L },
                    navArgument("studentId") { type = NavType.LongType; defaultValue = -1L },
                ),
            ) { back ->
                val lessonId  = back.arguments!!.getLong("lessonId").takeIf  { it != -1L }
                val studentId = back.arguments!!.getLong("studentId").takeIf { it != -1L }
                AddEditLessonScreen(
                    lessonId            = lessonId,
                    preSelectedStudentId = studentId,
                    onBack              = { navController.popBackStack() },
                )
            }
        }
    }
}
