package fr.formation.recettes.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.recettes.R
import fr.formation.recettes.logique.DUREE_MAX_MINUTES
import fr.formation.recettes.logique.FormulaireRecette
import fr.formation.recettes.ui.ajout.AjoutRecetteScreen
import fr.formation.recettes.ui.ajout.TAG_BOUTON_ENREGISTRER
import fr.formation.recettes.ui.ajout.TAG_CHAMP_DUREE
import fr.formation.recettes.ui.ajout.TAG_CHAMP_NOM
import fr.formation.recettes.ui.theme.CarnetRecettesTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AjoutRecetteScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexte = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun le_bouton_est_desactive_tant_que_le_formulaire_est_invalide() {
        var enregistre = false
        composeRule.setContent {
            // L'état est porté par le test, comme le ferait AjoutRecetteRoute
            var formulaire by remember { mutableStateOf(FormulaireRecette()) }
            CarnetRecettesTheme {
                AjoutRecetteScreen(
                    formulaire = formulaire,
                    onFormulaireChange = { formulaire = it },
                    onEnregistrer = { enregistre = true },
                )
            }
        }
        val bouton = composeRule.onNodeWithTag(TAG_BOUTON_ENREGISTRER)
        bouton.assertIsNotEnabled()

        composeRule.onNodeWithTag(TAG_CHAMP_NOM).performTextInput("Crêpes")
        bouton.assertIsNotEnabled()

        composeRule.onNodeWithTag(TAG_CHAMP_DUREE).performTextInput("5000")
        composeRule.onNodeWithText(contexte.getString(R.string.erreur_duree_hors_limites, DUREE_MAX_MINUTES))
            .assertExists()
        bouton.assertIsNotEnabled()
    }

    @Test
    fun un_formulaire_valide_peut_etre_enregistre() {
        var enregistre = false
        composeRule.setContent {
            CarnetRecettesTheme {
                AjoutRecetteScreen(
                    formulaire = FormulaireRecette(nom = "Crêpes", duree = "30"),
                    onFormulaireChange = {},
                    onEnregistrer = { enregistre = true },
                )
            }
        }
        composeRule.onNodeWithTag(TAG_BOUTON_ENREGISTRER).assertIsEnabled().performClick()
        assertTrue(enregistre)
    }
}
