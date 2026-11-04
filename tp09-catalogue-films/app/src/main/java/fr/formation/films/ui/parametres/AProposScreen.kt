package fr.formation.films.ui.parametres

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.films.ui.composants.FilmsTopBar
import fr.formation.films.ui.theme.CatalogueFilmsTheme

/** Écran secondaire du sous-graphe : l'onglet « Paramètres » reste sélectionné. */
@OptIn(ExperimentalMaterial3Api::class) // FilmsTopBar expose TopAppBarScrollBehavior
@Composable
fun AProposScreen(onRetour: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { FilmsTopBar(titre = "À propos", onRetour = onRetour) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Catalogue de films", style = MaterialTheme.typography.headlineSmall)
            Text("Version 1.0")
            Text("Corrigé du TP du chapitre « Navigation » : Navigation Compose avec routes type-safe.")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AProposScreenPreview() {
    CatalogueFilmsTheme { AProposScreen(onRetour = {}) }
}
