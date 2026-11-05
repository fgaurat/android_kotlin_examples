package fr.formation.todoapp.data

import fr.formation.todoapp.domain.Todo
import kotlinx.coroutines.delay
import java.io.IOException

/**
 * Faux repository pour les tests du ViewModel : pas de réseau, déterministe, inspectable.
 * - `latence` : simule la durée d'une requête (en temps virtuel avec runTest)
 * - `erreur` : si non null, toutes les opérations échouent avec cette erreur
 */
class FakeTodoRepository(
    initiales: List<Todo> = emptyList(),
    var latence: Long = 0,
) : TodoRepository {

    val todos = initiales.toMutableList()
    var erreur: Throwable? = null
    val recherchesRecues = mutableListOf<String>()        // pour vérifier les appels GET
    private var prochainId = 1000

    override suspend fun lister(utilisateurId: Int, recherche: String): Result<List<Todo>> = appel {
        recherchesRecues += recherche
        todos.filter { it.utilisateurId == utilisateurId && it.titre.contains(recherche, ignoreCase = true) }
    }

    override suspend fun ajouter(titre: String, utilisateurId: Int): Result<Todo> = appel {
        Todo((prochainId++).toString(), titre, terminee = false, utilisateurId = utilisateurId).also { todos += it }
    }

    override suspend fun changerStatut(id: String, terminee: Boolean): Result<Todo> = appel {
        val index = todos.indexOfFirst { it.id == id }
        todos[index].copy(terminee = terminee).also { todos[index] = it }
    }

    override suspend fun supprimer(id: String): Result<Unit> = appel {
        todos.removeAll { it.id == id }
        Unit
    }

    private suspend fun <T> appel(bloc: () -> T): Result<T> {
        delay(latence)
        return erreur?.let { Result.failure(it) } ?: Result.success(bloc())
    }

    companion object {
        val SERVEUR_ARRETE = ErreurTodo.Reseau(IOException("Failed to connect to /10.0.2.2:3000"))
    }
}
