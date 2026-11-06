package fr.formation.prefsujets.sujet3

import android.content.Context
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

// Étape 3 — Un repository et un ViewModel
// Nouveau :
// - data class Reglages(nom, sombre) : le reste de l'app ne voit plus ni clés ni Preferences
// - ReglagesRepository : expose Flow<Reglages>, et des fonctions suspend definirNom / definirSombre
//   .catch { IOException → emptyPreferences() } : un fichier illisible donne les valeurs par défaut, pas un crash
// - ReglagesViewModel : stateIn(WhileSubscribed(5_000), null) ; les écritures dans viewModelScope
// - Factory avec APPLICATION_KEY : le ViewModel a besoin du Context pour trouver le DataStore
// L'écran est le même qu'à l'étape 2 : seule l'organisation a changé.
// ✅ Le repository se teste sans écran (DataStore dans un fichier temporaire : voir tp12-hydratation)

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/prefsujets/ (ou …/prefsujets/sujet3/ pour rester dans ce projet) :
//
// ├── MainActivity.kt                MainActivity3, ReglagesApp
// ├── data/
// │   ├── ReglagesDataStore.kt       Context.dataStore
// │   ├── Reglages.kt                Reglages
// │   └── ReglagesRepository.kt      ReglagesRepository (+ ses clés)
// └── ui/
//     ├── theme/
//     │   └── Theme.kt               AppTheme
//     └── reglages/
//         ├── ReglagesViewModel.kt   ReglagesViewModel (+ Factory)
//         └── ReglagesEcran.kt       ReglagesEcran
//
// - dans un vrai projet, le repository est créé une fois dans un AppContainer (voir tp07-todos-mvvm-retrofit)
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/ReglagesDataStore.kt
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reglages-etape3")

// ---------- Data ----------

// 📄 data/Reglages.kt
data class Reglages(val nom: String, val sombre: Boolean)

// 📄 data/ReglagesRepository.kt
class ReglagesRepository(private val dataStore: DataStore<Preferences>) {

    private object Cles {
        val NOM = stringPreferencesKey("nom")
        val SOMBRE = booleanPreferencesKey("sombre")
    }

    val reglages: Flow<Reglages> = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Log.e("PrefSujets", "Lecture impossible, valeurs par défaut", e)
                emit(emptyPreferences())
            } else {
                throw e                                       // une vraie erreur de programmation : on la laisse
            }
        }
        .map { p -> Reglages(nom = p[Cles.NOM] ?: "", sombre = p[Cles.SOMBRE] ?: false) }

    suspend fun definirNom(nom: String) {
        dataStore.edit { it[Cles.NOM] = nom }
    }

    suspend fun definirSombre(sombre: Boolean) {
        dataStore.edit { it[Cles.SOMBRE] = sombre }
    }
}

// ---------- ViewModel ----------

// 📄 ui/reglages/ReglagesViewModel.kt
class ReglagesViewModel(private val repository: ReglagesRepository) : ViewModel() {

    // null = pas encore lu
    val reglages: StateFlow<Reglages?> = repository.reglages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    fun enregistrerNom(nom: String) {
        viewModelScope.launch { repository.definirNom(nom) }
    }

    fun changerSombre(sombre: Boolean) {
        viewModelScope.launch { repository.definirSombre(sombre) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!            // le Context de l'application
                ReglagesViewModel(ReglagesRepository(application.dataStore))
            }
        }
    }
}

// ---------- View ----------

// 📄 MainActivity.kt
class MainActivity3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReglagesApp() }
    }
}

// 📄 MainActivity.kt
@Composable
private fun ReglagesApp(viewModel: ReglagesViewModel = viewModel(factory = ReglagesViewModel.Factory)) {
    val reglages by viewModel.reglages.collectAsStateWithLifecycle()
    val r = reglages ?: return                         // rien à afficher pendant la toute première lecture

    AppTheme(sombre = r.sombre) {
        ReglagesEcran(
            nom = r.nom,
            sombre = r.sombre,
            onEnregistrerNom = viewModel::enregistrerNom,
            onSombreChange = viewModel::changerSombre,
        )
    }
}

// 📄 ui/theme/Theme.kt
@Composable
private fun AppTheme(sombre: Boolean, contenu: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (sombre) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), content = contenu)
    }
}

// 📄 ui/reglages/ReglagesEcran.kt
// Composable SANS état : il affiche ce qu'il reçoit et prévient (onXxx). Il ne sait pas OÙ c'est enregistré.
@Composable
private fun ReglagesEcran(
    nom: String,
    sombre: Boolean,
    onEnregistrerNom: (String) -> Unit,
    onSombreChange: (Boolean) -> Unit,
) {
    // Le texte en cours de saisie reste local ; on n'enregistre qu'au clic.
    // rememberSaveable(nom) : repart de la valeur enregistrée quand celle-ci change (ex. lue au démarrage)
    var brouillon by rememberSaveable(nom) { mutableStateOf(nom) }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (nom.isBlank()) "Bonjour !" else "Bonjour, $nom !",
            style = MaterialTheme.typography.headlineMedium,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = brouillon,
                onValueChange = { brouillon = it },
                label = { Text("Votre prénom") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { onEnregistrerNom(brouillon.trim()) }, enabled = brouillon.trim() != nom) {
                Text("Enregistrer")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Thème sombre", modifier = Modifier.weight(1f))
            Switch(checked = sombre, onCheckedChange = onSombreChange)
        }
    }
}
