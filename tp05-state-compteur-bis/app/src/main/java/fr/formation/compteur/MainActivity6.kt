package fr.formation.compteur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp

// Étape 6 — Pourquoi remonter l'état ? Deux compteurs et un total
// Le total a besoin des DEUX valeurs : l'état ne peut pas rester caché
// dans chaque Compteur. Il monte dans ScoreEcran, le parent commun.
// Compteur, lui, n'a pas changé : on le réutilise deux fois.

class MainActivity6 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ScoreEcran()
            }
        }
    }
}

// Avec état : les deux scores
@Composable
private fun ScoreEcran() {
    var scoreA by rememberSaveable { mutableIntStateOf(0) }
    var scoreB by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Compteur(libelle = "Équipe A", valeur = scoreA, onIncrementer = { scoreA++ })
        Compteur(libelle = "Équipe B", valeur = scoreB, onIncrementer = { scoreB++ })

        Text("Total : ${scoreA + scoreB}", style = MaterialTheme.typography.headlineMedium)

        OutlinedButton(onClick = {
            scoreA = 0
            scoreB = 0
        }) {
            Text("Tout remettre à 0")
        }
    }
}

// Sans état
@Composable
private fun Compteur(
    libelle: String,
    valeur: Int,
    onIncrementer: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("$libelle : $valeur", style = MaterialTheme.typography.titleLarge)
        Button(onClick = onIncrementer) {
            Text("+1")
        }
    }
}
