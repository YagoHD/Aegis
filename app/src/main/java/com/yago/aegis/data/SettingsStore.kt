package com.yago.aegis.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creamos la instancia de DataStore
private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsStore(private val context: Context) {

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val SHOW_BMI = booleanPreferencesKey("show_bmi")
        val SHOW_BODY_FAT = booleanPreferencesKey("show_body_fat")
        val SHOW_VISUAL_LOG = booleanPreferencesKey("show_visual_log")
        val SHOW_GIRTHS = booleanPreferencesKey("show_girths")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
    }

    // --- LECTURA (READ) ---
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "Guerrero Aegis" }
    val showBMI: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BMI] ?: true }
    val showBodyFat: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BODY_FAT] ?: true }
    val showVisualLog: Flow<Boolean> = context.dataStore.data.map { it[SHOW_VISUAL_LOG] ?: true }
    val showGirths: Flow<Boolean> = context.dataStore.data.map { it[SHOW_GIRTHS] ?: true }
    val avatarUri: Flow<String?> = context.dataStore.data.map { it[AVATAR_URI] }

    // --- ESCRITURA (WRITE) ---
    suspend fun saveName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun saveShowBMI(show: Boolean) {
        context.dataStore.edit { it[SHOW_BMI] = show }
    }

    suspend fun saveShowBodyFat(show: Boolean) {
        context.dataStore.edit { it[SHOW_BODY_FAT] = show }
    }

    suspend fun saveShowVisualLog(show: Boolean) {
        context.dataStore.edit { it[SHOW_VISUAL_LOG] = show }
    }

    suspend fun saveShowGirths(show: Boolean) {
        context.dataStore.edit { it[SHOW_GIRTHS] = show }
    }

    suspend fun saveAvatarUri(uri: String) {
        context.dataStore.edit { it[AVATAR_URI] = uri }
    }
}