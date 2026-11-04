package fr.formation.compteur.score

import org.junit.Assert.assertEquals
import org.junit.Test

class ScoreUiStateTest {

    @Test
    fun `ajouter modifie uniquement le score de l'équipe`() {
        val etat = ScoreUiState().ajouter(Equipe.A, +1).ajouter(Equipe.A, +1).ajouter(Equipe.B, +1)

        assertEquals(ScoreUiState(scoreA = 2, scoreB = 1), etat)
        assertEquals(3, etat.total)
    }

    @Test
    fun `le score ne descend pas sous 0`() {
        assertEquals(0, ScoreUiState().ajouter(Equipe.B, -1).scoreB)
    }

    @Test
    fun `le score ne dépasse pas SCORE_MAX`() {
        assertEquals(SCORE_MAX, ScoreUiState(scoreA = SCORE_MAX).ajouter(Equipe.A, +1).scoreA)
    }

    @Test
    fun `meneur selon les scores`() {
        assertEquals(Meneur.A, ScoreUiState(scoreA = 2, scoreB = 1).meneur)
        assertEquals(Meneur.B, ScoreUiState(scoreA = 0, scoreB = 1).meneur)
        assertEquals(Meneur.EGALITE, ScoreUiState(scoreA = 4, scoreB = 4).meneur)
    }
}
