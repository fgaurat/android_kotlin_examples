package fr.formation.compteur.score

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// Étape 7 — Un « state holder » : l'état et sa logique quittent le composable.
@Stable
class ScoreState(initial: ScoreUiState = ScoreUiState()) {

    var etat by mutableStateOf(initial)
        private set                                   // modifiable uniquement par les fonctions

    fun incrementer(equipe: Equipe) { etat = etat.ajouter(equipe, +1) }
    fun decrementer(equipe: Equipe) { etat = etat.ajouter(equipe, -1) }
    fun reinitialiser() { etat = ScoreUiState() }

    companion object {
        // Un Bundle n'accepte que des types simples : on sauvegarde les deux scores.
        val Saver: Saver<ScoreState, *> = listSaver(
            save = { listOf(it.etat.scoreA, it.etat.scoreB) },
            restore = { ScoreState(ScoreUiState(scoreA = it[0], scoreB = it[1])) },
        )
    }
}

@Composable
fun rememberScoreState(): ScoreState = rememberSaveable(saver = ScoreState.Saver) { ScoreState() }
