package com.yago.aegis.data

/**
 * Abstracción de la fuente de datos en la nube (US-05). `FirestoreDataSource` es la implementación
 * real; en pruebas se sustituye por un fake, evitando inicializar Firebase. `UserRepository` depende
 * de esta interfaz, no de Firestore directamente.
 */
interface CloudDataSource {
    suspend fun saveProfile(
        name: String, mass: String, height: Double, bodyFat: String,
        disciplineDay: Int, customMeasures: List<BodyMeasure>,
        basePhotoDate: String?, actualPhotoDate: String?
    )
    suspend fun getProfile(): Map<String, Any>?

    suspend fun saveRoutines(routines: List<Routine>)
    suspend fun getRoutines(): List<Routine>?

    suspend fun saveExercises(exercises: List<Exercise>)
    suspend fun getExercises(): List<Exercise>?

    suspend fun saveWorkoutHistory(history: List<WorkoutSession>)
    suspend fun getWorkoutHistory(): List<WorkoutSession>?
    suspend fun appendWorkoutSession(session: WorkoutSession)

    suspend fun saveTags(tags: List<String>)
    suspend fun getTags(): List<String>?

    suspend fun saveSettings(
        showBMI: Boolean, showBodyFat: Boolean, showVisualLog: Boolean, showGirths: Boolean,
        showVolumeCard: Boolean, showDisciplineCard: Boolean, showEvolutionGraph: Boolean,
        showAnalyticsList: Boolean, targetDaysPerWeek: Int,
        restTimerSeconds: Int, timerVibrate: Boolean, timerSound: Boolean
    )
    suspend fun getSettings(): Map<String, Any>?

    suspend fun saveBodyHistory(list: List<BodySnapshot>)
    suspend fun getBodyHistory(): List<BodySnapshot>?

    suspend fun savePhotoHistory(list: List<PhotoRecord>)
    suspend fun getPhotoHistory(): List<PhotoRecord>?

    suspend fun hasCloudData(): Boolean
    suspend fun deleteAllUserData()
}
