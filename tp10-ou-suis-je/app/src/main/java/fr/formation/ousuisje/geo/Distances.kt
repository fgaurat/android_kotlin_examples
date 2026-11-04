package fr.formation.ousuisje.geo

import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Logique pure (sans dépendance Android) : testable sur la JVM.
 * Le calcul réel de distance utilise Location.distanceBetween (voir localisation/),
 * injecté sous forme de [CalculDistance].
 */

/** Point de référence de l'étape 3 : Notre-Dame de Paris (point zéro des routes de France). */
data class Lieu(val nom: String, val latitude: Double, val longitude: Double)

val PARIS = Lieu("Paris", 48.853_4, 2.348_8)

/** Villes proposées en mode dégradé, quand la position est refusée. */
val VILLES_DE_REPLI = listOf(
    Lieu("Lyon", 45.764_0, 4.835_7),
    Lieu("Marseille", 43.296_5, 5.369_8),
    Lieu("Brest", 48.390_4, -4.486_1),
    Lieu("Strasbourg", 48.573_4, 7.752_1),
)

/** Distance en mètres entre deux points (même signature que Location.distanceBetween). */
fun interface CalculDistance {
    fun metres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float
}

/**
 * Formule de haversine (sphère de rayon moyen 6 371 km) : sert de référence dans les tests,
 * Location.distanceBetween (ellipsoïde WGS84) donne un résultat à ~0,5 % près.
 */
val calculHaversine = CalculDistance { lat1, lon1, lat2, lon2 ->
    val rayonTerre = 6_371_008.8
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    (2 * rayonTerre * asin(sqrt(a))).toFloat()
}

/** Au-delà de cette incertitude, une position « précise » est traitée comme approximative. */
const val SEUIL_PRECISION_METRES = 1_000f

/**
 * Étape 3 : la position est approximative si l'utilisateur n'a accordé que
 * ACCESS_COARSE_LOCATION (~3 km), ou si la précision annoncée est mauvaise.
 */
fun estApproximative(precisionAccordee: Boolean, precisionMetres: Float?): Boolean =
    !precisionAccordee || precisionMetres == null || precisionMetres > SEUIL_PRECISION_METRES

private val FR = Locale.FRANCE

/**
 * Distance lisible. En mode approximatif, on arrondit au kilomètre et on
 * n'affiche pas de décimale trompeuse.
 */
fun formaterDistance(metres: Float, approximative: Boolean): String {
    val km = metres / 1_000f
    return if (approximative) {
        if (metres < 3_000f) "moins de 3 km" else "≈ ${km.roundToInt()} km"
    } else when {
        metres < 1_000f -> "${metres.roundToInt()} m"
        km < 100f -> String.format(FR, "%.1f km", km)
        else -> "${km.roundToInt()} km"
    }
}

/** Précision (rayon de confiance à 68 %) fournie par Location.getAccuracy(). */
fun formaterPrecision(precisionMetres: Float?): String = when {
    precisionMetres == null -> "inconnue"
    precisionMetres < 1_000f -> "± ${precisionMetres.roundToInt()} m"
    else -> String.format(FR, "± %.1f km", precisionMetres / 1_000f)
}

fun formaterLatitude(latitude: Double): String =
    String.format(FR, "%.5f° %s", kotlin.math.abs(latitude), if (latitude >= 0) "N" else "S")

fun formaterLongitude(longitude: Double): String =
    String.format(FR, "%.5f° %s", kotlin.math.abs(longitude), if (longitude >= 0) "E" else "O")
