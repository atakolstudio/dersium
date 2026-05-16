package com.dersium.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun DersiumTheme(accentHex: String = "#6366F1", content: @Composable () -> Unit) {
    val accentColor = remember(accentHex) {
        try { Color(android.graphics.Color.parseColor(accentHex)) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }
    SideEffect { DersiumColors.applyAccent(accentHex) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accentColor, onPrimary = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.2f),
            secondary = DersiumColors.Income, onSecondary = Color.White,
            background = DersiumColors.Background, onBackground = DersiumColors.TextPrimary,
            surface = DersiumColors.Surface, onSurface = DersiumColors.TextPrimary,
            surfaceVariant = DersiumColors.SurfaceVariant, onSurfaceVariant = DersiumColors.TextSecondary,
            outline = DersiumColors.Outline, error = DersiumColors.Expense, onError = Color.White,
            surfaceContainer = DersiumColors.SurfaceVariant,
        ),
        typography = DersiumTypography,
        content = content,
    )
}

val DersiumTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 32.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp),
)
