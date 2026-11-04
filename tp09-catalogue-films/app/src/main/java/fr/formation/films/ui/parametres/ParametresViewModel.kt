package fr.formation.films.ui.parametres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.formation.films.data.FilmsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ParametresViewModel(private val repository: FilmsRepository) : ViewModel() {

    val nombreFavoris: StateFlow<Int> = repository.idsFavoris
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.idsFavoris.value.size)

    fun viderFavoris() = repository.viderFavoris()

    companion object {
        fun factory(repository: FilmsRepository) = viewModelFactory {
            initializer { ParametresViewModel(repository) }
        }
    }
}
