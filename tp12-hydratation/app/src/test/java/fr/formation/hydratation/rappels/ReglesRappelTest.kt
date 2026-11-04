package fr.formation.hydratation.rappels

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class ReglesRappelTest {

    private val zone = ZoneId.of("Europe/Paris")

    /** Horloge figée : le test ne dépend ni de l'heure ni du fuseau de la machine. */
    private fun horlogeA(heure: Int, minute: Int = 0): Clock =
        Clock.fixed(LocalDateTime.of(2026, 11, 1, heure, minute).atZone(zone).toInstant(), zone)

    private fun autorise(heure: Int, minute: Int = 0) = ReglesRappel(horlogeA(heure, minute)).rappelAutorise()

    @Test
    fun journee_rappelAutorise() {
        assertEquals(true, autorise(8, 0))     // borne de début incluse
        assertEquals(true, autorise(12, 30))
        assertEquals(true, autorise(21, 59))
    }

    @Test
    fun nuit_pasDeRappel() {
        assertEquals(false, autorise(22, 0))   // borne de fin exclue
        assertEquals(false, autorise(23, 45))
        assertEquals(false, autorise(0, 0))
        assertEquals(false, autorise(3, 0))
        assertEquals(false, autorise(7, 59))
    }
}
