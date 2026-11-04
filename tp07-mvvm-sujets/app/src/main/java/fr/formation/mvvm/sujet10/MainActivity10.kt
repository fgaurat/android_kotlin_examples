package fr.formation.mvvm.sujet10

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

// Sujet 10 — Le cycle de vie côté Compose
// 1. collectAsStateWithLifecycle() arrête la collecte quand l'écran n'est plus visible ;
//    collectAsState() continue, même application en arrière-plan.
//    Démonstration (Logcat, tag:Capteur) : choisir un mode, appuyer sur Accueil, attendre 10 s.
//    - avec cycle de vie : les émissions s'arrêtent ~5 s après (WhileSubscribed(5_000))
//    - sans cycle de vie : les émissions continuent → batterie et réseau gaspillés
// 2. LifecycleEventEffect : exécuter du code sur un événement du cycle de vie (ici ON_RESUME / ON_PAUSE).
// (Avec les vues XML, l'équivalent est lifecycleScope.launch { repeatOnLifecycle(STARTED) { … } }.)

class CapteurViewModel : ViewModel() {

    // Simule un capteur qui mesure en continu tant que quelqu'un l'observe
    val mesure: StateFlow<Int> = flow {
        var n = 0
        while (true) {
            Log.d("Capteur", "mesure n°$n")
            emit(n++)
            delay(1_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = 0)
}

class MainActivity10 : ComponentActivity() {
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
private fun Ecran(viewModel: CapteurViewModel = viewModel()) {
    var avecCycleDeVie by rememberSaveable { mutableStateOf(true) }
    val journal = remember { mutableStateListOf<String>() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { journal.add("ON_RESUME : écran visible") }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { journal.add("ON_PAUSE : écran masqué") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("collectAsStateWithLifecycle()", modifier = Modifier.weight(1f))
            Switch(checked = avecCycleDeVie, onCheckedChange = { avecCycleDeVie = it })
        }

        if (avecCycleDeVie) {
            val mesure by viewModel.mesure.collectAsStateWithLifecycle()
            Text("Mesure : $mesure (s'arrête en arrière-plan)", style = MaterialTheme.typography.headlineSmall)
        } else {
            val mesure by viewModel.mesure.collectAsState()
            Text("Mesure : $mesure (ne s'arrête jamais)", style = MaterialTheme.typography.headlineSmall)
        }

        Text("Événements du cycle de vie", style = MaterialTheme.typography.titleMedium)
        journal.forEach { Text(it) }
    }
}
