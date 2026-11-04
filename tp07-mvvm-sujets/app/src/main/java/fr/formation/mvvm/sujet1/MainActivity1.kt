package fr.formation.mvvm.sujet1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Sujet 1 — Pourquoi une architecture ? Le point de départ, SANS MVVM
// Tout est dans le composable : les données, les règles (ajout, bascule) et l'affichage.
// ❌ Ajouter des tâches puis tourner l'écran : tout est perdu.
// ❌ Les règles (titre non vide, numérotation) ne se testent qu'en lançant l'interface.
// ❌ Où mettre un appel réseau ? Une base de données ? Ici, nulle part de propre.

data class Tache(val id: Int, val titre: String, val faite: Boolean = false)

class MainActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    TachesEcran()
                }
            }
        }
    }
}

@Composable
private fun TachesEcran() {
    var taches by remember { mutableStateOf(listOf(Tache(1, "Acheter du pain"), Tache(2, "Réviser Kotlin"))) }
    var saisie by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = saisie, onValueChange = { saisie = it }, label = { Text("Nouvelle tâche") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                // Règle métier… au milieu de l'interface
                if (saisie.isNotBlank()) {
                    taches = taches + Tache(id = taches.size + 1, titre = saisie.trim())
                    saisie = ""
                }
            }) { Text("Ajouter") }
        }
        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = {
                        Checkbox(
                            checked = tache.faite,
                            onCheckedChange = { faite ->
                                taches = taches.map { if (it.id == tache.id) it.copy(faite = faite) else it }
                            },
                        )
                    },
                )
            }
        }
    }
}
