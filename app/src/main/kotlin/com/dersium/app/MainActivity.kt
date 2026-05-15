package com.dersium.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dersium.app.navigation.DersiumNavHost
import com.dersium.core.ui.theme.DersiumTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    /** Android 13+ notification permission request */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* User decision handled — notifications silently degrade if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen must be installed before super.onCreate
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash until prefs are loaded
        splashScreen.setKeepOnScreenCondition { viewModel.isLoading }

        // Android 15 / API 35 — full edge-to-edge enforced by OS
        enableEdgeToEdge()

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            DersiumTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.startDestination.collect { dest ->
                            startDestination = dest
                        }
                    }
                }

                if (startDestination != null) {
                    DersiumNavHost(startDestination = startDestination!!)
                }
            }
        }
    }
}
