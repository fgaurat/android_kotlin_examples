package fr.formation.films.ui.detail

import androidx.lifecycle.SavedStateHandle
import fr.formation.films.data.FilmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests JVM du ViewModel de détail.
 *
 * Le SavedStateHandle est construit à la main : la clé "id" porte le nom de la propriété
 * de la route DetailFilm(id), c'est ce que toRoute<DetailFilm>() décode.
 *
 * Pourquoi Robolectric ? SavedStateHandle.toRoute() passe en interne par un android.os.Bundle.
 * Sur la JVM pure, les classes Android sont des bouchons qui lèvent « Method ... not mocked ».
 * Robolectric fournit une vraie implémentation de Bundle : le ViewModel reste inchangé
 * (il lit bien sa route avec toRoute) et le test construit son SavedStateHandle à la main.
 */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DetailFilmViewModelTest {

    private lateinit var repository: FilmsRepository

    @Before
    fun setUp() {
        // viewModelScope utilise Dispatchers.Main, absent sur la JVM
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FilmsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun creerViewModel(id: Int) =
        DetailFilmViewModel(SavedStateHandle(mapOf("id" to id)), repository)

    @Test
    fun `lit l'identifiant de la route dans le SavedStateHandle`() {
        val viewModel = creerViewModel(3)

        assertEquals(3, viewModel.filmId)
        val etat = viewModel.etat.value
        assertTrue(etat is DetailFilmUiState.Affiche)
        assertEquals("La Haine", (etat as DetailFilmUiState.Affiche).film.titre)
        assertEquals(false, etat.estFavori)
    }

    @Test
    fun `un identifiant inconnu donne l'etat Introuvable`() {
        val viewModel = creerViewModel(999)

        assertEquals(DetailFilmUiState.Introuvable(999), viewModel.etat.value)
    }

    @Test
    fun `basculer le favori met a jour l'etat et le repository`() = runTest {
        val viewModel = creerViewModel(5)
        // Le StateFlow (WhileSubscribed) ne se met à jour que s'il est collecté
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.etat.collect {} }

        viewModel.basculerFavori()
        assertEquals(setOf(5), repository.idsFavoris.value)
        assertEquals(true, (viewModel.etat.value as DetailFilmUiState.Affiche).estFavori)

        viewModel.basculerFavori()
        assertEquals(emptySet<Int>(), repository.idsFavoris.value)
        assertEquals(false, (viewModel.etat.value as DetailFilmUiState.Affiche).estFavori)
    }

    @Test
    fun `un favori ajoute ailleurs est visible dans le detail`() = runTest {
        val viewModel = creerViewModel(1)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.etat.collect {} }

        repository.basculerFavori(1)   // par exemple depuis un autre écran

        assertEquals(true, (viewModel.etat.value as DetailFilmUiState.Affiche).estFavori)
    }

    @Test
    fun `basculer le favori d'un film introuvable ne fait rien`() {
        val viewModel = creerViewModel(42)

        viewModel.basculerFavori()

        assertTrue(repository.idsFavoris.value.isEmpty())
    }
}
