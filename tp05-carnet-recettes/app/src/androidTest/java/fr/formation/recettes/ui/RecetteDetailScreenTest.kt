package fr.formation.recettes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.recettes.R
import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.ui.detail.RecetteDetailRoute
import fr.formation.recettes.ui.theme.CarnetRecettesTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecetteDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexte = InstrumentationRegistry.getInstrumentation().targetContext
    private val recette = recettesFictives[5]   // Mousse au chocolat

    @Test
    fun cocher_un_ingredient_par_toute_la_ligne() {
        composeRule.setContent { CarnetRecettesTheme { RecetteDetailRoute(recette = recette, onFavoriChange = {}) } }

        val ligne = composeRule.onNodeWithText(recette.ingredients[0])
        ligne.assertIsOff()
        ligne.performClick()
        ligne.assertIsOn()
    }

    @Test
    fun les_etapes_apparaissent_apres_le_clic() {
        composeRule.setContent { CarnetRecettesTheme { RecetteDetailRoute(recette = recette, onFavoriChange = {}) } }
        val premiereEtape = contexte.getString(R.string.etape_description, 1, recette.etapes[0])

        composeRule.onNodeWithContentDescription(premiereEtape).assertDoesNotExist()
        composeRule.onNodeWithText(contexte.getString(R.string.afficher_etapes)).performClick()
        composeRule.onNodeWithContentDescription(premiereEtape).assertIsDisplayed()
    }
}
