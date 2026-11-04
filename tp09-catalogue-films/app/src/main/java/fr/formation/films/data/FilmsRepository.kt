package fr.formation.films.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Source de vérité unique du catalogue et des favoris.
 * Une seule instance (fournie par [AppContainer]) est partagée par tous les écrans :
 * ajouter un favori depuis le détail met à jour l'onglet Favoris.
 */
class FilmsRepository(val films: List<Film> = filmsFictifs) {

    private val _idsFavoris = MutableStateFlow<Set<Int>>(emptySet())
    val idsFavoris: StateFlow<Set<Int>> = _idsFavoris.asStateFlow()

    fun film(id: Int): Film? = films.firstOrNull { it.id == id }

    fun basculerFavori(id: Int) {
        _idsFavoris.update { ids -> if (id in ids) ids - id else ids + id }
    }

    fun viderFavoris() {
        _idsFavoris.value = emptySet()
    }
}
