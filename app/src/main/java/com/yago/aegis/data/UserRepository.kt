package com.yago.aegis.data

import com.yago.aegis.data.db.AegisDatabase
import com.yago.aegis.data.db.RoomMigrator
import com.yago.aegis.data.db.toDomain
import com.yago.aegis.data.db.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class UserRepository(
    private val settingsStore: SettingsStore,
    private val database: AegisDatabase,
    private val roomMigrator: RoomMigrator,
    private val firestore: CloudDataSource = FirestoreDataSource()
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // US-03 fase 3: garantiza que el volcado DataStore->Room terminó antes de leer/escribir Room.
    // Sin esto, una escritura hecha antes de que corra la migración sería borrada por el replaceAll
    // del migrador. Idempotente (flag + Mutex en RoomMigrator).
    private suspend fun ensureMigrated() = roomMigrator.migrateIfNeeded()

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
    val showEvolution = settingsStore.showEvolution
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
    val username: Flow<String?> = settingsStore.username   // @usuario social (copia local)
    suspend fun saveUsername(value: String) = settingsStore.saveUsername(value)
    // US-03 fase 3: estas 5 colecciones se leen de ROOM (off-main por Room; escritura granular).
    // onStart { ensureMigrated() } asegura el volcado DataStore->Room antes de la 1ª emisión.
    val routines: Flow<List<Routine>> = database.routineDao().observeAll()
        .onStart { ensureMigrated() }.map { list -> list.map { it.toDomain() } }
    val exerciseLibrary: Flow<List<Exercise>> = database.exerciseDao().observeAll()
        .onStart { ensureMigrated() }.map { list -> list.map { it.toDomain() } }
    val globalTags: Flow<List<String>> = settingsStore.globalTags
    val workoutHistory: Flow<List<WorkoutSession>> = database.workoutSessionDao().observeAll()
        .onStart { ensureMigrated() }.map { list -> list.map { it.toDomain() } }
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
    val bodyHistory: Flow<List<BodySnapshot>> = database.bodySnapshotDao().observeAll()
        .onStart { ensureMigrated() }.map { list -> list.map { it.toDomain() } }
    val photoHistory: Flow<List<PhotoRecord>> = database.photoRecordDao().observeAll()
        .onStart { ensureMigrated() }.map { list -> list.map { it.toDomain() } }

    fun getAllExercises(): Flow<List<Exercise>> = exerciseLibrary

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
    suspend fun updateSex(value: String) {
        settingsStore.saveSex(value)
        syncProfileToCloud()   // el sexo es de cuenta (afecta a los estándares del Panteón)
    }

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
        ensureMigrated()
        database.photoRecordDao().upsert(PhotoRecord(uri = uri, dateLabel = dateLabel).toEntity())
        // Sube el registro (sin las imágenes) — US-02
        pushInBackground {
            firestore.savePhotoHistory(SyncMerge.photoHistoryForCloud(database.photoRecordDao().getAll().map { it.toDomain() }))
        }
    }

    /** Guarda una snapshot corporal del día. */
    suspend fun saveBodySnapshot(snapshot: BodySnapshot) {
        ensureMigrated()
        database.bodySnapshotDao().upsert(snapshot.toEntity())
        pushInBackground { firestore.saveBodyHistory(database.bodySnapshotDao().getAll().map { it.toDomain() }) }
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
        ensureMigrated()
        val sessions = database.workoutSessionDao().getAll().map { it.toDomain() }
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

    /**
     * Borra todos los datos del usuario en Firestore (para el borrado de cuenta).
     * PROPAGA el error (US-09 review): si la limpieza falla, `FirebaseAuthRepository.deleteAccount`
     * lo trata como fallo y NO borra la cuenta de Auth (evita datos huérfanos); es reintentable.
     */
    suspend fun deleteCloudData() {
        firestore.deleteAllUserData()
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

    // Preferencia LOCAL (no se sincroniza a la nube): mostrar/ocultar el bloque de Evolución.
    suspend fun toggleEvolution(enabled: Boolean) {
        settingsStore.saveShowEvolution(enabled)
    }

    suspend fun updateOnboardingCompleted(completed: Boolean) =
        settingsStore.saveOnboardingCompleted(completed)

    suspend fun updateRoutines(list: List<Routine>) {
        ensureMigrated()
        database.routineDao().replaceAll(list.map { it.toEntity() })
        pushInBackground { firestore.saveRoutines(list) }
    }

    suspend fun updateExerciseLibrary(list: List<Exercise>) {
        ensureMigrated()
        database.exerciseDao().replaceAll(list.map { it.toEntity() })
        pushInBackground { firestore.saveExercises(list) }
    }

    suspend fun updateGlobalTags(tags: List<String>) {
        settingsStore.saveGlobalTags(tags)
        pushInBackground { firestore.saveTags(tags) }
    }

    suspend fun upsertExercise(exercise: Exercise) {
        ensureMigrated()
        database.exerciseDao().upsert(exercise.toEntity())   // granular: 1 fila, no reescribe 201
        pushInBackground { firestore.saveExercises(database.exerciseDao().getAll().map { it.toDomain() }) }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        ensureMigrated()
        database.exerciseDao().deleteByName(exercise.name)   // borra todos los de ese nombre (NOCASE)
        pushInBackground { firestore.saveExercises(database.exerciseDao().getAll().map { it.toDomain() }) }
    }

    suspend fun updateWorkoutSession(session: WorkoutSession) {
        // Actualiza una sesión existente (ej: añadir notas). Mantiene la semántica: no crea fantasmas.
        ensureMigrated()
        if (database.workoutSessionDao().getAll().any { it.id == session.id }) {
            database.workoutSessionDao().upsert(session.toEntity())
            pushInBackground { firestore.saveWorkoutHistory(database.workoutSessionDao().getAll().map { it.toDomain() }) }
        }
    }

    suspend fun saveWorkoutSession(session: WorkoutSession) {
        ensureMigrated()
        database.workoutSessionDao().upsert(session.toEntity())   // id nuevo -> inserta (append)
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
                sex = settingsStore.sex.first(),
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
            // Asegura el volcado DataStore->Room antes de sincronizar (Room es la fuente local).
            // Room ya no puede tener ids duplicados (PK), así que la deduplicación local sobra.
            ensureMigrated()

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
            // Si los datos vinieron de la caché (sin red), es "Cached", no "Success" confirmado.
            val cached = runCatching { firestore.isFromCache() }.getOrDefault(false)
            _syncState.value = if (cached) SyncState.Cached else SyncState.Success
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
            data["sex"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveSex(it) }
            (data["disciplineDay"] as? Long)?.toInt()?.let { settingsStore.saveDisciplineDay(it) }
            data["basePhotoDate"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveBasePhotoDate(it) }
            data["actualPhotoDate"]?.toString()?.takeIf { it.isNotEmpty() }?.let { settingsStore.saveActualPhotoDate(it) }
            data["customMeasures"]?.toString()?.takeIf { it.isNotEmpty() }?.let { json ->
                val type = object : com.google.gson.reflect.TypeToken<List<BodyMeasure>>() {}.type
                val measures: List<BodyMeasure> = com.google.gson.Gson().fromJson(json, type)
                settingsStore.saveCustomMeasures(measures)
            }
        }
        firestore.getRoutines()?.let { cloud -> database.routineDao().replaceAll(cloud.map { it.toEntity() }) }
        firestore.getExercises()?.let { cloud -> database.exerciseDao().replaceAll(cloud.map { it.toEntity() }) }
        firestore.getWorkoutHistory()?.let { cloudHistory ->
            val local = database.workoutSessionDao().getAll().map { it.toDomain() }
            val merged = SyncMerge.mergeHistory(local, cloudHistory)
            database.workoutSessionDao().replaceAll(merged.map { it.toEntity() })
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
            val local = database.bodySnapshotDao().getAll().map { it.toDomain() }
            val merged = SyncMerge.mergeBodyHistory(local, cloud)
            database.bodySnapshotDao().replaceAll(merged.map { it.toEntity() })
            pushInBackground { firestore.saveBodyHistory(merged) }
        }
        // Registro de fotos (US-02): merge por fecha, conservando la URI LOCAL si la hay
        firestore.getPhotoHistory()?.let { cloud ->
            val local = database.photoRecordDao().getAll().map { it.toDomain() }
            val merged = SyncMerge.mergePhotoHistory(local, cloud)
            database.photoRecordDao().replaceAll(merged.map { it.toEntity() })
            pushInBackground { firestore.savePhotoHistory(SyncMerge.photoHistoryForCloud(merged)) }
        }
    }

    private suspend fun uploadToCloud() {
        firestore.saveProfile(
            name = settingsStore.userName.first(),
            mass = settingsStore.currentMass.first(),
            height = settingsStore.height.first(),
            bodyFat = settingsStore.bodyFat.first(),
            sex = settingsStore.sex.first(),
            disciplineDay = settingsStore.disciplineDay.first(),
            customMeasures = settingsStore.customMeasures.first(),
            basePhotoDate = settingsStore.basePhotoDate.first(),
            actualPhotoDate = settingsStore.actualPhotoDate.first()
        )
        firestore.saveRoutines(database.routineDao().getAll().map { it.toDomain() })
        firestore.saveExercises(database.exerciseDao().getAll().map { it.toDomain() })
        firestore.saveWorkoutHistory(database.workoutSessionDao().getAll().map { it.toDomain() })
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
        firestore.saveBodyHistory(database.bodySnapshotDao().getAll().map { it.toDomain() })
        firestore.savePhotoHistory(SyncMerge.photoHistoryForCloud(database.photoRecordDao().getAll().map { it.toDomain() }))
    }
}

// Extensiones para workout settings — añadidas al final del archivo
// (las lecturas se exponen directamente desde settingsStore)
