package fr.formation.ousuisje.localisation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Étape 2 : Fused Location Provider (Google Play Services).
 * Si seule la position approximative est accordée, le FLP dégrade lui-même
 * la précision (~3 km) : aucun code particulier n'est nécessaire ici.
 */
class LocalisationDataSource(context: Context) : SourceLocalisation {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @OptIn(ExperimentalCoroutinesApi::class)   // await(CancellationTokenSource)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    override suspend fun positionCourante(): Position? {
        val annulation = CancellationTokenSource()
        // On affiche la précision : on demande la meilleure disponible
        val location = client
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, annulation.token)
            .await(annulation)                  // annule la requête si la coroutine est annulée
            ?: client.lastLocation.await()      // repli : dernière position connue (peut être null)
        return location?.versPosition()
    }

    /** Étape 9 (bonus, flavor pro) : API à callback transformée en Flow. */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    override fun suivrePosition(intervalleMs: Long): Flow<Position> = callbackFlow {
        val requete = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalleMs)
            .setMinUpdateIntervalMillis(intervalleMs / 2)
            .setMinUpdateDistanceMeters(10f)            // ignorer les micro-déplacements
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.versPosition()) }
            }
        }

        client.requestLocationUpdates(requete, callback, Looper.getMainLooper())
            .addOnFailureListener { close(it) }

        awaitClose { client.removeLocationUpdates(callback) }  // obligatoire : sinon fuite et batterie vidée
    }
}
