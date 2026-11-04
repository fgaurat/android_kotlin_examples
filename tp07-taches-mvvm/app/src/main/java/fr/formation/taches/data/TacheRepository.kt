package fr.formation.taches.data

import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import kotlinx.coroutines.flow.Flow

/** Le contrat vu par le reste de l'application (interface donnée par l'énoncé). */
interface TacheRepository {
    val taches: Flow<List<Tache>>
    suspend fun ajouter(titre: String, priorite: Priorite)
    suspend fun basculer(id: Long)          // faite ↔ à faire
    suspend fun supprimer(id: Long)
}
