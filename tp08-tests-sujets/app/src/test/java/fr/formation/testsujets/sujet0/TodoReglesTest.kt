package fr.formation.testsujets.sujet0

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Étape 0 — Premier test unitaire (JUnit 4)
// - une classe de test, des fonctions annotées @Test ; aucune n'a besoin d'Android
// - le nom entre `backticks` décrit le comportement attendu, en français : c'est lui qu'on lit quand ça casse
// - chaque test suit Arrange / Act / Assert (préparer, agir, vérifier)
// - assertEquals(ATTENDU, obtenu) : l'attendu EN PREMIER, sinon le message d'erreur est trompeur
// À essayer :
//   1. clic sur la flèche verte à côté de « class TodoReglesTest » → 8 tests verts
//   2. casser le code (ex. « count { it.terminee } » dans restantes) → relancer : lire le message rouge
//   3. ajouter un test : un titre de 51 caractères est refusé
// En ligne de commande : ./gradlew testDebugUnitTest --tests "fr.formation.testsujets.sujet0.*"
class TodoReglesTest {

    private val pain = Todo("1", "Acheter du pain")
    private val kotlin = Todo("2", "Réviser Kotlin", terminee = true)
    private val plombier = Todo("3", "Appeler le plombier")

    @Test
    fun `restantes compte les tâches non terminées`() {
        // Arrange
        val todos = listOf(pain, kotlin, plombier)
        // Act
        val resultat = TodoRegles.restantes(todos)
        // Assert
        assertEquals(2, resultat)
    }

    @Test
    fun `restantes vaut 0 pour une liste vide`() {
        assertEquals(0, TodoRegles.restantes(emptyList()))
    }

    @Test
    fun `pourcentageTermine arrondit à l'entier inférieur`() {
        assertEquals(33, TodoRegles.pourcentageTermine(listOf(pain, kotlin, plombier)))   // 1 sur 3
    }

    // Le cas limite qui ferait planter une division par zéro : à tester EN PRIORITÉ
    @Test
    fun `pourcentageTermine vaut 0 pour une liste vide`() {
        assertEquals(0, TodoRegles.pourcentageTermine(emptyList()))
    }

    @Test
    fun `resume accorde au singulier et au pluriel`() {
        assertEquals("Tout est fait", TodoRegles.resume(listOf(kotlin)))
        assertEquals("1 tâche restante", TodoRegles.resume(listOf(pain)))
        assertEquals("2 tâches restantes", TodoRegles.resume(listOf(pain, plombier)))
    }

    @Test
    fun `un titre normal est valide`() {
        assertTrue(TodoRegles.titreValide("Acheter du pain"))
    }

    @Test
    fun `un titre vide ou fait d'espaces est refusé`() {
        assertFalse(TodoRegles.titreValide(""))
        assertFalse(TodoRegles.titreValide("   "))
    }

    @Test
    fun `un titre de 50 caractères pile est accepté`() {
        assertTrue(TodoRegles.titreValide("a".repeat(50)))
    }
}
