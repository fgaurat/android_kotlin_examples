package fr.formation.compteur.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Compteur est sans état : on lui donne une valeur et on vérifie les événements remontés.
class CompteurTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun afficheLaValeurRecue() {
        composeRule.setContent {
            MaterialTheme { Compteur(libelle = "Équipe A", valeur = 7, onIncrementer = {}, onDecrementer = {}) }
        }

        composeRule.onNodeWithTag("valeur_Équipe A").assertTextEquals("7")
    }

    @Test
    fun leClicRemonteLEvenementSansChangerLaValeur() {
        var clics = 0
        composeRule.setContent {
            MaterialTheme { Compteur(libelle = "Équipe A", valeur = 7, onIncrementer = { clics++ }, onDecrementer = {}) }
        }

        composeRule.onNodeWithContentDescription("Ajouter un point : Équipe A").performClick()

        assertEquals(1, clics)
        composeRule.onNodeWithTag("valeur_Équipe A").assertTextEquals("7")   // l'appelant décide
    }

    @Test
    fun boutonsDesactivesAuxBornes() {
        composeRule.setContent {
            MaterialTheme {
                Compteur(libelle = "Équipe A", valeur = 0, onIncrementer = {}, onDecrementer = {}, max = 0)
            }
        }

        composeRule.onNodeWithContentDescription("Retirer un point : Équipe A").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Ajouter un point : Équipe A").assertIsNotEnabled()
    }

    @Test
    fun boutonsActifsEntreLesBornes() {
        composeRule.setContent {
            MaterialTheme { Compteur(libelle = "Équipe A", valeur = 5, onIncrementer = {}, onDecrementer = {}) }
        }

        composeRule.onNodeWithContentDescription("Retirer un point : Équipe A").assertIsEnabled()
        composeRule.onNodeWithContentDescription("Ajouter un point : Équipe A").assertIsEnabled()
    }
}
