package com.yago.aegis.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.Exercise
import com.yago.aegis.data.Routine
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoutinesViewModel(private val repository: UserRepository) : ViewModel() {

    class RoutinesViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoutinesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RoutinesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    // ✅ UNIFICADO: Esta es nuestra fuente única para la librería
    val allExercises: StateFlow<List<Exercise>> = repository.getAllExercises()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ✅ Etiquetas globales sincronizadas
    val globalTags: StateFlow<List<String>> = repository.globalTags
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("COMPOUND", "CHEST", "LEGS", "BACK", "SHOULDERS")
        )

    var tempExercises = mutableStateListOf<Exercise>()
        private set

    var routines = mutableStateListOf<Routine>()
        private set

    init {
        viewModelScope.launch {
            repository.routines.collect { savedRoutines ->
                routines.clear()
                routines.addAll(savedRoutines)
            }
        }
    }

    private fun persistChanges() {
        viewModelScope.launch {
            repository.updateRoutines(routines.toList())
        }
    }

    // --- GESTIÓN DE RUTINAS ---
    fun addRoutine(name: String) {
        val newId = (routines.maxOfOrNull { it.id } ?: 0) + 1
        routines.add(Routine(newId, name.uppercase(), emptyList(), 0))
        persistChanges()
    }

    fun removeRoutine(routine: Routine) {
        routines.remove(routine)
        persistChanges()
    }

    fun updateRoutineFull(id: Int, newName: String, newExercises: List<Exercise>) {
        val index = routines.indexOfFirst { it.id == id }
        if (index != -1) {
            routines[index] = routines[index].copy(
                name = newName.uppercase(),
                exercises = newExercises
            )
            persistChanges()
        }
    }

    // --- GESTIÓN DE LIBRERÍA (La fuente única) ---
    fun saveOrUpdateExercise(exercise: Exercise) {
        viewModelScope.launch {
            // Pasamos el nombre a Mayúsculas para mantener la estética
            val formattedExercise = exercise.copy(name = exercise.name.uppercase())
            repository.upsertExercise(formattedExercise)
        }
    }

    fun deleteExerciseFromLibrary(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
            // IMPORTANTE: Si borras un ejercicio de la librería,
            // deberías recorrer las rutinas y quitarlo de ahí también.
            removeExerciseFromAllRoutines(exercise.name)
        }
    }

    private fun removeExerciseFromAllRoutines(exerciseName: String) {
        val updatedRoutines = routines.map { routine ->
            routine.copy(exercises = routine.exercises.filterNot { it.name == exerciseName })
        }
        routines.clear()
        routines.addAll(updatedRoutines)
        persistChanges()
    }

    // --- GESTIÓN DE TAGS ---
    fun addGlobalTag(tag: String) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()
            if (!currentTags.contains(tag.uppercase())) {
                currentTags.add(tag.uppercase())
                repository.updateGlobalTags(currentTags)
            }
        }
    }

    // --- TEMPORALES PARA EDICIÓN ---
    fun setTempExercises(exercises: List<Exercise>?) {
        tempExercises.clear()
        exercises?.let { tempExercises.addAll(it) }
    }
    fun updateRoutine(id: Int, newName: String) {
        val index = routines.indexOfFirst { it.id == id }
        if (index != -1) {
            // Solo actualizamos el nombre, manteniendo los ejercicios que ya tenía
            routines[index] = routines[index].copy(name = newName.uppercase())
            persistChanges() // Guardamos en el disco (DataStore)
        }
    }
    fun addExerciseToTemp(exercise: Exercise) {
        if (!tempExercises.any { it.name == exercise.name }) {
            tempExercises.add(exercise)
        }
    }

    fun clearTempExercises() {
        tempExercises.clear()
    }

    fun moveExercise(from: Int, to: Int) {
        if (from in tempExercises.indices && to in tempExercises.indices) {
            tempExercises.apply { add(to, removeAt(from)) }
        }
    }
    fun removeGlobalTags(tagsToRemove: List<String>) {
        viewModelScope.launch {
            val currentTags = globalTags.value.toMutableList()

            currentTags.removeAll(tagsToRemove)

            repository.updateGlobalTags(currentTags)
        }
    }
}