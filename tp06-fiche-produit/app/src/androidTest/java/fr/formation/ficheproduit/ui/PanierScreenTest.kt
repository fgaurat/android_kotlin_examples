package fr.formation.ficheproduit.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.ficheproduit.R
import fr.formation.ficheproduit.donnees.couleursFictives
import fr.formation.ficheproduit.donnees.produitFictif
import fr.formation.ficheproduit.modele.LignePanier
import fr.formation.ficheproduit.modele.Taille
import fr.formation.ficheproduit.ui.panier.PanierScreen
import fr.formation.ficheproduit.ui.theme.FicheProduitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PanierScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexte = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun panier_vide_affiche_un_message() {
        composeRule.setContent {
            FicheProduitTheme {
                PanierScreen(lignes = emptyList(), onQuantiteChange = { _, _ -> }, onCommander = {}, onOngletClick = {})
            }
        }
        composeRule.onNodeWithText(contexte.getString(R.string.panier_vide)).assertIsDisplayed()
    }

    @Test
    fun le_bouton_plus_remonte_la_nouvelle_quantite() {
        val ligne = LignePanier(produitFictif.id, produitFictif.nom, Taille.M, couleursFictives[0], 2990, quantite = 2)
        var nouvelleQuantite = 0
        composeRule.setContent {
            FicheProduitTheme {
                PanierScreen(
                    lignes = listOf(ligne),
                    onQuantiteChange = { _, quantite -> nouvelleQuantite = quantite },
                    onCommander = {},
                    onOngletClick = {},
                )
            }
        }
        composeRule.onNodeWithContentDescription(contexte.getString(R.string.augmenter_quantite, ligne.nom))
            .performClick()
        assertEquals(3, nouvelleQuantite)
    }
}
