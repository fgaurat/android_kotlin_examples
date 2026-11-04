package fr.formation.panier.ui

import fr.formation.panier.MainDispatcherRule
import fr.formation.panier.data.CatalogueRepository
import fr.formation.panier.domain.Produit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/** Étape 5 : le repository est un mock MockK. */
class CatalogueViewModelMockkTest {

    // UnconfinedTestDispatcher : la coroutine de charger() s'exécute immédiatement
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<CatalogueRepository>()
    private val produits = listOf(Produit("1", "Stylo", 1.50), Produit("2", "Cahier", 3.20))

    @Test
    fun `l'état initial est Chargement`() {
        val viewModel = CatalogueViewModel(repository)

        assertEquals(CatalogueUiState.Chargement, viewModel.etat.value)
    }

    @Test
    fun `charger expose les produits du repository`() = runTest {
        coEvery { repository.produits() } returns produits
        val viewModel = CatalogueViewModel(repository)

        viewModel.charger()

        assertEquals(CatalogueUiState.Succes(produits), viewModel.etat.value)
        coVerify(exactly = 1) { repository.produits() }
    }

    @Test
    fun `une IOException donne un état Erreur avec son message`() = runTest {
        coEvery { repository.produits() } throws IOException("Réseau indisponible")
        val viewModel = CatalogueViewModel(repository)

        viewModel.charger()

        assertEquals(CatalogueUiState.Erreur("Réseau indisponible"), viewModel.etat.value)
        coVerify(exactly = 1) { repository.produits() }
    }

    @Test
    fun `une IOException sans message donne Erreur(null)`() = runTest {
        coEvery { repository.produits() } throws IOException()
        val viewModel = CatalogueViewModel(repository)

        viewModel.charger()

        assertEquals(CatalogueUiState.Erreur(null), viewModel.etat.value)
    }

    @Test
    fun `réessayer après une erreur rappelle le repository et affiche les produits`() = runTest {
        coEvery { repository.produits() } throws IOException("Hors ligne") andThen produits
        val viewModel = CatalogueViewModel(repository)

        viewModel.charger()
        viewModel.charger()

        assertEquals(CatalogueUiState.Succes(produits), viewModel.etat.value)
        coVerify(exactly = 2) { repository.produits() }
    }

    @Test
    fun `sans appel à charger le repository n'est jamais sollicité`() {
        CatalogueViewModel(repository)

        coVerify(exactly = 0) { repository.produits() }
    }
}
