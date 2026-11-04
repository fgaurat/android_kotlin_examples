package fr.formation.mvvm.sujet5

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

// Sujet 5 — Programmation réactive : Flow froid et flow chaud
// Un Flow est une suite de valeurs dans le temps, qu'on COLLECTE.
// - Flow FROID (flow { }) : rien ne se passe tant qu'on ne collecte pas,
//   et CHAQUE collecteur relance sa propre exécution depuis le début.
// - Flow CHAUD (StateFlow) : une seule source, une valeur courante PARTAGÉE par tous les collecteurs.
// Démonstration : lancer le 2e collecteur quelques secondes après le 1er, et comparer.
// Logcat (tag:Flow) : combien de fois le code des flows s'exécute-t-il ?

// Flow froid : un compte à rebours, rejoué pour chaque collecteur
fun compteARebours(): Flow<Int> = flow {
    for (i in 10 downTo 0) {
        Log.d("Flow", "compte à rebours : émission de $i")
        emit(i)
        delay(1_000)
    }
}

class HorlogeViewModel : ViewModel() {

    // Flow chaud : une seule horloge, partagée par tous les écrans qui l'observent
    val horloge: StateFlow<Int> = flow {
        var secondes = 0
        while (true) {
            Log.d("Flow", "horloge : émission de $secondes")
            emit(secondes++)
            delay(1_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = 0)
}

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
private fun Ecran(viewModel: HorlogeViewModel = viewModel()) {
    var secondCollecteur by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Button(onClick = { secondCollecteur = true }, enabled = !secondCollecteur) {
            Text("Lancer le 2e collecteur")
        }

        Text("Flow froid : compte à rebours", style = MaterialTheme.typography.titleLarge)
        CompteARebours("Collecteur 1")
        if (secondCollecteur) CompteARebours("Collecteur 2")       // repart de 10 !

        HorizontalDivider()

        Text("Flow chaud : StateFlow", style = MaterialTheme.typography.titleLarge)
        Horloge("Collecteur 1", viewModel.horloge)
        if (secondCollecteur) Horloge("Collecteur 2", viewModel.horloge)   // même valeur que le 1er
    }
}

@Composable
private fun CompteARebours(nom: String) {
    val flowFroid = remember { compteARebours() }
    val valeur by flowFroid.collectAsState(initial = 10)            // la collecte démarre ici
    Text("$nom : $valeur", style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun Horloge(nom: String, horloge: StateFlow<Int>) {
    val valeur by horloge.collectAsStateWithLifecycle()
    Text("$nom : $valeur s", style = MaterialTheme.typography.headlineSmall)
}
