package fr.formation.mvvm.sujet4

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Couche DATA : d'où viennent les données. Le reste de l'application ne connaît que l'interface.
interface TacheRepository {
    val taches: Flow<List<Tache>>               // les données « en direct »
    suspend fun ajouter(titre: String)
    suspend fun basculer(id: Int)
}

// Une implémentation en mémoire. Demain : une API REST ou une base Room,
// sans rien changer au ViewModel ni à l'écran.
class TacheRepositoryEnMemoire : TacheRepository {

    private val donnees = MutableStateFlow(listOf(Tache(1, "Acheter du pain"), Tache(2, "Réviser Kotlin")))
    override val taches: Flow<List<Tache>> = donnees.asStateFlow()

    override suspend fun ajouter(titre: String) {
        delay(300)                                              // simule la latence du réseau
        donnees.update { liste -> liste + Tache(id = liste.size + 1, titre = titre) }
    }

    override suspend fun basculer(id: Int) {
        donnees.update { liste -> liste.map { if (it.id == id) it.copy(faite = !it.faite) else it } }
    }
}

// Qui crée le repository ? Un seul endroit, qui sera partagé par tous les ViewModels.
// Dans une vraie application : un AppContainer porté par la classe Application (ou Hilt).
object Dependances {
    val tacheRepository: TacheRepository by lazy { TacheRepositoryEnMemoire() }
}
