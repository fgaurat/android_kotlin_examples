package fr.formation.films.data

/** Conteneur de dépendances minimal (injection manuelle, sans Hilt). */
class AppContainer {
    val filmsRepository = FilmsRepository()
}
