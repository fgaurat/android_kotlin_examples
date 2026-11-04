package fr.formation.compteur.score

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

// Étape 8 — L'état vit dans un ViewModel : il survit à la rotation sans rememberSaveable,
// et le SavedStateHandle le restaure après la mort du processus.
class ScoreViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    private val _etat = MutableStateFlow(
        ScoreUiState(
            scoreA = savedStateHandle[CLE_SCORE_A] ?: 0,
            scoreB = savedStateHandle[CLE_SCORE_B] ?: 0,
        )
    )
    val etat: StateFlow<ScoreUiState> = _etat.asStateFlow()   // lecture seule pour l'UI

    fun incrementer(equipe: Equipe) = mettreAJour { it.ajouter(equipe, +1) }
    fun decrementer(equipe: Equipe) = mettreAJour { it.ajouter(equipe, -1) }
    fun reinitialiser() = mettreAJour { ScoreUiState() }

    private fun mettreAJour(transformation: (ScoreUiState) -> ScoreUiState) {
        val nouveau = _etat.updateAndGet(transformation)      // mise à jour atomique
        savedStateHandle[CLE_SCORE_A] = nouveau.scoreA
        savedStateHandle[CLE_SCORE_B] = nouveau.scoreB
    }

    companion object {
        const val CLE_SCORE_A = "scoreA"
        const val CLE_SCORE_B = "scoreB"
    }
}
