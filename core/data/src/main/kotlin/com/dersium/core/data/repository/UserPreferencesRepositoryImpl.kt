package com.dersium.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.dersium.core.domain.model.ThemeAccentColor
import com.dersium.core.domain.model.UserPreferences
import com.dersium.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private object Keys {
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val PIN_CODE = stringPreferencesKey("pin_code")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val ACTIVE_SEASON_ID = longPreferencesKey("active_season_id")
        val THEME_ACCENT_COLOR = stringPreferencesKey("theme_accent_color")
        val CURRENCY = stringPreferencesKey("currency")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    override val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            UserPreferences(
                isPinEnabled = prefs[Keys.IS_PIN_ENABLED] ?: false,
                pinCode = prefs[Keys.PIN_CODE] ?: "",
                isBiometricEnabled = prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false,
                activeSeasonId = prefs[Keys.ACTIVE_SEASON_ID] ?: 1L,
                themeAccentColor = ThemeAccentColor.fromName(
                    prefs[Keys.THEME_ACCENT_COLOR] ?: ThemeAccentColor.INDIGO.name
                ),
                currency = prefs[Keys.CURRENCY] ?: "₺",
                isFirstLaunch = prefs[Keys.IS_FIRST_LAUNCH] ?: true,
                isPremium = prefs[Keys.IS_PREMIUM] ?: false,
            )
        }

    override suspend fun setPinCode(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_CODE] = pin
            prefs[Keys.IS_PIN_ENABLED] = pin.isNotEmpty()
        }
    }

    override suspend fun clearPinCode() {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_CODE] = ""
            prefs[Keys.IS_PIN_ENABLED] = false
            prefs[Keys.IS_BIOMETRIC_ENABLED] = false
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    override suspend fun setActiveSeasonId(seasonId: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_SEASON_ID] = seasonId
        }
    }

    override suspend fun setThemeAccentColor(color: ThemeAccentColor) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_ACCENT_COLOR] = color.name
        }
    }

    override suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
        }
    }

    override suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PREMIUM] = isPremium
        }
    }
}
