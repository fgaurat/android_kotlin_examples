package fr.formation.panier.bonus

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/** Bonus : le format "%.2f €" dépend de la locale par défaut de l'appareil. */
class FormatPrixTest {

    private val localeInitiale = Locale.getDefault()

    @After
    fun restaurerLocale() = Locale.setDefault(localeInitiale)

    @Test
    fun `le format fourni change de séparateur quand l'appareil est en anglais`() {
        // Reproduit exactement l'expression utilisée par ProduitItem
        Locale.setDefault(Locale.FRANCE)
        assertEquals("12,50 €", "%.2f €".format(12.5))

        Locale.setDefault(Locale.US)
        assertEquals("12.50 €", "%.2f €".format(12.5))
    }

    @Test
    fun `formaterPrix affiche une virgule même quand l'appareil est en anglais`() {
        Locale.setDefault(Locale.US)

        assertEquals("12,50 €", formaterPrix(12.5))
    }

    @Test
    fun `formaterPrix arrondit au centime`() {
        assertEquals("0,01 €", formaterPrix(0.005))
        assertEquals("1234,57 €", formaterPrix(1234.567))
    }
}
