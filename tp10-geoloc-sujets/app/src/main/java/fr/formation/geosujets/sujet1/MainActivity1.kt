package fr.formation.geosujets.sujet1

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.launch

// Étape 1 — Demander la permission à l'exécution
// Nouveau :
// - rememberLauncherForActivityResult(RequestMultiplePermissions()) : affiche la boîte de dialogue du système
//   et rend la réponse (Map<permission, accordée ?>) ; on demande FINE et COARSE ensemble
// - shouldShowRequestPermissionRationale : vrai après un 1er refus → EXPLIQUER avant de redemander
// - 2e refus : le système n'affiche plus la boîte → seul recours, les paramètres de l'application
// - tout est rangé dans rememberPermissionLocalisation() : réutilisé tel quel dans les étapes suivantes
// À essayer : « Autoriser » → Refuser → l'explication s'affiche → « Autoriser » → Refuser :
//   refus définitif, bouton « Ouvrir les paramètres » ; y accorder la permission, revenir : ✅.
//   Choisir « Approximative » dans la boîte de dialogue : seule COARSE est accordée.
// Pour recommencer : adb shell pm reset-permissions fr.formation.geosujets (ou désinstaller l'app)

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/geosujets/ (ou …/geosujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt                  MainActivity1
// ├── permissions/
// │   ├── Permissions.kt               PERMISSIONS_LOCALISATION, aLaPermission, ouvrirParametresApp, trouverActivity
// │   ├── PermissionLocalisation.kt    PermissionLocalisation, rememberPermissionLocalisation
// │   └── BlocPermission.kt            BlocPermission
// └── ui/
//     └── GeolocEcran.kt               GeolocEcran
//
// - rememberPermissionLocalisation est un « state holder » : il garde l'état ET sait le modifier
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 MainActivity.kt
class MainActivity1 : ComponentActivity() {
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
// On demande les DEUX : l'utilisateur choisit ensuite « précise » ou « approximative » (Android 12+)
private val PERMISSIONS_LOCALISATION = arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)

// 📄 permissions/Permissions.kt
private fun Context.aLaPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

// 📄 permissions/Permissions.kt
// Ouvre la fiche de l'application dans les Paramètres (Autorisations → Position)
private fun Context.ouvrirParametresApp() {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
}

// 📄 permissions/Permissions.kt
// Le Context d'un composable est l'Activity, ou un ContextWrapper qui l'entoure
private tailrec fun Context.trouverActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.trouverActivity()
    else -> error("Aucune Activity")
}

// 📄 permissions/PermissionLocalisation.kt
// L'état de la permission, et de quoi la demander
data class PermissionLocalisation(
    val accordee: Boolean,          // précise OU approximative
    val precise: Boolean,           // ACCESS_FINE_LOCATION
    val refusDefinitif: Boolean,    // la boîte de dialogue ne s'affichera plus
    val expliquer: Boolean,         // le système conseille d'expliquer avant de redemander
    val demander: () -> Unit,
)

// 📄 permissions/PermissionLocalisation.kt
@Composable
private fun rememberPermissionLocalisation(): PermissionLocalisation {
    val context = LocalContext.current
    val activity = remember(context) { context.trouverActivity() }

    var precise by remember { mutableStateOf(context.aLaPermission(ACCESS_FINE_LOCATION)) }
    var approximative by remember { mutableStateOf(context.aLaPermission(ACCESS_COARSE_LOCATION)) }
    var refusDefinitif by rememberSaveable { mutableStateOf(false) }

    // Le lanceur : affiche la boîte de dialogue du SYSTÈME, puis rappelle ce bloc avec la réponse
    val lanceur = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { reponses ->
        precise = reponses[ACCESS_FINE_LOCATION] == true
        approximative = reponses[ACCESS_COARSE_LOCATION] == true
        // Refusée, et le système ne propose plus d'expliquer : c'était le 2e refus → plus de boîte de dialogue.
        // (Approximation classique : fermer la 1re boîte sans répondre donne le même résultat.)
        refusDefinitif = !approximative && !activity.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)
    }

    // Revérifier à chaque retour au premier plan : l'utilisateur a pu tout changer dans les Paramètres
    LifecycleResumeEffect(Unit) {
        precise = context.aLaPermission(ACCESS_FINE_LOCATION)
        approximative = context.aLaPermission(ACCESS_COARSE_LOCATION)
        if (approximative) refusDefinitif = false
        onPauseOrDispose { }
    }

    return PermissionLocalisation(
        accordee = approximative || precise,
        precise = precise,
        refusDefinitif = refusDefinitif,
        expliquer = !approximative && activity.shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION),
        demander = { lanceur.launch(PERMISSIONS_LOCALISATION) },
    )
}

// 📄 permissions/BlocPermission.kt
// Ce qu'on affiche tant que la localisation n'est pas autorisée
@Composable
private fun BlocPermission(permission: PermissionLocalisation) {
    val context = LocalContext.current
    when {
        permission.refusDefinitif -> {
            Text("❌ Localisation refusée. La demande ne peut plus s'afficher : autorisez-la dans les paramètres de l'application.")
            Button(onClick = { context.ouvrirParametresApp() }) { Text("Ouvrir les paramètres") }
        }
        // Expliquer AVANT de redemander : pourquoi on en a besoin, et ce qu'on en fait
        permission.expliquer -> {
            Text("📍 Votre position sert seulement à l'afficher ici et à calculer une distance. Elle ne quitte pas l'appareil.")
            Button(onClick = permission.demander) { Text("Autoriser") }
        }
        else -> Button(onClick = permission.demander) { Text("Autoriser la localisation") }
    }
}

// 📄 ui/GeolocEcran.kt
@Composable
private fun GeolocEcran() {
    val permission = rememberPermissionLocalisation()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Où suis-je ?", style = MaterialTheme.typography.headlineMedium)
        if (permission.accordee) {
            Text(if (permission.precise) "✅ Position précise autorisée" else "✅ Position approximative autorisée (≈ 3 km)")
            Text("➡ Étape 2 : obtenir la position.")
        } else {
            BlocPermission(permission)
        }
    }
}
