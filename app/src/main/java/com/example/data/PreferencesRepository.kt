package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "meong_prefs")

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val LAST_PLAYER_NAME = stringPreferencesKey("last_player_name")
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SOUND_ENABLED] ?: true
    }

    val lastPlayerName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_PLAYER_NAME] ?: "Majikan MOCHI"
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setLastPlayerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_PLAYER_NAME] = name.trim().ifEmpty { "Majikan MOCHI" }
        }
    }
}
