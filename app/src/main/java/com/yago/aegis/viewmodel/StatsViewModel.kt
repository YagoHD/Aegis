package com.yago.aegis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.SettingsStore
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

class StatsViewModelFactory(private val settingsStore: SettingsStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(settingsStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class StatsViewModel(private val settingsStore: SettingsStore) : ViewModel() {

    val showVolumeCard = settingsStore.showVolumeCard
    val showDisciplineCard = settingsStore.showDisciplineCard
    val showEvolutionGraph = settingsStore.showEvolutionGraph
    val showAnalyticsList = settingsStore.showAnalyticsList
    val targetDaysPerWeek = settingsStore.targetDaysPerWeek
    val allExercises: StateFlow<List<Exercise>> = settingsStore.exerciseLibrary
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    // El historial completo desde DataStore
    val workoutHistory: StateFlow<List<WorkoutSession>> = settingsStore.workoutHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 1. DISCIPLINA SEMANAL (ej: 4/5)
    val weeklyDiscipline: Flow<Pair<Int, Int>> = workoutHistory.map { history ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        val startOfWeek = calendar.timeInMillis

        val sessionsThisWeek = history.count { it.date >= startOfWeek }
        val goal = 5 // Esto lo podríamos hacer dinámico después
        Pair(sessionsThisWeek, goal)
    }

    // 2. VOLUMEN SEMANAL Y COMPARATIVA (%)
    val weeklyVolumeStats: Flow<Pair<Double, Double>> = workoutHistory.map { history ->
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L

        val thisWeekVol = history.filter { it.date >= (now - oneWeekMs) }.sumOf { calculateVolume(it) }
        val lastWeekVol = history.filter { it.date in (now - 2 * oneWeekMs)..(now - oneWeekMs) }.sumOf { calculateVolume(it) }

        val diffPercentage = if (lastWeekVol > 0) ((thisWeekVol - lastWeekVol) / lastWeekVol) * 100 else 0.0
        Pair(thisWeekVol, diffPercentage)
    }

    private fun calculateVolume(session: WorkoutSession): Double {
        return session.exercisesProgress.sumOf { prog ->
            prog.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
        }
    }

    var searchQuery by mutableStateOf("")
    var selectedMuscleGroup by mutableStateOf("ALL")

    // Obtenemos la librería de ejercicios
    val exerciseLibrary: StateFlow<List<Exercise>> = settingsStore.exerciseLibrary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // LISTA FILTRADA: Se actualiza automáticamente cuando cambia la búsqueda o el grupo
    val filteredExercises: Flow<List<Exercise>> = combine(
        exerciseLibrary,
        workoutHistory, // Ahora también escuchamos los cambios en el historial
        snapshotFlow { searchQuery },
        snapshotFlow { selectedMuscleGroup }
    ) { library, history, query, group ->
        library.filter { exercise ->
            // 1. Filtrado por búsqueda y grupo
            val matchesQuery = exercise.name.contains(query, ignoreCase = true)
            val matchesGroup = group == "ALL" || exercise.muscleGroup.uppercase() == group.uppercase()
            matchesQuery && matchesGroup
        }.map { exercise ->
            // 2. CÁLCULO DEL PR REAL PARA CADA EJERCICIO
            // Buscamos en todo el historial el peso máximo levantado para ESTE ejercicio
            val maxWeightInHistory = history
                .flatMap { session -> session.exercisesProgress }
                .filter { it.exercise.id == exercise.id }
                .flatMap { it.sets }
                .filter { it.isCompleted } // Solo contamos los que terminaste
                .maxOfOrNull { it.weight } ?: exercise.oneRepMax

            // Devolvemos una copia del ejercicio con el PR actualizado "al vuelo"
            exercise.copy(oneRepMax = maxWeightInHistory)
        }
    }

    val monthlyVolumeEvolution: Flow<List<Pair<String, Double>>> = workoutHistory.map { history ->
        val calendar = Calendar.getInstance()
        val last3Months = mutableListOf<Pair<String, Double>>()

        for (i in 2 downTo 0) {
            val tempCal = Calendar.getInstance()
            tempCal.add(Calendar.MONTH, -i)
            val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(tempCal.time).uppercase()
            val month = tempCal.get(Calendar.MONTH)
            val year = tempCal.get(Calendar.YEAR)

            val volume = history.filter {
                val sessionCal = Calendar.getInstance().apply { timeInMillis = it.date }
                sessionCal.get(Calendar.MONTH) == month && sessionCal.get(Calendar.YEAR) == year
            }.sumOf { session ->
                session.exercisesProgress.sumOf { prog ->
                    prog.sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
                }
            }
            last3Months.add(monthName to volume)
        }
        last3Months
    }

    fun updateTargetDays(days: Int) {
        viewModelScope.launch { settingsStore.updateTargetDays(days) }
    }

    fun toggleVolumeCard(enabled: Boolean) {
        viewModelScope.launch { settingsStore.toggleStatSection("volume", enabled) }
    }

    fun toggleDisciplineCard(enabled: Boolean) {
        viewModelScope.launch { settingsStore.toggleStatSection("discipline", enabled) }
    }

    fun toggleEvolutionGraph(enabled: Boolean) {
        viewModelScope.launch { settingsStore.toggleStatSection("evolution", enabled) }
    }

    fun toggleAnalyticsList(enabled: Boolean) {
        viewModelScope.launch { settingsStore.toggleStatSection("analytics", enabled) }
    }

    fun getExerciseHistory(exerciseId: Long): Flow<List<WorkoutSession>> {
        return workoutHistory.map { history ->
            // 1. Filtramos la lista global de sesiones (history)
            history.filter { session ->
                // 2. Miramos dentro de 'exercisesProgress' de cada sesión
                session.exercisesProgress.any { progress ->
                    // 3. Comprobamos si el ID del ejercicio coincide
                    progress.exercise.id == exerciseId
                }
            }
        }
    }
}