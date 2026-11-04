package fr.formation.panier.bonus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import fr.formation.panier.domain.Produit
import fr.formation.panier.ui.ProduitItem
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.util.Locale

/** Bonus : prix affiché quand l'appareil est en anglais. */
class ProduitItemLocaleTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val stylo = Produit("1", "Stylo", 12.5)
    private val localeInitiale = Locale.getDefault()

    @After
    fun restaurerLocale() = Locale.setDefault(localeInitiale)

    @Test
    fun produitItemFourniAfficheUnPointEnAnglais() {
        Locale.setDefault(Locale.US)

        composeRule.setContent { ProduitItem(stylo, onAjouter = {}) }

        // Le défaut : le séparateur décimal suit la langue de l'appareil
        composeRule.onNodeWithText("12.50 €").assertIsDisplayed()
    }

    @Test
    fun produitItemCorrigeAfficheUneVirguleEnAnglais() {
        Locale.setDefault(Locale.US)

        composeRule.setContent { ProduitItemCorrige(stylo, onAjouter = {}) }

        composeRule.onNodeWithText("12,50 €").assertIsDisplayed()
    }
}
