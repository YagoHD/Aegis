package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.PhotoType
import com.yago.aegis.data.UserProfile
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val customMeasures: List<BodyMeasure> = emptyList()
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val onboardingCompleted: Flow<Boolean> = repository.onboardingCompleted

    init {
        collectProfileData()
    }

    private fun collectProfileData() {
        viewModelScope.launch {
            repository.userName.collect { name ->
                _uiState.update { it.copy(user = it.user.copy(name = name)) }
            }
        }
        viewModelScope.launch {
            repository.currentMass.collect { mass ->
                _uiState.update { it.copy(user = it.user.copy(currentMass = mass)) }
            }
        }
        viewModelScope.launch {
            repository.height.collect { h ->
                _uiState.update { it.copy(user = it.user.copy(height = h.toInt())) }
            }
        }
        viewModelScope.launch {
            repository.bodyFat.collect { fat ->
                _uiState.update { it.copy(user = it.user.copy(bodyFat = fat)) }
            }
        }
        // avatarUri recogida UNA sola vez (estaba duplicada en el código original)
        viewModelScope.launch {
            repository.avatarUri.collect { uri ->
                _uiState.update { it.copy(user = it.user.copy(profilePhotoUri = uri)) }
            }
        }
        viewModelScope.launch {
            repository.basePhotoUri.collect { uri ->
                _uiState.update { it.copy(user = it.user.copy(basePhotoUri = uri)) }
            }
        }
        viewModelScope.launch {
            repository.actualPhotoUri.collect { uri ->
                _uiState.update { it.copy(user = it.user.copy(actualPhotoUri = uri)) }
            }
        }
        viewModelScope.launch {
            repository.basePhotoDate.collect { date ->
                _uiState.update { it.copy(user = it.user.copy(basePhotoDate = date)) }
            }
        }
        viewModelScope.launch {
            repository.actualPhotoDate.collect { date ->
                _uiState.update { it.copy(user = it.user.copy(actualPhotoDate = date)) }
            }
        }
        viewModelScope.launch {
            repository.disciplineDay.collect { days ->
                _uiState.update { it.copy(user = it.user.copy(disciplineDay = days)) }
            }
        }
        viewModelScope.launch {
            repository.showBMI.collect { show ->
                _uiState.update { it.copy(showBMI = show) }
            }
        }
        viewModelScope.launch {
            repository.showBodyFat.collect { show ->
                _uiState.update { it.copy(showBodyFat = show) }
            }
        }
        viewModelScope.launch {
            repository.showVisualLog.collect { show ->
                _uiState.update { it.copy(showVisualLog = show) }
            }
        }
        viewModelScope.launch {
            repository.showGirths.collect { show ->
                _uiState.update { it.copy(showGirths = show) }
            }
        }
        viewModelScope.launch {
            repository.customMeasures.collect { measures ->
                _uiState.update { it.copy(customMeasures = measures) }
            }
        }
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
                    repository.updateBasePhoto(uri)
                    repository.updateBasePhotoDate(todayDate)
                }
                PhotoType.ACTUAL -> {
                    repository.updateActualPhoto(uri)
                    repository.updateActualPhotoDate(todayDate)
                }
            }
        }
    }

    fun incrementDisciplineDay() {
        val newDay = _uiState.value.user.disciplineDay + 1
        viewModelScope.launch { repository.updateDisciplineDay(newDay) }
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
