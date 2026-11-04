package fr.formation.films.ui.parametres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.formation.films.ui.composants.FilmsTopBar
import fr.formation.films.ui.theme.CatalogueFilmsTheme

/** Point d'entrée du sous-graphe ParametresGraphe. */
@OptIn(ExperimentalMaterial3Api::class) // FilmsTopBar expose TopAppBarScrollBehavior
@Composable
fun ParametresScreen(
    nombreFavoris: Int,
    onViderFavoris: () -> Unit,
    onAProposClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { FilmsTopBar(titre = "Paramètres") },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Favoris") },
                supportingContent = { Text("$nombreFavoris film(s) dans vos favoris") },
                leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null) },
                trailingContent = {
                    TextButton(onClick = onViderFavoris, enabled = nombreFavoris > 0) {
                        Text("Vider")
                    }
                },
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("À propos") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onAProposClick),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ParametresScreenPreview() {
    CatalogueFilmsTheme {
        ParametresScreen(nombreFavoris = 2, onViderFavoris = {}, onAProposClick = {})
    }
}
