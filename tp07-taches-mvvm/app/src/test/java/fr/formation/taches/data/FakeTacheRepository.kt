package fr.formation.taches.data

import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Faux repository pour les tests : instantané, déterministe, et inspectable. */
class FakeTacheRepository(initiales: List<Tache> = emptyList()) : TacheRepository {

    override val taches = MutableStateFlow(initiales)
    private var prochainId = (initiales.maxOfOrNull { it.id } ?: 0L) + 1

    override suspend fun ajouter(titre: String, priorite: Priorite) {
        taches.update { it + Tache(prochainId++, titre, faite = false, priorite = priorite) }
    }

    override suspend fun basculer(id: Long) {
        taches.update { liste -> liste.map { if (it.id == id) it.copy(faite = !it.faite) else it } }
    }

    override suspend fun supprimer(id: Long) {
        taches.update { liste -> liste.filterNot { it.id == id } }
    }
}
