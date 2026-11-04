package fr.formation.todos.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GrandeListeTest {

    @Test
    fun statistiques_sur_100_000_taches() {
        val stats = genererTodos(100_000).statistiques()
        assertEquals(100_000, stats.total)
        assertEquals(33_333, stats.terminees)          // un multiple de 3 sur trois
        assertEquals(10, stats.parUtilisateur.size)
        assertEquals(10_000, stats.parUtilisateur.getValue(1))
    }

    @Test
    fun premieresAFaire_s_arrete_des_qu_elle_a_ses_resultats() {
        var generees = 0
        val todos = genererTodos(100_000).onEach { generees++ }

        val titres = todos.premieresAFaire(utilisateurId = 1)

        assertEquals(listOf("Tâche n°10", "Tâche n°20", "Tâche n°40", "Tâche n°50", "Tâche n°70"), titres)
        assertEquals(70, generees)   // paresse : 70 tâches créées, pas 100 000
    }
}
