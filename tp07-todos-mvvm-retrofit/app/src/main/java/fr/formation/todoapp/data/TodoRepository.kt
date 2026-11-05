package fr.formation.todoapp.data

import fr.formation.todoapp.domain.Todo

/**
 * Le contrat vu par le ViewModel : des objets du domaine, et des erreurs du domaine dans un Result.
 * Le ViewModel ne sait pas que les données viennent de Retrofit : en test, on lui passe un faux.
 */
interface TodoRepository {
    suspend fun lister(utilisateurId: Int, recherche: String = ""): Result<List<Todo>>
    suspend fun ajouter(titre: String, utilisateurId: Int): Result<Todo>
    suspend fun changerStatut(id: String, terminee: Boolean): Result<Todo>
    suspend fun supprimer(id: String): Result<Unit>
}
