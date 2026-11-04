package fr.formation.ousuisje.localisation

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Location
import androidx.annotation.RequiresPermission
import fr.formation.ousuisje.geo.CalculDistance
import kotlinx.coroutines.flow.Flow

/** Position indépendante du framework : le ViewModel reste testable sur la JVM. */
data class Position(
    val latitude: Double,
    val longitude: Double,
    /** Rayon d'incertitude en mètres, null si inconnu. */
    val precisionMetres: Float?,
)

fun Location.versPosition() = Position(
    latitude = latitude,
    longitude = longitude,
    precisionMetres = if (hasAccuracy()) accuracy else null,
)

/** Abstraction de la source de position : remplacée par un faux dans les tests. */
interface SourceLocalisation {

    /** Position courante, ou null si indisponible (localisation désactivée…). */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    suspend fun positionCourante(): Position?

    /** Suivi continu (flavor pro) : Flow froid, un abonnement par collecteur. */
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun suivrePosition(intervalleMs: Long): Flow<Position>
}

/** Étape 3 : calcul de distance du framework (ellipsoïde WGS84). */
val calculDistanceAndroid = CalculDistance { lat1, lon1, lat2, lon2 ->
    val resultats = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, resultats)
    resultats[0]
}
