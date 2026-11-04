package fr.formation.taches.ui.taches

import androidx.lifecycle.SavedStateHandle
import fr.formation.taches.MainDispatcherRule
import fr.formation.taches.data.FakeTacheRepository
import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TachesViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()   // UnconfinedTestDispatcher

    private val lire = Tache(1, "Lire le guide d'architecture", faite = false, priorite = Priorite.Normale)
    private val coder = Tache(2, "Coder le ViewModel", faite = true, priorite = Priorite.Haute)
    private val tester = Tache(3, "Tester le ViewModel", faite = false, priorite = Priorite.Basse)

    private val repository = FakeTacheRepository(listOf(lire, coder, tester))

    private fun creerViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        TachesViewModel(repository, savedStateHandle)

    /** stateIn(WhileSubscribed) : il faut un collecteur pour démarrer le flux. */
    private fun TestScope.observer(viewModel: TachesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.etat.collect() }
    }

    @Test
    fun `avant toute collecte, l'état est en chargement`() = runTest {
        val viewModel = creerViewModel()

        assertTrue(viewModel.etat.value.chargement)
    }

    @Test
    fun `sans filtre ni recherche, toutes les tâches sont affichées`() = runTest {
        val viewModel = creerViewModel()
        observer(viewModel)

        val etat = viewModel.etat.value
        assertFalse(etat.chargement)
        assertEquals(listOf(lire, coder, tester), etat.taches)
        assertEquals(Filtre.Toutes, etat.filtre)
    }

    @Test
    fun `le filtre A faire puis Faites`() = runTest {
        val viewModel = creerViewModel()
        observer(viewModel)

        viewModel.onFiltreChange(Filtre.AFaire)
        assertEquals(listOf(lire, tester), viewModel.etat.value.taches)

        viewModel.onFiltreChange(Filtre.Faites)
        assertEquals(listOf(coder), viewModel.etat.value.taches)
        assertEquals(Filtre.Faites, viewModel.etat.value.filtre)
    }

    @Test
    fun `la recherche n'est appliquée qu'après 300 ms sans frappe`() = runTest {
        val viewModel = creerViewModel()
        observer(viewModel)

        viewModel.onRechercheChange("vie")
        viewModel.onRechercheChange("viewmodel")
        assertEquals("viewmodel", viewModel.recherche.value)             // le champ suit immédiatement

        advanceTimeBy(299); runCurrent()
        assertEquals(3, viewModel.etat.value.taches.size)                // pas encore filtré

        advanceTimeBy(1); runCurrent()
        assertEquals(listOf(coder, tester), viewModel.etat.value.taches) // insensible à la casse
        assertEquals("viewmodel", viewModel.etat.value.recherche)
    }

    @Test
    fun `filtre et recherche se combinent`() = runTest {
        val viewModel = creerViewModel()
        observer(viewModel)

        viewModel.onFiltreChange(Filtre.AFaire)
        viewModel.onRechercheChange("  ViewModel  ")                     // espaces ignorés
        advanceTimeBy(300); runCurrent()

        assertEquals(listOf(tester), viewModel.etat.value.taches)
    }

    @Test
    fun `le filtre est écrit dans le SavedStateHandle`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = creerViewModel(savedStateHandle)

        viewModel.onFiltreChange(Filtre.Faites)

        assertEquals(Filtre.Faites, savedStateHandle.get<Filtre>(TachesViewModel.CLE_FILTRE))
    }

    @Test
    fun `après la mort du processus, le filtre est restauré`() = runTest {
        // Le système recrée le ViewModel avec le Bundle sauvegardé
        val restaure = SavedStateHandle(mapOf(TachesViewModel.CLE_FILTRE to Filtre.AFaire))
        val viewModel = creerViewModel(restaure)

        assertEquals(Filtre.AFaire, viewModel.etat.value.filtre)        // dès la valeur initiale
        observer(viewModel)
        assertEquals(listOf(lire, tester), viewModel.etat.value.taches)
    }

    @Test
    fun `ajouter enlève les espaces et garde la priorité`() = runTest {
        val viewModel = creerViewModel()
        observer(viewModel)

        viewModel.ajouter("  Réviser Kotlin ", Priorite.Haute)

        assertEquals(Tache(4, "Réviser Kotlin", faite = false, priorite = Priorite.Haute),
            viewModel.etat.value.taches.last())
    }

    @Test
    fun `un titre vide n'est pas ajouté`() = runTest {
        val viewModel = creerViewModel()

        viewModel.ajouter("   ", Priorite.Normale)

        assertEquals(3, repository.taches.value.size)
    }

    @Test
    fun `basculer et supprimer passent par le repository`() = runTest {
        val viewModel = creerViewModel()
        observer(viewModel)

        viewModel.basculer(lire)
        viewModel.supprimer(coder)

        assertEquals(listOf(lire.copy(faite = true), tester), viewModel.etat.value.taches)
    }

    @Test
    fun `restantesEnStateFlow compte les tâches à faire`() = runTest {
        val viewModel = creerViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.restantesEnStateFlow.collect()
        }

        assertEquals(2, viewModel.restantesEnStateFlow.value)
        viewModel.basculer(lire)
        assertEquals(1, viewModel.restantesEnStateFlow.value)
    }
}
