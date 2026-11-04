package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Sujet 5 — Les principaux composants : texte, boutons, icônes, images, carte
// Les listes ont leur propre sujet (MainActivity6).

class MainActivity5 : ComponentActivity() {
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
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Text : styles du thème, graisse, nombre de lignes
        Text("Titre", style = MaterialTheme.typography.headlineMedium)
        Text("Texte en gras", fontWeight = FontWeight.Bold)
        Text(
            "Un texte beaucoup trop long pour tenir sur une seule ligne est coupé proprement",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        HorizontalDivider()

        // Boutons : du plus important au plus discret
        Button(onClick = {}) { Text("Button") }
        FilledTonalButton(onClick = {}) { Text("FilledTonalButton") }
        ElevatedButton(onClick = {}) { Text("ElevatedButton") }
        OutlinedButton(onClick = {}) { Text("OutlinedButton") }
        TextButton(onClick = {}) { Text("TextButton") }
        Button(onClick = {}, enabled = false) { Text("Désactivé") }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Icône cliquable : toujours une contentDescription pour l'accessibilité
            IconButton(onClick = {}) {
                Icon(Icons.Filled.Favorite, contentDescription = "Ajouter aux favoris")
            }
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter")
            }
        }

        HorizontalDivider()

        // Image depuis les ressources (res/drawable) ; décorative → contentDescription = null
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )

        // Card : un conteneur Material avec fond et coins arrondis
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Une carte", style = MaterialTheme.typography.titleMedium)
                Text("Elle regroupe des informations liées.")
            }
        }
    }
}
