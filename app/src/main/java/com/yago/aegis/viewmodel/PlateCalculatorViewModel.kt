package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlateCalculatorUiState(
    val targetWeight: String = "",
    val platesPerSide: List<Double> = emptyList(),
    val remainder: Double = 0.0,
    val isValid: Boolean = true
)

class PlateCalculatorViewModel(private val repository: UserRepository) : ViewModel() {

    val availablePlates: StateFlow<List<Double>> = repository.availablePlates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(1.25, 2.5, 5.0, 10.0, 15.0, 20.0, 25.0))

    val barWeight: StateFlow<Float> = repository.barWeight
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20f)

    private val _uiState = MutableStateFlow(PlateCalculatorUiState())
    val uiState: StateFlow<PlateCalculatorUiState> = _uiState.asStateFlow()

    fun setTargetWeight(input: String) {
        _uiState.update { it.copy(targetWeight = input) }
        calculate(input)
    }

    fun setBarWeight(weight: Float) {
        viewModelScope.launch { repository.updateBarWeight(weight) }
        calculate(_uiState.value.targetWeight)
    }

    fun togglePlate(plate: Double) {
        val current = availablePlates.value.toMutableList()
        if (current.contains(plate)) current.remove(plate) else current.add(plate)
        val sorted = current.sorted()
        viewModelScope.launch { repository.updateAvailablePlates(sorted) }
    }

    private fun calculate(input: String) {
        val target = input.toDoubleOrNull()
        val bar = barWeight.value.toDouble()
        val plates = availablePlates.value

        if (target == null || target < bar) {
            _uiState.update { it.copy(platesPerSide = emptyList(), remainder = 0.0, isValid = target == null || target <= 0) }
            return
        }

        var remaining = (target - bar) / 2.0
        val result = mutableListOf<Double>()

        for (plate in plates.sortedDescending()) {
            while (remaining >= plate - 0.001) {
                result.add(plate)
                remaining -= plate
            }
        }

        _uiState.update {
            it.copy(
                platesPerSide = result,
                remainder = if (remaining > 0.001) remaining else 0.0,
                isValid = remaining <= 0.001
            )
        }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlateCalculatorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PlateCalculatorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
