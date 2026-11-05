package fr.formation.todoapp.ui.todos

import androidx.lifecycle.SavedStateHandle
import fr.formation.todoapp.MainDispatcherRule
import fr.formation.todoapp.data.FakeTodoRepository
import fr.formation.todoapp.domain.Todo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Le ViewModel est testé avec un faux repository : ni Retrofit, ni serveur, ni émulateur. */
@OptIn(ExperimentalCoroutinesApi::class)
class TodosViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()   // remplace Dispatchers.Main

    private val pain = Todo("1", "Acheter du pain", terminee = false, utilisateurId = 1)
    private val kotlin = Todo("2", "Réviser Kotlin", terminee = true, utilisateurId = 1)
    private val autre = Todo("3", "Tâche d'un autre utilisateur", terminee = false, utilisateurId = 2)

    private val repository = FakeTodoRepository(listOf(pain, kotlin, autre))

    private fun creerViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        TodosViewModel(repository, savedStateHandle)

    private fun TodosViewModel.todos() = (etat.value as TodosUiState.Succes).todos

    @Test
    fun `au démarrage, Chargement puis les tâches de l'utilisateur 1`() = runTest {
        repository.latence = 500
        val viewModel = creerViewModel()

        assertEquals(TodosUiState.Chargement, viewModel.etat.value)
        advanceUntilIdle()

        assertEquals(TodosUiState.Succes(listOf(pain, kotlin)), viewModel.etat.value)
        assertEquals(1, (viewModel.etat.value as TodosUiState.Succes).restantes)
    }

    @Test
    fun `serveur arrêté donne Erreur, puis Réessayer recharge`() = runTest {
        repository.erreur = FakeTodoRepository.SERVEUR_ARRETE
        val viewModel = creerViewModel()
        advanceUntilIdle()

        val etat = viewModel.etat.value
        assertTrue(etat is TodosUiState.Erreur)
        assertTrue((etat as TodosUiState.Erreur).message.contains("JSON Server"))

        repository.erreur = null                    // on relance JSON Server…
        viewModel.rafraichir()
        advanceUntilIdle()

        assertEquals(listOf(pain, kotlin), viewModel.todos())
    }

    @Test
    fun `la recherche n'est envoyée qu'après 300 ms sans frappe`() = runTest {
        val viewModel = creerViewModel()
        advanceUntilIdle()

        viewModel.onRechercheChange("ko")
        viewModel.onRechercheChange("kotlin")
        assertEquals("kotlin", viewModel.recherche.value)          // le champ suit immédiatement

        advanceTimeBy(299); runCurrent()
        assertEquals(listOf(""), repository.recherchesRecues)      // seulement le chargement initial

        advanceTimeBy(1); runCurrent()
        assertEquals(listOf("", "kotlin"), repository.recherchesRecues)   // une seule requête de recherche
        assertEquals(listOf(kotlin), viewModel.todos())
    }

    @Test
    fun `après la mort du processus, la recherche est restaurée et rejouée`() = runTest {
        // Le système recrée le ViewModel avec le Bundle sauvegardé
        val restaure = SavedStateHandle(mapOf(TodosViewModel.CLE_RECHERCHE to "pain"))
        val viewModel = creerViewModel(restaure)
        advanceUntilIdle()

        assertEquals("pain", viewModel.recherche.value)
        assertEquals(listOf("pain"), repository.recherchesRecues)  // pas d'attente de 300 ms au démarrage
        assertEquals(listOf(pain), viewModel.todos())
    }

    @Test
    fun `basculer remplace la tâche par celle renvoyée par le serveur`() = runTest {
        val viewModel = creerViewModel()
        advanceUntilIdle()

        viewModel.basculer(pain)
        advanceUntilIdle()

        assertEquals(listOf(pain.copy(terminee = true), kotlin), viewModel.todos())
    }

    @Test
    fun `ajouter enlève les espaces, un titre vide est ignoré`() = runTest {
        val viewModel = creerViewModel()
        advanceUntilIdle()

        viewModel.ajouter("   ")
        viewModel.ajouter("  Ranger le bureau ")
        advanceUntilIdle()

        assertEquals(3, viewModel.todos().size)
        assertEquals("Ranger le bureau", viewModel.todos().last().titre)
    }

    @Test
    fun `échec de la suppression, la liste est inchangée et un message est affiché`() = runTest {
        val viewModel = creerViewModel()
        advanceUntilIdle()

        repository.erreur = FakeTodoRepository.SERVEUR_ARRETE
        viewModel.supprimer(pain)
        advanceUntilIdle()

        assertEquals(listOf(pain, kotlin), viewModel.todos())
        assertTrue(viewModel.message.value!!.startsWith("Suppression impossible"))

        viewModel.messageAffiche()                 // l'écran a montré la Snackbar
        assertNull(viewModel.message.value)
    }

    @Test
    fun `supprimer retire la tâche de la liste`() = runTest {
        val viewModel = creerViewModel()
        advanceUntilIdle()

        viewModel.supprimer(kotlin)
        advanceUntilIdle()

        assertEquals(listOf(pain), viewModel.todos())
    }
}
