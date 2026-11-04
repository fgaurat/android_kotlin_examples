package fr.formation.compteur.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.formation.compteur.R
import fr.formation.compteur.score.Equipe
import fr.formation.compteur.score.SCORE_MAX
import fr.formation.compteur.score.ScoreState
import fr.formation.compteur.score.ScoreUiState
import fr.formation.compteur.score.ScoreViewModel
import fr.formation.compteur.score.rememberScoreState

// Les « routes » possèdent l'état et le passent aux composables sans état.

// Étape 5 — State hoisting : l'état est remonté hors de Compteur.
@Composable
fun CompteurRoute(modifier: Modifier = Modifier) {
    var valeur by rememberSaveable { mutableIntStateOf(0) }
    Compteur(
        libelle = stringResource(R.string.compteur),
        valeur = valeur,
        onIncrementer = { valeur = (valeur + 1).coerceAtMost(SCORE_MAX) },
        onDecrementer = { valeur = (valeur - 1).coerceAtLeast(0) },
        modifier = modifier,
    )
}

// Étape 6 — État partagé : le total a besoin des deux scores,
// l'état monte donc au plus petit ancêtre commun des deux compteurs.
@Composable
fun ScoreRoute(modifier: Modifier = Modifier) {
    var scoreA by rememberSaveable { mutableIntStateOf(0) }
    var scoreB by rememberSaveable { mutableIntStateOf(0) }
    ScoreScreen(
        etat = ScoreUiState(scoreA = scoreA, scoreB = scoreB),
        onIncrementer = { equipe ->
            when (equipe) {
                Equipe.A -> scoreA = (scoreA + 1).coerceAtMost(SCORE_MAX)
                Equipe.B -> scoreB = (scoreB + 1).coerceAtMost(SCORE_MAX)
            }
        },
        onDecrementer = { equipe ->
            when (equipe) {
                Equipe.A -> scoreA = (scoreA - 1).coerceAtLeast(0)
                Equipe.B -> scoreB = (scoreB - 1).coerceAtLeast(0)
            }
        },
        onReinitialiser = { scoreA = 0; scoreB = 0 },
        modifier = modifier,
    )
}

// Étape 7 — State holder : la route ne fait plus que relier l'état à l'écran.
@Composable
fun ScoreStateRoute(modifier: Modifier = Modifier, state: ScoreState = rememberScoreState()) {
    ScoreScreen(
        etat = state.etat,
        onIncrementer = state::incrementer,
        onDecrementer = state::decrementer,
        onReinitialiser = state::reinitialiser,
        modifier = modifier,
    )
}

// Étape 8 — ViewModel : l'état est exposé en StateFlow, collecté selon le cycle de vie.
@Composable
fun ScoreViewModelRoute(modifier: Modifier = Modifier, viewModel: ScoreViewModel = viewModel()) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    ScoreScreen(
        etat = etat,
        onIncrementer = viewModel::incrementer,
        onDecrementer = viewModel::decrementer,
        onReinitialiser = viewModel::reinitialiser,
        modifier = modifier,
    )
}
