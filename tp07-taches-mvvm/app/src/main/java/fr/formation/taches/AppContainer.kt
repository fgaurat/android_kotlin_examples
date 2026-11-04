package fr.formation.taches

import fr.formation.taches.data.TacheRepository
import fr.formation.taches.data.TacheRepositoryEnMemoire

/** Injection manuelle : le conteneur décrit les dépendances partagées par toute l'application. */
interface AppContainer {
    val tacheRepository: TacheRepository
}

class DefaultAppContainer : AppContainer {
    override val tacheRepository: TacheRepository by lazy {     // instance unique
        TacheRepositoryEnMemoire()
    }
}
