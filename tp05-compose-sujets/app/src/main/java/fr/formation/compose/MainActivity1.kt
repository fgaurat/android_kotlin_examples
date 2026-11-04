package fr.formation.compose

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Sujet 1 — Fonctionnement de Jetpack Compose
// - Une interface = des fonctions @Composable qui DÉCRIVENT l'écran à partir de données.
// - On ne modifie jamais une vue : on change les données, Compose rappelle les fonctions
//   concernées (recomposition).
// - Logcat (tag:Compose) : seule Salutation est recomposée quand le nom change,
//   Titre ne l'est pas car ses paramètres n'ont pas changé.

class MainActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {                                   // point d'entrée de Compose
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
    var nom by remember { mutableStateOf("Android") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Titre()
        Salutation(nom = nom)
        Button(onClick = { nom = if (nom == "Android") "Kotlin" else "Android" }) {
            Text("Changer de nom")
        }
    }
}

@Composable
private fun Titre() {
    SideEffect { Log.d("Compose", "composition de Titre") }
    Text("Ma première interface", style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun Salutation(nom: String) {
    SideEffect { Log.d("Compose", "composition de Salutation($nom)") }
    Text("Bonjour $nom !", style = MaterialTheme.typography.displaySmall)
}
