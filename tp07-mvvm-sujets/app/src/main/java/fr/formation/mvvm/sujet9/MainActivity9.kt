package fr.formation.mvvm.sujet9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Sujet 9 — SavedStateHandle et mort du processus
// Le ViewModel survit à la rotation… mais PAS à la mort du processus :
// en arrière-plan, Android peut tuer l'application pour libérer de la mémoire,
// puis la recréer quand l'utilisateur revient. Le SavedStateHandle, lui, est sauvegardé.
//
// Démonstration :
// 1. Incrémenter les deux compteurs
// 2. Touche Accueil (l'application passe en arrière-plan)
// 3. Tuer le processus (simule ce que fait Android) :
//    Android Studio > Logcat > bouton « Terminate Application » (carré rouge),
//    ou : adb shell run-as fr.formation.mvvm kill -9 $(adb shell pidof fr.formation.mvvm)
// 4. Revenir dans l'application par l'écran des applications récentes :
//    seul le compteur du SavedStateHandle est restauré.

class CompteursViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // ❌ Simple StateFlow : perdu si le processus meurt
    private val _compteurSimple = MutableStateFlow(0)
    val compteurSimple: StateFlow<Int> = _compteurSimple.asStateFlow()

    // ✅ StateFlow adossé au SavedStateHandle : restauré après la mort du processus
    val compteurSauvegarde: StateFlow<Int> = savedStateHandle.getStateFlow("compteur", 0)

    fun incrementer() {
        _compteurSimple.value++
        savedStateHandle["compteur"] = compteurSauvegarde.value + 1     // écrire dans le handle met à jour le flow
    }
}

class MainActivity9 : ComponentActivity() {
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

// viewModel() fournit automatiquement le SavedStateHandle : aucune factory à écrire
@Composable
private fun Ecran(viewModel: CompteursViewModel = viewModel()) {
    val simple by viewModel.compteurSimple.collectAsStateWithLifecycle()
    val sauvegarde by viewModel.compteurSauvegarde.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("StateFlow simple : $simple", style = MaterialTheme.typography.headlineSmall)
        Text("SavedStateHandle : $sauvegarde", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = viewModel::incrementer) { Text("+1 aux deux compteurs") }
    }
}
