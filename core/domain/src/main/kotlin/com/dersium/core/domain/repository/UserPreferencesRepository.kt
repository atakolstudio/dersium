package com.dersium.core.domain.repository

import com.dersium.core.domain.model.ThemeAccentColor
import com.dersium.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setPinCode(pin: String)
    suspend fun clearPinCode()
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setActiveSeasonId(seasonId: Long)
    suspend fun setThemeAccentColor(color: ThemeAccentColor)
    suspend fun setFirstLaunchCompleted()
    suspend fun setPremium(isPremium: Boolean)
}
