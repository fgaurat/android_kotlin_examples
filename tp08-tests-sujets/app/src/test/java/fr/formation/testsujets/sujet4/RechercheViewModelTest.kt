package fr.formation.testsujets.sujet4

import fr.formation.testsujets.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

// Étape 4 — Temps virtuel et debounce
// - advanceTimeBy(ms) : avancer l'horloge virtuelle d'une durée précise (le test, lui, ne dure rien)
// - stateIn(WhileSubscribed) ne démarre QUE s'il y a un collecteur : on en lance un dans backgroundScope
//   (backgroundScope : coroutines annulées automatiquement à la fin du test)
// - mock MockK : coVerify(exactly = 0) prouve que « ul » n'a jamais été envoyé au serveur
@OptIn(ExperimentalCoroutinesApi::class)
class RechercheViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()       // UnconfinedTestDispatcher

    private val repository = mockk<TodoRepository>()
    private val todo = Todo("6", "qui ullam ratione quibusdam voluptatem quia omnis")

    private fun TestScope.creerEtObserver(): RechercheViewModel {
        val viewModel = RechercheViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.resultats.collect() }
        return viewModel
    }

    @Test
    fun `la recherche attend 300 ms de silence avant d'appeler le repository`() = runTest {
        coEvery { repository.rechercher("ullam") } returns Result.success(listOf(todo))
        val viewModel = creerEtObserver()

        viewModel.onRechercheChange("ul")
        advanceTimeBy(100)
        viewModel.onRechercheChange("ullam")             // relance le délai
        advanceTimeBy(299)
        coVerify(exactly = 0) { repository.rechercher(any()) }

        advanceTimeBy(2)                                 // 300 ms écoulées depuis « ullam »
        coVerify(exactly = 1) { repository.rechercher("ullam") }
        assertEquals(listOf(todo), viewModel.resultats.value)
    }

    @Test
    fun `la même recherche, espaces en plus, n'est pas relancée`() = runTest {
        coEvery { repository.rechercher("ullam") } returns Result.success(listOf(todo))
        val viewModel = creerEtObserver()

        viewModel.onRechercheChange("ullam")
        advanceTimeBy(301)
        viewModel.onRechercheChange("ullam  ")
        advanceTimeBy(301)

        coVerify(exactly = 1) { repository.rechercher("ullam") }
    }

    @Test
    fun `un champ vide n'appelle pas le repository et donne une liste vide`() = runTest {
        val viewModel = creerEtObserver()

        viewModel.onRechercheChange("   ")
        advanceTimeBy(301)

        coVerify(exactly = 0) { repository.rechercher(any()) }
        assertEquals(emptyList<Todo>(), viewModel.resultats.value)
    }

    @Test
    fun `un échec du repository donne une liste vide`() = runTest {
        coEvery { repository.rechercher(any()) } returns Result.failure(IOException())
        val viewModel = creerEtObserver()

        viewModel.onRechercheChange("ullam")
        advanceTimeBy(301)

        assertEquals(emptyList<Todo>(), viewModel.resultats.value)
    }
}
