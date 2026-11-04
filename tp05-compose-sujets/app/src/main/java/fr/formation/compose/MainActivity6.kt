package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Sujet 6 — Les listes : LazyColumn et LazyRow
// « Lazy » : seuls les éléments VISIBLES sont composés, comme un RecyclerView.
// - items(liste, key = { it.id }) : la clé permet à Compose de suivre chaque élément
//   (ajout, suppression, déplacement) sans tout recomposer.
// - Ne jamais mettre une LazyColumn dans une Column avec verticalScroll.

private data class Ville(val id: Int, val nom: String, val pays: String)

private val villes = List(50) { i -> Ville(id = i, nom = "Ville n°${i + 1}", pays = if (i % 2 == 0) "France" else "Belgique") }

private val categories = listOf("Toutes", "Mer", "Montagne", "Campagne", "Capitales", "Patrimoine")

class MainActivity6 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Ecran()
                }
            }
        }
    }
}

@Composable
private fun Ecran() {
    Column {
        // Liste horizontale
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { categorie ->
                AssistChip(onClick = {}, label = { Text(categorie) })
            }
        }

        // Liste verticale : 50 éléments, seuls ceux à l'écran existent
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Text("Destinations", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
            }
            items(villes, key = { it.id }) { ville ->
                ListItem(
                    headlineContent = { Text(ville.nom) },
                    supportingContent = { Text(ville.pays) },
                )
                HorizontalDivider()
            }
        }
    }
}
