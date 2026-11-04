package fr.formation.compteur.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.formation.compteur.etapes.Etape3Remember
import fr.formation.compteur.etapes.Etape4RememberSaveable
import org.junit.Rule
import org.junit.Test

// StateRestorationTester simule une recréation de l'Activity (rotation, mort du processus).
class RestaurationEtatTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun restaurer(contenu: @Composable () -> Unit, actions: () -> Unit) {
        val testeur = StateRestorationTester(composeRule)
        testeur.setContent { MaterialTheme { contenu() } }
        actions()
        testeur.emulateSavedInstanceStateRestore()
    }

    @Test
    fun remember_perdAprèsRecreation() {
        restaurer({ Etape3Remember() }) {
            composeRule.onNodeWithText("+1").performClick()
            composeRule.onNodeWithText("Compteur : 1").assertIsDisplayed()
        }
        composeRule.onNodeWithText("Compteur : 0").assertIsDisplayed()
    }

    @Test
    fun rememberSaveable_conserveAprèsRecreation() {
        restaurer({ Etape4RememberSaveable() }) {
            composeRule.onNodeWithText("+1").performClick()
        }
        composeRule.onNodeWithText("Compteur : 1").assertIsDisplayed()
    }

    @Test
    fun scoreRoute_partageLEtatEtLeConserve() {
        restaurer({ ScoreRoute() }) {
            repeat(3) { composeRule.onNodeWithContentDescription("Ajouter un point : Équipe A").performClick() }
            composeRule.onNodeWithContentDescription("Ajouter un point : Équipe B").performClick()
        }
        composeRule.onNodeWithTag("valeur_Équipe A").assertTextEquals("3")
        composeRule.onNodeWithText("Total : 4").assertIsDisplayed()
        composeRule.onNodeWithText("L'équipe A mène").assertIsDisplayed()
    }

    @Test
    fun scoreStateRoute_conserveGraceAuSaver() {
        restaurer({ ScoreStateRoute() }) {
            composeRule.onNodeWithContentDescription("Ajouter un point : Équipe B").performClick()
        }
        composeRule.onNodeWithTag("valeur_Équipe B").assertTextEquals("1")
        composeRule.onNodeWithText("Réinitialiser").performClick()
        composeRule.onNodeWithText("Total : 0").assertIsDisplayed()
    }
}
