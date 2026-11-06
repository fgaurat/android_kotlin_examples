package fr.formation.testsujets.sujet6

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Étape 6 — Test UI Compose, sur l'émulateur
// - createComposeRule() : une Activity vide (ui-test-manifest) dans laquelle on pose UN composable
// - TROUVER un nœud de l'arbre sémantique : onNodeWithText, onNodeWithContentDescription, onNodeWithTag
// - AGIR : performClick, performTextInput… ; VÉRIFIER : assertIsDisplayed, assertIsOn / assertIsOff
// - synchronisation automatique : la règle attend que Compose soit au repos avant chaque vérification
// - un faux état + des callbacks qui notent ce qu'ils reçoivent : ni ViewModel, ni réseau
// À essayer : lancer depuis la flèche verte (un émulateur doit tourner) ; dans Logcat, filtre « ARBRE » :
//   l'arbre sémantique imprimé par printToLog. Changer « Réessayer » en « Recommencer » dans l'écran :
//   le test casse, comme pour un utilisateur qui ne trouverait plus le bouton.
// En ligne de commande : ./gradlew connectedDebugAndroidTest
class TodosEcranTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val pain = Todo("1", "Acheter du pain")
    private val kotlin = Todo("2", "Réviser Kotlin", terminee = true)

    private fun afficher(
        etat: TodosUiState,
        onBasculer: (Todo) -> Unit = {},
        onSupprimer: (Todo) -> Unit = {},
        onReessayer: () -> Unit = {},
    ) {
        composeRule.setContent {
            MaterialTheme { TodosEcran(etat, onBasculer, onSupprimer, onReessayer) }
        }
    }

    @Test
    fun chargement_afficheLIndicateur() {
        afficher(TodosUiState.Chargement)

        composeRule.onNodeWithTag(TAG_CHARGEMENT).assertIsDisplayed()
    }

    @Test
    fun succes_afficheLesTachesEtLeurEtat() {
        afficher(TodosUiState.Succes(listOf(pain, kotlin)))
        composeRule.onRoot().printToLog("ARBRE")             // pour voir l'arbre sémantique dans Logcat

        composeRule.onNodeWithText("1 à faire").assertIsDisplayed()
        composeRule.onNodeWithText("Acheter du pain").assertIsOff()     // texte et case fusionnés : un seul nœud
        composeRule.onNodeWithText("Réviser Kotlin").assertIsOn()
    }

    @Test
    fun toucherUneLigne_remonteLaTache() {
        var basculee: Todo? = null
        afficher(TodosUiState.Succes(listOf(pain, kotlin)), onBasculer = { basculee = it })

        composeRule.onNodeWithText("Acheter du pain").performClick()

        assertEquals(pain, basculee)
    }

    @Test
    fun corbeille_remonteLaBonneTache() {
        var supprimee: Todo? = null
        afficher(TodosUiState.Succes(listOf(pain, kotlin)), onSupprimer = { supprimee = it })

        composeRule.onNodeWithContentDescription("Supprimer Réviser Kotlin").performClick()

        assertEquals(kotlin, supprimee)
    }

    @Test
    fun erreur_afficheLeMessageEtReessayer() {
        var essais = 0
        afficher(TodosUiState.Erreur("Serveur injoignable"), onReessayer = { essais++ })

        composeRule.onNodeWithText("Serveur injoignable").assertIsDisplayed()
        composeRule.onNodeWithText("Réessayer").performClick()

        assertEquals(1, essais)
    }
}
