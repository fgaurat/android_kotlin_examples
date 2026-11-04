package fr.formation.compteur.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.compteur.R
import fr.formation.compteur.score.SCORE_MAX

/**
 * Compteur **sans état** : il affiche [valeur] et remonte les clics.
 * C'est l'appelant qui possède l'état et décide de la nouvelle valeur.
 */
@Composable
fun Compteur(
    libelle: String,
    valeur: Int,
    onIncrementer: () -> Unit,
    onDecrementer: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = SCORE_MAX,
) {
    val descriptionPlus = stringResource(R.string.ajouter_point, libelle)
    val descriptionMoins = stringResource(R.string.retirer_point, libelle)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(libelle, style = MaterialTheme.typography.titleMedium)
        Text(
            text = valeur.toString(),
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .testTag("valeur_$libelle")
                .semantics { liveRegion = LiveRegionMode.Polite },   // TalkBack annonce le changement
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
                onClick = onDecrementer,
                enabled = valeur > min,
                modifier = Modifier.semantics { contentDescription = descriptionMoins },
            ) { Text("−") }
            FilledTonalButton(
                onClick = onIncrementer,
                enabled = valeur < max,
                modifier = Modifier.semantics { contentDescription = descriptionPlus },
            ) { Text("+") }
        }
    }
}

// Sans état, le composable se prévisualise avec n'importe quelle valeur, y compris les bornes.
@Preview(showBackground = true)
@Composable
private fun CompteurPreview() {
    MaterialTheme { Compteur(libelle = "Équipe A", valeur = 42, onIncrementer = {}, onDecrementer = {}) }
}

@Preview(showBackground = true)
@Composable
private fun CompteurMinimumPreview() {
    MaterialTheme { Compteur(libelle = "Équipe B", valeur = 0, onIncrementer = {}, onDecrementer = {}) }
}
