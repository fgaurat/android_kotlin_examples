package fr.formation.compteur.score

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

// Le ViewModel ne lance aucune coroutine : un simple test JVM suffit, sans dispatcher de test.
class ScoreViewModelTest {

    @Test
    fun `incrementer et decrementer mettent à jour le StateFlow`() {
        val viewModel = ScoreViewModel(SavedStateHandle())

        viewModel.incrementer(Equipe.A)
        viewModel.incrementer(Equipe.A)
        viewModel.decrementer(Equipe.A)
        viewModel.incrementer(Equipe.B)

        assertEquals(ScoreUiState(scoreA = 1, scoreB = 1), viewModel.etat.value)
    }

    @Test
    fun `reinitialiser remet les scores à 0`() {
        val viewModel = ScoreViewModel(SavedStateHandle())
        viewModel.incrementer(Equipe.B)

        viewModel.reinitialiser()

        assertEquals(ScoreUiState(), viewModel.etat.value)
    }

    @Test
    fun `les scores sont écrits dans le SavedStateHandle`() {
        val handle = SavedStateHandle()
        val viewModel = ScoreViewModel(handle)

        viewModel.incrementer(Equipe.A)

        assertEquals(1, handle.get<Int>(ScoreViewModel.CLE_SCORE_A))
        assertEquals(0, handle.get<Int>(ScoreViewModel.CLE_SCORE_B))
    }

    @Test
    fun `les scores sont restaurés après la mort du processus`() {
        val handle = SavedStateHandle(mapOf(ScoreViewModel.CLE_SCORE_A to 5, ScoreViewModel.CLE_SCORE_B to 2))

        val viewModel = ScoreViewModel(handle)

        assertEquals(ScoreUiState(scoreA = 5, scoreB = 2), viewModel.etat.value)
    }
}
