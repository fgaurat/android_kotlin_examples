package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Sujet 9 — Gestion des événements
// - Un composant SIGNALE un événement par une lambda : onClick, onValueChange…
// - Le composant enfant ne décide rien : il remonte l'événement à son parent,
//   qui sait quoi en faire (ici : écrire dans le journal).

class MainActivity9 : ComponentActivity() {
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
    var journal by remember { mutableStateOf("Aucun événement") }
    var texte by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 1. Clic simple
        Button(onClick = { journal = "Clic sur le bouton" }) {
            Text("Bouton")
        }

        // 2. Saisie : un événement à chaque caractère
        OutlinedTextField(
            value = texte,
            onValueChange = {
                texte = it
                journal = "Saisie : « $it »"
            },
            label = { Text("Tapez quelque chose") },
        )

        // 3. Clic, double clic, appui long sur n'importe quel élément
        ZoneTactile(
            onClic = { journal = "Clic sur la zone" },
            onDoubleClic = { journal = "Double clic sur la zone" },
            onAppuiLong = { journal = "Appui long sur la zone" },
        )

        Text("Dernier événement : $journal", style = MaterialTheme.typography.titleMedium)
    }
}

// Le composant enfant expose ses événements, sans savoir ce que le parent en fera
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZoneTactile(onClic: () -> Unit, onDoubleClic: () -> Unit, onAppuiLong: () -> Unit) {
    Text(
        "Clic, double clic ou appui long",
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .combinedClickable(onClick = onClic, onDoubleClick = onDoubleClic, onLongClick = onAppuiLong)
            .padding(24.dp),
    )
}
