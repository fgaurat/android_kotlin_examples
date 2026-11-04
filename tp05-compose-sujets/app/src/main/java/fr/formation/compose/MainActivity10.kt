package fr.formation.compose

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Sujet 10 — Gestion des états
// Les bases (var, remember, rememberSaveable, state hoisting) sont détaillées pas à pas
// dans le projet tp05-state-compteur-bis. Ici, on les applique à une liste de courses :
// - mutableStateOf       : une valeur observée (le texte saisi)
// - mutableStateListOf   : une LISTE observée (ajout / suppression → recomposition)
// - valeur dérivée       : un simple calcul à partir de l'état (le nombre d'articles)
// - state hoisting       : ChampAjout est sans état, ListeCourses possède l'état

class MainActivity10 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    ListeCourses()
                }
            }
        }
    }
}

// Avec état
@Composable
private fun ListeCourses() {
    var saisie by rememberSaveable { mutableStateOf("") }
    val articles = remember { mutableStateListOf("Pain", "Lait") }   // ⚠️ remember : perdu à la rotation

    val nombre = articles.size                                       // recalculé à chaque recomposition

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChampAjout(
            saisie = saisie,
            onSaisieChange = { saisie = it },
            onAjouter = {
                articles.add(saisie.trim())
                saisie = ""
            },
        )

        Text("$nombre article(s)", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(articles) { article ->
                ListItem(
                    headlineContent = { Text(article) },
                    trailingContent = {
                        IconButton(onClick = { articles.remove(article) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer $article")
                        }
                    },
                )
            }
        }
    }
}

// Sans état : reçoit la saisie, remonte les événements
@Composable
private fun ChampAjout(saisie: String, onSaisieChange: (String) -> Unit, onAjouter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = saisie,
            onValueChange = onSaisieChange,
            label = { Text("Article") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Button(onClick = onAjouter, enabled = saisie.isNotBlank()) {
            Text("Ajouter")
        }
    }
}
