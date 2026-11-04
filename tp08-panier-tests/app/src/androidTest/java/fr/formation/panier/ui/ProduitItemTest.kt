package fr.formation.panier.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import fr.formation.panier.domain.Produit
import fr.formation.panier.ui.theme.PanierTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale

/** Étape 7 : test UI de ProduitItem (noms en camelCase dans androidTest/). */
class ProduitItemTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val clavier = Produit("1", "Clavier mécanique", 79.9)
    private val localeInitiale = Locale.getDefault()

    // Le prix fourni dépend de la locale (voir le bonus) : on la fixe pour un test reproductible
    @Before
    fun forcerLocaleFrancaise() = Locale.setDefault(Locale.FRANCE)

    @After
    fun restaurerLocale() = Locale.setDefault(localeInitiale)

    @Test
    fun afficheLeNomEtLePrix() {
        composeRule.setContent {
            PanierTheme { ProduitItem(clavier, onAjouter = {}) }
        }

        composeRule.onNodeWithText("Clavier mécanique").assertIsDisplayed()
        composeRule.onNodeWithText("79,90 €").assertIsDisplayed()
    }

    @Test
    fun leClicSurAjouterAppelleOnAjouter() {
        var nombreClics = 0
        composeRule.setContent {
            PanierTheme { ProduitItem(clavier, onAjouter = { nombreClics++ }) }
        }

        composeRule.onNodeWithText("Ajouter").performClick()

        assertEquals(1, nombreClics)
    }

    @Test
    fun afficheLArbreSemantique() {
        composeRule.setContent {
            PanierTheme { ProduitItem(clavier, onAjouter = {}) }
        }

        // Visible dans Logcat, filtre « ARBRE »
        composeRule.onRoot().printToLog("ARBRE")
        // Arbre fusionné par défaut ; useUnmergedTree = true montre le Text interne du bouton
        composeRule.onRoot(useUnmergedTree = true).printToLog("ARBRE_NON_FUSIONNE")
    }
}
