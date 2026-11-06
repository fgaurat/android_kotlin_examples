package fr.formation.testsujets.sujet1

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

// Étape 1 — Tester une exception : assertThrows
// - assertThrows(Type::class.java) { … } échoue si le bloc NE lève PAS l'exception attendue
// - il RENVOIE l'exception : on peut vérifier son message
class NouvelleTodoTest {

    @Test
    fun `nouvelleTodo retire les espaces autour du titre`() {
        assertEquals("Acheter du pain", nouvelleTodo("1", "  Acheter du pain ").titre)
    }

    @Test
    fun `nouvelleTodo refuse un titre vide`() {
        val erreur = assertThrows(IllegalArgumentException::class.java) {
            nouvelleTodo("1", "   ")
        }
        assertEquals("Le titre est obligatoire", erreur.message)
    }
}
