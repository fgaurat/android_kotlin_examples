package fr.formation.mvvm.sujet2

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Sujet 2 — MVVM : Model · View · ViewModel
// - Model     : Tache, les données
// - ViewModel : TachesViewModel, l'état de l'écran et les règles
// - View      : TachesEcran, qui AFFICHE l'état et TRANSMET les actions
// ✅ Tourner l'écran : les tâches sont conservées, le ViewModel survit à la recréation de l'Activity.
// ✅ ajouter() et basculer() se testent sans interface (un simple test JUnit).

// Model
data class Tache(val id: Int, val titre: String, val faite: Boolean = false)

// ViewModel
class TachesViewModel : ViewModel() {

    private val _taches = MutableStateFlow(listOf(Tache(1, "Acheter du pain"), Tache(2, "Réviser Kotlin")))
    val taches: StateFlow<List<Tache>> = _taches.asStateFlow()     // l'UI lit, mais ne modifie pas

    fun ajouter(titre: String) {
        if (titre.isBlank()) return
        _taches.update { liste -> liste + Tache(id = liste.size + 1, titre = titre.trim()) }
    }

    fun basculer(id: Int, faite: Boolean) {
        _taches.update { liste -> liste.map { if (it.id == id) it.copy(faite = faite) else it } }
    }
}

class MainActivity2 : ComponentActivity() {
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

// View
@Composable
private fun TachesEcran(viewModel: TachesViewModel = viewModel()) {
    val taches by viewModel.taches.collectAsStateWithLifecycle()
    var saisie by rememberSaveable { mutableStateOf("") }   // simple état d'interface : il reste ici

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = saisie, onValueChange = { saisie = it }, label = { Text("Nouvelle tâche") }, modifier = Modifier.weight(1f))
            Button(onClick = {
                viewModel.ajouter(saisie)
                saisie = ""
            }) { Text("Ajouter") }
        }
        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = {
                        Checkbox(checked = tache.faite, onCheckedChange = { viewModel.basculer(tache.id, it) })
                    },
                )
            }
        }
    }
}
