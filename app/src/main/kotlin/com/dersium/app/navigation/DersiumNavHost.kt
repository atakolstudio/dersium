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
import com.dersium.feature.settings.PrivacyPolicyScreen
import com.dersium.feature.export.ExportScreen
import com.dersium.feature.students.AddEditStudentScreen
import com.dersium.feature.students.StudentDetailScreen
import com.dersium.feature.students.StudentsScreen

// Bottom-bar tab destinations, mapped to the plain string ids DersiumBottomBar already
// speaks (kept stable on purpose so core:ui doesn't need to know about Screen at all).
private val bottomBarTabs: List<Pair<Screen, String>> = listOf(
    Screen.Home      to "home",
    Screen.Students  to "students",
    Screen.Lessons   to "lessons",
    Screen.Calendar  to "calendar",
    Screen.Financial to "financial",
    Screen.Reports   to "reports",
)

@Composable
fun DersiumNavHost(
    startDestination: Screen = Screen.Home,
    navController: NavHostController = rememberNavController(),
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentTabId = remember(currentDestination) {
        bottomBarTabs.firstOrNull { (screen, _) -> currentDestination?.hasRoute(screen::class) == true }?.second
    }
    val showBottomBar = currentTabId != null

    // Helper to go to a main tab (save/restore state)
    fun navigateToTab(screen: Screen) {
        navController.navigate(screen) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DersiumBottomBar(
                    currentRoute = currentTabId ?: "home",
                    onNavigate = { tabId ->
                        val screen = bottomBarTabs.firstOrNull { it.second == tabId }?.first ?: Screen.Home
                        navigateToTab(screen)
                    },
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
            composable<Screen.Auth> {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Auth) { inclusive = true }
                        }
                    },
                )
            }

            // ── Main tabs ─────────────────────────────────────────────────────
            composable<Screen.Home> {
                HomeScreen(
                    onAddLesson          = { navController.navigate(Screen.AddEditLesson()) },
                    onNavigateToStudents = { navigateToTab(Screen.Students) },
                    onNavigateToReports  = { navigateToTab(Screen.Reports) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings) },
                    onStudentClick = { id -> navController.navigate(Screen.StudentDetail(id)) },
                )
            }

            composable<Screen.Students> {
                StudentsScreen(
                    onStudentClick = { id -> navController.navigate(Screen.StudentDetail(id)) },
                    onAddStudent   = { navController.navigate(Screen.AddEditStudent()) },
                )
            }

            composable<Screen.Lessons> {
                LessonsScreen(
                    onAddLesson  = { navController.navigate(Screen.AddEditLesson()) },
                    onEditLesson = { id -> navController.navigate(Screen.AddEditLesson(lessonId = id)) },
                )
            }

            composable<Screen.Calendar> {
                CalendarScreen(
                    onAddLesson = { navController.navigate(Screen.AddEditLesson()) },
                )
            }

            composable<Screen.Financial> { FinancialScreen() }
            composable<Screen.Reports>   { ReportsScreen()   }

            // ── Detail / edit screens ─────────────────────────────────────────
            composable<Screen.Settings> {
                SettingsScreen(onBack = { navController.popBackStack() }, onExport = { navController.navigate(Screen.Export) }, onPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy) })
            }

            composable<Screen.StudentDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.StudentDetail>()
                StudentDetailScreen(
                    studentId  = args.studentId,
                    onBack     = { navController.popBackStack() },
                    onEdit     = { navController.navigate(Screen.AddEditStudent(args.studentId)) },
                    onAddLesson = { navController.navigate(Screen.AddEditLesson(studentId = args.studentId)) },
                )
            }

            composable<Screen.AddEditStudent> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.AddEditStudent>()
                AddEditStudentScreen(
                    studentId = args.studentId,
                    onBack    = { navController.popBackStack() },
                )
            }

            composable<Screen.AddEditLesson> { backStackEntry ->
                val args = backStackEntry.toRoute<Screen.AddEditLesson>()
                AddEditLessonScreen(
                    lessonId            = args.lessonId,
                    preSelectedStudentId = args.studentId,
                    onBack              = { navController.popBackStack() },
                )
            }

            composable<Screen.Export> {
                ExportScreen(onBack = { navController.popBackStack() })
            }

            composable<Screen.PrivacyPolicy> {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
