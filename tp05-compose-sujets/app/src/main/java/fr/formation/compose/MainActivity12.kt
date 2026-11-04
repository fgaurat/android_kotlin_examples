package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Sujet 12 — Mise en forme adaptable
// Ne jamais fixer de largeurs en dur : on décrit comment le contenu S'ADAPTE à la place disponible.
// Tester en tournant l'écran, ou dans la Preview avec différentes largeurs.

class MainActivity12 : ComponentActivity() {
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

private val motsCles = listOf("Kotlin", "Compose", "Material 3", "Android", "Coroutines", "Flow", "MVVM", "Tests")

@Composable
private fun Ecran() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. FlowRow : les éléments passent à la ligne quand la place manque
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            motsCles.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
        }

        // 2. weight : deux boutons qui se partagent la largeur, quelle qu'elle soit
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Annuler") }
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Valider") }
        }

        // 3. widthIn(max) : pleine largeur sur téléphone, 500 dp maximum sur tablette
        Card(modifier = Modifier.fillMaxWidth().widthIn(max = 500.dp)) {
            Text(
                "Une carte qui ne s'étire pas indéfiniment sur grand écran.",
                modifier = Modifier.padding(16.dp),
            )
        }

        // 4. Texte long : on limite les lignes plutôt que de casser la mise en page
        Text(
            "Un très long titre qui pourrait déborder sur un petit écran ou avec une grande police",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // 5. BoxWithConstraints : changer de disposition selon la place disponible
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 400.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Blocs() }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Blocs() }
            }
        }
    }
}

@Composable
private fun Blocs() {
    Card { Text("Bloc 1 : en colonne si étroit", modifier = Modifier.padding(16.dp)) }
    Card { Text("Bloc 2 : en ligne si large", modifier = Modifier.padding(16.dp)) }
}
