package fr.formation.taches.ui.taches

import fr.formation.taches.domain.Tache

/**
 * Ce que l'écran affiche, et rien d'autre.
 * - `taches` : déjà filtrées (filtre + recherche) par le ViewModel
 * - `recherche` : le texte réellement appliqué, après debounce
 * - `chargement` : vrai tant que le repository n'a rien émis
 */
data class TachesUiState(
    val taches: List<Tache> = emptyList(),
    val filtre: Filtre = Filtre.Toutes,
    val recherche: String = "",
    val chargement: Boolean = false,
)
