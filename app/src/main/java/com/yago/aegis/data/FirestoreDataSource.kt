package com.yago.aegis.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

/**
 * FirestoreDataSource gestiona toda la lectura y escritura en Firestore.
 *
 * Estructura en Firestore:
 * users/{userId}/
 *   ├── profile      → nombre, peso, altura, grasa, medidas, fotos
 *   ├── routines     → lista de rutinas con sus ejercicios
 *   ├── exercises    → librería de ejercicios
 *   ├── history      → historial de entrenamientos
 *   ├── tags         → tags globales
 *   └── settings     → preferencias de UI
 */
class FirestoreDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gson = Gson()

    private val userId: String?
        get() = auth.currentUser?.uid

    // Devuelve la referencia al documento del usuario o null si no hay sesión
    private fun userDoc(collection: String) = userId?.let {
        db.collection("users").document(it).collection("data").document(collection)
    }

    // ─────────────────────────────────────────────
    // PERFIL
    // ─────────────────────────────────────────────

    suspend fun saveProfile(
        name: String,
        mass: String,
        height: Double,
        bodyFat: String,
        disciplineDay: Int,
        customMeasures: List<BodyMeasure>,
        basePhotoDate: String?,
        actualPhotoDate: String?
    ) {
        userDoc("profile")?.set(
            mapOf(
                "name" to name,
                "mass" to mass,
                "height" to height,
                "bodyFat" to bodyFat,
                "disciplineDay" to disciplineDay,
                "customMeasures" to gson.toJson(customMeasures),
                "basePhotoDate" to (basePhotoDate ?: ""),
                "actualPhotoDate" to (actualPhotoDate ?: ""),
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )?.await()
    }

    suspend fun getProfile(): Map<String, Any>? {
        return try {
            userDoc("profile")?.get()?.await()?.data
        } catch (e: Exception) { null }
    }

    // ─────────────────────────────────────────────
    // RUTINAS
    // ─────────────────────────────────────────────

    suspend fun saveRoutines(routines: List<Routine>) {
        userDoc("routines")?.set(
            mapOf(
                "data" to gson.toJson(routines),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    suspend fun getRoutines(): List<Routine>? {
        return try {
            val doc = userDoc("routines")?.get()?.await() ?: return null
            val json = doc.getString("data") ?: return null
            val type = object : TypeToken<List<Routine>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { null }
    }

    // ─────────────────────────────────────────────
    // LIBRERÍA DE EJERCICIOS
    // ─────────────────────────────────────────────

    suspend fun saveExercises(exercises: List<Exercise>) {
        userDoc("exercises")?.set(
            mapOf(
                "data" to gson.toJson(exercises),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    suspend fun getExercises(): List<Exercise>? {
        return try {
            val doc = userDoc("exercises")?.get()?.await() ?: return null
            val json = doc.getString("data") ?: return null
            val type = object : TypeToken<List<Exercise>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { null }
    }

    // ─────────────────────────────────────────────
    // HISTORIAL DE ENTRENAMIENTOS
    // ─────────────────────────────────────────────

    suspend fun saveWorkoutHistory(history: List<WorkoutSession>) {
        userDoc("history")?.set(
            mapOf(
                "data" to gson.toJson(history),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    suspend fun getWorkoutHistory(): List<WorkoutSession>? {
        return try {
            val doc = userDoc("history")?.get()?.await() ?: return null
            val json = doc.getString("data") ?: return null
            val type = object : TypeToken<List<WorkoutSession>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { null }
    }

    suspend fun appendWorkoutSession(session: WorkoutSession) {
        // Leemos el historial actual, añadimos la sesión y guardamos
        val current = getWorkoutHistory()?.toMutableList() ?: mutableListOf()
        current.add(session)
        saveWorkoutHistory(current)
    }

    // ─────────────────────────────────────────────
    // TAGS GLOBALES
    // ─────────────────────────────────────────────

    suspend fun saveTags(tags: List<String>) {
        userDoc("tags")?.set(
            mapOf(
                "data" to gson.toJson(tags),
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    suspend fun getTags(): List<String>? {
        return try {
            val doc = userDoc("tags")?.get()?.await() ?: return null
            val json = doc.getString("data") ?: return null
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { null }
    }

    // ─────────────────────────────────────────────
    // PREFERENCIAS DE UI
    // ─────────────────────────────────────────────

    suspend fun saveSettings(
        showBMI: Boolean,
        showBodyFat: Boolean,
        showVisualLog: Boolean,
        showGirths: Boolean,
        showVolumeCard: Boolean,
        showDisciplineCard: Boolean,
        showEvolutionGraph: Boolean,
        showAnalyticsList: Boolean,
        targetDaysPerWeek: Int
    ) {
        userDoc("settings")?.set(
            mapOf(
                "showBMI" to showBMI,
                "showBodyFat" to showBodyFat,
                "showVisualLog" to showVisualLog,
                "showGirths" to showGirths,
                "showVolumeCard" to showVolumeCard,
                "showDisciplineCard" to showDisciplineCard,
                "showEvolutionGraph" to showEvolutionGraph,
                "showAnalyticsList" to showAnalyticsList,
                "targetDaysPerWeek" to targetDaysPerWeek,
                "updatedAt" to System.currentTimeMillis()
            )
        )?.await()
    }

    suspend fun getSettings(): Map<String, Any>? {
        return try {
            userDoc("settings")?.get()?.await()?.data
        } catch (e: Exception) { null }
    }

    // ─────────────────────────────────────────────
    // COMPROBACIÓN DE DATOS EN NUBE
    // ─────────────────────────────────────────────

    /** Devuelve true si el usuario ya tiene datos en Firestore */
    suspend fun hasCloudData(): Boolean {
        return try {
            val doc = userDoc("profile")?.get()?.await()
            doc?.exists() == true
        } catch (e: Exception) { false }
    }
}
