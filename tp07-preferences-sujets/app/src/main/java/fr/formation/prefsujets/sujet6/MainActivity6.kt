package fr.formation.prefsujets.sujet6

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Étape 6 — DataStore, l'exemple minimal : un compteur qui survit à la fermeture
// Indépendante des autres étapes : DataStore UNIQUEMENT, pas de SharedPreferences, pas de ViewModel.
// Les 4 gestes à retenir :
// 1. DÉCLARER  : val Context.dataStore by preferencesDataStore(name = "compteur")   (au niveau du fichier)
// 2. UNE CLÉ   : val CLE_COMPTEUR = intPreferencesKey("compteur")                  (typée : Int)
// 3. LIRE      : dataStore.data.map { it[CLE_COMPTEUR] ?: 0 }    → un Flow<Int>, 0 si la clé est absente
// 4. ÉCRIRE    : dataStore.edit { it[CLE_COMPTEUR] = … }          → fonction suspend, dans une coroutine
//                dataStore.edit { it.remove(CLE_COMPTEUR) }       → supprimer la clé
// ✅ Toucher « +1 » plusieurs fois, fermer l'app (balayer dans les récents), la rouvrir : la valeur est là.
// ✅ Dans edit { }, lire PUIS écrire est atomique : deux « +1 » très rapides donnent bien +2.
// À voir : Device Explorer → data/data/fr.formation.prefsujets/files/datastore/compteur.preferences_pb

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/prefsujets/ (ou …/prefsujets/sujet6/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity6, CompteurRoute
// ├── data/
// │   └── CompteurDataStore.kt     Context.dataStore, CLE_COMPTEUR
// └── ui/
//     └── compteur/
//         └── CompteurEcran.kt     CompteurEcran (+ sa @Preview)
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// ---------- 1 et 2 : le DataStore et sa clé ----------

// 📄 data/CompteurDataStore.kt
// Un délégué au niveau du fichier : UNE seule instance pour le fichier "compteur", partagée par toute l'app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "compteur")

// 📄 data/CompteurDataStore.kt
private val CLE_COMPTEUR = intPreferencesKey("compteur")

// ---------- 3 et 4 : lire et écrire ----------

// 📄 MainActivity.kt
class MainActivity6 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { CompteurRoute(dataStore) }
            }
        }
    }
}

// 📄 MainActivity.kt
// Composable AVEC état : c'est lui qui parle à DataStore (state hoisting : l'écran, lui, ne sait rien)
@Composable
private fun CompteurRoute(dataStore: DataStore<Preferences>) {
    // 3. LIRE : Preferences → Int. Le Flow réémet à chaque écriture dans le fichier.
    //    remember : on ne recrée pas le Flow à chaque recomposition
    val compteurFlow: Flow<Int> = remember { dataStore.data.map { prefs -> prefs[CLE_COMPTEUR] ?: 0 } }
    // null tant que le fichier n'est pas lu (quelques millisecondes, en arrière-plan)
    val compteur by compteurFlow.collectAsStateWithLifecycle(initialValue = null)

    // 4. ÉCRIRE : edit est suspend → il faut une coroutine, liée à la vie du composable
    val scope = rememberCoroutineScope()

    CompteurEcran(
        compteur = compteur,
        onAjouter = {
            scope.launch {
                // Lecture + écriture dans la MÊME transaction : aucune autre écriture ne peut s'intercaler
                dataStore.edit { prefs -> prefs[CLE_COMPTEUR] = (prefs[CLE_COMPTEUR] ?: 0) + 1 }
            }
        },
        onRetirer = {
            scope.launch {
                dataStore.edit { prefs -> prefs[CLE_COMPTEUR] = maxOf(0, (prefs[CLE_COMPTEUR] ?: 0) - 1) }
            }
        },
        onRemettreAZero = {
            // On supprime la clé : la lecture retombe sur la valeur par défaut (0)
            scope.launch { dataStore.edit { prefs -> prefs.remove(CLE_COMPTEUR) } }
        },
    )
}

// ---------- Écran ----------

// 📄 ui/compteur/CompteurEcran.kt
// Composable SANS état : il affiche ce qu'il reçoit et prévient (onXxx). Il ne sait pas OÙ c'est enregistré.
@Composable
private fun CompteurEcran(
    compteur: Int?,                       // null = pas encore lu
    onAjouter: () -> Unit,
    onRetirer: () -> Unit,
    onRemettreAZero: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Compteur enregistré", style = MaterialTheme.typography.titleMedium)
        Text(
            text = compteur?.toString() ?: "…",
            style = MaterialTheme.typography.displayLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onRetirer, enabled = (compteur ?: 0) > 0) { Text("−1") }
            Button(onClick = onAjouter, enabled = compteur != null) { Text("+1") }
        }
        TextButton(onClick = onRemettreAZero, enabled = (compteur ?: 0) > 0) { Text("Remettre à zéro") }
    }
}

// 📄 ui/compteur/CompteurEcran.kt
// Grâce au state hoisting, l'aperçu n'a besoin ni de DataStore ni de Context
@Preview(showBackground = true)
@Composable
private fun CompteurEcranPreview() {
    MaterialTheme { CompteurEcran(compteur = 7, onAjouter = {}, onRetirer = {}, onRemettreAZero = {}) }
}
