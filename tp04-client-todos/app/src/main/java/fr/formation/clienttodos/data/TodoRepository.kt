package fr.formation.clienttodos.data

import fr.formation.clienttodos.domain.model.PageTodos
import fr.formation.clienttodos.domain.model.Todo
import fr.formation.clienttodos.domain.model.TodoAvecAuteur

/** Le contrat vu par l'UI : des objets du domaine, et des erreurs du domaine dans un Result. */
interface TodoRepository {
    suspend fun lister(utilisateurId: Int? = null): Result<List<Todo>>
    suspend fun lire(id: String): Result<Todo>
    suspend fun ajouter(titre: String, utilisateurId: Int): Result<Todo>
    suspend fun changerStatut(id: String, terminee: Boolean): Result<Todo>
    suspend fun remplacer(todo: Todo): Result<Todo>
    suspend fun supprimer(id: String): Result<Unit>

    // Bonus
    suspend fun lireAvecAuteur(todoId: String, auteurId: String): Result<TodoAvecAuteur>
    suspend fun listerPage(page: Int, parPage: Int = 10): Result<PageTodos>
}
