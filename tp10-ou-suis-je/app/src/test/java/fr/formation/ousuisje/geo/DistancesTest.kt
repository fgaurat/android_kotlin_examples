package fr.formation.ousuisje.geo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DistancesTest {

    @Test
    fun haversine_parisLyon_environ392km() {
        val lyon = VILLES_DE_REPLI.first { it.nom == "Lyon" }
        val metres = calculHaversine.metres(lyon.latitude, lyon.longitude, PARIS.latitude, PARIS.longitude)
        assertEquals(392_000f, metres, 3_000f)
    }

    @Test
    fun haversine_memePoint_zero() {
        assertEquals(0f, calculHaversine.metres(PARIS.latitude, PARIS.longitude, PARIS.latitude, PARIS.longitude), 0.01f)
    }

    @Test
    fun formaterDistance_precise() {
        assertEquals("850 m", formaterDistance(849.6f, approximative = false))
        assertEquals("12,3 km", formaterDistance(12_340f, approximative = false))
        assertEquals("392 km", formaterDistance(391_820f, approximative = false))
    }

    @Test
    fun formaterDistance_approximative_arrondiAuKm() {
        assertEquals("≈ 392 km", formaterDistance(391_820f, approximative = true))
        assertEquals("≈ 12 km", formaterDistance(12_340f, approximative = true))
        assertEquals("moins de 3 km", formaterDistance(850f, approximative = true))
    }

    @Test
    fun formaterPrecision_metresOuKilometres() {
        assertEquals("± 12 m", formaterPrecision(12.4f))
        assertEquals("± 3,0 km", formaterPrecision(3_000f))
        assertEquals("inconnue", formaterPrecision(null))
    }

    @Test
    fun formaterCoordonnees_hemispheres() {
        assertEquals("48,85340° N", formaterLatitude(48.8534))
        assertEquals("4,48610° O", formaterLongitude(-4.4861))
        assertEquals("33,86880° S", formaterLatitude(-33.8688))
    }

    @Test
    fun estApproximative_selonPermissionEtPrecision() {
        assertTrue(estApproximative(precisionAccordee = false, precisionMetres = 5f))
        assertTrue(estApproximative(precisionAccordee = true, precisionMetres = 2_500f))
        assertTrue(estApproximative(precisionAccordee = true, precisionMetres = null))
        assertFalse(estApproximative(precisionAccordee = true, precisionMetres = 15f))
    }
}
