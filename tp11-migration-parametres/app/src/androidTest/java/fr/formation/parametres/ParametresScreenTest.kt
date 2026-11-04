package fr.formation.parametres

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import fr.formation.parametres.ui.theme.ParametresTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Étape 6 : test UI du composable sans état (noms en camelCase dans androidTest/). */
class ParametresScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clicSurFahrenheitRemonteLUniteEtLaSelectionne() {
        var uniteChoisie: Unite? = null
        composeRule.setContent {
            // L'état est tenu par le test, comme le ferait l'Activity avec le ViewModel
            var etat by mutableStateOf(ParametresUiState())
            ParametresTheme {
                ParametresScreen(
                    etat = etat,
                    onUniteChoisie = { uniteChoisie = it; etat = etat.copy(unite = it) },
                    onThemeSombreChange = {},
                )
            }
        }
        composeRule.onNodeWithText("°C", substring = true).assertIsSelected()

        composeRule.onNodeWithText("°F", substring = true).performClick()

        assertEquals(Unite.FAHRENHEIT, uniteChoisie)
        composeRule.onNodeWithText("°F", substring = true).assertIsSelected()
        composeRule.onNodeWithText("°C", substring = true).assertIsNotSelected()
    }

    @Test
    fun lesLignesDUniteOntLeRoleRadioButton() {
        composeRule.setContent {
            ParametresScreen(ParametresUiState(), onUniteChoisie = {}, onThemeSombreChange = {})
        }

        composeRule.onNodeWithText("°F", substring = true)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
    }

    @Test
    fun clicSurLaLigneThemeSombreRemonteTrue() {
        var themeSombre: Boolean? = null
        composeRule.setContent {
            ParametresScreen(
                ParametresUiState(themeSombre = false),
                onUniteChoisie = {},
                onThemeSombreChange = { themeSombre = it },
            )
        }
        composeRule.onNodeWithText("Thème sombre").assertIsOff()

        composeRule.onNodeWithText("Thème sombre").performClick()

        assertEquals(true, themeSombre)
    }

    @Test
    fun leSwitchRefleteLEtat() {
        composeRule.setContent {
            ParametresScreen(
                ParametresUiState(themeSombre = true),
                onUniteChoisie = {},
                onThemeSombreChange = {},
            )
        }

        composeRule.onNodeWithText("Thème sombre").assertIsOn()
    }
}
