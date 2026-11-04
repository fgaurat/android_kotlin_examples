package fr.formation.films.ui.navigation

import fr.formation.films.data.Film
import kotlinx.serialization.Serializable

// Une route = un type sérialisable : le compilateur vérifie les arguments et leurs types.

/** Liste du catalogue : destination de départ. */
@Serializable data object Films

/** Détail d'un film : on ne transmet que l'identifiant, le film est relu dans le repository. */
@Serializable data class DetailFilm(val id: Int)

/** Films marqués comme favoris. */
@Serializable data object Favoris

/** Sous-graphe des paramètres : on y entre par le graphe, il ouvre [Parametres]. */
@Serializable data object ParametresGraphe
@Serializable data object Parametres
@Serializable data object APropos

/** Préfixe des deep links : films://detail/{id}. */
const val BASE_DEEP_LINK_DETAIL = "films://detail"

fun Film.versDetail() = DetailFilm(id)
