package fr.formation.iaenaction.todo

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test JUnit de resume() (étape 3) : généré par l'IA (« Generate Unit Tests »), puis relu et complété.
 *
 * À la relecture, le test généré ne vérifiait que « 3 tâches, 1 terminée » : il passait aussi avec la
 * version fautive pour ce cas-là. Ajouts : liste vide, une seule tâche, 0 / 1 / plusieurs terminées.
 */
class ResumeTest {

    private fun todo(id: Int, terminee: Boolean) = Todo(1, id.toString(), "Tâche $id", terminee)

    @Test
    fun liste_vide_au_singulier() {
        assertEquals("0 tâche, 0 terminée", emptyList<Todo>().resume())
    }

    @Test
    fun une_tache_non_terminee() {
        assertEquals("1 tâche, 0 terminée", listOf(todo(1, false)).resume())
    }

    @Test
    fun une_tache_terminee() {
        assertEquals("1 tâche, 1 terminée", listOf(todo(1, true)).resume())
    }

    @Test
    fun exemple_du_support_trois_taches_une_terminee() {
        assertEquals(
            "3 tâches, 1 terminée",
            listOf(todo(1, true), todo(2, false), todo(3, false)).resume(),
        )
    }

    @Test
    fun plusieurs_terminees_au_pluriel() {
        assertEquals(
            "3 tâches, 2 terminées",
            listOf(todo(1, true), todo(2, true), todo(3, false)).resume(),
        )
    }

    @Test
    fun toutes_terminees() {
        assertEquals("2 tâches, 2 terminées", listOf(todo(1, true), todo(2, true)).resume())
    }

    @Test
    fun accorder_pluriel_a_partir_de_deux() {
        assertEquals("0 tâche", accorder(0, "tâche", "tâches"))
        assertEquals("1 tâche", accorder(1, "tâche", "tâches"))
        assertEquals("2 tâches", accorder(2, "tâche", "tâches"))
        assertEquals("200 tâches", accorder(200, "tâche", "tâches"))
    }
}
