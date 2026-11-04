package fr.formation.gestiontaches.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TodoStoreTest {

    // TodoStore est un object : son état survit d'un test à l'autre, on le remet à zéro.
    @Before
    fun setUp() {
        TodoStore.reinitialiser()
    }

    @Test
    fun le_store_contient_10_taches_de_3_utilisateurs_dont_5_terminees() {
        val todos = TodoStore.todos
        assertEquals(10, todos.size)
        assertEquals(setOf(1, 2, 3), todos.map { it.utilisateurId }.toSet())
        assertEquals(5, todos.count { it.terminee })
        assertEquals("les id sont uniques", todos.size, todos.map { it.id }.distinct().size)
    }

    @Test
    fun le_resume_du_store_correspond_a_l_enonce() {
        assertEquals("10 tâches, 5 terminées (50 %)", TodoStore.todos.resume())
    }

    @Test
    fun terminer_une_tache_a_faire_renvoie_Succes_avec_une_copie() {
        val avant = TodoStore.todos.first { it.id == "1" }

        val resultat = TodoStore.terminer("1")

        assertTrue(resultat is ResultatAction.Succes)
        val terminee = (resultat as ResultatAction.Succes).todo
        assertEquals(avant.copy(terminee = true), terminee)
        assertFalse("l'instance d'origine n'est pas modifiée", avant.terminee)
        assertTrue(TodoStore.todos.first { it.id == "1" }.terminee)
        assertEquals("10 tâches, 6 terminées (60 %)", TodoStore.todos.resume())
    }

    @Test
    fun terminer_une_tache_deja_terminee_renvoie_DejaTerminee() {
        assertEquals(ResultatAction.DejaTerminee, TodoStore.terminer("4"))
    }

    @Test
    fun terminer_deux_fois_la_meme_tache() {
        assertTrue(TodoStore.terminer("2") is ResultatAction.Succes)
        assertEquals(ResultatAction.DejaTerminee, TodoStore.terminer("2"))
    }

    @Test
    fun terminer_un_id_inconnu_renvoie_Introuvable() {
        assertEquals(ResultatAction.Introuvable, TodoStore.terminer("999"))
        assertEquals(5, TodoStore.todos.count { it.terminee })
    }

    @Test
    fun filtrer_avec_une_lambda() {
        val todos = TodoStore.filtrer { it.utilisateurId == 2 }
        assertEquals(listOf("21", "22", "26"), todos.map { it.id })
    }

    @Test
    fun filtrer_avec_une_reference_de_fonction() {
        assertEquals(listOf("1", "2", "21", "41", "45"), TodoStore.filtrer(::estAFaire).map { it.id })
    }

    @Test
    fun filtrer_avec_une_reference_de_propriete() {
        assertEquals(listOf("4", "8", "22", "26", "43"), TodoStore.filtrer(Todo::terminee).map { it.id })
    }

    @Test
    fun ajouter_cree_une_tache_a_faire_avec_un_nouvel_id() {
        val todo = TodoStore.ajouter("  préparer le TP  ", utilisateurId = 2)

        assertEquals(Todo("46", "préparer le TP", terminee = false, utilisateurId = 2), todo)
        assertEquals(11, TodoStore.todos.size)
        assertEquals(todo, TodoStore.todos.last())
        assertEquals("47", TodoStore.ajouter("autre", utilisateurId = 1).id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun ajouter_refuse_un_titre_vide() {
        TodoStore.ajouter("   ", utilisateurId = 1)
    }

    @Test
    fun supprimer_une_tache_existante_puis_inconnue() {
        assertTrue(TodoStore.supprimer("21"))
        assertEquals(9, TodoStore.todos.size)
        assertFalse(TodoStore.todos.any { it.id == "21" })
        assertFalse(TodoStore.supprimer("21"))
        assertEquals(ResultatAction.Introuvable, TodoStore.terminer("21"))
    }

    @Test
    fun la_liste_exposee_est_une_copie_qui_ne_modifie_pas_le_store() {
        val todos = TodoStore.todos
        assertNotSame(todos, TodoStore.todos)

        // Même un cast ne permet pas de modifier le store
        (todos as MutableList<Todo>).clear()
        assertEquals(10, TodoStore.todos.size)
    }

    @Test
    fun une_liste_lue_avant_une_modification_ne_change_pas() {
        val avant = TodoStore.todos
        TodoStore.terminer("1")
        TodoStore.supprimer("2")
        assertEquals(10, avant.size)
        assertFalse(avant.first { it.id == "1" }.terminee)
    }

    @Test
    fun reinitialiser_restaure_l_etat_initial() {
        TodoStore.terminer("1")
        TodoStore.ajouter("nouvelle", 1)
        TodoStore.supprimer("45")
        TodoStore.reinitialiser()
        assertEquals("10 tâches, 5 terminées (50 %)", TodoStore.todos.resume())
    }

    @Test
    fun message_adapte_avec_un_when_exhaustif() {
        // Même logique que dans MainActivity, sans ressources Android : pas de else, le compilateur vérifie.
        fun message(resultat: ResultatAction): String = when (resultat) {
            is ResultatAction.Succes -> "« ${resultat.todo.titreCourt} » est terminée"
            ResultatAction.DejaTerminee -> "Cette tâche est déjà terminée"
            ResultatAction.Introuvable -> "Tâche introuvable"
        }
        assertEquals("« delectus aut autem » est terminée", message(TodoStore.terminer("1")))
        assertEquals("Cette tâche est déjà terminée", message(TodoStore.terminer("1")))
        assertEquals("Tâche introuvable", message(TodoStore.terminer("0")))
    }
}
