package fr.formation.todos.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExtensionsTest {

    // Liste locale : les tests ne dépendent pas de l'état de TodoStore
    private val todos = listOf(
        Todo("1", "b", terminee = true, utilisateurId = 1),
        Todo("2", "a", terminee = true, utilisateurId = 1),
        Todo("3", "c", terminee = false, utilisateurId = 2),
        Todo("4", "un titre nettement plus long", terminee = false, utilisateurId = 1),
    )

    @Test
    fun titreCourt_tronque_au_dela_de_20_caracteres() {
        assertEquals("b", todos[0].titreCourt)
        assertEquals("un titre nettement…", todos[3].titreCourt)
        assertEquals(true, todos[3].titreCourt.length <= 20)
    }

    @Test
    fun aFaire_terminees_et_selon() {
        assertEquals(listOf("3", "4"), todos.aFaire().map { it.id })
        assertEquals(listOf("1", "2"), todos.terminees().map { it.id })
        assertEquals(todos, todos.selon(Filtre.TOUTES))
        assertEquals(todos.aFaire(), todos.selon(Filtre.A_FAIRE))
        assertEquals(todos.terminees(), todos.selon(Filtre.TERMINEES))
    }

    @Test
    fun regroupements_par_utilisateur() {
        assertEquals(setOf(1, 2), todos.parUtilisateur().keys)
        assertEquals(mapOf(1 to 3, 2 to 1), todos.nombreParUtilisateur())
        assertEquals(1, todos.utilisateurLePlusActif())
    }

    @Test
    fun titreLePlusLong_et_index() {
        assertEquals("4", todos.titreLePlusLong()?.id)
        assertEquals("c", todos.indexParId().getValue("3").titre)
    }

    @Test
    fun triees_met_les_taches_a_faire_d_abord_puis_par_titre() {
        assertEquals(listOf("3", "4", "2", "1"), todos.triees().map { it.id })
    }

    @Test
    fun resume_et_pourcentage() {
        assertEquals(50, todos.pourcentageTerminees())
        assertEquals("4 tâches, 2 terminées (50 %), utilisateur le plus actif : 1", todos.resume())
        assertEquals("10 tâches, 5 terminées (50 %), utilisateur le plus actif : 1", TodoStore.apply { reinitialiser() }.todos.resume())
    }

    @Test
    fun une_liste_vide_ne_plante_pas() {
        val vide = emptyList<Todo>()
        assertNull(vide.utilisateurLePlusActif())
        assertNull(vide.titreLePlusLong())
        assertEquals("0 tâches, 0 terminées (0 %)", vide.resume())
    }
}
