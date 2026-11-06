package fr.formation.testsujets.sujet3

import fr.formation.testsujets.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Étape 3 — Tester un ViewModel
// - @get:Rule MainDispatcherRule : remplace Dispatchers.Main (celui de viewModelScope) pendant le test
// - StandardTestDispatcher : les coroutines n'avancent que sur ordre → on voit Chargement
// - advanceUntilIdle() : exécute tout ce qui est en attente, y compris les delay (temps VIRTUEL)
// - on vérifie etat.value : exactement ce que verra l'écran, sans Compose ni émulateur
// À essayer : retirer la règle → « Module with the Main dispatcher is missing »
@OptIn(ExperimentalCoroutinesApi::class)
class TodosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeTodoRepository(latence = 1_000)
    private val pain = Todo("1", "Acheter du pain")

    @Test
    fun `à la création, Chargement puis Succes`() = runTest {
        repository.todos += pain
        val viewModel = TodosViewModel(repository)

        assertEquals(TodosUiState.Chargement, viewModel.etat.value)     // la coroutine n'a pas encore tourné

        advanceUntilIdle()
        assertEquals(TodosUiState.Succes(listOf(pain)), viewModel.etat.value)
    }

    @Test
    fun `le délai du fake est virtuel`() = runTest {
        TodosViewModel(repository)
        advanceUntilIdle()

        assertEquals(1_000, currentTime)        // 1 s « écoulée »… et le test a duré quelques millisecondes
    }

    @Test
    fun `une erreur réseau donne Erreur`() = runTest {
        repository.erreur = IOException()
        val viewModel = TodosViewModel(repository)
        advanceUntilIdle()

        assertEquals(TodosUiState.Erreur("Serveur injoignable"), viewModel.etat.value)
    }

    @Test
    fun `réessayer après une erreur donne Succes`() = runTest {
        repository.erreur = IOException()
        val viewModel = TodosViewModel(repository)
        advanceUntilIdle()

        repository.erreur = null                // le serveur est revenu
        repository.todos += pain
        viewModel.charger()
        assertEquals(TodosUiState.Chargement, viewModel.etat.value)
        advanceUntilIdle()

        assertEquals(TodosUiState.Succes(listOf(pain)), viewModel.etat.value)
    }

    @Test
    fun `basculer remplace la tâche par celle renvoyée par le serveur`() = runTest {
        repository.todos += pain
        val viewModel = TodosViewModel(repository)
        advanceUntilIdle()

        viewModel.basculer(pain)
        advanceUntilIdle()

        assertEquals(TodosUiState.Succes(listOf(pain.copy(terminee = true))), viewModel.etat.value)
    }

    @Test
    fun `basculer en échec ne change pas la liste`() = runTest {
        repository.todos += pain
        val viewModel = TodosViewModel(repository)
        advanceUntilIdle()

        repository.erreur = IOException()
        viewModel.basculer(pain)
        advanceUntilIdle()

        assertEquals(TodosUiState.Succes(listOf(pain)), viewModel.etat.value)
    }
}
