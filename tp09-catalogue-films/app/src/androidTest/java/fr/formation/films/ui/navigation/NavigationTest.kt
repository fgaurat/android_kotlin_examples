package fr.formation.films.ui.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.formation.films.data.AppContainer
import fr.formation.films.ui.theme.CatalogueFilmsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test instrumenté (émulateur ou appareil) : on injecte un TestNavHostController
 * dans FilmsApp, on clique dans l'interface et on vérifie la pile de navigation.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var container: AppContainer

    @Before
    fun setUp() {
        container = AppContainer()
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            CatalogueFilmsTheme {
                FilmsApp(container = container, navController = navController)
            }
        }
    }

    private fun destinationCourante() = navController.currentBackStackEntry?.destination

    @Test
    fun demarre_sur_la_liste_des_films() {
        assertTrue(destinationCourante()?.hasRoute<Films>() == true)
        composeTestRule.onNodeWithText("Catalogue").assertIsDisplayed()
    }

    @Test
    fun clic_sur_un_film_ouvre_son_detail_puis_retour() {
        composeTestRule.onNodeWithText("La Haine").performClick()

        val entree = navController.currentBackStackEntry!!
        assertTrue(entree.destination.hasRoute<DetailFilm>())
        assertEquals(3, entree.toRoute<DetailFilm>().id)

        composeTestRule.onNodeWithContentDescription("Retour").performClick()
        assertTrue(destinationCourante()?.hasRoute<Films>() == true)
    }

    @Test
    fun favori_ajoute_depuis_le_detail_apparait_dans_l_onglet_favoris() {
        composeTestRule.onNodeWithText("Parasite").performClick()
        composeTestRule.onNodeWithContentDescription("Ajouter aux favoris").performClick()
        composeTestRule.onNodeWithContentDescription("Retour").performClick()

        composeTestRule.onNodeWithText("Favoris").performClick()
        assertTrue(destinationCourante()?.hasRoute<Favoris>() == true)
        composeTestRule.onNodeWithText("Parasite").assertIsDisplayed()
    }

    @Test
    fun a_propos_garde_l_onglet_parametres_selectionne_et_l_etat_est_restaure() {
        composeTestRule.onNodeWithText("Paramètres").performClick()
        composeTestRule.onNodeWithText("À propos").performClick()
        assertTrue(destinationCourante()?.hasRoute<APropos>() == true)
        // hierarchy : APropos appartient à ParametresGraphe
        composeTestRule.onNodeWithText("Paramètres").assertIsSelected()

        // Aller sur Films puis revenir sur Paramètres : restoreState rouvre APropos
        composeTestRule.onNodeWithText("Films").performClick()
        composeTestRule.onNodeWithText("Paramètres").performClick()
        assertTrue(destinationCourante()?.hasRoute<APropos>() == true)
    }

    @Test
    fun re_clic_sur_l_onglet_courant_n_empile_pas_de_doublon() {
        val tailleAvant = navController.currentBackStack.value.size
        composeTestRule.onNodeWithText("Films").performClick()
        composeTestRule.onNodeWithText("Films").performClick()
        assertEquals(tailleAvant, navController.currentBackStack.value.size)
    }
}
