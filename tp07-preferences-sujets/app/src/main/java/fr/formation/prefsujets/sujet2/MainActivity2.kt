package fr.formation.prefsujets.sujet2

import android.content.Context
import android.os.Bundle
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

// Étape 2 — Preferences DataStore
// Nouveau :
// - val Context.dataStore by preferencesDataStore(name = …) : UNE instance par fichier et par processus
//   (déclarée au niveau du fichier ; deux instances sur le même fichier → IllegalStateException)
// - des clés TYPÉES : stringPreferencesKey("nom"), booleanPreferencesKey("sombre")
// - lecture : dataStore.data est un Flow<Preferences> → collectAsStateWithLifecycle
// - écriture : dataStore.edit { it[CLE] = valeur } est une fonction suspend → dans une coroutine,
//   jamais sur le thread principal ; c'est une TRANSACTION (tout ou rien)
// ✅ Plus d'état Compose à synchroniser : l'écran affiche ce que le Flow émet.
//    Après « Enregistrer », « Bonjour, … » change tout seul : DataStore a réémis.
// ❌ Reste à faire : l'écran parle directement à DataStore (clés, coroutines) → repository + ViewModel (étape 3)
// À voir : Device Explorer → data/data/fr.formation.prefsujets/files/datastore/reglages-etape2.preferences_pb

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/prefsujets/ (ou …/prefsujets/sujet2/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity2, ReglagesApp
// ├── data/
// │   └── ReglagesDataStore.kt     Context.dataStore, Cles
// └── ui/
//     ├── theme/
//     │   └── Theme.kt             AppTheme
//     └── reglages/
//         └── ReglagesEcran.kt     ReglagesEcran
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/ReglagesDataStore.kt
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reglages-etape2")

// 📄 data/ReglagesDataStore.kt
private object Cles {
    val NOM = stringPreferencesKey("nom")
    val SOMBRE = booleanPreferencesKey("sombre")
}

// 📄 MainActivity.kt
class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReglagesApp(dataStore) }       // applicationContext.dataStore : même instance partout
    }
}

// 📄 MainActivity.kt
@Composable
private fun ReglagesApp(dataStore: DataStore<Preferences>) {
    // null tant que le fichier n'est pas lu (quelques millisecondes, en arrière-plan)
    val prefs by remember { dataStore.data }.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    val p = prefs
    if (p == null) return                            // rien à afficher pendant la toute première lecture

    val sombre = p[Cles.SOMBRE] ?: false             // absent du fichier → valeur par défaut
    AppTheme(sombre = sombre) {
        ReglagesEcran(
            nom = p[Cles.NOM] ?: "",
            sombre = sombre,
            // On écrit, et c'est tout : DataStore réémet, l'écran se met à jour
            onEnregistrerNom = { nouveau -> scope.launch { dataStore.edit { it[Cles.NOM] = nouveau } } },
            onSombreChange = { valeur -> scope.launch { dataStore.edit { it[Cles.SOMBRE] = valeur } } },
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
