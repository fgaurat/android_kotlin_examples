package fr.formation.panier.ui

import app.cash.turbine.test
import fr.formation.panier.MainDispatcherRule
import fr.formation.panier.domain.Produit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Étape 6 : le repository est un fake avec delay(1_000), testé en temps virtuel puis avec Turbine. */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogueViewModelFakeTest {

    // StandardTestDispatcher : les coroutines attendent que le scheduler avance,
    // ce qui permet d'observer l'état intermédiaire Chargement
    @get:Rule val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val produits = listOf(Produit("1", "Stylo", 1.50), Produit("2", "Cahier", 3.20))
    private val repository = FakeCatalogueRepository(produits)

    // --- Temps virtuel ---

    @Test
    fun `charger passe par Chargement puis Succes après une seconde virtuelle`() = runTest {
        val viewModel = CatalogueViewModel(repository)

        viewModel.charger()
        runCurrent()                                         // la coroutine démarre, puis suspend sur delay
        assertEquals(CatalogueUiState.Chargement, viewModel.etat.value)

        advanceTimeBy(999)
        assertEquals(CatalogueUiState.Chargement, viewModel.etat.value)

        advanceTimeBy(2)                                     // la seconde est écoulée
        assertEquals(CatalogueUiState.Succes(produits), viewModel.etat.value)
        assertEquals(1, repository.nombreAppels)
    }

    @Test
    fun `le delay d'une seconde ne ralentit pas le test`() = runTest {
        val viewModel = CatalogueViewModel(repository)

        viewModel.charger()
        advanceUntilIdle()

        assertEquals(1_000, currentTime)                     // une seconde virtuelle, quelques ms réelles
        assertEquals(CatalogueUiState.Succes(produits), viewModel.etat.value)
    }

    @Test
    fun `un serveur injoignable donne Erreur après le délai`() = runTest {
        val viewModel = CatalogueViewModel(FakeCatalogueRepository.horsLigne())

        viewModel.charger()
        advanceUntilIdle()

        assertEquals(CatalogueUiState.Erreur("Serveur injoignable"), viewModel.etat.value)
    }

    @Test
    fun `un catalogue vide donne Succes avec une liste vide`() = runTest {
        val viewModel = CatalogueViewModel(FakeCatalogueRepository(emptyList()))

        viewModel.charger()
        advanceUntilIdle()

        assertEquals(CatalogueUiState.Succes(emptyList()), viewModel.etat.value)
    }

    // --- Turbine ---

    @Test
    fun `avec Turbine, l'état émet Chargement puis Succes`() = runTest {
        val viewModel = CatalogueViewModel(repository)

        viewModel.etat.test {
            assertEquals(CatalogueUiState.Chargement, awaitItem())    // valeur initiale

            viewModel.charger()

            // Chargement réémis n'est pas visible : un StateFlow ignore une valeur égale à la courante
            assertEquals(CatalogueUiState.Succes(produits), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `avec Turbine, un rechargement après une erreur repasse par Chargement`() = runTest {
        val repository = FakeCatalogueRepository.horsLigne()
        val viewModel = CatalogueViewModel(repository)

        viewModel.etat.test {
            assertEquals(CatalogueUiState.Chargement, awaitItem())
            viewModel.charger()
            assertEquals(CatalogueUiState.Erreur("Serveur injoignable"), awaitItem())

            repository.erreur = null
            repository.produits = produits
            viewModel.charger()

            assertEquals(CatalogueUiState.Chargement, awaitItem())
            assertEquals(CatalogueUiState.Succes(produits), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
