package com.yago.aegis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.AppTags
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.WorkoutSession
import com.yago.aegis.data.effectiveSlots
import com.yago.aegis.data.withSafeDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// StatsViewModel ahora recibe UserRepository (capa de datos correcta),
// NO SettingsStore directamente como estaba antes.
class StatsViewModel(private val repository: UserRepository) : ViewModel() {

    // Preferencias de visualización
    val showVolumeCard = repository.showVolumeCard
    val showDisciplineCard = repository.showDisciplineCard
    val showEvolutionGraph = repository.showEvolutionGraph
    val showAnalyticsList = repository.showAnalyticsList
    val targetDaysPerWeek = repository.targetDaysPerWeek
    val restTimerSeconds = repository.restTimerSeconds

    // Librería de ejercicios
    val allExercises: StateFlow<List<Exercise>> = repository.exerciseLibrary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), emptyList())

    // Historial de entrenamientos
    val workoutHistory: StateFlow<List<WorkoutSession>> = repository.workoutHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), emptyList())

    // Disciplina semanal: (sesiones esta semana, objetivo)
    // Usa semana de calendario (lunes 00:00) — misma definición que el volumen semanal.
    val weeklyDiscipline: Flow<Pair<Int, Int>> = combine(
        workoutHistory,
        repository.targetDaysPerWeek
    ) { history, target ->
        val startOfWeek = startOfWeekMillis(0)
        val sessionsThisWeek = history.count { it.date >= startOfWeek }
        Pair(sessionsThisWeek, target)
    }

    // Volumen semanal y comparativa con la semana anterior.
    // Misma ventana lunes–domingo que la disciplina, para que ambos números cuadren.
    val weeklyVolumeStats: Flow<Pair<Double, Double>> = workoutHistory.map { history ->
        val startOfThisWeek = startOfWeekMillis(0)
        val startOfLastWeek = startOfWeekMillis(1)
        val thisWeekVol = history.filter { it.date >= startOfThisWeek }.sumOf { calculateVolume(it) }
        val lastWeekVol = history.filter { it.date in startOfLastWeek until startOfThisWeek }.sumOf { calculateVolume(it) }
        val diff = if (lastWeekVol > 0) ((thisWeekVol - lastWeekVol) / lastWeekVol) * 100 else 0.0
        Pair(thisWeekVol, diff)
    }.flowOn(Dispatchers.Default)

    // Evolución de volumen mensual (últimos 3 meses)
    val monthlyVolumeEvolution: Flow<List<Pair<String, Double>>> = workoutHistory.map { history ->
        (2 downTo 0).map { i ->
            val tempCal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(tempCal.time).uppercase()
            val month = tempCal.get(Calendar.MONTH)
            val year = tempCal.get(Calendar.YEAR)
            val volume = history.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.date }
                c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
            }.sumOf { calculateVolume(it) }
            monthName to volume
        }
    }.flowOn(Dispatchers.Default)

    // Búsqueda y filtro de ejercicios (estado UI local al ViewModel)
    var searchQuery by mutableStateOf("")
    var selectedTag by mutableStateOf("ALL")
    // Filtro: mostrar solo ejercicios con datos (algún set completado en el historial)
    var showOnlyWithData by mutableStateOf(false)
    // Filtro: ejercicios de una rutina concreta (null = todas)
    var selectedRoutineId by mutableStateOf<Int?>(null)

    // Tags para el filtro en Stats: los canónicos de la app (fijos).
    val availableStatsTags: StateFlow<List<String>> = MutableStateFlow(AppTags.ALL)

    // Rutinas del usuario (para el filtro "por rutina"). withSafeDefaults evita listas null de Gson.
    val routines: StateFlow<List<Routine>> = repository.routines
        .map { list -> list.map { it.withSafeDefaults() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class StatsFilters(
        val query: String,
        val tag: String,
        val onlyWithData: Boolean,
        val routineId: Int?
    )

    val filteredExercises: Flow<List<Exercise>> = combine(
        allExercises,
        workoutHistory,
        routines,
        snapshotFlow { StatsFilters(searchQuery, selectedTag, showOnlyWithData, selectedRoutineId) }
    ) { library, history, routineList, f ->
        // Ejercicios con datos: nombres (normalizados) con al menos un set completado.
        val trainedNames: Set<String> = history.asSequence()
            .flatMap { it.exercisesProgress.asSequence() }
            .filter { prog -> prog.sets.any { it.isCompleted } }
            .map { normalizeName(it.exercise.name) }
            .toSet()
        // Ejercicios de la rutina seleccionada (por nombre, robusto a recargas de base).
        val routineNames: Set<String>? = f.routineId?.let { rid ->
            routineList.firstOrNull { it.id == rid }
                ?.effectiveSlots()
                ?.flatMap { slot -> slot.variants.map { normalizeName(it.name) } }
                ?.toSet() ?: emptySet()
        }
        library.filter { exercise ->
            val matchesQuery = exercise.name.contains(f.query, ignoreCase = true)
            val matchesTag = f.tag == "ALL" || exercise.tags.any { it.uppercase() == f.tag.uppercase() }
                || exercise.muscleGroup.uppercase() == f.tag.uppercase()
            val name = normalizeName(exercise.name)
            val matchesData = !f.onlyWithData || name in trainedNames
            val matchesRoutine = routineNames == null || name in routineNames
            matchesQuery && matchesTag && matchesData && matchesRoutine
        }.map { exercise ->
            val maxWeight = history
                .flatMap { it.exercisesProgress }
                .filter { it.exercise.id == exercise.id }
                .flatMap { it.sets }
                .filter { it.isCompleted }
                .maxOfOrNull { it.weight } ?: exercise.oneRepMax
            exercise.copy(oneRepMax = maxWeight)
        }
    }.flowOn(Dispatchers.Default)

    // Normaliza nombres para comparar (quita el Zero Width Space de los base y espacios).
    private fun normalizeName(name: String): String = name.replace("​", "").trim().uppercase()

    fun getExerciseHistory(exerciseId: Long): Flow<List<WorkoutSession>> {
        return workoutHistory.map { history ->
            history.filter { session ->
                session.exercisesProgress.any { it.exercise.id == exerciseId }
            }
        }
    }

    // --- ACCIONES ---

    fun updateTargetDays(days: Int) {
        viewModelScope.launch { repository.updateTargetDays(days) }
    }
    fun updateRestTimerSeconds(seconds: Int) {
        viewModelScope.launch { repository.updateRestTimerSeconds(seconds) }
    }

    fun toggleVolumeCard(enabled: Boolean) {
        viewModelScope.launch { repository.toggleStatSection("volume", enabled) }
    }

    fun toggleDisciplineCard(enabled: Boolean) {
        viewModelScope.launch { repository.toggleStatSection("discipline", enabled) }
    }

    fun toggleEvolutionGraph(enabled: Boolean) {
        viewModelScope.launch { repository.toggleStatSection("evolution", enabled) }
    }

    fun toggleAnalyticsList(enabled: Boolean) {
        viewModelScope.launch { repository.toggleStatSection("analytics", enabled) }
    }

    // --- UTILIDADES PRIVADAS ---

    private fun calculateVolume(session: WorkoutSession): Double =
        com.yago.aegis.data.WorkoutStats.sessionVolume(session)

    // Inicio (lunes 00:00:00) de la semana actual menos [weeksAgo] semanas, en millis.
    private fun startOfWeekMillis(weeksAgo: Int): Long {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.WEEK_OF_YEAR, -weeksAgo)
        }
        return cal.timeInMillis
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
