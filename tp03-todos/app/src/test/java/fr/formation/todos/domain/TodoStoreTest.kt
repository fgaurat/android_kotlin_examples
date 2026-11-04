package fr.formation.todos.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TodoStoreTest {

    @Before
    fun setUp() {
        // TodoStore est un object partagé : on repart toujours du même état
        TodoStore.reinitialiser()
    }

    @Test
    fun la_liste_contient_10_taches_dont_5_terminees() {
        assertEquals(10, TodoStore.todos.size)
        assertEquals(5, TodoStore.todos.terminees().size)
    }

    @Test
    fun les_identifiants_sont_uniques() {
        assertEquals(TodoStore.todos.size, TodoStore.todos.map { it.id }.toSet().size)
    }

    @Test
    fun terminer_une_tache_a_faire_renvoie_Succes_et_la_marque_terminee() {
        val resultat = TodoStore.terminer("1")

        assertTrue(resultat is ResultatAction.Succes)
        resultat as ResultatAction.Succes
        assertTrue(resultat.todo.terminee)
        assertTrue(TodoStore.todos.indexParId().getValue("1").terminee)
        assertEquals(6, TodoStore.todos.terminees().size)
    }

    @Test
    fun terminer_une_tache_deja_terminee_renvoie_DejaTerminee() {
        assertEquals(ResultatAction.DejaTerminee, TodoStore.terminer("4"))
    }

    @Test
    fun terminer_un_id_inconnu_renvoie_Introuvable() {
        assertEquals(ResultatAction.Introuvable, TodoStore.terminer("999"))
    }

    @Test
    fun rouvrir_remet_la_tache_a_faire() {
        assertTrue(TodoStore.rouvrir("4"))
        assertFalse(TodoStore.todos.indexParId().getValue("4").terminee)
        assertFalse(TodoStore.rouvrir("4"))   // déjà à faire
    }

    @Test
    fun filtrer_avec_une_lambda_et_avec_une_reference() {
        assertEquals(5, TodoStore.filtrer { it.utilisateurId == 1 }.size)
        assertEquals(listOf("21", "22", "25", "41", "44"), TodoStore.filtrer(::aUnTitreLong).map { it.id })
    }

    @Test
    fun la_liste_exposee_est_une_copie() {
        val copie = TodoStore.todos
        TodoStore.terminer("1")
        assertFalse(copie.first { it.id == "1" }.terminee)   // la copie n'a pas bougé
    }

    @Test
    fun ajouter_prend_l_id_suivant_et_supprimer_le_retire() {
        val todo = TodoStore.ajouter("Préparer le TP", utilisateurId = 1)
        assertEquals("45", todo.id)
        assertEquals(11, TodoStore.todos.size)

        assertTrue(TodoStore.supprimer("45"))
        assertFalse(TodoStore.supprimer("45"))
        assertEquals(10, TodoStore.todos.size)
    }

    @Test
    fun les_messages_couvrent_tous_les_cas() {
        val todo = TodoStore.todos.first()
        assertEquals("Bravo, « ${todo.titre} » est terminée", ResultatAction.Succes(todo).message())
        assertEquals("Cette tâche était déjà terminée", ResultatAction.DejaTerminee.message())
        assertEquals("Aucune tâche ne porte cet identifiant", ResultatAction.Introuvable.message())
    }
}
