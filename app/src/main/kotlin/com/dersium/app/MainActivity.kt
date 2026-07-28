package com.dersium.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dersium.app.navigation.DersiumNavHost
import com.dersium.app.navigation.Screen
import com.dersium.app.widget.DersiumWidget
import androidx.glance.appwidget.updateAll
import com.dersium.core.ui.theme.DersiumTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val notifLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    // Which screen a launcher shortcut ("Yeni Ders Ekle" / "Yeni Öğrenci Ekle") wants to
    // land on, if any. mutableStateOf so a shortcut tap while the app is already running
    // (onNewIntent, since MainActivity is singleTop) also reaches the Compose tree.
    private var pendingShortcut by mutableStateOf<Screen?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { viewModel.isLoading }
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        pendingShortcut = intent.toShortcutScreen()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            DersiumTheme(accentHex = state.accentHex) {
                if (state.startDestination != null) {
                    DersiumNavHost(
                        startDestination = state.startDestination!!,
                        pendingShortcut = pendingShortcut,
                        onShortcutConsumed = { pendingShortcut = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingShortcut = intent.toShortcutScreen()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { DersiumWidget().updateAll(this@MainActivity) }
    }

    private fun Intent?.toShortcutScreen(): Screen? = when (this?.action) {
        "com.dersium.app.ACTION_ADD_LESSON" -> Screen.AddEditLesson()
        "com.dersium.app.ACTION_ADD_STUDENT" -> Screen.AddEditStudent()
        else -> null
    }
}
