package fr.formation.ficheproduit.logique

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class PrixTest {

    @Test
    fun `prix en francais avec virgule et symbole euro`() {
        val prix = formaterPrix(2990, Locale.FRANCE)
        assertTrue(prix, prix.startsWith("29,90"))
        assertTrue(prix, prix.endsWith("€"))
    }

    @Test
    fun `prix en anglais avec point decimal`() {
        assertEquals("€29.90", formaterPrix(2990, Locale.US))
    }

    @Test
    fun `note avec le separateur de la langue`() {
        assertEquals("4,5", formaterNote(4.5f, Locale.FRANCE))
        assertEquals("4.0", formaterNote(4f, Locale.US))
    }
}
