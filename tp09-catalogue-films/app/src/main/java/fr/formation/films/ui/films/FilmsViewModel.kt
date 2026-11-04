package fr.formation.films.ui.films

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.formation.films.data.Film
import fr.formation.films.data.FilmsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FilmsUiState(
    val films: List<Film> = emptyList(),
    val idsFavoris: Set<Int> = emptySet(),
)

class FilmsViewModel(repository: FilmsRepository) : ViewModel() {

    val etat: StateFlow<FilmsUiState> = repository.idsFavoris
        .map { ids -> FilmsUiState(repository.films, ids) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FilmsUiState(repository.films, repository.idsFavoris.value),
        )

    companion object {
        fun factory(repository: FilmsRepository) = viewModelFactory {
            initializer { FilmsViewModel(repository) }
        }
    }
}
