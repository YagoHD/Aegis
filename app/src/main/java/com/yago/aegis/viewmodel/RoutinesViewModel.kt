package com.yago.aegis.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.DefaultExercises
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: UserRepository) : ViewModel() {

    val allExercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalTags: StateFlow<List<String>> = repository.globalTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("PUSH", "PULL", "LEGS"))

    var routines = mutableStateListOf<Routine>()
        private set

    var tempExercises = mutableStateListOf<Exercise>()
        private set

    // Búsqueda en librería de ejercicios: movida aquí desde ExercisesLibraryScreen
    var librarySearchQuery by mutableStateOf("")
    var selectedLibraryTag by mutableStateOf("ALL")

    // Tags disponibles derivados de los ejercicios + tags globales
    val availableLibraryTags: StateFlow<List<String>> = combine(
        allExercises,
        globalTags
    ) { exercises, globals ->
        val fromExercises = exercises.flatMap { it.tags }.map { it.uppercase() }
        (globals.map { it.uppercase() } + fromExercises)
            .filter { it != DefaultExercises.BASE_TAG.uppercase() }
            .distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ejercicios del usuario (sin BASE_TAG)
    val filteredUserExercises: StateFlow<List<Exercise>> = combine(
        allExercises,
        snapshotFlow { librarySearchQuery },
        snapshotFlow { selectedLibraryTag }
    ) { exercises, query, tag ->
        exercises
            .filter { DefaultExercises.BASE_TAG !in it.tags }
            .filter { exercise ->
                val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
                val matchesTag = tag == "ALL" || exercise.tags.any { it.uppercase() == tag.uppercase() }
                    || exercise.muscleGroup.uppercase() == tag.uppercase()
                matchesQuery && matchesTag
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ejercicios base (con BASE_TAG)
    val filteredBaseExercises: StateFlow<List<Exercise>> = combine(
        allExercises,
        snapshotFlow { librarySearchQuery },
        snapshotFlow { selectedLibraryTag }
    ) { exercises, query, tag ->
        exercises
            .filter { DefaultExercises.BASE_TAG in it.tags }
            .filter { exercise ->
                val matchesQuery = query.isBlank() || exercise.name.contains(query, ignoreCase = true)
                val matchesTag = tag == "ALL" || exercise.tags.any { it.uppercase() == tag.uppercase() }
                    || exercise.muscleGroup.uppercase() == tag.uppercase()
                matchesQuery && matchesTag
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combinado para compatibilidad con otras pantallas (AddExercise, etc.)
    val filteredLibraryExercises: StateFlow<List<Exercise>> = combine(
        filteredUserExercises,
        filteredBaseExercises
    ) { user, base -> user + base }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.routines.collect { savedRoutines ->
                routines.clear()
                routines.addAll(savedRoutines)
            }
        }
    }

    private fun persistChanges() {
        viewModelScope.launch { repository.updateRoutines(routines.toList()) }
    }

    // --- RENDIMIENTO ---

    fun updateExercisePerformance(exerciseId: Long, summary: String, new1RM: Double) {
        viewModelScope.launch {
            val currentLibrary = repository.getAllExercises().first()
            val exerciseInLibrary = currentLibrary.find { it.id == exerciseId }
            exerciseInLibrary?.let { current ->
                val updatedPR = if (new1RM > current.oneRepMax) {
                    kotlin.math.round(new1RM * 10) / 10.0
                } else {
                    current.oneRepMax
                }
                repository.upsertExercise(current.copy(lastPerformance = summary, oneRepMax = updatedPR))
            }

            val updatedRoutines = routines.map { routine ->
                routine.copy(exercises = routine.exercises.map { exercise ->
                    if (exercise.id == exerciseId) {
                        val updatedPR = if (new1RM > exercise.oneRepMax) kotlin.math.round(new1RM * 10) / 10.0
                        else exercise.oneRepMax
                        exercise.copy(lastPerformance = summary, oneRepMax = updatedPR)
                    } else exercise
                })
            }
            routines.clear()
            routines.addAll(updatedRoutines)
            persistChanges()
        }
    }

    // --- RUTINAS ---

    fun addRoutine(name: String, iconName: String = "dumbbell") {
        val newId = (routines.maxOfOrNull { it.id } ?: 0) + 1
        routines.add(Routine(id = newId, name = name.uppercase(), exercises = emptyList(), iconName = iconName))
        persistChanges()
    }

    fun removeRoutine(routine: Routine) {
        routines.remove(routine)
        persistChanges()
    }

    fun updateRoutineFull(id: Int, newName: String, newExercises: List<Exercise>, newIconName: String) {
        val index = routines.indexOfFirst { it.id == id }
        if (index != -1) {
            routines[index] = routines[index].copy(
                name = newName.uppercase(),
                exercises = newExercises,
                iconName = newIconName
            )
            persistChanges()
        }
    }

    // --- LIBRERÍA DE EJERCICIOS ---

    fun saveOrUpdateExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.upsertExercise(exercise.copy(name = exercise.name.uppercase()))
        }
    }

    // True si hay al menos 1 ejercicio con BASE_TAG en la librería
    val hasDefaultExercises: StateFlow<Boolean> = allExercises.map { exercises ->
        exercises.any { DefaultExercises.BASE_TAG in it.tags }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun loadDefaultExercises() {
        viewModelScope.launch {
            val current = allExercises.value
            // Los nombres base llevan ZWS (Zero Width Space) al final
            // así que NUNCA coinciden con nombres creados por el usuario
            // Solo saltamos los que ya están cargados (mismo ID base)
            val existingIds = current.map { it.id }.toSet()
            val toAdd = DefaultExercises.getAll().filter { it.id !in existingIds }
            toAdd.forEach { repository.upsertExercise(it) }
        }
    }

    fun deleteDefaultExercises() {
        viewModelScope.launch {
            // ÚNICO criterio: tiene BASE_TAG → es base → se elimina
            // Ejercicios del usuario NUNCA tienen BASE_TAG, así que nunca se tocan
            val toDelete = allExercises.value.filter {
                DefaultExercises.BASE_TAG in it.tags
            }
            toDelete.forEach { repository.deleteExercise(it) }
        }
    }

    fun deleteExerciseFromLibrary(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
            removeExerciseFromAllRoutines(exercise.name)
        }
    }

    private fun removeExerciseFromAllRoutines(exerciseName: String) {
        val updated = routines.map { routine ->
            routine.copy(exercises = routine.exercises.filterNot { it.name == exerciseName })
        }
        routines.clear()
        routines.addAll(updated)
        persistChanges()
    }

    // --- TAGS ---

    fun addGlobalTag(tag: String) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            if (!currentTags.contains(tag.uppercase())) {
                currentTags.add(tag.uppercase())
                repository.updateGlobalTags(currentTags)
            }
        }
    }

    fun removeGlobalTags(tagsToRemove: List<String>) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            currentTags.removeAll(tagsToRemove)
            repository.updateGlobalTags(currentTags)
        }
    }

    // --- TEMPORAL (EDICIÓN DE RUTINA) ---

    fun setTempExercises(exercises: List<Exercise>?) {
        tempExercises.clear()
        exercises?.let { tempExercises.addAll(it) }
    }

    fun addExerciseToTemp(exercise: Exercise) {
        if (!tempExercises.any { it.id == exercise.id }) tempExercises.add(exercise)
    }

    fun clearTempExercises() {
        tempExercises.clear()
    }

    fun moveExercise(from: Int, to: Int) {
        if (from in tempExercises.indices && to in tempExercises.indices) {
            tempExercises.apply { add(to, removeAt(from)) }
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutinesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RoutinesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
