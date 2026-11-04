package fr.formation.mvvm.sujet4

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Sujet 4 — Isoler les couches
// Ce sujet est réparti en 4 fichiers, un par rôle :
//
//   MainActivity4.kt     VIEW        affiche l'état, transmet les actions
//        │ dépend de
//   TachesViewModel.kt   VIEWMODEL   état de l'écran, règles de présentation
//        │ dépend de
//   TacheRepository.kt   DATA        origine des données (mémoire, API, base…)
//        │ utilise
//   Tache.kt             MODEL       les données, sans dépendance Android
//
// Règle : chaque couche ne connaît que celle du dessous, jamais celle du dessus.
// Ici, l'écran est en plus découpé en route (avec ViewModel) et écran sans état.

class MainActivity4 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    TachesRoute()
                }
            }
        }
    }
}

// Relie le ViewModel à l'écran
@Composable
private fun TachesRoute(viewModel: TachesViewModel = viewModel(factory = TachesViewModel.Factory)) {
    val taches by viewModel.taches.collectAsStateWithLifecycle()
    TachesEcran(taches = taches, onAjouter = viewModel::ajouter, onBasculer = viewModel::basculer)
}

// Sans état et sans ViewModel : prévisualisable et testable
@Composable
private fun TachesEcran(taches: List<Tache>, onAjouter: (String) -> Unit, onBasculer: (Int) -> Unit) {
    var saisie by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = saisie, onValueChange = { saisie = it }, label = { Text("Nouvelle tâche") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                onAjouter(saisie)
                saisie = ""
            }) { Text("Ajouter") }
        }
        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = { Checkbox(checked = tache.faite, onCheckedChange = { onBasculer(tache.id) }) },
                )
            }
        }
    }
}
