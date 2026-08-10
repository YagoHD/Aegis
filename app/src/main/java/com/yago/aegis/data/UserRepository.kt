package com.yago.aegis.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserRepository(
    private val settingsStore: SettingsStore,
    private val firestore: FirestoreDataSource = FirestoreDataSource()
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // US-01: estado observable de sincronización. La escritura local nunca se bloquea por esto.
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    /**
     * Envío a la nube en 2º plano: no bloquea, no flip a "Syncing" (para no parpadear en cada
     * guardado), pero deja de ser silencioso — marca Error si falla y se recupera a Success.
     */
    private fun pushInBackground(block: suspend () -> Unit) {
        syncScope.launch {
            runCatching { block() }
                .onFailure { _syncState.value = SyncState.Error(it.message ?: "Error de sincronización") }
                .onSuccess { if (_syncState.value is SyncState.Error) _syncState.value = SyncState.Success }
        }
    }

    val userName = settingsStore.userName
    val showBMI = settingsStore.showBMI
    val showBodyFat = settingsStore.showBodyFat
    val showVisualLog = settingsStore.showVisualLog
    val showGirths = settingsStore.showGirths
    val avatarUri = settingsStore.avatarUri
    val currentMass = settingsStore.currentMass
    val height = settingsStore.height
    val bodyFat = settingsStore.bodyFat
    val sex = settingsStore.sex
    val customMeasures = settingsStore.customMeasures
    val basePhotoUri = settingsStore.basePhotoUri
    val basePhotoDate = settingsStore.basePhotoDate
    val actualPhotoUri = settingsStore.actualPhotoUri
    val actualPhotoDate = settingsStore.actualPhotoDate
    val disciplineDay: Flow<Int> = settingsStore.disciplineDay
    val onboardingCompleted: Flow<Boolean> = settingsStore.onboardingCompleted
    val routines: Flow<List<Routine>> = settingsStore.routines
    val exerciseLibrary: Flow<List<Exercise>> = settingsStore.exerciseLibrary
    val globalTags: Flow<List<String>> = settingsStore.globalTags
    val workoutHistory: Flow<List<WorkoutSession>> = settingsStore.workoutHistory
    // Sesión de entreno en curso (persistida para no perderla si muere el proceso)
    val activeSession: Flow<WorkoutSession?> = settingsStore.activeSession
    val activeRoutineId: Flow<Int?> = settingsStore.activeRoutineId
    val activeSessionStart: Flow<Long> = settingsStore.activeSessionStart
    suspend fun saveActiveSession(session: WorkoutSession?, routineId: Int?, startTime: Long) =
        settingsStore.saveActiveSession(session, routineId, startTime)
    val showVolumeCard = settingsStore.showVolumeCard
    val showDisciplineCard = settingsStore.showDisciplineCard
    val showEvolutionGraph = settingsStore.showEvolutionGraph
    val showAnalyticsList = settingsStore.showAnalyticsList
    val targetDaysPerWeek = settingsStore.targetDaysPerWeek
    val restTimerSeconds = settingsStore.restTimerSeconds
    val timerVibrate = settingsStore.timerVibrate
    val timerSound = settingsStore.timerSound
    val showRestTimer = settingsStore.showRestTimer
    val timerPosX = settingsStore.timerPosX
    val timerPosY = settingsStore.timerPosY
    val availablePlates = settingsStore.availablePlates
    val barWeight = settingsStore.barWeight
    val bodyHistory: Flow<List<BodySnapshot>> = settingsStore.bodyHistory
    val photoHistory: Flow<List<PhotoRecord>> = settingsStore.photoHistory

    fun getAllExercises(): Flow<List<Exercise>> = settingsStore.exerciseLibrary

    suspend fun updateName(name: String) {
        settingsStore.saveName(name)
        syncProfileToCloud()
    }

    suspend fun updateMass(mass: String) {
        settingsStore.saveMass(mass)
        syncProfileToCloud()
    }

    suspend fun updateHeight(h: Double) {
        settingsStore.saveHeight(h)
        syncProfileToCloud()
    }

    suspend fun updateBodyFat(fat: String) {
        settingsStore.saveBodyFat(fat)
        syncProfileToCloud()
    }

    // El sexo se guarda local por ahora; se añadirá al sync en la nube con el motor del Panteón.
    suspend fun updateSex(value: String) = settingsStore.saveSex(value)

    suspend fun updateDisciplineDay(days: Int) {
        settingsStore.saveDisciplineDay(days)
        syncProfileToCloud()
    }

    suspend fun updateMeasures(list: List<BodyMeasure>) {
        settingsStore.saveCustomMeasures(list)
        syncProfileToCloud()
    }

    suspend fun updateAvatar(uri: String) = settingsStore.saveAvatarUri(uri)
    suspend fun updateBasePhoto(uri: String) = settingsStore.saveBasePhotoUri(uri)
    suspend fun updateActualPhoto(uri: String) = settingsStore.saveActualPhotoUri(uri)

    suspend fun updateBasePhotoDate(date: String) {
        settingsStore.saveBasePhotoDate(date)
        syncProfileToCloud()
    }

    suspend fun updateActualPhotoDate(date: String) {
        settingsStore.saveActualPhotoDate(date)
        syncProfileToCloud()
    }

    /** Archiva la foto actual en el historial antes de reemplazarla. */
    suspend fun archiveCurrentActualPhoto(dateLabel: String) {
        val uri = settingsStore.actualPhotoUri.first() ?: return
        settingsStore.addPhotoToHistory(
            PhotoRecord(uri = uri, dateLabel = dateLabel)
        )
        // Sube el registro (sin las imágenes) — US-02
        pushInBackground { firestore.savePhotoHistory(photoHistoryForCloud(settingsStore.photoHistory.first())) }
    }

    /** Guarda una snapshot corporal del día. */
    suspend fun saveBodySnapshot(snapshot: BodySnapshot) {
        settingsStore.saveBodySnapshot(snapshot)
        pushInBackground { firestore.saveBodyHistory(settingsStore.bodyHistory.first()) }
    }

    /**
     * Racha de SEMANAS consecutivas cumpliendo el objetivo semanal de entrenamientos.
     *
     * El descanso es parte del entrenamiento: castigar los días libres (racha por días
     * consecutivos) es contraproducente en una app de gym. En su lugar contamos semanas
     * (lunes–domingo) en las que se alcanzó [targetDaysPerWeek]. La semana en curso, si
     * aún no llega al objetivo, no rompe la racha — empezamos a contar desde la anterior.
     */
    suspend fun computeCurrentStreak(): Int {
        val sessions = settingsStore.workoutHistory.first()
        if (sessions.isEmpty()) return 0
        val target = settingsStore.targetDaysPerWeek.first().coerceAtLeast(1)

        val zone = java.time.ZoneId.systemDefault()
        val trainingDays = sessions
            .map { java.time.Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }
            .toSet()

        fun daysTrainedInWeek(monday: java.time.LocalDate): Int {
            val sunday = monday.plusDays(6)
            return trainingDays.count { !it.isBefore(monday) && !it.isAfter(sunday) }
        }

        // Lunes de la semana actual
        var weekStart = java.time.LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

        // Si la semana en curso aún no cumple el objetivo, no rompe la racha:
        // arrancamos el conteo desde la semana pasada.
        if (daysTrainedInWeek(weekStart) < target) weekStart = weekStart.minusWeeks(1)

        var streak = 0
        while (daysTrainedInWeek(weekStart) >= target) {
            streak++
            weekStart = weekStart.minusWeeks(1)
        }
        return streak
    }

    /** Borra todos los datos del usuario en Firestore (para borrado de cuenta). */
    suspend fun deleteCloudData() {
        runCatching { firestore.deleteAllUserData() }
    }

    suspend fun toggleBMI(enabled: Boolean) {
        settingsStore.saveShowBMI(enabled)
        syncSettingsToCloud()
    }

    suspend fun toggleBodyFat(enabled: Boolean) {
        settingsStore.saveShowBodyFat(enabled)
        syncSettingsToCloud()
    }

    suspend fun toggleVisualLog(enabled: Boolean) {
        settingsStore.saveShowVisualLog(enabled)
        syncSettingsToCloud()
    }

    suspend fun toggleGirths(enabled: Boolean) {
        settingsStore.saveShowGirths(enabled)
        syncSettingsToCloud()
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) =
        settingsStore.saveOnboardingCompleted(completed)

    suspend fun updateRoutines(list: List<Routine>) {
        settingsStore.saveRoutines(list)
        pushInBackground { firestore.saveRoutines(list) }
    }

    suspend fun updateExerciseLibrary(list: List<Exercise>) {
        settingsStore.saveExerciseLibrary(list)
        pushInBackground { firestore.saveExercises(list) }
    }

    suspend fun updateGlobalTags(tags: List<String>) {
        settingsStore.saveGlobalTags(tags)
        pushInBackground { firestore.saveTags(tags) }
    }

    suspend fun upsertExercise(exercise: Exercise) {
        val currentList = settingsStore.exerciseLibrary.first().toMutableList()
        val index = currentList.indexOfFirst { it.id == exercise.id }
        if (index != -1) currentList[index] = exercise else currentList.add(exercise)
        settingsStore.saveExerciseLibrary(currentList)
        pushInBackground { firestore.saveExercises(currentList) }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        val currentList = settingsStore.exerciseLibrary.first().toMutableList()
        currentList.removeAll { it.name.equals(exercise.name, ignoreCase = true) }
        settingsStore.saveExerciseLibrary(currentList)
        pushInBackground { firestore.saveExercises(currentList) }
    }

    suspend fun updateWorkoutSession(session: WorkoutSession) {
        // Actualiza una sesión existente en el historial (ej: añadir notas)
        val history = settingsStore.workoutHistory.first().toMutableList()
        val idx = history.indexOfFirst { it.id == session.id }
        if (idx >= 0) {
            history[idx] = session
            settingsStore.replaceWorkoutHistory(history)
            pushInBackground { firestore.saveWorkoutHistory(history) }
        }
    }

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        settingsStore.saveWorkoutSession(session)
        pushInBackground { firestore.appendWorkoutSession(session) }
    }

    suspend fun updateTimerVibrate(enabled: Boolean) {
        settingsStore.saveTimerVibrate(enabled)
        syncSettingsToCloud()
    }
    suspend fun updateTimerSound(enabled: Boolean) {
        settingsStore.saveTimerSound(enabled)
        syncSettingsToCloud()
    }
    suspend fun updateShowRestTimer(show: Boolean) {
        settingsStore.saveShowRestTimer(show)
        syncSettingsToCloud()
    }
    suspend fun updateTimerPosition(x: Float, y: Float) {
        settingsStore.saveTimerPosition(x, y)
        // La posición es preferencia local, no se sincroniza con la nube
    }
    suspend fun updateAvailablePlates(plates: List<Double>) = settingsStore.saveAvailablePlates(plates)
    suspend fun updateBarWeight(weight: Float) = settingsStore.saveBarWeight(weight)
    suspend fun updateTargetDays(days: Int) {
        settingsStore.updateTargetDays(days)
        syncSettingsToCloud()
    }
    suspend fun updateRestTimerSeconds(seconds: Int) {
        settingsStore.updateRestTimerSeconds(seconds)
        syncSettingsToCloud()
    }

    suspend fun toggleStatSection(keyName: String, isEnabled: Boolean) {
        settingsStore.toggleStatSection(keyName, isEnabled)
        syncSettingsToCloud()
    }

    private fun syncProfileToCloud() {
        pushInBackground {
            firestore.saveProfile(
                name = settingsStore.userName.first(),
                mass = settingsStore.currentMass.first(),
                height = settingsStore.height.first(),
                bodyFat = settingsStore.bodyFat.first(),
                disciplineDay = settingsStore.disciplineDay.first(),
                customMeasures = settingsStore.customMeasures.first(),
                basePhotoDate = settingsStore.basePhotoDate.first(),
                actualPhotoDate = settingsStore.actualPhotoDate.first()
            )
        }
    }

    private fun syncSettingsToCloud() {
        pushInBackground {
            firestore.saveSettings(
                showBMI = settingsStore.showBMI.first(),
                showBodyFat = settingsStore.showBodyFat.first(),
                showVisualLog = settingsStore.showVisualLog.first(),
                showGirths = settingsStore.showGirths.first(),
                showVolumeCard = settingsStore.showVolumeCard.first(),
                showDisciplineCard = settingsStore.showDisciplineCard.first(),
                showEvolutionGraph = settingsStore.showEvolutionGraph.first(),
                showAnalyticsList = settingsStore.showAnalyticsList.first(),
                targetDaysPerWeek = settingsStore.targetDaysPerWeek.first(),
                restTimerSeconds = settingsStore.restTimerSeconds.first(),
                timerVibrate = settingsStore.timerVibrate.first(),
                timerSound = settingsStore.timerSound.first()
            )
        }
    }

    suspend fun clearLocalData() {
        settingsStore.clearAll()
    }

    suspend fun syncOnLogin() {
        _syncState.value = SyncState.Syncing
        runCatching {
            // 1. Limpiar duplicados del historial local
            settingsStore.deduplicateWorkoutHistory()

            if (firestore.hasCloudData()) {
                // 2. Deduplicar también el historial en Firestore
                val cloudHistory = firestore.getWorkoutHistory()
                if (cloudHistory != null) {
                    val deduped = cloudHistory.distinctBy { it.id }.sortedBy { it.date }
                    if (deduped.size < cloudHistory.size) {
                        // Había duplicados en la nube — guardar versión limpia
                        firestore.saveWorkoutHistory(deduped)
                    }
                }
                downloadFromCloud()
            } else {
                uploadToCloud()
            }
        }.onSuccess {
            _syncState.value = SyncState.Success
        }.onFailure {
            // Los datos locales quedan intactos; el usuario puede reintentar.
            _syncState.value = SyncState.Error(it.message ?: "No se pudo sincronizar con la nube")
        }
    }

    /** Reintenta la sincronización (US-01: acción "Reintentar sincronización"). */
    suspend fun retrySync() = syncOnLogin()

    private suspend fun downloadFromCloud() {
        firestore.getProfile()?.let { data ->
            data["name"]?.toString()?.let { settingsStore.saveName(it) }
            data["mass"]?.toString()?.let { settingsStore.saveMass(it) }
            (data["height"] as? Double)?.let { settingsStore.saveHeight(it) }
            data["bodyFat"]?.toString()?.let { settingsStore.saveBodyFat(it) }
            (data["disciplineDay"] as? Long)?.toInt()?.let { settingsStore.saveDisciplineDay(it) }
            data["basePhotoDate"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveBasePhotoDate(it) }
            data["actualPhotoDate"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveActualPhotoDate(it) }
            data["customMeasures"]?.toString()?.takeIf { it.isNotEmpty() }?.let { json ->
                val type = object : com.google.gson.reflect.TypeToken<List<BodyMeasure>>() {}.type
                val measures: List<BodyMeasure> = com.google.gson.Gson().fromJson(json, type)
                settingsStore.saveCustomMeasures(measures)
            }
        }
        firestore.getRoutines()?.let { settingsStore.saveRoutines(it) }
        firestore.getExercises()?.let { settingsStore.saveExerciseLibrary(it) }
        firestore.getWorkoutHistory()?.let { cloudHistory ->
            val localHistory = settingsStore.workoutHistory.first()
            // Merge deduplicado: combinar local + nube, eliminar duplicados por ID
            // y reemplazar el historial local completo con el resultado limpio
            val merged = (localHistory + cloudHistory)
                .distinctBy { it.id }
                .sortedBy { it.date }
            // Sobrescribir el historial local completo con la versión limpia
            settingsStore.replaceWorkoutHistory(merged)
            // Sincronizar la versión limpia a Firestore también
            pushInBackground { firestore.saveWorkoutHistory(merged) }
        }
        firestore.getTags()?.let { settingsStore.saveGlobalTags(it) }
        firestore.getSettings()?.let { data ->
            (data["showBMI"] as? Boolean)?.let { settingsStore.saveShowBMI(it) }
            (data["showBodyFat"] as? Boolean)?.let { settingsStore.saveShowBodyFat(it) }
            (data["showVisualLog"] as? Boolean)?.let { settingsStore.saveShowVisualLog(it) }
            (data["showGirths"] as? Boolean)?.let { settingsStore.saveShowGirths(it) }
            (data["targetDaysPerWeek"] as? Long)?.toInt()?.let { settingsStore.updateTargetDays(it) }
            // Secciones de stats (antes se subían pero no se descargaban)
            (data["showVolumeCard"] as? Boolean)?.let { settingsStore.toggleStatSection("volume", it) }
            (data["showDisciplineCard"] as? Boolean)?.let { settingsStore.toggleStatSection("discipline", it) }
            (data["showEvolutionGraph"] as? Boolean)?.let { settingsStore.toggleStatSection("evolution", it) }
            (data["showAnalyticsList"] as? Boolean)?.let { settingsStore.toggleStatSection("analytics", it) }
            // Temporizador (US-02: de cuenta)
            (data["restTimerSeconds"] as? Long)?.toInt()?.let { settingsStore.updateRestTimerSeconds(it) }
            (data["timerVibrate"] as? Boolean)?.let { settingsStore.saveTimerVibrate(it) }
            (data["timerSound"] as? Boolean)?.let { settingsStore.saveTimerSound(it) }
        }
        // Historial corporal (US-02): merge por fecha
        firestore.getBodyHistory()?.let { cloud ->
            val local = settingsStore.bodyHistory.first()
            val merged = (local + cloud).distinctBy { it.date }.sortedBy { it.date }
            settingsStore.saveBodyHistory(merged)
            pushInBackground { firestore.saveBodyHistory(merged) }
        }
        // Registro de fotos (US-02): merge por fecha, conservando la URI LOCAL si la hay
        firestore.getPhotoHistory()?.let { cloud ->
            val byDate = settingsStore.photoHistory.first().associateBy { it.date }.toMutableMap()
            for (p in cloud) if (!byDate.containsKey(p.date)) byDate[p.date] = p
            val merged = byDate.values.sortedBy { it.date }
            settingsStore.savePhotoHistory(merged)
            pushInBackground { firestore.savePhotoHistory(photoHistoryForCloud(merged)) }
        }
    }

    private suspend fun uploadToCloud() {
        firestore.saveProfile(
            name = settingsStore.userName.first(),
            mass = settingsStore.currentMass.first(),
            height = settingsStore.height.first(),
            bodyFat = settingsStore.bodyFat.first(),
            disciplineDay = settingsStore.disciplineDay.first(),
            customMeasures = settingsStore.customMeasures.first(),
            basePhotoDate = settingsStore.basePhotoDate.first(),
            actualPhotoDate = settingsStore.actualPhotoDate.first()
        )
        firestore.saveRoutines(settingsStore.routines.first())
        firestore.saveExercises(settingsStore.exerciseLibrary.first())
        firestore.saveWorkoutHistory(settingsStore.workoutHistory.first())
        firestore.saveTags(settingsStore.globalTags.first())
        firestore.saveSettings(
            showBMI = settingsStore.showBMI.first(),
            showBodyFat = settingsStore.showBodyFat.first(),
            showVisualLog = settingsStore.showVisualLog.first(),
            showGirths = settingsStore.showGirths.first(),
            showVolumeCard = settingsStore.showVolumeCard.first(),
            showDisciplineCard = settingsStore.showDisciplineCard.first(),
            showEvolutionGraph = settingsStore.showEvolutionGraph.first(),
            showAnalyticsList = settingsStore.showAnalyticsList.first(),
            targetDaysPerWeek = settingsStore.targetDaysPerWeek.first(),
            restTimerSeconds = settingsStore.restTimerSeconds.first(),
            timerVibrate = settingsStore.timerVibrate.first(),
            timerSound = settingsStore.timerSound.first()
        )
        firestore.saveBodyHistory(settingsStore.bodyHistory.first())
        firestore.savePhotoHistory(photoHistoryForCloud(settingsStore.photoHistory.first()))
    }

    // El registro de fotos viaja SIN las imágenes: vaciamos la URI local (no resuelve en otro móvil)
    private fun photoHistoryForCloud(list: List<PhotoRecord>): List<PhotoRecord> =
        list.map { it.copy(uri = "") }
}

// Extensiones para workout settings — añadidas al final del archivo
// (las lecturas se exponen directamente desde settingsStore)
