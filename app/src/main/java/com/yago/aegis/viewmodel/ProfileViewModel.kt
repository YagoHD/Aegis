package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.BodySnapshot
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.ExerciseProgress
import com.yago.aegis.data.ExerciseSet
import com.yago.aegis.data.ExerciseSlot
import com.yago.aegis.data.MuscleContribution
import com.yago.aegis.data.Routine
import com.yago.aegis.data.LevelState
import com.yago.aegis.data.LevelSystem
import com.yago.aegis.data.Rank
import com.yago.aegis.data.RankEngine
import com.yago.aegis.data.divisionFromProgress
import com.yago.aegis.data.XpEntry
import com.yago.aegis.data.PhotoRecord
import com.yago.aegis.data.PhotoType
import com.yago.aegis.data.UserProfile
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.effectiveSlots
import com.yago.aegis.data.resolveLoadType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// UiState sellado: toda la UI lee de aquí, no de múltiples variables sueltas
data class ProfileUiState(
    val user: UserProfile = UserProfile(
        name = "Cargando...",
        disciplineDay = 0,
        currentMass = "0.0",
        height = 170,
        bodyFat = "0.0",
        goal = "BULK"
    ),
    val showBMI: Boolean = true,
    val showBodyFat: Boolean = true,
    val showVisualLog: Boolean = true,
    val showGirths: Boolean = true,
    val showEvolution: Boolean = true,
    val customMeasures: List<BodyMeasure> = emptyList(),
    val bodyHistory: List<BodySnapshot> = emptyList(),
    val photoHistory: List<PhotoRecord> = emptyList(),
    val level: LevelState = LevelState(),
    val levelBreakdown: List<XpEntry> = emptyList()
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val onboardingCompleted: Flow<Boolean> = repository.onboardingCompleted

    // US-01: estado de sincronización observable para la UI + acción de reintento
    val syncState: StateFlow<com.yago.aegis.data.SyncState> = repository.syncState
    fun retrySync() = viewModelScope.launch { repository.retrySync() }

    // Estado para recalcular nivel/XP cuando cambian historial o racha (llegan por vías distintas)
    private var latestHistory: List<WorkoutSession> = emptyList()
    private var latestStreak: Int = 0

    init {
        collectProfileData()
    }

    private fun recomputeLevel() {
        val level = LevelSystem.compute(latestHistory, latestStreak)
        val breakdown = LevelSystem.breakdown(latestHistory, latestStreak)
        _uiState.update { it.copy(level = level, levelBreakdown = breakdown) }
    }

    private fun collectProfileData() {
        // Grupo 1: métricas corporales — un solo update atómico
        viewModelScope.launch {
            combine(
                repository.userName,
                repository.currentMass,
                repository.height,
                repository.bodyFat,
                repository.disciplineDay
            ) { name, mass, height, fat, day ->
                { user: UserProfile -> user.copy(name = name, currentMass = mass, height = height.toInt(), bodyFat = fat, disciplineDay = day) }
            }.collect { update ->
                _uiState.update { it.copy(user = update(it.user)) }
            }
        }

        // Grupo 2: fotos — un solo update atómico
        viewModelScope.launch {
            combine(
                repository.avatarUri,
                repository.basePhotoUri,
                repository.actualPhotoUri,
                repository.basePhotoDate,
                repository.actualPhotoDate
            ) { avatar, base, actual, baseDate, actualDate ->
                { user: UserProfile -> user.copy(profilePhotoUri = avatar, basePhotoUri = base, actualPhotoUri = actual, basePhotoDate = baseDate, actualPhotoDate = actualDate) }
            }.collect { update ->
                _uiState.update { it.copy(user = update(it.user)) }
            }
        }

        // Grupo 3: toggles de UI — un solo update atómico
        viewModelScope.launch {
            combine(
                repository.showBMI,
                repository.showBodyFat,
                repository.showVisualLog,
                repository.showGirths,
                repository.showEvolution
            ) { bmi, fat, visual, girths, evolution ->
                { state: ProfileUiState -> state.copy(showBMI = bmi, showBodyFat = fat, showVisualLog = visual, showGirths = girths, showEvolution = evolution) }
            }.collect { update ->
                _uiState.update { update(it) }
            }
        }

        // Grupo 4: medidas personalizadas
        viewModelScope.launch {
            repository.customMeasures.collect { measures ->
                _uiState.update { it.copy(customMeasures = measures) }
            }
        }

        // Grupo 4b: sexo (para estándares del Panteón)
        viewModelScope.launch {
            repository.sex.collect { s ->
                _uiState.update { it.copy(user = it.user.copy(sex = s)) }
            }
        }

        // Grupo 5: historial corporal + fotos
        viewModelScope.launch {
            repository.bodyHistory.collect { history ->
                _uiState.update { it.copy(bodyHistory = history) }
            }
        }
        viewModelScope.launch {
            repository.photoHistory.collect { photos ->
                _uiState.update { it.copy(photoHistory = photos) }
            }
        }

        // Grupo 5b: historial de entrenos → nivel/XP (reactivo)
        viewModelScope.launch {
            repository.workoutHistory.collect { history ->
                latestHistory = history
                recomputeLevel()
            }
        }

        // Grupo 6: racha — computada una vez al arrancar (se actualiza al terminar entreno)
        viewModelScope.launch {
            latestStreak = repository.computeCurrentStreak()
            _uiState.update { it.copy(user = it.user.copy(currentStreak = latestStreak)) }
            recomputeLevel()
        }
    }

    // --- EXPORTACIÓN ---

    /**
     * Serializa TODOS los datos "analizables" del usuario a un JSON legible por una IA:
     * perfil + nivel, historial de métricas corporales, medidas actuales, rutinas,
     * biblioteca de ejercicios (con contribuciones musculares) e historial completo de
     * entrenos (series con peso/reps/completado). Estructura anidada intacta a propósito:
     * el JSON conserva la relación sesión→ejercicio→serie que un CSV perdería.
     * Se dispara desde Ajustes → "Exportar datos". Corre en Default (Gson + recorridos).
     */
    suspend fun buildExportJson(): String = withContext(Dispatchers.Default) {
        val name = repository.userName.first()
        val sex = repository.sex.first()
        val height = repository.height.first()
        val mass = repository.currentMass.first()
        val bodyFat = repository.bodyFat.first()
        val disciplineDay = repository.disciplineDay.first()
        val streak = repository.computeCurrentStreak()
        val history = repository.workoutHistory.first()
        val library = repository.exerciseLibrary.first()
        val routines = repository.routines.first()
        val bodyHistory = repository.bodyHistory.first()
        val measures = repository.customMeasures.first()
        val level = LevelSystem.compute(history, streak)
        // Rangos del Panteón (competitivo): tier + división por músculo, igual que en la pantalla.
        // La masa se guarda como texto; toleramos coma decimal (locale ES) para el bodyweight.
        val bw = mass.replace(",", ".").toDoubleOrNull() ?: 0.0
        val panteon = RankEngine.compute(history, library, bw, sex)

        // Serializa un ejercicio con todo su detalle analizable. Se reutiliza en la biblioteca
        // y en las variantes de cada hueco de rutina (las que se alternan con la flecha), para
        // que el JSON sea autoexplicativo sin tener que cruzar por nombre.
        fun exerciseToMap(e: Exercise): Map<String, Any?> = linkedMapOf(
            "name" to e.name,
            "muscleGroup" to e.muscleGroup,
            "type" to e.type,
            "loadType" to e.resolveLoadType().name,
            "oneRepMaxKg" to e.oneRepMax,
            "bestSet" to e.bestSet,
            "tags" to e.tags,
            "muscleContributions" to (e.muscleContributions ?: emptyList()).map { c ->
                linkedMapOf("muscle" to c.muscle, "percent" to c.percent)
            }
        )

        val root = linkedMapOf<String, Any?>(
            "app" to "Aegis",
            "schemaVersion" to 1,
            "exportedAt" to System.currentTimeMillis(),
            "profile" to linkedMapOf(
                "name" to name,
                "sex" to sex,
                "heightCm" to height,
                "bodyweightKg" to mass,
                "bodyFatPct" to bodyFat,
                "trainingDaysPerWeek" to disciplineDay,
                "streakWeeks" to streak,
                "level" to level.level,
                "totalXp" to level.totalXp
            ),
            "bodyHistory" to bodyHistory.map { snap ->
                linkedMapOf(
                    "date" to snap.date,
                    "massKg" to snap.mass,
                    "bodyFatPct" to snap.bodyFat,
                    "measures" to snap.customMeasures.map { m ->
                        linkedMapOf("id" to m.id, "name" to m.name, "value" to m.value)
                    }
                )
            },
            "currentMeasures" to measures.map { m ->
                linkedMapOf("id" to m.id, "name" to m.name, "value" to m.value)
            },
            "routines" to routines.map { r ->
                linkedMapOf(
                    "name" to r.name,
                    // Cada hueco (slot) es un ejercicio con sus variantes alternativas (las que se
                    // cambian con la flecha durante el entreno). Cada variante va con detalle completo.
                    "slots" to r.effectiveSlots().map { slot ->
                        linkedMapOf("variants" to slot.variants.map { exerciseToMap(it) })
                    }
                )
            },
            "exerciseLibrary" to library.map { exerciseToMap(it) },
            "workoutHistory" to history.sortedByDescending { it.date }.map { s ->
                linkedMapOf(
                    "date" to s.date,
                    "routineName" to s.routineName,
                    "notes" to s.notes,
                    "exercises" to s.exercisesProgress.map { p ->
                        linkedMapOf(
                            "name" to p.exercise.name,
                            "muscleGroup" to p.exercise.muscleGroup,
                            "loadType" to p.exercise.resolveLoadType().name,
                            "sets" to p.sets.map { st ->
                                linkedMapOf(
                                    "weightKg" to st.weight,
                                    "reps" to st.reps,
                                    "completed" to st.isCompleted
                                )
                            }
                        )
                    }
                )
            },
            // Rangos del Panteón calculados por la app (lo que ves en la pestaña MIS RANGOS).
            // Ventana de 84 días con decaimiento; solo cuentan ejercicios BASE con peso.
            "panteon" to linkedMapOf(
                "windowDays" to RankEngine.DECAY_WINDOW_DAYS,
                "strongestGroup" to panteon.strongest?.group?.name,
                "weakestGroup" to panteon.weakest?.group?.name,
                "groups" to panteon.groups.map { g ->
                    linkedMapOf(
                        "group" to g.group.name,
                        "groupDisplay" to g.group.display,
                        "tier" to g.tier.name,
                        "rank" to Rank(g.tier, divisionFromProgress(g.progressToNext)).label,
                        "progressToNext" to g.progressToNext,
                        "daysSinceTrained" to g.daysSinceTrained,
                        "fatigue" to g.fatigue.name,
                        "subgroups" to g.subgroups.map { sr ->
                            linkedMapOf(
                                "subgroup" to sr.subgroup.name,
                                "subgroupDisplay" to sr.subgroup.display,
                                "tier" to sr.tier.name,
                                "rank" to Rank(sr.tier, divisionFromProgress(sr.progressToNext)).label,
                                "ratio" to sr.ratio,
                                "progressToNext" to sr.progressToNext,
                                "approx" to sr.approx,
                                "windowVolume" to sr.windowVolume
                            )
                        }
                    )
                }
            )
        )
        GsonBuilder().setPrettyPrinting().create().toJson(root)
    }

    // --- IMPORTACIÓN / RESTAURACIÓN ---

    /** Resumen de lo importado, para mostrar al usuario cuánto entró. */
    data class ImportSummary(
        val routines: Int,
        val exercises: Int,
        val sessions: Int,
        val bodySnapshots: Int
    )

    /**
     * Restaura datos desde un JSON generado por "Exportar datos".
     *
     * [replace] = true  → REEMPLAZA los datos actuales por los del fichero (restaurar un backup).
     * [replace] = false → FUSIONA: añade lo que falta (ejercicios/rutinas/medidas nuevas por nombre;
     *                      todo el historial de entrenos y corporal) sin tocar el perfil.
     *
     * Lanza excepción si el JSON no es válido; el llamador la captura y avisa al usuario.
     * Corre en Default (el parseo Gson no debe bloquear el hilo principal).
     */
    suspend fun importFromJson(json: String, replace: Boolean): ImportSummary =
        withContext(Dispatchers.Default) {
            val root = Gson().fromJson(json, ExportRoot::class.java)
                ?: throw IllegalArgumentException("JSON vacío o ilegible")

            // Reconstruye el dominio desde el export (todo tolerante a nulos/campos ausentes).
            val importedExercises = (root.exerciseLibrary ?: emptyList()).map { it.toDomainExercise() }
            val importedMeasures = (root.currentMeasures ?: emptyList()).mapNotNull { it.toDomainMeasure() }
            val importedSessions = (root.workoutHistory ?: emptyList()).map { it.toDomainSession() }
            val importedBody = (root.bodyHistory ?: emptyList()).map { it.toDomainBody() }
            val rawRoutines = root.routines ?: emptyList()

            if (replace) {
                repository.updateExerciseLibrary(importedExercises)
                val routines = rawRoutines.mapIndexed { i, r -> r.toDomainRoutine(i + 1) }
                repository.updateRoutines(routines)
                repository.updateMeasures(importedMeasures)
                repository.replaceWorkoutHistory(importedSessions)
                repository.replaceBodyHistory(importedBody)
                root.profile?.let { p ->
                    p.name?.let { repository.updateName(it) }
                    p.sex?.let { repository.updateSex(it) }
                    p.heightCm?.let { repository.updateHeight(it) }
                    p.bodyweightKg?.let { repository.updateMass(it) }
                    p.bodyFatPct?.let { repository.updateBodyFat(it) }
                    p.trainingDaysPerWeek?.let { repository.updateDisciplineDay(it) }
                }
                ImportSummary(routines.size, importedExercises.size, importedSessions.size, importedBody.size)
            } else {
                // FUSIÓN — no toca el perfil.
                val curExercises = repository.exerciseLibrary.first()
                val existingExNames = curExercises.map { normalizeName(it.name) }.toSet()
                val newExercises = importedExercises.filter { normalizeName(it.name) !in existingExNames }
                if (newExercises.isNotEmpty()) {
                    repository.updateExerciseLibrary(curExercises + newExercises)
                }

                val curRoutines = repository.routines.first()
                val existingRoutineNames = curRoutines.map { normalizeName(it.name) }.toSet()
                val baseRoutineId = curRoutines.maxOfOrNull { it.id } ?: 0
                val newRoutines = rawRoutines
                    .filter { normalizeName(it.name ?: "") !in existingRoutineNames }
                    .mapIndexed { i, r -> r.toDomainRoutine(baseRoutineId + i + 1) }
                if (newRoutines.isNotEmpty()) {
                    repository.updateRoutines(curRoutines + newRoutines)
                }

                val curMeasures = repository.customMeasures.first()
                val existingMeasureIds = curMeasures.map { it.id }.toSet()
                val newMeasures = importedMeasures.filter { it.id !in existingMeasureIds }
                if (newMeasures.isNotEmpty()) {
                    repository.updateMeasures(curMeasures + newMeasures)
                }

                if (importedSessions.isNotEmpty()) {
                    val curSessions = repository.workoutHistory.first()
                    val existingSessionKeys = curSessions.map { it.date to it.routineName }.toSet()
                    val newSessions = importedSessions.filter { (it.date to it.routineName) !in existingSessionKeys }
                    if (newSessions.isNotEmpty()) {
                        repository.replaceWorkoutHistory(curSessions + newSessions)
                    }
                }
                if (importedBody.isNotEmpty()) {
                    val curBody = repository.bodyHistory.first()
                    val existingBodyDates = curBody.map { it.date }.toSet()
                    val newBody = importedBody.filter { it.date !in existingBodyDates }
                    if (newBody.isNotEmpty()) {
                        repository.replaceBodyHistory(curBody + newBody)
                    }
                }
                ImportSummary(newRoutines.size, newExercises.size, importedSessions.size, importedBody.size)
            }
        }

    /** Normaliza un nombre para comparar (quita el Zero-Width-Space de ejercicios base y espacios). */
    private fun normalizeName(s: String) = s.replace("​", "").trim().uppercase()

    // --- ESCRITURA ---

    private var debounceJob: Job? = null

    fun updateName(newName: String) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            repository.updateName(newName)
        }
    }

    fun updateMass(newMass: String) {
        viewModelScope.launch { repository.updateMass(newMass) }
    }

    fun updateBodyFat(newFat: String) {
        viewModelScope.launch { repository.updateBodyFat(newFat) }
    }

    fun updateSex(value: String) {
        viewModelScope.launch { repository.updateSex(value) }
    }

    fun updateHeight(newHeight: Double) {
        viewModelScope.launch { repository.updateHeight(newHeight) }
    }

    fun updateAvatar(uri: String) {
        viewModelScope.launch { repository.updateAvatar(uri) }
    }

    fun toggleBMI(enabled: Boolean) = viewModelScope.launch { repository.toggleBMI(enabled) }
    fun toggleBodyFat(enabled: Boolean) = viewModelScope.launch { repository.toggleBodyFat(enabled) }
    fun toggleVisualLog(enabled: Boolean) = viewModelScope.launch { repository.toggleVisualLog(enabled) }
    fun toggleGirths(enabled: Boolean) = viewModelScope.launch { repository.toggleGirths(enabled) }
    fun toggleEvolution(enabled: Boolean) = viewModelScope.launch { repository.toggleEvolution(enabled) }

    fun updateMeasureValue(id: String, newValue: String) {
        val updatedList = _uiState.value.customMeasures.map {
            if (it.id == id) it.copy(value = newValue) else it
        }
        viewModelScope.launch { repository.updateMeasures(updatedList) }
    }

    fun addMeasure(name: String) {
        val newId = name.uppercase().replace(" ", "_")
        val newList = _uiState.value.customMeasures + BodyMeasure(newId, name, "0.0")
        viewModelScope.launch { repository.updateMeasures(newList) }
    }

    fun removeMeasure(id: String) {
        val newList = _uiState.value.customMeasures.filter { it.id != id }
        viewModelScope.launch { repository.updateMeasures(newList) }
    }

    fun updatePhoto(uri: String, type: PhotoType) {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM", Locale("es", "ES"))
        val todayDate = LocalDate.now().format(formatter).uppercase()
        viewModelScope.launch {
            when (type) {
                PhotoType.BASE -> {
                    // Archiva la foto base anterior antes de reemplazarla
                    repository.archiveCurrentActualPhoto(
                        _uiState.value.user.basePhotoDate ?: todayDate
                    )
                    repository.updateBasePhoto(uri)
                    repository.updateBasePhotoDate(todayDate)
                }
                PhotoType.ACTUAL -> {
                    // Archiva la foto actual anterior antes de reemplazarla
                    repository.archiveCurrentActualPhoto(
                        _uiState.value.user.actualPhotoDate ?: todayDate
                    )
                    repository.updateActualPhoto(uri)
                    repository.updateActualPhotoDate(todayDate)
                }
            }
        }
    }

    /** Guarda un snapshot de las métricas actuales con la fecha de hoy. */
    fun saveBodySnapshot() {
        val state = _uiState.value
        val snapshot = BodySnapshot(
            date = System.currentTimeMillis(),
            mass = state.user.currentMass,
            bodyFat = state.user.bodyFat,
            customMeasures = state.customMeasures
        )
        viewModelScope.launch { repository.saveBodySnapshot(snapshot) }
    }

    /** Recalcula y actualiza la racha tras finalizar un entreno. */
    fun refreshStreak() {
        viewModelScope.launch {
            latestStreak = repository.computeCurrentStreak()
            _uiState.update { it.copy(user = it.user.copy(currentStreak = latestStreak)) }
            recomputeLevel()
        }
    }

    fun incrementDisciplineDay() {
        val newDay = _uiState.value.user.disciplineDay + 1
        viewModelScope.launch {
            repository.updateDisciplineDay(newDay)
            // Recalcula la racha justo después de guardar la sesión
            latestStreak = repository.computeCurrentStreak()
            _uiState.update { it.copy(user = it.user.copy(currentStreak = latestStreak)) }
            recomputeLevel()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { repository.updateOnboardingCompleted(true) }
    }

    fun calcularBMI(): Double {
        val state = _uiState.value
        val mass = state.user.currentMass.toDoubleOrNull() ?: 0.0
        val heightInMeters = state.user.height / 100.0
        return if (mass > 0 && heightInMeters > 0) mass / (heightInMeters * heightInMeters) else 0.0
    }

    // Factory manual (sin Hilt). Si en el futuro añades Hilt, elimina este bloque.
    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// ---------------------------------------------------------------------------
// DTOs de importación: reflejan 1:1 el JSON de "Exportar datos" (buildExportJson).
// Todos los campos nullable con default → Gson tolera claves ausentes y genera el
// constructor sin argumentos que necesita para deserializar. Ver export-data-feature.
// ---------------------------------------------------------------------------

private data class ExportRoot(
    val schemaVersion: Int? = null,
    val profile: ExportProfile? = null,
    val bodyHistory: List<ExportBody>? = null,
    val currentMeasures: List<ExportMeasure>? = null,
    val routines: List<ExportRoutine>? = null,
    val exerciseLibrary: List<ExportExercise>? = null,
    val workoutHistory: List<ExportSession>? = null
)

private data class ExportProfile(
    val name: String? = null,
    val sex: String? = null,
    val heightCm: Double? = null,
    val bodyweightKg: String? = null,
    val bodyFatPct: String? = null,
    val trainingDaysPerWeek: Int? = null
)

private data class ExportMeasure(
    val id: String? = null,
    val name: String? = null,
    val value: String? = null
)

private data class ExportBody(
    val date: Long? = null,
    val massKg: String? = null,
    val bodyFatPct: String? = null,
    val measures: List<ExportMeasure>? = null
)

private data class ExportContribution(
    val muscle: String? = null,
    val percent: Int? = null
)

private data class ExportExercise(
    val name: String? = null,
    val muscleGroup: String? = null,
    val type: String? = null,
    val loadType: String? = null,
    val oneRepMaxKg: Double? = null,
    val bestSet: String? = null,
    val tags: List<String>? = null,
    val muscleContributions: List<ExportContribution>? = null
)

private data class ExportSlot(
    val variants: List<ExportExercise>? = null
)

private data class ExportRoutine(
    val name: String? = null,
    val slots: List<ExportSlot>? = null
)

private data class ExportSet(
    val weightKg: Double? = null,
    val reps: Int? = null,
    val completed: Boolean? = null
)

private data class ExportSessionExercise(
    val name: String? = null,
    val muscleGroup: String? = null,
    val loadType: String? = null,
    val sets: List<ExportSet>? = null
)

private data class ExportSession(
    val date: Long? = null,
    val routineName: String? = null,
    val notes: String? = null,
    val exercises: List<ExportSessionExercise>? = null
)

// --- Mappers DTO → dominio ---

private fun ExportExercise.toDomainExercise(): Exercise = Exercise(
    name = name ?: "",
    type = type ?: "CUSTOM",          // 'type' es obligatorio en Exercise (no tiene default)
    muscleGroup = muscleGroup ?: "",
    tags = tags ?: emptyList(),
    oneRepMax = oneRepMaxKg ?: 0.0,
    bestSet = bestSet ?: "--",
    muscleContributions = (muscleContributions ?: emptyList()).map {
        MuscleContribution(muscle = it.muscle ?: "", percent = it.percent ?: 0)
    },
    loadType = loadType
)

private fun ExportMeasure.toDomainMeasure(): BodyMeasure? {
    val n = name ?: return null
    return BodyMeasure(id = id ?: n, name = n, value = value ?: "")
}

private fun ExportRoutine.toDomainRoutine(newId: Int): Routine = Routine(
    id = newId,                        // Routine.id es Int obligatorio: se regenera al importar
    name = name ?: "",
    exerciseSlots = (slots ?: emptyList()).map { slot ->
        ExerciseSlot(variants = (slot.variants ?: emptyList()).map { it.toDomainExercise() })
    }
)

private fun ExportSession.toDomainSession(): WorkoutSession = WorkoutSession(
    routineName = routineName ?: "",
    date = date ?: System.currentTimeMillis(),
    notes = notes ?: "",
    exercisesProgress = (exercises ?: emptyList()).map { ex ->
        ExerciseProgress(
            exercise = Exercise(
                name = ex.name ?: "",
                type = "CUSTOM",
                muscleGroup = ex.muscleGroup ?: "",
                loadType = ex.loadType
            ),
            sets = (ex.sets ?: emptyList()).map { st ->
                ExerciseSet(
                    reps = st.reps ?: 0,
                    weight = st.weightKg ?: 0.0,
                    isCompleted = st.completed ?: false
                )
            }
        )
    }
)

private fun ExportBody.toDomainBody(): BodySnapshot = BodySnapshot(
    date = date ?: System.currentTimeMillis(),
    mass = massKg ?: "0.0",
    bodyFat = bodyFatPct ?: "0.0",
    customMeasures = (measures ?: emptyList()).mapNotNull { it.toDomainMeasure() }
)
