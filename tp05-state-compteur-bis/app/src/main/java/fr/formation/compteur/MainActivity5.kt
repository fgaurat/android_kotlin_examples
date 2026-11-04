package fr.formation.compteur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Étape 5 — State hoisting : on remonte l'état
// - CompteurEcran POSSÈDE l'état.
// - Compteur est SANS ÉTAT : il reçoit la valeur (l'état descend)
//   et signale les clics (les événements remontent).
// ✅ Compteur se prévisualise avec n'importe quelle valeur (voir la Preview).

class MainActivity5 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CompteurEcran()
                }
            }
        }
    }
}

// Avec état
@Composable
private fun CompteurEcran() {
    var compteur by rememberSaveable { mutableIntStateOf(0) }

    Compteur(
        valeur = compteur,
        onIncrementer = { compteur++ },
        onReinitialiser = { compteur = 0 },
    )
}

// Sans état
@Composable
private fun Compteur(
    valeur: Int,
    onIncrementer: () -> Unit,
    onReinitialiser: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Compteur : $valeur", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onIncrementer) {
                Text("+1")
            }
            OutlinedButton(onClick = onReinitialiser) {
                Text("Remettre à 0")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompteurPreview() {
    MaterialTheme {
        Compteur(valeur = 42, onIncrementer = {}, onReinitialiser = {})
    }
}
