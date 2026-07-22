package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.PanteonResult
import com.yago.aegis.data.RankEngine
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Calcula los rangos del Panteón a partir del historial, la librería, el peso y el sexo.
 * Recalcula reactivamente cuando cambia cualquiera de esas fuentes.
 */
class PanteonViewModel(private val repository: UserRepository) : ViewModel() {

    val result: StateFlow<PanteonResult> = combine(
        repository.workoutHistory,
        repository.exerciseLibrary,
        repository.currentMass,
        repository.sex
    ) { history, library, mass, sex ->
        val bodyweight = mass.toDoubleOrNull() ?: 0.0
        RankEngine.compute(history, library, bodyweight, sex)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PanteonResult.EMPTY)

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PanteonViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PanteonViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
