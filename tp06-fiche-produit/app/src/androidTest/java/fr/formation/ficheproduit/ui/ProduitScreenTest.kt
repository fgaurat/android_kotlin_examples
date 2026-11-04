package fr.formation.ficheproduit.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.ficheproduit.R
import fr.formation.ficheproduit.donnees.produitFictif
import fr.formation.ficheproduit.modele.Taille
import fr.formation.ficheproduit.ui.produit.ActionsProduit
import fr.formation.ficheproduit.ui.produit.ProduitScreen
import fr.formation.ficheproduit.ui.produit.SelectionProduit
import fr.formation.ficheproduit.ui.produit.TAG_BOUTON_AJOUTER
import fr.formation.ficheproduit.ui.theme.FicheProduitTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProduitScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexte = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun ajout_impossible_sans_taille_et_clic_sur_taille_remonte() {
        var tailleCliquee: Taille? = null
        composeRule.setContent {
            FicheProduitTheme {
                ProduitScreen(
                    produit = produitFictif,
                    selection = SelectionProduit(),
                    nombreArticlesPanier = 0,
                    actions = ActionsProduit(onTailleChange = { tailleCliquee = it }),
                    onOngletClick = {},
                )
            }
        }
        composeRule.onNodeWithTag(TAG_BOUTON_AJOUTER).assertIsNotEnabled()
        composeRule.onNodeWithText("M").performScrollTo().performClick()
        assertEquals(Taille.M, tailleCliquee)
    }

    @Test
    fun la_taille_epuisee_est_desactivee() {
        composeRule.setContent {
            FicheProduitTheme {
                ProduitScreen(
                    produit = produitFictif,
                    selection = SelectionProduit(taille = Taille.M),
                    nombreArticlesPanier = 0,
                    actions = ActionsProduit(),
                    onOngletClick = {},
                )
            }
        }
        composeRule.onNodeWithText("XL").performScrollTo().assertIsNotEnabled()
        composeRule.onNodeWithText("M").assertIsSelected()
        composeRule.onNodeWithTag(TAG_BOUTON_AJOUTER).assertIsEnabled()
    }

    @Test
    fun la_note_est_lue_en_une_phrase() {
        composeRule.setContent { FicheProduitTheme { FicheProduitApp() } }
        val avis = contexte.resources.getQuantityString(R.plurals.nb_avis, 128, 128)
        val locale = contexte.resources.configuration.locales[0]
        val note = fr.formation.ficheproduit.logique.formaterNote(4.5f, locale)
        composeRule.onNodeWithContentDescription(contexte.getString(R.string.note_description, note, avis))
            .assertExists()
    }

    @Test
    fun parcours_choisir_une_taille_puis_ajouter_met_a_jour_le_badge() {
        composeRule.setContent { FicheProduitTheme { FicheProduitApp() } }

        composeRule.onNodeWithText("L").performScrollTo().performClick()
        composeRule.onNodeWithTag(TAG_BOUTON_AJOUTER).assertIsEnabled().performClick()

        val badge = contexte.resources.getQuantityString(R.plurals.articles_panier, 1, 1)
        composeRule.onNodeWithContentDescription(badge, substring = true).assertIsDisplayed()
    }

    @Test
    fun la_description_se_deplie() {
        composeRule.setContent { FicheProduitTheme { FicheProduitApp() } }
        val entete = composeRule.onNode(hasStateDescription(contexte.getString(R.string.etat_replie)))
        entete.performScrollTo().performClick()
        composeRule.onNode(hasStateDescription(contexte.getString(R.string.etat_deplie))).assertExists()
    }
}
