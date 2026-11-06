package fr.formation.geosujets.sujet0

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

// Étape 0 — Déclarer les permissions, et vérifier si elles sont accordées
// - AndroidManifest.xml : <uses-permission ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION />
// - ce sont des permissions « dangereuses » : les DÉCLARER ne suffit pas, l'utilisateur doit les ACCORDER
// - ContextCompat.checkSelfPermission(…) == PERMISSION_GRANTED : accordée ou non ?
// - LifecycleResumeEffect : revérifier à chaque retour sur l'écran
// À essayer : au premier lancement, les deux sont ❌ ;
//   « Ouvrir les paramètres » → Autorisations → Position → « Autoriser seulement si l'appli est utilisée »,
//   revenir : ✅ sans relancer l'app. Essayer aussi « Position exacte » désactivé : seule COARSE est ✅.
// ➡ Étape 1 : demander la permission depuis l'application.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/geosujets/ (ou …/geosujets/sujet0/ pour rester dans ce projet) :
//
// ├── MainActivity.kt                MainActivity0
// ├── permissions/
// │   └── Permissions.kt             aLaPermission, ouvrirParametresApp
// └── ui/
//     └── GeolocEcran.kt             GeolocEcran
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 MainActivity.kt
class MainActivity0 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { GeolocEcran() }
            }
        }
    }
}

// 📄 permissions/Permissions.kt
private fun Context.aLaPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

// 📄 permissions/Permissions.kt
// Ouvre la fiche de l'application dans les Paramètres (Autorisations → Position)
private fun Context.ouvrirParametresApp() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
}

// 📄 ui/GeolocEcran.kt
@Composable
private fun GeolocEcran() {
    val context = LocalContext.current
    var precise by remember { mutableStateOf(context.aLaPermission(ACCESS_FINE_LOCATION)) }
    var approximative by remember { mutableStateOf(context.aLaPermission(ACCESS_COARSE_LOCATION)) }

    // onResume : au lancement, et à chaque retour depuis les Paramètres
    LifecycleResumeEffect(Unit) {
        precise = context.aLaPermission(ACCESS_FINE_LOCATION)
        approximative = context.aLaPermission(ACCESS_COARSE_LOCATION)
        onPauseOrDispose { }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Où suis-je ?", style = MaterialTheme.typography.headlineMedium)
        Text(if (precise) "✅ Position précise (ACCESS_FINE_LOCATION)" else "❌ Position précise (ACCESS_FINE_LOCATION)")
        Text(if (approximative) "✅ Position approximative (ACCESS_COARSE_LOCATION)" else "❌ Position approximative (ACCESS_COARSE_LOCATION)")
        Text("Déclarées dans le manifest, mais pas encore accordées : il faut les demander (étape 1).")
        OutlinedButton(onClick = { context.ouvrirParametresApp() }) { Text("Ouvrir les paramètres de l'application") }
    }
}
