package fr.formation.mvvm.sujet3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

// Sujet 3 — Flux unidirectionnel (UDF) et état d'écran
//
//        ┌──────────── état (TachesUiState) ────────────┐
//        │                                              ▼
//   TachesViewModel                                TachesEcran
//        ▲                                              │
//        └──────────── événements (charger()) ──────────┘
//
// - L'état de l'écran est UNE seule valeur : Chargement, Succes ou Erreur.
// - L'écran ne fait qu'un « when » : impossible d'afficher à la fois le chargement et l'erreur.
// - viewModelScope : la coroutine est annulée automatiquement quand le ViewModel est détruit.
// Le chargement échoue une fois sur deux : tester le bouton « Réessayer ».

data class Tache(val id: Int, val titre: String)

sealed interface TachesUiState {
    data object Chargement : TachesUiState
    data class Succes(val taches: List<Tache>) : TachesUiState
    data class Erreur(val message: String) : TachesUiState
}

class TachesViewModel : ViewModel() {

    private val _etat = MutableStateFlow<TachesUiState>(TachesUiState.Chargement)
    val etat: StateFlow<TachesUiState> = _etat.asStateFlow()

    init {
        charger()
    }

    fun charger() {
        viewModelScope.launch {
            _etat.value = TachesUiState.Chargement
            delay(1_500)                                          // simule un appel réseau
            _etat.value = if (Random.nextBoolean()) {
                TachesUiState.Succes(listOf(Tache(1, "Acheter du pain"), Tache(2, "Réviser Kotlin")))
            } else {
                TachesUiState.Erreur("Serveur injoignable")
            }
        }
    }
}

class MainActivity3 : ComponentActivity() {
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
private fun TachesEcran(viewModel: TachesViewModel = viewModel()) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val e = etat) {
            TachesUiState.Chargement -> CircularProgressIndicator()

            is TachesUiState.Erreur -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(e.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.charger() }) { Text("Réessayer") }
            }

            is TachesUiState.Succes -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(e.taches, key = { it.id }) { tache ->
                    ListItem(headlineContent = { Text(tache.titre) })
                }
            }
        }
    }
}
