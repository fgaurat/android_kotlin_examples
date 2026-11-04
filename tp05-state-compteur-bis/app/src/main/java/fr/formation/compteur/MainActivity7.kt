package fr.formation.compteur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

// Étape 7 — L'état dans un ViewModel, exposé en StateFlow
// - Le ViewModel survit à la rotation : plus besoin de rememberSaveable.
// - _compteur (privé) se modifie, compteur (public) se lit seulement.
// - collectAsStateWithLifecycle() transforme le StateFlow en état Compose.
// Compteur (sans état) est exactement le même qu'à l'étape 5.

class MainActivity7 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CompteurEcran()
                }
            }
        }
    }
}

class CompteurViewModel : ViewModel() {

    private val _compteur = MutableStateFlow(0)
    val compteur: StateFlow<Int> = _compteur.asStateFlow()

    fun incrementer() {
        _compteur.update { it + 1 }
    }

    fun reinitialiser() {
        _compteur.value = 0
    }
}

// Avec état : il vient du ViewModel
@Composable
private fun CompteurEcran(viewModel: CompteurViewModel = viewModel()) {
    val compteur by viewModel.compteur.collectAsStateWithLifecycle()

    Compteur(
        valeur = compteur,
        onIncrementer = { viewModel.incrementer() },
        onReinitialiser = { viewModel.reinitialiser() },
    )
}

// Sans état : identique à l'étape 5
@Composable
private fun Compteur(
    valeur: Int,
    onIncrementer: () -> Unit,
    onReinitialiser: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Compteur : $valeur", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onIncrementer) {
                Text("+1")
            }
            OutlinedButton(onClick = onReinitialiser) {
                Text("Remettre à 0")
            }
        }
    }
}
