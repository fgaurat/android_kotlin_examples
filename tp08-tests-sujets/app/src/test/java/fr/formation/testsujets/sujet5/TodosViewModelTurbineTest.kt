package fr.formation.testsujets.sujet5

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import fr.formation.testsujets.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Étape 5 — Tester un Flow avec Turbine
// - flow.test { … } : collecte le flow ; awaitItem() suspend jusqu'à la PROCHAINE émission (échec après 3 s)
// - on vérifie la SUITE des valeurs, pas seulement la dernière : Chargement PUIS Succes
// - expectNoEvents() : rien d'autre n'a été émis ; cancelAndIgnoreRemainingEvents() : terminer proprement
// - turbineScope { flow.testIn(backgroundScope) } : observer DEUX flows en même temps (état + messages)
// ⚠ Un StateFlow ne garde que la DERNIÈRE valeur : avec MainDispatcherRule() (Unconfined) et une latence 0,
//   init a déjà fini quand on s'abonne, le premier awaitItem() reçoit Succes. On teste ce que l'écran
//   peut observer, pas chaque affectation.
@OptIn(ExperimentalCoroutinesApi::class)
class TodosViewModelTurbineTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeTodoRepository()
    private val pain = Todo("1", "Acheter du pain")
    private val kotlin = Todo("2", "Réviser Kotlin")

    @Test
    fun `l'état émet Chargement puis Succes`() = runTest {
        repository.todos += pain
        val viewModel = TodosViewModel(repository)

        viewModel.etat.test {
            assertEquals(TodosUiState.Chargement, awaitItem())          // valeur actuelle, reçue à l'abonnement
            assertEquals(TodosUiState.Succes(listOf(pain)), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `l'état émet Chargement puis Erreur`() = runTest {
        repository.erreur = IOException()
        val viewModel = TodosViewModel(repository)

        viewModel.etat.test {
            assertEquals(TodosUiState.Chargement, awaitItem())
            assertEquals(TodosUiState.Erreur("Serveur injoignable"), awaitItem())
        }
    }

    @Test
    fun `supprimer retire la tâche ET envoie un message`() = runTest {
        repository.todos += listOf(pain, kotlin)
        val viewModel = TodosViewModel(repository)
        advanceUntilIdle()                                             // liste chargée

        turbineScope {
            val etats = viewModel.etat.testIn(backgroundScope)
            val messages = viewModel.messages.testIn(backgroundScope)
            assertEquals(TodosUiState.Succes(listOf(pain, kotlin)), etats.awaitItem())

            viewModel.supprimer(pain)

            assertEquals(TodosUiState.Succes(listOf(kotlin)), etats.awaitItem())
            assertEquals("« Acheter du pain » supprimée", messages.awaitItem())
            etats.cancelAndIgnoreRemainingEvents()
            messages.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `une suppression en échec envoie un message et ne change pas l'état`() = runTest {
        repository.todos += pain
        val viewModel = TodosViewModel(repository)
        advanceUntilIdle()
        repository.erreur = IOException()

        turbineScope {
            val etats = viewModel.etat.testIn(backgroundScope)
            val messages = viewModel.messages.testIn(backgroundScope)
            assertEquals(TodosUiState.Succes(listOf(pain)), etats.awaitItem())

            viewModel.supprimer(pain)

            assertEquals("Suppression impossible", messages.awaitItem())
            etats.expectNoEvents()                                    // l'état n'a pas bougé
            etats.cancelAndIgnoreRemainingEvents()
            messages.cancelAndIgnoreRemainingEvents()
        }
    }
}
