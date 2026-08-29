package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.BodySnapshot
import com.yago.aegis.data.Exercise
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
