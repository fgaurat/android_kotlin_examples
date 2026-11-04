package fr.formation.compteur.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.formation.compteur.R
import fr.formation.compteur.score.Equipe
import fr.formation.compteur.score.Meneur
import fr.formation.compteur.score.ScoreUiState
import fr.formation.compteur.ui.theme.CompteurTheme

/**
 * Tableau de score **sans état**. Il reste identique aux étapes 6, 7 et 8 :
 * seul change le propriétaire de l'état (rememberSaveable, state holder, ViewModel).
 */
@Composable
fun ScoreScreen(
    etat: ScoreUiState,
    onIncrementer: (Equipe) -> Unit,
    onDecrementer: (Equipe) -> Unit,
    onReinitialiser: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Equipe.entries.forEach { equipe ->
                Compteur(
                    libelle = stringResource(equipe.libelle),
                    valeur = etat.score(equipe),
                    onIncrementer = { onIncrementer(equipe) },
                    onDecrementer = { onDecrementer(equipe) },
                )
            }
        }
        Text(stringResource(R.string.total, etat.total), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(etat.meneur.message), style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onReinitialiser, enabled = etat.total > 0) {
            Text(stringResource(R.string.reinitialiser))
        }
    }
}

@get:StringRes
private val Equipe.libelle: Int
    get() = when (this) {
        Equipe.A -> R.string.equipe_a
        Equipe.B -> R.string.equipe_b
    }

@get:StringRes
private val Meneur.message: Int
    get() = when (this) {
        Meneur.A -> R.string.meneur_a
        Meneur.B -> R.string.meneur_b
        Meneur.EGALITE -> R.string.egalite
    }

@PreviewLightDark
@Composable
private fun ScoreScreenPreview() {
    CompteurTheme {
        ScoreScreen(etat = ScoreUiState(scoreA = 3, scoreB = 1), onIncrementer = {}, onDecrementer = {}, onReinitialiser = {})
    }
}
