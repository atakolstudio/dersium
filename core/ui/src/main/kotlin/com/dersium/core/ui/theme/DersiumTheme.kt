package com.dersium.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Colour tokens ──────────────────────────────────────────────────────────────
object DersiumColors {
    // Backgrounds
    val Background      = Color(0xFF0A0A0F)
    val Surface         = Color(0xFF13131A)
    val SurfaceVariant  = Color(0xFF1C1C27)
    val SurfaceElevated = Color(0xFF22222F)
    val Outline         = Color(0xFF2E2E3E)

    // Brand – Indigo (default)
    val Primary         = Color(0xFF6366F1)
    val PrimaryLight    = Color(0xFF818CF8)
    val PrimaryDark     = Color(0xFF4F46E5)
    val PrimaryContainer= Color(0xFF1E1B4B)

    // Income – Green
    val Income          = Color(0xFF22C55E)
    val IncomeLight     = Color(0xFF4ADE80)
    val IncomeContainer = Color(0xFF052E16)

    // Expense – Red
    val Expense         = Color(0xFFEF4444)
    val ExpenseLight    = Color(0xFFF87171)
    val ExpenseContainer= Color(0xFF450A0A)

    // Pending – Amber
    val Pending         = Color(0xFFF59E0B)
    val PendingLight    = Color(0xFFFBBF24)
    val PendingContainer= Color(0xFF431407)

    // Text
    val TextPrimary     = Color(0xFFF1F5F9)
    val TextSecondary   = Color(0xFF94A3B8)
    val TextTertiary    = Color(0xFF64748B)
    val TextDisabled    = Color(0xFF334155)

    // Accent aliases for alt themes
    val AccentGreen     = Color(0xFF22C55E)
    val AccentBlue      = Color(0xFF3B82F6)
}

private val DarkColorScheme = darkColorScheme(
    primary          = DersiumColors.Primary,
    onPrimary        = Color.White,
    primaryContainer = DersiumColors.PrimaryContainer,
    onPrimaryContainer = DersiumColors.PrimaryLight,
    secondary        = DersiumColors.Income,
    onSecondary      = Color.White,
    secondaryContainer = DersiumColors.IncomeContainer,
    onSecondaryContainer = DersiumColors.IncomeLight,
    tertiary         = DersiumColors.Pending,
    onTertiary       = Color.Black,
    background       = DersiumColors.Background,
    onBackground     = DersiumColors.TextPrimary,
    surface          = DersiumColors.Surface,
    onSurface        = DersiumColors.TextPrimary,
    surfaceVariant   = DersiumColors.SurfaceVariant,
    onSurfaceVariant = DersiumColors.TextSecondary,
    outline          = DersiumColors.Outline,
    error            = DersiumColors.Expense,
    onError          = Color.White,
    errorContainer   = DersiumColors.ExpenseContainer,
    onErrorContainer = DersiumColors.ExpenseLight,
    surfaceContainer = DersiumColors.SurfaceVariant,
    surfaceContainerHigh = DersiumColors.SurfaceElevated,
)

// ── Typography ─────────────────────────────────────────────────────────────────
val DersiumTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

// ── Theme entry point ──────────────────────────────────────────────────────────
@Composable
fun DersiumTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = DersiumTypography,
        content     = content,
    )
}
