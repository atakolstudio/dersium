package com.dersium.feature.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dersium.core.ui.theme.DersiumColors

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.authSuccess.collect { onAuthSuccess() }
    }

    // Auto-trigger biometric if enabled
    LaunchedEffect(uiState.isBiometricEnabled, uiState.isLoading) {
        if (!uiState.isLoading && uiState.isBiometricEnabled) {
            showBiometricPrompt(
                activity = context as? FragmentActivity ?: return@LaunchedEffect,
                onSuccess = viewModel::onBiometricSuccess,
                onError = {},
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DersiumColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            // App logo / title
            Text(
                text = "Dersium",
                style = MaterialTheme.typography.headlineLarge,
                color = DersiumColors.Primary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "PIN Girin",
                style = MaterialTheme.typography.titleMedium,
                color = DersiumColors.TextSecondary,
            )

            // PIN dots
            PinDots(
                filledCount = uiState.enteredDigits.length,
                hasError = uiState.error != null,
            )

            // Error message
            AnimatedVisibility(visible = uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DersiumColors.Expense,
                    textAlign = TextAlign.Center,
                )
            }

            // Keypad
            PinKeypad(
                onDigit = viewModel::onDigitEntered,
                onBackspace = viewModel::onBackspace,
                showBiometric = uiState.isBiometricEnabled,
                onBiometric = {
                    showBiometricPrompt(
                        activity = context as? FragmentActivity ?: return@PinKeypad,
                        onSuccess = viewModel::onBiometricSuccess,
                        onError = {},
                    )
                },
            )
        }
    }
}

@Composable
private fun PinDots(filledCount: Int, hasError: Boolean) {
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(hasError) {
        if (hasError) {
            repeat(4) {
                shakeAnim.animateTo(10f, tween(50))
                shakeAnim.animateTo(-10f, tween(50))
            }
            shakeAnim.animateTo(0f, tween(50))
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.offset(x = shakeAnim.value.dp),
    ) {
        repeat(4) { index ->
            val filled = index < filledCount
            val scale by animateFloatAsState(
                targetValue = if (filled) 1.2f else 1f,
                animationSpec = spring(Spring.DampingRatioMediumBouncy),
                label = "dot$index",
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        if (filled) DersiumColors.Primary
                        else DersiumColors.SurfaceVariant
                    )
                    .border(
                        1.5.dp,
                        if (filled) DersiumColors.Primary else DersiumColors.Outline,
                        CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun PinKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    showBiometric: Boolean,
    onBiometric: () -> Unit,
) {
    val keys = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { digit ->
                    PinKey(label = digit.toString(), onClick = { onDigit(digit) })
                }
            }
        }
        // Bottom row: biometric | 0 | backspace
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (showBiometric) {
                PinIconKey(icon = Icons.Default.Fingerprint, onClick = onBiometric)
            } else {
                Spacer(Modifier.size(80.dp))
            }
            PinKey(label = "0", onClick = { onDigit(0) })
            PinIconKey(icon = Icons.Default.Backspace, onClick = onBackspace)
        }
    }
}

@Composable
private fun PinKey(label: String, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "keyScale",
    )
    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(DersiumColors.SurfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                pressed = true
                onClick()
                pressed = false
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            color = DersiumColors.TextPrimary,
        )
    }
}

@Composable
private fun PinIconKey(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = DersiumColors.TextSecondary, modifier = Modifier.size(28.dp))
    }
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onSuccess()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            onError(errString.toString())
        }
        override fun onAuthenticationFailed() {
            onError("Kimlik doğrulaması başarısız")
        }
    })
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Dersium")
        .setSubtitle("Uygulamaya erişmek için biyometrik doğrulama yapın")
        .setNegativeButtonText("PIN Kullan")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()
    biometricPrompt.authenticate(promptInfo)
}
