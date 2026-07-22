package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.BodyMeasure
import com.yago.aegis.data.BodySnapshot
import com.yago.aegis.data.PhotoRecord
import com.yago.aegis.data.PhotoType
import com.yago.aegis.data.UserProfile
import com.yago.aegis.data.UserRepository
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
    val customMeasures: List<BodyMeasure> = emptyList(),
    val bodyHistory: List<BodySnapshot> = emptyList(),
    val photoHistory: List<PhotoRecord> = emptyList()
)

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val onboardingCompleted: Flow<Boolean> = repository.onboardingCompleted

    init {
        collectProfileData()
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
                repository.showGirths
            ) { bmi, fat, visual, girths ->
                { state: ProfileUiState -> state.copy(showBMI = bmi, showBodyFat = fat, showVisualLog = visual, showGirths = girths) }
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

        // Grupo 6: racha — computada una vez al arrancar (se actualiza al terminar entreno)
        viewModelScope.launch {
            val streak = repository.computeCurrentStreak()
            _uiState.update { it.copy(user = it.user.copy(currentStreak = streak)) }
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
            val streak = repository.computeCurrentStreak()
            _uiState.update { it.copy(user = it.user.copy(currentStreak = streak)) }
        }
    }

    fun incrementDisciplineDay() {
        val newDay = _uiState.value.user.disciplineDay + 1
        viewModelScope.launch {
            repository.updateDisciplineDay(newDay)
            // Recalcula la racha justo después de guardar la sesión
            val streak = repository.computeCurrentStreak()
            _uiState.update { it.copy(user = it.user.copy(currentStreak = streak)) }
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
