package fr.formation.prefsujets.sujet0

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

// Étape 0 — Des réglages… en mémoire seulement
// Un prénom et un thème sombre, gardés avec rememberSaveable.
// ✅ Tourner l'écran : les réglages sont conservés
// ❌ Fermer l'application (balayer dans les récents) puis la rouvrir : TOUT EST PERDU.
//    rememberSaveable survit à la rotation et à la mort du processus en arrière-plan,
//    mais pas à une fermeture volontaire par l'utilisateur.
// ➡ Étape 1 : enregistrer les réglages sur l'appareil.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/prefsujets/ (ou …/prefsujets/sujet0/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity0, ReglagesApp
// └── ui/
//     ├── theme/
//     │   └── Theme.kt             AppTheme
//     └── reglages/
//         └── ReglagesEcran.kt     ReglagesEcran
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 MainActivity.kt
class MainActivity0 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ReglagesApp() }
    }
}

// 📄 MainActivity.kt
@Composable
private fun ReglagesApp() {
    var nom by rememberSaveable { mutableStateOf("") }
    var sombre by rememberSaveable { mutableStateOf(false) }

    AppTheme(sombre = sombre) {
        ReglagesEcran(
            nom = nom,
            sombre = sombre,
            onEnregistrerNom = { nom = it },
            onSombreChange = { sombre = it },
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
