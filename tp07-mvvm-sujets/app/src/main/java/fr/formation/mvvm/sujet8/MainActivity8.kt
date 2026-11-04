package fr.formation.mvvm.sujet8

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Sujet 8 — Cycle de vie d'une Activity et durée de vie du ViewModel
//
//   onCreate → onStart → onResume   [écran visible et interactif]
//                ↑           ↓
//   onRestart ← onStop ← onPause    [écran masqué : touche Accueil, autre application]
//                 ↓
//             onDestroy             [fin, ou RECRÉATION : rotation, langue, mode sombre]
//
// Le journal à l'écran est stocké dans le ViewModel, qui SURVIT à la recréation :
// tourner l'écran et observer la séquence onPause … onDestroy → onCreate …,
// alors que « ViewModel créé » n'apparaît qu'une fois.
// onCleared() n'est appelé qu'à la vraie fin : bouton retour (voir Logcat, tag:CycleDeVie).

class JournalViewModel : ViewModel() {

    private val _evenements = MutableStateFlow(listOf("ViewModel créé"))
    val evenements: StateFlow<List<String>> = _evenements.asStateFlow()

    fun noter(evenement: String) {
        Log.d("CycleDeVie", evenement)
        _evenements.update { it + evenement }
    }

    override fun onCleared() {
        Log.d("CycleDeVie", "ViewModel détruit (onCleared)")
    }
}

class MainActivity8 : ComponentActivity() {

    private val viewModel: JournalViewModel by viewModels()     // même instance après une rotation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.noter("onCreate (Activity ${Integer.toHexString(hashCode())})")
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Journal(viewModel)
                }
            }
        }
    }

    override fun onStart() { super.onStart(); viewModel.noter("onStart") }
    override fun onResume() { super.onResume(); viewModel.noter("onResume") }
    override fun onPause() { viewModel.noter("onPause"); super.onPause() }
    override fun onStop() { viewModel.noter("onStop"); super.onStop() }
    override fun onRestart() { super.onRestart(); viewModel.noter("onRestart") }
    override fun onDestroy() {
        viewModel.noter(if (isChangingConfigurations) "onDestroy (recréation)" else "onDestroy (fin)")
        super.onDestroy()
    }
}

@Composable
private fun Journal(viewModel: JournalViewModel) {
    val evenements by viewModel.evenements.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Journal du cycle de vie", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(evenements) { evenement -> Text(evenement, modifier = Modifier.padding(vertical = 2.dp)) }
        }
    }
}
