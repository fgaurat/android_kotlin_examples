package fr.formation.prefsujets.sujet1

import android.content.Context
import android.content.SharedPreferences
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
import androidx.core.content.edit

// Étape 1 — SharedPreferences : l'API historique
// Nouveau :
// - getSharedPreferences("reglages", MODE_PRIVATE) : un fichier XML privé de l'application
//   (/data/data/fr.formation.prefsujets/shared_prefs/reglages.xml)
// - lecture : getString(CLE, défaut), getBoolean(CLE, défaut)
// - écriture : edit { putString(…) } (extension de core-ktx, qui fait apply() : écriture en arrière-plan)
// ✅ Fermer l'app et la rouvrir : prénom et thème sont conservés
// ❌ La première lecture attend le chargement du fichier, sur le thread principal : un gros fichier
//    bloque l'écran (StrictMode, avec detectDiskReads, le signale)
// ❌ Pas d'observation : si une autre partie de l'app modifie le fichier, cet écran ne le voit pas
//    (il faudrait un OnSharedPreferenceChangeListener)
// ❌ Pas de gestion d'erreur, pas de transaction : deux écritures concurrentes peuvent se marcher dessus
// Le guide Android recommande DataStore à la place (étape 2).
// À voir : Device Explorer → data/data/fr.formation.prefsujets/shared_prefs/reglages.xml

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/prefsujets/ (ou …/prefsujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1, ReglagesApp
// ├── data/
// │   └── Cles.kt                  FICHIER, CLE_NOM, CLE_SOMBRE
// └── ui/
//     ├── theme/
//     │   └── Theme.kt             AppTheme
//     └── reglages/
//         └── ReglagesEcran.kt     ReglagesEcran
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/Cles.kt
// Ces noms sont repris à l'étape 4 : la migration vers DataStore lit CE fichier, avec CES clés
const val FICHIER = "reglages"

// 📄 data/Cles.kt
const val CLE_NOM = "nom"

// 📄 data/Cles.kt
const val CLE_SOMBRE = "sombre"

// 📄 MainActivity.kt
class MainActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(FICHIER, Context.MODE_PRIVATE)
        setContent { ReglagesApp(prefs) }
    }
}

// 📄 MainActivity.kt
@Composable
private fun ReglagesApp(prefs: SharedPreferences) {
    // Lu UNE fois, au démarrage, puis gardé en mémoire dans un état Compose
    var nom by rememberSaveable { mutableStateOf(prefs.getString(CLE_NOM, "") ?: "") }
    var sombre by rememberSaveable { mutableStateOf(prefs.getBoolean(CLE_SOMBRE, false)) }

    AppTheme(sombre = sombre) {
        ReglagesEcran(
            nom = nom,
            sombre = sombre,
            onEnregistrerNom = { nouveau ->
                nom = nouveau                                    // 1. l'écran…
                prefs.edit { putString(CLE_NOM, nouveau) }       // 2. … et le fichier : deux choses à synchroniser
                Log.d("PrefSujets", "SharedPreferences : nom = $nouveau")
            },
            onSombreChange = { valeur ->
                sombre = valeur
                prefs.edit { putBoolean(CLE_SOMBRE, valeur) }
            },
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
