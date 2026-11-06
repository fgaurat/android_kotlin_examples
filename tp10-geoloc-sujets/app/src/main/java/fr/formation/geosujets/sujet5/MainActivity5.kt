package fr.formation.geosujets.sujet5

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Étape 5 — Un ViewModel, une source de localisation, une distance
// Nouveau (sur la base de l'étape 4) :
// - SourceLocalisation (interface) + SourceFused : le ViewModel ne connaît plus Google Play Services
//   → en test, un faux qui renvoie des positions choisies (voir tp10-ou-suis-je, OuSuisJeViewModelTest)
// - GeolocViewModel : la position survit à la rotation ; le suivi est un Flow dans le ViewModel :
//   combine(suivi demandé, permission) + flatMapLatest, exposé avec stateIn(WhileSubscribed(5_000))
//   → en arrière-plan, l'écran ne collecte plus et le suivi s'arrête 5 s plus tard
// - la PERMISSION reste dans l'écran (il faut une Activity pour la demander) : l'écran prévient le ViewModel
//   avec permissionChangee(…), dans un LaunchedEffect
// - distance jusqu'à Paris avec Location.distanceBetween
// À essayer : position simulée à Lyon (45.7640, 4.8357) → environ 392 km ; tourner l'écran pendant
//   le suivi : il continue sans redémarrer (Logcat « GeoSujets ») ; touche Accueil : ARRÊTÉ après 5 s.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/geosujets/ (ou …/geosujets/sujet5/ pour rester dans ce projet) :
//
// ├── MainActivity.kt                  MainActivity5
// ├── data/
// │   ├── Position.kt                  Position, versPosition
// │   └── Distance.kt                  PARIS, distanceKm
// ├── localisation/
// │   ├── SourceLocalisation.kt        SourceLocalisation (interface)
// │   ├── SourceFused.kt               SourceFused (Fused Location Provider)
// │   └── Localisation.kt              localisationActivee, ouvrirParametresLocalisation
// ├── permissions/
// │   ├── Permissions.kt               PERMISSIONS_LOCALISATION, aLaPermission, ouvrirParametresApp, trouverActivity
// │   ├── PermissionLocalisation.kt    PermissionLocalisation, rememberPermissionLocalisation (= étape 1)
// │   └── BlocPermission.kt            BlocPermission (= étape 1)
// └── ui/
//     ├── GeolocUiState.kt             GeolocUiState
//     ├── GeolocViewModel.kt           GeolocViewModel (+ Factory)
//     ├── GeolocEcran.kt               GeolocRoute (avec état), GeolocEcran (sans état)
//     └── CartePosition.kt             CartePosition
//
// - dans un vrai projet, SourceFused est créée une fois dans un AppContainer (voir tp07-todos-mvvm-retrofit)
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 MainActivity.kt
class MainActivity5 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { GeolocRoute() }
            }
        }
    }
}

// 📄 data/Position.kt
data class Position(val latitude: Double, val longitude: Double, val precisionM: Float)

// 📄 data/Position.kt
private fun Location.versPosition() = Position(latitude, longitude, accuracy)

// 📄 data/Distance.kt
private val PARIS = Position(latitude = 48.8566, longitude = 2.3522, precisionM = 0f)

// 📄 data/Distance.kt
// API du framework Android : absente des tests JVM (tp10-ou-suis-je l'injecte pour pouvoir tester)
private fun distanceKm(de: Position, a: Position): Double {
    val resultat = FloatArray(1)
    Location.distanceBetween(de.latitude, de.longitude, a.latitude, a.longitude, resultat)
    return resultat[0] / 1000.0
}

// 📄 localisation/SourceLocalisation.kt
// Le contrat vu par le ViewModel : rien sur Google Play Services → remplaçable par un faux en test
interface SourceLocalisation {
    suspend fun positionCourante(precise: Boolean): Position?
    fun suivrePosition(precise: Boolean): Flow<Position>
}

// 📄 localisation/SourceFused.kt
// MissingPermission : le ViewModel n'appelle la source qu'avec la permission (et SecurityException est rattrapée)
@SuppressLint("MissingPermission")
class SourceFused(context: Context) : SourceLocalisation {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    private fun priorite(precise: Boolean) =
        if (precise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun positionCourante(precise: Boolean): Position? {
        val annulation = CancellationTokenSource()
        return client.getCurrentLocation(priorite(precise), annulation.token).await(annulation)?.versPosition()
            ?: client.lastLocation.await()?.versPosition()              // repli : la dernière connue
    }

    override fun suivrePosition(precise: Boolean): Flow<Position> = callbackFlow {
        val requete = LocationRequest.Builder(priorite(precise), 5_000)
            .setMinUpdateIntervalMillis(2_500)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(resultat: LocationResult) {
                resultat.lastLocation?.let { trySend(it.versPosition()) }
            }
        }
        Log.d("GeoSujets", "Suivi : DÉMARRÉ")
        client.requestLocationUpdates(requete, callback, Looper.getMainLooper()).addOnFailureListener { close(it) }
        awaitClose {
            Log.d("GeoSujets", "Suivi : ARRÊTÉ")
            client.removeLocationUpdates(callback)
        }
    }
}

// 📄 localisation/Localisation.kt
// Le bouton « Localisation » des paramètres rapides : coupé, aucune application n'obtient de position
private fun Context.localisationActivee(): Boolean =
    LocationManagerCompat.isLocationEnabled(getSystemService(LocationManager::class.java))

// 📄 localisation/Localisation.kt
private fun Context.ouvrirParametresLocalisation() {
    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

// 📄 ui/GeolocUiState.kt
data class GeolocUiState(
    val position: Position? = null,
    val distanceParisKm: Double? = null,
    val enCours: Boolean = false,
    val message: String? = null,
    val suivi: Boolean = false,
)

// 📄 ui/GeolocViewModel.kt
@OptIn(ExperimentalCoroutinesApi::class)           // flatMapLatest
class GeolocViewModel(private val source: SourceLocalisation) : ViewModel() {

    // Ce que l'écran nous dit de la permission (c'est lui qui la demande : il faut une Activity)
    private data class Acces(val accordee: Boolean = false, val precise: Boolean = false)
    private val acces = MutableStateFlow(Acces())

    private val suiviDemande = MutableStateFlow(false)
    private val ponctuel = MutableStateFlow(GeolocUiState())       // position, enCours, message

    // Le suivi : actif seulement si demandé ET autorisé. flatMapLatest coupe l'ancien Flow
    // (→ awaitClose → removeLocationUpdates) dès que l'une des deux conditions change.
    private val positionSuivie: Flow<Position?> = combine(suiviDemande, acces) { suivi, a -> suivi to a }
        .flatMapLatest { (suivi, a) ->
            if (suivi && a.accordee) source.suivrePosition(a.precise).catch { e -> Log.e("GeoSujets", "Suivi", e) }
            else flowOf(null)
        }
        .onEach { p -> if (p != null) ponctuel.update { it.copy(position = p) } }   // garder la dernière reçue

    // WhileSubscribed(5_000) : l'écran ne collecte plus (arrière-plan) → 5 s plus tard, le suivi s'arrête
    val etat: StateFlow<GeolocUiState> = combine(ponctuel, suiviDemande, positionSuivie.onStart { emit(null) }) { p, suivi, _ ->
        p.copy(suivi = suivi, distanceParisKm = p.position?.let { distanceKm(it, PARIS) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GeolocUiState())

    fun permissionChangee(accordee: Boolean, precise: Boolean) {
        acces.value = Acces(accordee, precise)
    }

    fun localiser() {
        viewModelScope.launch {
            ponctuel.update { it.copy(enCours = true, message = null) }
            val message = try {
                val lue = source.positionCourante(acces.value.precise)
                if (lue != null) ponctuel.update { it.copy(position = lue) }
                if (lue == null) "Aucune position disponible." else null
            } catch (e: SecurityException) {
                "Permission retirée."
            }
            ponctuel.update { it.copy(enCours = false, message = message) }
        }
    }

    fun basculerSuivi() {
        suiviDemande.update { !it }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { GeolocViewModel(SourceFused(this[APPLICATION_KEY]!!)) }
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
// Composable AVEC état : la permission, la localisation activée, et le ViewModel
@Composable
private fun GeolocRoute(viewModel: GeolocViewModel = viewModel(factory = GeolocViewModel.Factory)) {
    val context = LocalContext.current
    val permission = rememberPermissionLocalisation()
    val etat by viewModel.etat.collectAsStateWithLifecycle()

    var localisationActive by remember { mutableStateOf(context.localisationActivee()) }
    LifecycleResumeEffect(Unit) {
        localisationActive = context.localisationActivee()
        onPauseOrDispose { }
    }

    // Prévenir le ViewModel à chaque changement de permission (accordée, retirée, passée en précise)
    LaunchedEffect(permission.accordee, permission.precise) {
        viewModel.permissionChangee(permission.accordee, permission.precise)
    }

    GeolocEcran(
        etat = etat,
        permission = permission,
        localisationActive = localisationActive,
        onLocaliser = viewModel::localiser,
        onBasculerSuivi = viewModel::basculerSuivi,
    )
}

// 📄 ui/GeolocEcran.kt
// Composable SANS état : il affiche et prévient
@Composable
private fun GeolocEcran(
    etat: GeolocUiState,
    permission: PermissionLocalisation,
    localisationActive: Boolean,
    onLocaliser: () -> Unit,
    onBasculerSuivi: () -> Unit,
) {
    val context = LocalContext.current
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
        if (!localisationActive) {
            Text("📴 La localisation du téléphone est désactivée : aucune application ne peut obtenir de position.")
            Button(onClick = { context.ouvrirParametresLocalisation() }) { Text("Activer la localisation") }
            return@Column
        }
        if (!permission.precise) {
            Text("📍 Position approximative : précision d'environ 3 km.")
            OutlinedButton(onClick = permission.demander) { Text("Autoriser la position précise") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onLocaliser, enabled = !etat.enCours && !etat.suivi) { Text("Où suis-je ?") }
            if (etat.enCours) CircularProgressIndicator()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Suivre ma position", modifier = Modifier.weight(1f))
            Switch(checked = etat.suivi, onCheckedChange = { onBasculerSuivi() })
        }
        etat.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        CartePosition(etat.position)
        etat.distanceParisKm?.let { km ->
            Text(String.format(Locale.FRANCE, "Paris est à %.0f km", km), style = MaterialTheme.typography.titleMedium)
        }
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
