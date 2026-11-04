package fr.formation.ousuisje.localisation

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.ousuisje.BuildConfig
import fr.formation.ousuisje.geo.PARIS
import fr.formation.ousuisje.geo.VILLES_DE_REPLI
import fr.formation.ousuisje.geo.calculHaversine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Location.distanceBetween n'existe que sur l'appareil : test instrumenté. */
@RunWith(AndroidJUnit4::class)
class DistanceAndroidTest {

    @Test
    fun distanceBetween_procheDeHaversine() {
        VILLES_DE_REPLI.forEach { ville ->
            val android = calculDistanceAndroid.metres(ville.latitude, ville.longitude, PARIS.latitude, PARIS.longitude)
            val reference = calculHaversine.metres(ville.latitude, ville.longitude, PARIS.latitude, PARIS.longitude)
            // Ellipsoïde WGS84 contre sphère : écart inférieur à 0,5 %
            assertEquals(ville.nom, reference, android, reference * 0.005f)
        }
    }

    @Test
    fun applicationId_suitLesFlavors() {
        val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        assertTrue(packageName.startsWith("fr.formation.ousuisje"))
        assertEquals(!BuildConfig.SUIVI_CONTINU, packageName.contains(".gratuit"))
        assertEquals(BuildConfig.ENVIRONNEMENT == "dev", packageName.endsWith(".dev"))
    }
}
