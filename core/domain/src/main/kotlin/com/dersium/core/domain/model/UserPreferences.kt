package com.dersium.core.domain.model

data class UserPreferences(
    val isPinEnabled: Boolean = false,
    val pinCode: String = "",
    val isBiometricEnabled: Boolean = false,
    val activeSeasonId: Long = 1L,
    val themeAccentColor: ThemeAccentColor = ThemeAccentColor.INDIGO,
    val currency: String = "₺",
    val isFirstLaunch: Boolean = true,
    val isPremium: Boolean = false,
    val maxFreeStudents: Int = 5,
)

enum class ThemeAccentColor(val displayName: String, val colorHex: String) {
    INDIGO("İndigo", "#6366F1"),
    GREEN("Yeşil", "#22C55E"),
    BLUE("Mavi", "#3B82F6");

    /** Alias for colourHex so both `.hex` and `.colorHex` work at call sites. */
    val hex: String get() = colorHex

    companion object {
        fun fromName(name: String) = entries.find { it.name == name } ?: INDIGO
    }
}
