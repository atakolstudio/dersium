package com.dersium.core.ui.theme

import android.os.Build
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object DersiumColors {
    val Background      = Color(0xFF0A0A0F)
    val Surface         = Color(0xFF13131A)
    val SurfaceVariant  = Color(0xFF1C1C27)
    val SurfaceElevated = Color(0xFF22222F)
    val Outline         = Color(0xFF2E2E3E)
    var Primary         = Color(0xFF6366F1)
    var PrimaryLight    = Color(0xFF818CF8)
    var PrimaryDark     = Color(0xFF4F46E5)
    var PrimaryContainer= Color(0xFF1E1B4B)
    val Income          = Color(0xFF22C55E)
    val IncomeLight     = Color(0xFF4ADE80)
    val IncomeContainer = Color(0xFF052E16)
    val Expense         = Color(0xFFEF4444)
    val ExpenseLight    = Color(0xFFF87171)
    val ExpenseContainer= Color(0xFF450A0A)
    val Pending         = Color(0xFFF59E0B)
    val PendingLight    = Color(0xFFFBBF24)
    val PendingContainer= Color(0xFF431407)
    val TextPrimary     = Color(0xFFF1F5F9)
    val TextSecondary   = Color(0xFF94A3B8)
    val TextTertiary    = Color(0xFF64748B)
    val TextDisabled    = Color(0xFF334155)

    // Subtle glass/gradient tokens for elevated surfaces (cards, sheets, headers)
    val GlassHighlight  = Color(0x14FFFFFF)
    val ScrimTop        = Color(0xCC0A0A0F)

    fun applyAccent(hex: String) {
        try {
            val base = Color(android.graphics.Color.parseColor(hex))
            Primary           = base
            PrimaryLight      = base.copy(red = (base.red + 0.12f).coerceAtMost(1f), blue = (base.blue + 0.08f).coerceAtMost(1f))
            PrimaryDark       = base.copy(red = (base.red - 0.1f).coerceAtLeast(0f))
            PrimaryContainer  = base.copy(alpha = 0.18f)
        } catch (_: Exception) {}
    }
}

/**
 * Shape tokens following Material 3 expressive guidance: larger, softer corner radii
 * for a friendlier, more modern feel across cards, sheets and dialogs.
 */
val DersiumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * Centralized motion tokens so every screen animates consistently (spring-based,
 * "expressive" motion rather than generic linear tweens).
 */
object DersiumMotion {
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0f, 1.0f)

    fun <T> springSnappy(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    fun <T> springSmooth(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    val enter = tween<Float>(durationMillis = 280, easing = EmphasizedEasing)
    val exit = tween<Float>(durationMillis = 200, easing = EmphasizedEasing)
}

@Composable
fun DersiumTheme(
    accentHex: String = "#6366F1",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val accentColor = remember(accentHex) {
        try { Color(android.graphics.Color.parseColor(accentHex)) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }
    SideEffect { DersiumColors.applyAccent(accentHex) }

    val context = LocalContext.current
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Respect device wallpaper-derived palette (Material You) when explicitly opted in,
        // while keeping our own dark neutrals as the base.
        dynamicDarkColorScheme(context).copy(
            background = DersiumColors.Background,
            surface = DersiumColors.Surface,
        )
    } else {
        darkColorScheme(
            primary = accentColor, onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.2f),
            secondary = DersiumColors.Income, onSecondary = Color.White,
            background = DersiumColors.Background, onBackground = DersiumColors.TextPrimary,
            surface = DersiumColors.Surface, onSurface = DersiumColors.TextPrimary,
            surfaceVariant = DersiumColors.SurfaceVariant, onSurfaceVariant = DersiumColors.TextSecondary,
            outline = DersiumColors.Outline, error = DersiumColors.Expense, onError = Color.White,
            surfaceContainer = DersiumColors.SurfaceVariant,
            surfaceContainerHigh = DersiumColors.SurfaceElevated,
            surfaceContainerHighest = DersiumColors.SurfaceElevated,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DersiumTypography,
        shapes = DersiumShapes,
        content = content,
    )
}

// Material 3 expressive-inspired type scale: tighter letter spacing on display/headline
// sizes, more generous line height on body copy for readability in dense financial tables.
val DersiumTypography = Typography(
    displayLarge   = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = (-0.25).sp),
    displayMedium  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 36.sp, lineHeight = 44.sp),
    displaySmall   = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.2).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.1).sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
)
