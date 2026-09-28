package com.yago.aegis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yago.aegis.data.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Controla los tutoriales de coach-marks (overlay desvanecido) que aparecen la PRIMERA vez
 * que el usuario entra a cada pantalla. Reiniciable desde Ajustes.
 */
class TutorialViewModel(private val repository: UserRepository) : ViewModel() {

    // null = todavía no cargado desde DataStore (evita parpadeo del overlay en usuarios que ya lo vieron).
    val seenTutorials: StateFlow<Set<String>?> = repository.seenTutorials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun markSeen(key: String) {
        // Evita escrituras redundantes si ya estaba marcado.
        if (seenTutorials.value?.contains(key) == true) return
        viewModelScope.launch { repository.markTutorialSeen(key) }
    }

    fun resetAll() {
        viewModelScope.launch { repository.resetTutorials() }
    }

    class Factory(private val repository: UserRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TutorialViewModel(repository) as T
        }
    }
}
