package fr.formation.mvvm.sujet7

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewmodel.compose.viewModel

// Sujet 7 — LiveData
// L'ancêtre de StateFlow, très présent dans le code existant (vues XML, Fragments).
// - MutableLiveData privée, LiveData publique : même principe que _etat / etat
// - Observation « lifecycle-aware » : l'observateur ne reçoit les valeurs que si l'écran est visible
// - Côté Compose : observeAsState() ; côté Activity/Fragment : observe(lifecycleOwner) { … }
// Aujourd'hui, Google recommande StateFlow (Kotlin pur, opérateurs de Flow, testable sans Android).
// Passerelles : liveData.asFlow() et flow.asLiveData().

data class Tache(val id: Int, val titre: String, val faite: Boolean = false)

class TachesLiveDataViewModel : ViewModel() {

    private val _taches = MutableLiveData(listOf(Tache(1, "Acheter du pain"), Tache(2, "Réviser Kotlin"), Tache(3, "Ranger le bureau")))
    val taches: LiveData<List<Tache>> = _taches

    // Une LiveData dérivée, recalculée à chaque changement de _taches
    val nombreRestantes: LiveData<Int> = _taches.map { liste -> liste.count { !it.faite } }

    fun basculer(id: Int, faite: Boolean) {
        _taches.value = _taches.value.orEmpty().map { if (it.id == id) it.copy(faite = faite) else it }
    }
}

class MainActivity7 : ComponentActivity() {

    private val viewModel: TachesLiveDataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observation classique (comme avec les vues XML) : arrêtée automatiquement quand l'Activity s'arrête
        viewModel.nombreRestantes.observe(this) { nombre ->
            Log.d("LiveData", "tâches restantes : $nombre")
        }

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
private fun TachesEcran(viewModel: TachesLiveDataViewModel = viewModel()) {
    val taches by viewModel.taches.observeAsState(initial = emptyList())
    val restantes by viewModel.nombreRestantes.observeAsState(initial = 0)

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$restantes tâche(s) restante(s)", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = { Checkbox(checked = tache.faite, onCheckedChange = { viewModel.basculer(tache.id, it) }) },
                )
            }
        }
    }
}
