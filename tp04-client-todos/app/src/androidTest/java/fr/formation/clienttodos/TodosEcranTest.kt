package fr.formation.clienttodos

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.formation.clienttodos.data.ErreurTodo
import fr.formation.clienttodos.data.TodoRepository
import fr.formation.clienttodos.domain.model.PageTodos
import fr.formation.clienttodos.domain.model.Todo
import fr.formation.clienttodos.domain.model.TodoAvecAuteur
import fr.formation.clienttodos.ui.TodosEcran
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/** Test d'UI sur appareil : l'écran avec un faux repository, sans JSON Server. */
@RunWith(AndroidJUnit4::class)
class TodosEcranTest {

    @get:Rule val composeRule = createComposeRule()

    private class FauxRepository(var enLigne: Boolean = true) : TodoRepository {
        val todos = mutableListOf(Todo("1", "delectus aut autem", false, 1))
        private fun <T> siEnLigne(bloc: () -> T): Result<T> =
            if (enLigne) Result.success(bloc()) else Result.failure(ErreurTodo.Reseau(IOException("hors ligne")))

        override suspend fun lister(utilisateurId: Int?) = siEnLigne { todos.toList() }
        override suspend fun lire(id: String) = siEnLigne { todos.first { it.id == id } }
        override suspend fun ajouter(titre: String, utilisateurId: Int) =
            siEnLigne { Todo("x${todos.size}", titre, false, utilisateurId).also { todos += it } }
        override suspend fun changerStatut(id: String, terminee: Boolean) = siEnLigne {
            val maj = todos.first { it.id == id }.copy(terminee = terminee)
            todos.replaceAll { if (it.id == id) maj else it }
            maj
        }
        override suspend fun remplacer(todo: Todo) = siEnLigne { todo }
        override suspend fun supprimer(id: String) = siEnLigne { todos.removeAll { it.id == id }; Unit }
        override suspend fun lireAvecAuteur(todoId: String, auteurId: String): Result<TodoAvecAuteur> =
            Result.failure(UnsupportedOperationException())
        override suspend fun listerPage(page: Int, parPage: Int) =
            siEnLigne { PageTodos(todos, page, 1, todos.size, null, null) }
    }

    @Test
    fun affiche_les_taches_puis_ajoute_une_tache() {
        composeRule.setContent { TodosEcran(FauxRepository()) }

        composeRule.onNodeWithText("☐ delectus aut autem", substring = true).assertExists()
        composeRule.onNodeWithText("Ajouter").performClick()
        composeRule.onNodeWithText("☐ Préparer le TP", substring = true).assertExists()
    }

    @Test
    fun affiche_un_message_si_le_serveur_est_injoignable() {
        composeRule.setContent { TodosEcran(FauxRepository(enLigne = false)) }

        composeRule.onNodeWithText("Erreur : Serveur injoignable", substring = true).assertExists()
    }
}
