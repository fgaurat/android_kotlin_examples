package fr.formation.geosujets.sujet2

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Étape 2 — Obtenir la position : Fused Location Provider
// Nouveau :
// - LocationServices.getFusedLocationProviderClient(context) : le fournisseur de Google Play Services,
//   qui combine GPS, Wi-Fi et réseau mobile (il faut un émulateur / appareil AVEC Google Play)
// - getCurrentLocation(priorité, jeton).await(annulation) : UNE position fraîche, dans une coroutine
//   (kotlinx-coroutines-play-services transforme la Task de Google en fonction suspend)
// - lastLocation : la dernière position connue du système, immédiate mais peut-être ancienne, ou null
// - une position = latitude, longitude, et une PRÉCISION en mètres (rayon du cercle de confiance)
// - SecurityException rattrapée : la permission peut être retirée pendant que l'app tourne
// À essayer : émulateur → ⋮ (Extended controls) → Location → saisir un point → « Set location »,
//   puis « Où suis-je ? ». En ligne de commande : adb emu geo fix 2.3522 48.8566 (longitude d'abord !)
// ❌ Reste à faire : position approximative, localisation désactivée (étape 3)

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/geosujets/ (ou …/geosujets/sujet2/ pour rester dans ce projet) :
//
// ├── MainActivity.kt                  MainActivity2
// ├── data/
// │   └── Position.kt                  Position, versPosition
// ├── localisation/
// │   └── Localisation.kt              positionCourante, dernierePosition
// ├── permissions/
// │   ├── Permissions.kt               PERMISSIONS_LOCALISATION, aLaPermission, ouvrirParametresApp, trouverActivity
// │   ├── PermissionLocalisation.kt    PermissionLocalisation, rememberPermissionLocalisation (= étape 1)
// │   └── BlocPermission.kt            BlocPermission (= étape 1)
// └── ui/
//     ├── GeolocEcran.kt               GeolocEcran
//     └── CartePosition.kt             CartePosition
//
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 MainActivity.kt
class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { GeolocEcran() }
            }
        }
    }
}

// 📄 data/Position.kt
data class Position(val latitude: Double, val longitude: Double, val precisionM: Float)

// 📄 data/Position.kt
private fun Location.versPosition() = Position(latitude, longitude, accuracy)

// 📄 localisation/Localisation.kt
// MissingPermission : la permission est vérifiée avant l'appel (et SecurityException est rattrapée)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalCoroutinesApi::class)            // await(annulation)
private suspend fun FusedLocationProviderClient.positionCourante(): Position? {
    val annulation = CancellationTokenSource()
    return getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, annulation.token)
        .await(annulation)                          // si la coroutine est annulée, la requête aussi
        ?.versPosition()                            // null : aucune position obtenue (localisation coupée…)
}

// 📄 localisation/Localisation.kt
// La dernière position connue par le système : immédiate, mais peut être ancienne… ou null
@SuppressLint("MissingPermission")
private suspend fun FusedLocationProviderClient.dernierePosition(): Position? =
    lastLocation.await()?.versPosition()

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
    val context = LocalContext.current
    val permission = rememberPermissionLocalisation()
    val client = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    var position by remember { mutableStateOf<Position?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var enCours by remember { mutableStateOf(false) }

    // Les deux boutons font la même chose avec une fonction différente
    fun localiser(lire: suspend () -> Position?) {
        scope.launch {
            enCours = true
            message = null
            try {
                val lue = lire()
                if (lue == null) message = "Aucune position disponible." else position = lue
            } catch (e: SecurityException) {
                message = "Permission retirée."
            } finally {
                enCours = false
            }
        }
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
        if (!permission.accordee) {
            BlocPermission(permission)
            return@Column
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { localiser { client.positionCourante() } }, enabled = !enCours) { Text("Où suis-je ?") }
            OutlinedButton(onClick = { localiser { client.dernierePosition() } }, enabled = !enCours) {
                Text("Dernière connue")
            }
            if (enCours) CircularProgressIndicator()
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        CartePosition(position)
    }
}

// 📄 ui/CartePosition.kt
@Composable
private fun CartePosition(position: Position?) {
    if (position == null) {
        Text("Aucune position pour l'instant.")
        return
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(String.format(Locale.FRANCE, "Latitude : %.5f", position.latitude))
            Text(String.format(Locale.FRANCE, "Longitude : %.5f", position.longitude))
            Text("Précision : ± ${position.precisionM.roundToInt()} m")       // rayon du cercle de confiance
        }
    }
}
