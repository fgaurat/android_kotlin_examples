package fr.formation.ousuisje.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/*
 * Étape 1 : demande de permission conforme à la checklist du chapitre
 * (Activity Result API, COARSE + FINE ensemble, rationale, refus définitif,
 * revérification au retour au premier plan — voir OuSuisJeScreen).
 */

val PERMISSIONS_LOCALISATION = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
)

fun Context.aLaPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/** Approximative OU précise : la position approximative suffit. */
fun Context.localisationAccordee(): Boolean =
    aLaPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ||
        aLaPermission(Manifest.permission.ACCESS_FINE_LOCATION)

fun Context.localisationPreciseAccordee(): Boolean =
    aLaPermission(Manifest.permission.ACCESS_FINE_LOCATION)

fun Activity.doitExpliquer(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

/** Refus définitif : seul l'écran des paramètres de l'app permet de changer d'avis. */
fun Context.ouvrirParametresApp() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ),
    )
}

/** Localisation désactivée au niveau du système (réglages rapides). */
fun Context.ouvrirReglagesLocalisation() {
    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
}

class DemandeLocalisation {
    var afficherExplication by mutableStateOf(false)
    var refusDefinitif by mutableStateOf(false)
    var lancer: () -> Unit = {}
    var confirmer: () -> Unit = {}
    /** Redemande les deux permissions pour passer d'approximative à précise. */
    var demanderPrecise: () -> Unit = {}
}

/**
 * @param onResultat appelé après chaque réponse (pour rafraîchir l'état des permissions)
 * @param onAccordee appelé dès qu'une position (approximative ou précise) est autorisée
 */
@Composable
fun rememberDemandeLocalisation(
    onResultat: () -> Unit,
    onAccordee: () -> Unit,
): DemandeLocalisation {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val demande = remember { DemandeLocalisation() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { resultats: Map<String, Boolean> ->
        onResultat()
        if (resultats.values.any { it } || context.localisationAccordee()) {
            demande.refusDefinitif = false
            onAccordee()
        } else {
            // false après un refus = la boîte système ne s'affichera plus
            demande.refusDefinitif = activity != null &&
                PERMISSIONS_LOCALISATION.none { activity.doitExpliquer(it) }
        }
    }

    demande.lancer = {
        when {
            context.localisationAccordee() -> onAccordee()          // vérifier à chaque usage
            activity != null && PERMISSIONS_LOCALISATION.any { activity.doitExpliquer(it) } ->
                demande.afficherExplication = true
            else -> launcher.launch(PERMISSIONS_LOCALISATION)       // 1re fois ou refus définitif
        }
    }
    demande.confirmer = {
        demande.afficherExplication = false
        launcher.launch(PERMISSIONS_LOCALISATION)
    }
    demande.demanderPrecise = { launcher.launch(PERMISSIONS_LOCALISATION) }
    return demande
}
