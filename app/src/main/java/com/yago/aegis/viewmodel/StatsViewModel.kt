package com.yago.aegis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.UserRepository
import com.yago.aegis.data.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historial de entrenamientos
    val workoutHistory: StateFlow<List<WorkoutSession>> = repository.workoutHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Disciplina semanal: (sesiones esta semana, objetivo)
    val weeklyDiscipline: Flow<Pair<Int, Int>> = combine(
        workoutHistory,
        repository.targetDaysPerWeek
    ) { history, target ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startOfWeek = calendar.timeInMillis
        val sessionsThisWeek = history.count { it.date >= startOfWeek }
        Pair(sessionsThisWeek, target)
    }

    // Volumen semanal y comparativa con semana anterior
    val weeklyVolumeStats: Flow<Pair<Double, Double>> = workoutHistory.map { history ->
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        val thisWeekVol = history.filter { it.date >= (now - oneWeekMs) }.sumOf { calculateVolume(it) }
        val lastWeekVol = history.filter { it.date in (now - 2 * oneWeekMs)..(now - oneWeekMs) }.sumOf { calculateVolume(it) }
        val diff = if (lastWeekVol > 0) ((thisWeekVol - lastWeekVol) / lastWeekVol) * 100 else 0.0
        Pair(thisWeekVol, diff)
    }

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
    }

    // Búsqueda y filtro de ejercicios (estado UI local al ViewModel)
    var searchQuery by mutableStateOf("")
    var selectedTag by mutableStateOf("ALL")

    // Tags disponibles para el filtro en Stats
    val availableStatsTags: StateFlow<List<String>> = allExercises.map { exercises ->
        exercises.flatMap { it.tags }
            .map { it.uppercase() }
            .filter { it != DefaultExercises.BASE_TAG.uppercase() }
            .distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExercises: Flow<List<Exercise>> = combine(
        allExercises,
        workoutHistory,
        snapshotFlow { searchQuery },
        snapshotFlow { selectedTag }
    ) { library, history, query, tag ->
        library.filter { exercise ->
            val matchesQuery = exercise.name.contains(query, ignoreCase = true)
            val matchesTag = tag == "ALL" || exercise.tags.any { it.uppercase() == tag.uppercase() }
                || exercise.muscleGroup.uppercase() == tag.uppercase()
            matchesQuery && matchesTag
        }.map { exercise ->
            val maxWeight = history
                .flatMap { it.exercisesProgress }
                .filter { it.exercise.id == exercise.id }
                .flatMap { it.sets }
                .filter { it.isCompleted }
                .maxOfOrNull { it.weight } ?: exercise.oneRepMax
            exercise.copy(oneRepMax = maxWeight)
        }
    }

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

    private fun calculateVolume(session: WorkoutSession): Double {
        return session.exercisesProgress.sumOf { prog ->
            prog.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
        }
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
