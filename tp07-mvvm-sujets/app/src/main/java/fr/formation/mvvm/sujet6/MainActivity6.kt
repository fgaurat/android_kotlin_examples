package fr.formation.mvvm.sujet6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Sujet 6 — Les opérateurs de Flow : une recherche réactive
// Le résultat n'est jamais calculé « à la main » : on DÉCRIT comment il découle des entrées.
//
//   recherche ──debounce(300)──┐
//                              combine ──flatMapLatest(rechercher)──map(masquer faites)──stateIn──▶ écran
//   masquerFaites ─────────────┘
//
// - debounce      : attend 300 ms sans frappe avant de chercher (pas une requête par lettre)
// - combine       : recalcule dès que l'UNE des entrées change
// - flatMapLatest : une nouvelle recherche ANNULE la précédente, encore en cours
// - map           : transforme chaque valeur
// - stateIn       : transforme le flow en StateFlow pour l'écran
// Logcat (tag:Recherche) : taper vite « kotlin » ne déclenche qu'une seule requête.

data class Tache(val id: Int, val titre: String, val faite: Boolean)

private val toutesLesTaches = listOf(
    Tache(1, "Réviser Kotlin", faite = true),
    Tache(2, "Installer Android Studio", faite = true),
    Tache(3, "Lire la doc Compose", faite = false),
    Tache(4, "Écrire les tests Kotlin", faite = false),
    Tache(5, "Préparer la démo MVVM", faite = false),
)

// Simule une recherche côté serveur
private fun rechercher(texte: String): Flow<List<Tache>> = flow {
    Log.d("Recherche", "requête envoyée pour « $texte »")
    delay(500)
    emit(toutesLesTaches.filter { it.titre.contains(texte, ignoreCase = true) })
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RechercheViewModel : ViewModel() {

    // Les entrées : modifiées par l'écran
    private val _recherche = MutableStateFlow("")
    val recherche: StateFlow<String> = _recherche.asStateFlow()

    private val _masquerFaites = MutableStateFlow(false)
    val masquerFaites: StateFlow<Boolean> = _masquerFaites.asStateFlow()

    // La sortie : entièrement déduite des entrées
    val resultats: StateFlow<List<Tache>> =
        combine(_recherche.debounce(300), _masquerFaites) { texte, masquer -> texte to masquer }
            .flatMapLatest { (texte, masquer) ->
                rechercher(texte).map { liste -> if (masquer) liste.filterNot { it.faite } else liste }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onRechercheChange(texte: String) {
        _recherche.value = texte
    }

    fun onMasquerFaitesChange(masquer: Boolean) {
        _masquerFaites.value = masquer
    }
}

class MainActivity6 : ComponentActivity() {
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
private fun Ecran(viewModel: RechercheViewModel = viewModel()) {
    val recherche by viewModel.recherche.collectAsStateWithLifecycle()
    val masquerFaites by viewModel.masquerFaites.collectAsStateWithLifecycle()
    val resultats by viewModel.resultats.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = recherche,
            onValueChange = viewModel::onRechercheChange,
            label = { Text("Rechercher") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Masquer les tâches faites", modifier = Modifier.weight(1f))
            Switch(checked = masquerFaites, onCheckedChange = viewModel::onMasquerFaitesChange)
        }
        Text("${resultats.size} résultat(s)", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(resultats, key = { it.id }) { tache ->
                ListItem(headlineContent = { Text(tache.titre) }, supportingContent = { Text(if (tache.faite) "Faite" else "À faire") })
            }
        }
    }
}
