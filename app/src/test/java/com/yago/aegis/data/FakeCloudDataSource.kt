package com.yago.aegis.data

/**
 * Fake de [CloudDataSource] para tests de [UserRepository] sin Firebase.
 * Configurable: nube vacía / con datos, fallo de lectura de red, y datos servidos desde caché.
 * Registra qué se "subió" para poder afirmar el camino de subida.
 */
class FakeCloudDataSource : CloudDataSource {

    // --- Configuración del escenario ---
    var failReads = false   // los reads (incluido hasCloudData) lanzan, simulando fallo de red
    var failDelete = false  // deleteAllUserData() lanza, simulando fallo al limpiar la nube
    var fromCache = false   // valor devuelto por isFromCache()

    // --- Datos "en la nube" (null = no existen) ---
    var profile: Map<String, Any>? = null
    var routines: List<Routine>? = null
    var exercises: List<Exercise>? = null
    var history: List<WorkoutSession>? = null
    var tags: List<String>? = null
    var settings: Map<String, Any>? = null
    var bodyHistory: List<BodySnapshot>? = null
    var photoHistory: List<PhotoRecord>? = null

    // --- Registro de subidas (para aserciones) ---
    var saveProfileCalls = 0
    var savedRoutines: List<Routine>? = null
    var savedWorkoutHistory: List<WorkoutSession>? = null

    private fun <T> read(value: T): T {
        if (failReads) throw RuntimeException("fallo de lectura de red simulado")
        return value
    }

    override suspend fun saveProfile(
        name: String, mass: String, height: Double, bodyFat: String, sex: String,
        disciplineDay: Int, customMeasures: List<BodyMeasure>,
        basePhotoDate: String?, actualPhotoDate: String?
    ) {
        saveProfileCalls++
        profile = mapOf("name" to name, "mass" to mass, "height" to height, "bodyFat" to bodyFat, "sex" to sex)
    }
    override suspend fun getProfile(): Map<String, Any>? = read(profile)

    override suspend fun saveRoutines(routines: List<Routine>) { savedRoutines = routines; this.routines = routines }
    override suspend fun getRoutines(): List<Routine>? = read(routines)

    override suspend fun saveExercises(exercises: List<Exercise>) { this.exercises = exercises }
    override suspend fun getExercises(): List<Exercise>? = read(exercises)

    override suspend fun saveWorkoutHistory(history: List<WorkoutSession>) { savedWorkoutHistory = history; this.history = history }
    override suspend fun getWorkoutHistory(): List<WorkoutSession>? = read(history)
    override suspend fun appendWorkoutSession(session: WorkoutSession) { history = (history ?: emptyList()) + session }

    override suspend fun saveTags(tags: List<String>) { this.tags = tags }
    override suspend fun getTags(): List<String>? = read(tags)

    override suspend fun saveSettings(
        showBMI: Boolean, showBodyFat: Boolean, showVisualLog: Boolean, showGirths: Boolean,
        showVolumeCard: Boolean, showDisciplineCard: Boolean, showEvolutionGraph: Boolean,
        showAnalyticsList: Boolean, targetDaysPerWeek: Int,
        restTimerSeconds: Int, timerVibrate: Boolean, timerSound: Boolean
    ) {
        settings = mapOf(
            "targetDaysPerWeek" to targetDaysPerWeek.toLong(),
            "restTimerSeconds" to restTimerSeconds.toLong()
        )
    }
    override suspend fun getSettings(): Map<String, Any>? = read(settings)

    override suspend fun saveBodyHistory(list: List<BodySnapshot>) { bodyHistory = list }
    override suspend fun getBodyHistory(): List<BodySnapshot>? = read(bodyHistory)

    override suspend fun savePhotoHistory(list: List<PhotoRecord>) { photoHistory = list }
    override suspend fun getPhotoHistory(): List<PhotoRecord>? = read(photoHistory)

    override suspend fun hasCloudData(): Boolean {
        if (failReads) throw RuntimeException("fallo de lectura de red simulado")
        return profile != null
    }
    override suspend fun isFromCache(): Boolean = fromCache

    override suspend fun deleteAllUserData() {
        if (failDelete) throw RuntimeException("fallo de borrado simulado")
        profile = null; routines = null; exercises = null; history = null
        tags = null; settings = null; bodyHistory = null; photoHistory = null
    }
}
