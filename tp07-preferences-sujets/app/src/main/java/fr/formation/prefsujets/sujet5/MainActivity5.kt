package fr.formation.prefsujets.sujet5

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
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
import java.util.Locale

// Étape 5 — Un écran de paramètres complet, appliqué à toute l'application
// Nouveau (sur la base de l'étape 3) :
// - des ÉNUMÉRATIONS enregistrées par leur nom : Theme (Système / Clair / Sombre), Unite (°C / °F)
//   lecture tolérante : un nom inconnu (ancienne version de l'app) → valeur par défaut, pas de crash
// - le thème est appliqué à TOUTE l'app : l'Activity lit le ViewModel et choisit le MaterialTheme
//   (Système → isSystemInDarkTheme())
// - l'unité sert ailleurs dans l'écran : la température affichée suit le réglage
// - « Réinitialiser » : dataStore.edit { it.clear() } → toutes les valeurs par défaut
// - RadioButton accessibles : selectableGroup + selectable(role = Role.RadioButton) sur toute la ligne
// À essayer : choisir « Système », puis basculer le mode sombre du téléphone (Paramètres rapides) ;
//   fermer et rouvrir l'app : tout est conservé ; « Réinitialiser ».
// Remarque : pendant la toute première lecture (quelques ms), rien n'est affiché ; pour éviter tout
//   flash, garder le splash screen à l'écran pendant ce temps (voir tp07-todos-splash, étape 2).

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/prefsujets/ (ou …/prefsujets/sujet5/ pour rester dans ce projet) :
//
// ├── MainActivity.kt                MainActivity5
// ├── data/
// │   ├── ReglagesDataStore.kt       Context.dataStore
// │   ├── Reglages.kt                Theme, Unite, Reglages
// │   └── ReglagesRepository.kt      ReglagesRepository (+ ses clés)
// └── ui/
//     ├── theme/
//     │   └── Theme.kt               AppTheme
//     └── reglages/
//         ├── ReglagesViewModel.kt   ReglagesViewModel (+ Factory)
//         ├── ReglagesEcran.kt       ReglagesEcran (+ @Preview)
//         └── ChoixUnique.kt         ChoixUnique (groupe de RadioButton réutilisable)
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/ReglagesDataStore.kt
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reglages-etape5")

// ---------- Data ----------

// 📄 data/Reglages.kt
enum class Theme(val libelle: String) { SYSTEME("Système"), CLAIR("Clair"), SOMBRE("Sombre") }

// 📄 data/Reglages.kt
enum class Unite(val libelle: String) { CELSIUS("Celsius (°C)"), FAHRENHEIT("Fahrenheit (°F)") }

// 📄 data/Reglages.kt
data class Reglages(
    val nom: String = "",
    val theme: Theme = Theme.SYSTEME,
    val unite: Unite = Unite.CELSIUS,
)

// 📄 data/ReglagesRepository.kt
class ReglagesRepository(private val dataStore: DataStore<Preferences>) {

    private object Cles {
        val NOM = stringPreferencesKey("nom")
        val THEME = stringPreferencesKey("theme")      // on enregistre Theme.name : "SOMBRE"
        val UNITE = stringPreferencesKey("unite")
    }

    private val parDefaut = Reglages()

    val reglages: Flow<Reglages> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            Reglages(
                nom = p[Cles.NOM] ?: parDefaut.nom,
                // entries.find : un nom inconnu (renommé, supprimé…) donne null → valeur par défaut
                theme = Theme.entries.find { it.name == p[Cles.THEME] } ?: parDefaut.theme,
                unite = Unite.entries.find { it.name == p[Cles.UNITE] } ?: parDefaut.unite,
            )
        }

    suspend fun definirNom(nom: String) { dataStore.edit { it[Cles.NOM] = nom } }
    suspend fun definirTheme(theme: Theme) { dataStore.edit { it[Cles.THEME] = theme.name } }
    suspend fun definirUnite(unite: Unite) { dataStore.edit { it[Cles.UNITE] = unite.name } }

    suspend fun reinitialiser() {
        dataStore.edit { it.clear() }                   // le fichier est vidé : retour aux valeurs par défaut
        Log.d("PrefSujets", "Réglages réinitialisés")
    }
}

// ---------- ViewModel ----------

// 📄 ui/reglages/ReglagesViewModel.kt
class ReglagesViewModel(private val repository: ReglagesRepository) : ViewModel() {

    val reglages: StateFlow<Reglages?> = repository.reglages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    fun enregistrerNom(nom: String) { viewModelScope.launch { repository.definirNom(nom) } }
    fun choisirTheme(theme: Theme) { viewModelScope.launch { repository.definirTheme(theme) } }
    fun choisirUnite(unite: Unite) { viewModelScope.launch { repository.definirUnite(unite) } }
    fun reinitialiser() { viewModelScope.launch { repository.reinitialiser() } }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { ReglagesViewModel(ReglagesRepository(this[APPLICATION_KEY]!!.dataStore)) }
        }
    }
}

// ---------- View ----------

// 📄 MainActivity.kt
class MainActivity5 : ComponentActivity() {

    // Obtenu dans l'Activity : c'est elle qui applique le thème à toute l'application
    private val viewModel: ReglagesViewModel by viewModels { ReglagesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val reglages by viewModel.reglages.collectAsStateWithLifecycle()
            val r = reglages ?: return@setContent      // pas encore lu : rien (voir la remarque en tête)

            AppTheme(theme = r.theme) {
                ReglagesEcran(
                    reglages = r,
                    onEnregistrerNom = viewModel::enregistrerNom,
                    onThemeChoisi = viewModel::choisirTheme,
                    onUniteChoisie = viewModel::choisirUnite,
                    onReinitialiser = viewModel::reinitialiser,
                )
            }
        }
    }
}

// 📄 ui/theme/Theme.kt
@Composable
private fun AppTheme(theme: Theme, contenu: @Composable () -> Unit) {
    val sombre = when (theme) {
        Theme.SYSTEME -> isSystemInDarkTheme()          // suit le réglage du téléphone, en direct
        Theme.CLAIR -> false
        Theme.SOMBRE -> true
    }
    MaterialTheme(colorScheme = if (sombre) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), content = contenu)
    }
}

// 📄 ui/reglages/ReglagesEcran.kt
@Composable
private fun ReglagesEcran(
    reglages: Reglages,
    onEnregistrerNom: (String) -> Unit,
    onThemeChoisi: (Theme) -> Unit,
    onUniteChoisie: (Unite) -> Unit,
    onReinitialiser: () -> Unit,
) {
    var brouillon by rememberSaveable(reglages.nom) { mutableStateOf(reglages.nom) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (reglages.nom.isBlank()) "Bonjour !" else "Bonjour, ${reglages.nom} !",
            style = MaterialTheme.typography.headlineMedium,
        )
        // L'unité choisie sert ailleurs dans l'app : ici, la température du jour
        Text("Il fait ${temperature(21.0, reglages.unite)} aujourd'hui.")

        HorizontalDivider()
        Text("Prénom", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = brouillon,
                onValueChange = { brouillon = it },
                label = { Text("Votre prénom") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { onEnregistrerNom(brouillon.trim()) }, enabled = brouillon.trim() != reglages.nom) {
                Text("Enregistrer")
            }
        }

        HorizontalDivider()
        ChoixUnique("Thème", Theme.entries, reglages.theme, { it.libelle }, onThemeChoisi)

        HorizontalDivider()
        ChoixUnique("Unité de température", Unite.entries, reglages.unite, { it.libelle }, onUniteChoisie)

        HorizontalDivider()
        OutlinedButton(onClick = onReinitialiser) { Text("Réinitialiser les réglages") }
    }
}

// 📄 ui/reglages/ChoixUnique.kt
// Un groupe de boutons radio : toute la ligne est cliquable, et TalkBack annonce « bouton radio, 1 sur 3 »
@Composable
private fun <T> ChoixUnique(
    titre: String,
    options: List<T>,
    selection: T,
    libelle: (T) -> String,
    onChoix: (T) -> Unit,
) {
    Text(titre, style = MaterialTheme.typography.titleMedium)
    Column(modifier = Modifier.selectableGroup()) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = option == selection, onClick = { onChoix(option) }, role = Role.RadioButton)
                    .padding(vertical = 8.dp),
            ) {
                RadioButton(selected = option == selection, onClick = null)    // null : c'est la ligne qui gère le clic
                Text(libelle(option), modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

// 📄 ui/reglages/ReglagesEcran.kt (même fichier que ReglagesEcran)
private fun temperature(celsius: Double, unite: Unite): String = when (unite) {
    Unite.CELSIUS -> String.format(Locale.FRANCE, "%.0f °C", celsius)
    Unite.FAHRENHEIT -> String.format(Locale.FRANCE, "%.0f °F", celsius * 9 / 5 + 32)
}

// 📄 ui/reglages/ReglagesEcran.kt (même fichier que ReglagesEcran)
@Preview(showBackground = true)
@Composable
private fun ReglagesEcranPreview() {
    AppTheme(theme = Theme.CLAIR) {
        ReglagesEcran(
            reglages = Reglages(nom = "Alice", unite = Unite.FAHRENHEIT),
            onEnregistrerNom = {}, onThemeChoisi = {}, onUniteChoisie = {}, onReinitialiser = {},
        )
    }
}
