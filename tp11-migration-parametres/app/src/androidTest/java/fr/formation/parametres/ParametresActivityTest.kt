package fr.formation.parametres

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Étape 6 : l'écran complet, branché sur le vrai ParametresViewModel. */
class ParametresActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ParametresActivity>()

    private fun viewModel(): ParametresViewModel {
        lateinit var vm: ParametresViewModel
        composeRule.activityRule.scenario.onActivity {
            vm = ViewModelProvider(it)[ParametresViewModel::class.java]
        }
        return vm
    }

    @Test
    fun clicSurFahrenheitMetAJourLeViewModel() {
        composeRule.onNodeWithText("°F", substring = true).performClick()

        composeRule.onNodeWithText("°F", substring = true).assertIsSelected()
        composeRule.onNodeWithText("°C", substring = true).assertIsNotSelected()
        assertEquals(Unite.FAHRENHEIT, viewModel().etat.value.unite)
    }

    @Test
    fun activerLeThemeSombreMetAJourLeViewModel() {
        composeRule.onNodeWithText("Thème sombre").performClick()

        composeRule.onNodeWithText("Thème sombre").assertIsOn()
        assertTrue(viewModel().etat.value.themeSombre)
    }

    @Test
    fun lUniteChoisieSurvitALaRotation() {
        composeRule.onNodeWithText("°F", substring = true).performClick()

        // Recrée l'Activity comme une rotation : le ViewModel, lui, survit
        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("°F", substring = true).assertIsSelected()
    }
}
