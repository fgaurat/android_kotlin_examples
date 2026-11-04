package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Sujet 8 — Créer des composants réutilisables
// Conventions d'un bon composable réutilisable :
// 1. les données en paramètres, les événements en lambdas (onXxx) ;
// 2. un paramètre « modifier: Modifier = Modifier », appliqué à l'élément racine ;
// 3. des « slots » (@Composable () -> Unit) pour laisser l'appelant choisir le contenu.

class MainActivity8 : ComponentActivity() {
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
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Même composant, contenus différents grâce aux slots
        CarteSection(
            titre = "Article",
            actions = {
                IconButton(onClick = {}) { Icon(Icons.Filled.Share, contentDescription = "Partager") }
                IconButton(onClick = {}) { Icon(Icons.Filled.Delete, contentDescription = "Supprimer") }
            },
        ) {
            Text("Jetpack Compose simplifie la création d'interfaces.")
        }

        CarteSection(
            titre = "Météo",
            actions = { TextButton(onClick = {}) { Text("Détails") } },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("☀️ 24 °C", style = MaterialTheme.typography.displaySmall)
            Text("Lyon, ensoleillé")
        }
    }
}

/** Une carte avec un titre, des actions à droite et un contenu libre. */
@Composable
private fun CarteSection(
    titre: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},       // slot : les boutons de l'en-tête
    contenu: @Composable ColumnScope.() -> Unit,          // slot : le corps de la carte
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                actions()
            }
            contenu()
        }
    }
}
