package fr.formation.recettes.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.recettes.R
import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.ui.liste.RecettesScreen
import fr.formation.recettes.ui.theme.CarnetRecettesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests de l'écran SANS état : on lui passe des données et on vérifie les événements remontés. */
@RunWith(AndroidJUnit4::class)
class RecettesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexte = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun affiche_les_recettes_et_remonte_le_clic() {
        var cliquee: String? = null
        composeRule.setContent {
            CarnetRecettesTheme {
                RecettesScreen(
                    recettes = recettesFictives,
                    recherche = "",
                    onRechercheChange = {},
                    categoriesActives = emptySet(),
                    onCategorieClick = {},
                    onRecetteClick = { cliquee = it.id },
                    onFavoriChange = { _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText("Velouté de potimarron").assertIsDisplayed().performClick()
        assertEquals("1", cliquee)
    }

    @Test
    fun la_saisie_et_les_filtres_remontent_les_evenements() {
        var recherche = ""
        var categorie: Categorie? = null
        composeRule.setContent {
            // Le champ est contrôlé : comme dans l'application, l'état est porté par l'appelant
            var texte by remember { mutableStateOf("") }
            CarnetRecettesTheme {
                RecettesScreen(
                    recettes = recettesFictives,
                    recherche = texte,
                    onRechercheChange = { texte = it; recherche = it },
                    categoriesActives = emptySet(),
                    onCategorieClick = { categorie = it },
                    onRecetteClick = {},
                    onFavoriChange = { _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText(contexte.getString(R.string.rechercher_recette)).performTextInput("tarte")
        composeRule.onNodeWithText(contexte.getString(R.string.categorie_dessert)).performClick()

        assertEquals("tarte", recherche)
        assertEquals(Categorie.Dessert, categorie)
    }

    @Test
    fun liste_vide_affiche_un_message() {
        composeRule.setContent {
            CarnetRecettesTheme {
                RecettesScreen(
                    recettes = emptyList(),
                    recherche = "homard",
                    onRechercheChange = {},
                    categoriesActives = emptySet(),
                    onCategorieClick = {},
                    onRecetteClick = {},
                    onFavoriChange = { _, _ -> },
                )
            }
        }
        composeRule.onNodeWithText(contexte.getString(R.string.aucune_recette)).assertIsDisplayed()
    }

    @Test
    fun parcours_complet_filtrer_puis_ouvrir_le_detail() {
        composeRule.setContent { CarnetRecettesTheme { CarnetRecettesApp() } }

        composeRule.onNodeWithText(contexte.getString(R.string.categorie_dessert)).performClick().assertIsSelected()
        composeRule.onNodeWithText("Velouté de potimarron").assertDoesNotExist()
        composeRule.onNodeWithText("Mousse au chocolat").performClick()
        composeRule.onNodeWithText(contexte.getString(R.string.ingredients)).assertIsDisplayed()
    }

    @Test
    fun le_chip_de_categorie_reflete_son_etat() {
        composeRule.setContent {
            CarnetRecettesTheme {
                RecettesScreen(
                    recettes = recettesFictives,
                    recherche = "",
                    onRechercheChange = {},
                    categoriesActives = setOf(Categorie.Plat),
                    onCategorieClick = {},
                    onRecetteClick = {},
                    onFavoriChange = { _, _ -> },
                )
            }
        }
        // FilterChip expose un état « sélectionné » (pas « coché »)
        composeRule.onNodeWithText(contexte.getString(R.string.categorie_plat)).assertIsSelected()
        composeRule.onNodeWithText(contexte.getString(R.string.categorie_entree)).assertIsNotSelected()
    }
}
