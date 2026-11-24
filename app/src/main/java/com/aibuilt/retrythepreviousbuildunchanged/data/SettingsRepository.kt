package com.aibuilt.retrythepreviousbuildunchanged.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aibuilt.retrythepreviousbuildunchanged.model.AppSettings
import com.aibuilt.retrythepreviousbuildunchanged.model.SortOrder
import com.aibuilt.retrythepreviousbuildunchanged.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val SETTINGS_STORE_NAME = "retry_log_settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_STORE_NAME)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEFAULT_ATTEMPTS = intPreferencesKey("default_attempts")
        val SHOW_RESOLVED = booleanPreferencesKey("show_resolved")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            AppSettings(
                sortOrder = preferences[Keys.SORT_ORDER]
                    ?.let { stored -> SortOrder.entries.firstOrNull { it.name == stored } }
                    ?: SortOrder.RECENT,
                themeMode = preferences[Keys.THEME_MODE]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.SYSTEM,
                defaultMaxAttempts = (preferences[Keys.DEFAULT_ATTEMPTS] ?: 3).coerceIn(1, 25),
                showResolved = preferences[Keys.SHOW_RESOLVED] ?: true
            )
        }

    suspend fun setSortOrder(order: SortOrder) {
        context.settingsDataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDefaultMaxAttempts(value: Int) {
        context.settingsDataStore.edit { it[Keys.DEFAULT_ATTEMPTS] = value.coerceIn(1, 25) }
    }

    suspend fun setShowResolved(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.SHOW_RESOLVED] = value }
    }
}
