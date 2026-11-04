package fr.formation.taches

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import fr.formation.taches.ui.taches.Filtre
import fr.formation.taches.ui.taches.TachesScreen
import fr.formation.taches.ui.taches.TachesUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** TachesScreen ne dépend que d'un état et de callbacks : on le teste sans ViewModel. */
@RunWith(AndroidJUnit4::class)
class TachesScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun le_formulaire_remonte_le_titre_et_la_priorite() {
        var ajout: Pair<String, Priorite>? = null
        composeRule.setContent {
            TachesScreen(
                etat = TachesUiState(taches = listOf(Tache(1, "Lire"))),
                recherche = "", restantes = 1,
                onRechercheChange = {}, onFiltreChange = {},
                onAjouter = { titre, priorite -> ajout = titre to priorite },
                onBasculer = {}, onSupprimer = {},
            )
        }

        composeRule.onNodeWithText("Ajouter").assertIsNotEnabled()        // titre vide
        composeRule.onNodeWithTag("champTitre").performTextInput("Réviser")
        composeRule.onNodeWithText("Haute").performClick()
        composeRule.onNodeWithText("Ajouter").performClick()

        assertEquals("Réviser" to Priorite.Haute, ajout)
    }

    @Test
    fun un_clic_sur_un_filtre_remonte_l_evenement() {
        var filtre: Filtre? = null
        composeRule.setContent {
            TachesScreen(
                etat = TachesUiState(), recherche = "", restantes = 0,
                onRechercheChange = {}, onFiltreChange = { filtre = it },
                onAjouter = { _, _ -> }, onBasculer = {}, onSupprimer = {},
            )
        }

        composeRule.onNodeWithText("Faites").performClick()

        assertEquals(Filtre.Faites, filtre)
    }
}
