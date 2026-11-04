package fr.formation.films.ui.favoris

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

class FavorisViewModel(repository: FilmsRepository) : ViewModel() {

    private fun filmsFavoris(repository: FilmsRepository, ids: Set<Int>): List<Film> =
        repository.films.filter { it.id in ids }

    val favoris: StateFlow<List<Film>> = repository.idsFavoris
        .map { ids -> filmsFavoris(repository, ids) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = filmsFavoris(repository, repository.idsFavoris.value),
        )

    companion object {
        fun factory(repository: FilmsRepository) = viewModelFactory {
            initializer { FavorisViewModel(repository) }
        }
    }
}
