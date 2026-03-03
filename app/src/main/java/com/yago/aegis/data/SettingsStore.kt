package com.yago.aegis.data

import android.content.Context
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Creamos la instancia de DataStore
private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsStore(private val context: Context) {
    private val gson = Gson()

    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val SHOW_BMI = booleanPreferencesKey("show_bmi")
        val SHOW_BODY_FAT = booleanPreferencesKey("show_body_fat")
        val SHOW_VISUAL_LOG = booleanPreferencesKey("show_visual_log")
        val SHOW_GIRTHS = booleanPreferencesKey("show_girths")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val CURRENT_MASS = stringPreferencesKey("current_mass")
        val HEIGHT = doublePreferencesKey("height")
        val BODY_FAT = stringPreferencesKey("body_fat")
        val CUSTOM_MEASURES = stringPreferencesKey("custom_measures")
        val BASE_PHOTO_URI = stringPreferencesKey("base_photo_uri")
        val ACTUAL_PHOTO_URI = stringPreferencesKey("actual_photo_uri")
        val BASE_PHOTO_DATE = stringPreferencesKey("base_photo_date")
        val ACTUAL_PHOTO_DATE = stringPreferencesKey("actual_photo_date")
        private val ROUTINES_KEY = stringPreferencesKey("routines_list")
        private val EXERCISES_LIBRARY_KEY = stringPreferencesKey("exercises_library")
        private val GLOBAL_TAGS_KEY = stringPreferencesKey("global_tags")
    }

    // --- LECTURA (READ) ---
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "Guerrero Aegis" }
    val showBMI: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BMI] ?: true }
    val showBodyFat: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BODY_FAT] ?: true }
    val showVisualLog: Flow<Boolean> = context.dataStore.data.map { it[SHOW_VISUAL_LOG] ?: true }
    val showGirths: Flow<Boolean> = context.dataStore.data.map { it[SHOW_GIRTHS] ?: true }
    val avatarUri: Flow<String?> = context.dataStore.data.map { it[AVATAR_URI] }
    val currentMass: Flow<String> = context.dataStore.data.map { it[CURRENT_MASS] ?: "0.0" }
    val height: Flow<Double> = context.dataStore.data.map { it[HEIGHT] ?: 1.70 }
    val bodyFat: Flow<String> = context.dataStore.data.map { it[BODY_FAT] ?: "0.0" }
    val basePhotoUri: Flow<String?> = context.dataStore.data.map { it[BASE_PHOTO_URI] }
    val actualPhotoUri: Flow<String?> = context.dataStore.data.map { it[ACTUAL_PHOTO_URI] }
    val basePhotoDate: Flow<String?> = context.dataStore.data.map { it[BASE_PHOTO_DATE] }
    val actualPhotoDate: Flow<String?> = context.dataStore.data.map { it[ACTUAL_PHOTO_DATE] }
    val routines: Flow<List<Routine>> = context.dataStore.data.map { prefs ->
        val json = prefs[ROUTINES_KEY] ?: ""
        if (json.isEmpty()) emptyList()
        else {
            val type = object : TypeToken<List<Routine>>() {}.type
            gson.fromJson(json, type)
        }
    }

    val exerciseLibrary: Flow<List<Exercise>> = context.dataStore.data.map { prefs ->
        val json = prefs[EXERCISES_LIBRARY_KEY] ?: ""
        if (json.isEmpty()) emptyList()
        else {
            val type = object : TypeToken<List<Exercise>>() {}.type
            gson.fromJson(json, type)
        }
    }

    val globalTags: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[GLOBAL_TAGS_KEY] ?: ""
        if (json.isEmpty()) listOf("COMPOUND", "ISOLATION", "CHEST", "LEGS")
        else {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        }
    }

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

    suspend fun saveMass(mass: String) {
        context.dataStore.edit { it[CURRENT_MASS] = mass }
    }

    suspend fun saveHeight(height: Double) {
        context.dataStore.edit { it[HEIGHT] = height }
    }

    suspend fun saveBodyFat(fat: String) {
        context.dataStore.edit { it[BODY_FAT] = fat }
    }

    val customMeasures: Flow<List<BodyMeasure>> = context.dataStore.data.map { preferences ->
        val json = preferences[CUSTOM_MEASURES]
        if (json.isNullOrEmpty()) {
            // ✅ LISTA POR DEFECTO: Estas aparecerán la primera vez que abras la app
            listOf(
                BodyMeasure("ARM", "Brazo", "0.0"),
                BodyMeasure("CHEST", "Pecho", "0.0"),
                BodyMeasure("WAIST", "Cintura", "0.0"),
                BodyMeasure("THIGH", "Muslo", "0.0")
            )
        } else {
            val type = object : TypeToken<List<BodyMeasure>>() {}.type
            gson.fromJson(json, type)
        }
    }

    suspend fun saveExerciseLibrary(list: List<Exercise>) {
        val json = gson.toJson(list)
        context.dataStore.edit { it[EXERCISES_LIBRARY_KEY] = json }
    }

    suspend fun saveGlobalTags(tags: List<String>) {
        val json = gson.toJson(tags)
        context.dataStore.edit { it[GLOBAL_TAGS_KEY] = json }
    }

    // Guardar la lista: Convierte List<BodyMeasure> a un solo String JSON
    suspend fun saveCustomMeasures(measures: List<BodyMeasure>) {
        val json = gson.toJson(measures)
        context.dataStore.edit { it[CUSTOM_MEASURES] = json }
    }

    suspend fun saveBasePhotoUri(uri: String) {
        context.dataStore.edit { it[BASE_PHOTO_URI] = uri }
    }

    suspend fun saveActualPhotoUri(uri: String) {
        context.dataStore.edit { it[ACTUAL_PHOTO_URI] = uri }
    }

    suspend fun saveBasePhotoDate(date: String) {
        context.dataStore.edit { it[BASE_PHOTO_DATE] = date }
    }
    suspend fun saveActualPhotoDate(date: String) {
        context.dataStore.edit { it[ACTUAL_PHOTO_DATE] = date }
    }

    suspend fun saveRoutines(list: List<Routine>) {
        val json = gson.toJson(list)
        context.dataStore.edit { it[ROUTINES_KEY] = json }
    }
}