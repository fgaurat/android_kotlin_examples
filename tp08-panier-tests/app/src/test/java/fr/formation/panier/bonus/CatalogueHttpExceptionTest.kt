package fr.formation.panier.bonus

import fr.formation.panier.MainDispatcherRule
import fr.formation.panier.data.CatalogueRepository
import fr.formation.panier.ui.CatalogueUiState
import fr.formation.panier.ui.CatalogueViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** Bonus : une HttpException (404, 500…) n'est PAS une IOException. */
class CatalogueHttpExceptionTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<CatalogueRepository>()
    private val erreur500 = HttpException(Response.error<Any>(500, "".toResponseBody()))

    @Test
    fun `HttpException n'hérite pas de IOException`() {
        assertFalse(erreur500 is IOException)   // c'est une RuntimeException
    }

    @Test
    fun `le ViewModel fourni laisse passer une HttpException, qui fait planter l'application`() {
        coEvery { repository.produits() } throws erreur500
        val viewModel = CatalogueViewModel(repository)

        // L'exception non interceptée remonte jusqu'à viewModelScope.
        // Sur un appareil : plantage. En test : runTest la relance à la fin du test.
        val levee = assertThrows(HttpException::class.java) {
            runTest { viewModel.charger() }
        }

        assertEquals(500, levee.code())
        // L'écran reste bloqué sur le chargement (s'il n'a pas planté)
        assertEquals(CatalogueUiState.Chargement, viewModel.etat.value)
    }

    @Test
    fun `le ViewModel corrigé transforme une HttpException en état Erreur`() = runTest {
        coEvery { repository.produits() } throws erreur500
        val viewModel = CatalogueViewModelCorrige(repository)

        viewModel.charger()

        assertEquals(CatalogueUiState.Erreur("HTTP 500 Response.error()"), viewModel.etat.value)
    }

    @Test
    fun `le ViewModel corrigé gère toujours une IOException`() = runTest {
        coEvery { repository.produits() } throws IOException("Réseau indisponible")
        val viewModel = CatalogueViewModelCorrige(repository)

        viewModel.charger()

        assertEquals(CatalogueUiState.Erreur("Réseau indisponible"), viewModel.etat.value)
    }

    @Test
    fun `le ViewModel corrigé ne transforme pas une annulation en erreur`() = runTest {
        coEvery { repository.produits() } throws CancellationException("Écran quitté")
        val viewModel = CatalogueViewModelCorrige(repository)

        viewModel.charger()

        // L'annulation est relancée : la coroutine s'arrête sans afficher d'erreur
        assertEquals(CatalogueUiState.Chargement, viewModel.etat.value)
    }
}
