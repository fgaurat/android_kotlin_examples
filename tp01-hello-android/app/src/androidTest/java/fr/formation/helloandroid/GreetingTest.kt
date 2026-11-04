package fr.formation.helloandroid

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import fr.formation.helloandroid.ui.theme.HelloAndroidTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests instrumentés : nécessitent un émulateur ou un appareil
 * (./gradlew connectedDebugAndroidTest).
 */
@RunWith(AndroidJUnit4::class)
class GreetingTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun le_package_est_celui_de_l_enonce() {
        val contexte = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("fr.formation.helloandroid", contexte.packageName)
    }

    @Test
    fun greeting_affiche_le_message_de_bienvenue_et_le_nom() {
        composeRule.setContent {
            HelloAndroidTheme { Greeting(name = "Lyon") }
        }

        composeRule.onNodeWithText("Bienvenue sur Android").assertExists()
        composeRule.onNodeWithText("Bonjour Lyon !").assertExists()
    }
}
