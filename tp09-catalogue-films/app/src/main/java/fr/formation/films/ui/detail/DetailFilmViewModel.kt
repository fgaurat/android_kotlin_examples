package fr.formation.films.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import fr.formation.films.data.Film
import fr.formation.films.data.FilmsRepository
import fr.formation.films.ui.navigation.DetailFilm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface DetailFilmUiState {
    /** Identifiant inconnu (deep link vers un film qui n'existe pas, par exemple). */
    data class Introuvable(val id: Int) : DetailFilmUiState
    data class Affiche(val film: Film, val estFavori: Boolean) : DetailFilmUiState
}

class DetailFilmViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: FilmsRepository,
) : ViewModel() {

    /** Le SavedStateHandle contient les arguments de la route : toRoute() les décode. */
    val filmId: Int = savedStateHandle.toRoute<DetailFilm>().id

    private val film: Film? = repository.film(filmId)

    val etat: StateFlow<DetailFilmUiState> = repository.idsFavoris
        .map { ids -> etatPour(ids) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = etatPour(repository.idsFavoris.value),
        )

    fun basculerFavori() {
        if (film != null) repository.basculerFavori(film.id)
    }

    private fun etatPour(ids: Set<Int>): DetailFilmUiState =
        if (film == null) DetailFilmUiState.Introuvable(filmId)
        else DetailFilmUiState.Affiche(film, film.id in ids)

    companion object {
        fun factory(repository: FilmsRepository) = viewModelFactory {
            initializer { DetailFilmViewModel(createSavedStateHandle(), repository) }
        }
    }
}
