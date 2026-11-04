package fr.formation.gestiontaches.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExtensionsTest {

    private lateinit var todos: List<Todo>

    @Before
    fun setUp() {
        TodoStore.reinitialiser()
        todos = TodoStore.todos
    }

    private fun todo(id: String, titre: String = "tâche $id", terminee: Boolean = false, utilisateurId: Int = 1) =
        Todo(id, titre, terminee, utilisateurId)

    // Étape 3

    @Test
    fun titreCourt_ne_change_pas_un_titre_de_20_caracteres_au_plus() {
        assertEquals("delectus aut autem", todo("1", "delectus aut autem").titreCourt)
        val vingt = "a".repeat(20)
        assertEquals(vingt, todo("1", vingt).titreCourt)
    }

    @Test
    fun titreCourt_tronque_un_titre_long_a_20_caracteres() {
        val court = todo("2", "quis ut nam facilis et officia qui").titreCourt
        assertEquals("quis ut nam facilis…", court)
        assertEquals(20, court.length)
        assertEquals(20, todo("3", "a".repeat(21)).titreCourt.length)
    }

    @Test
    fun titreCourt_ne_depasse_jamais_20_caracteres() {
        todos.forEach { assertTrue(it.titreCourt, it.titreCourt.length <= LONGUEUR_TITRE_COURT) }
    }

    @Test
    fun aFaire_et_terminees_partagent_la_liste() {
        assertEquals(listOf("1", "2", "21", "41", "45"), todos.aFaire().map { it.id })
        assertEquals(listOf("4", "8", "22", "26", "43"), todos.terminees().map { it.id })
    }

    @Test
    fun selon_applique_chaque_filtre() {
        assertSame(todos, todos.selon(Filtre.TOUTES))
        assertEquals(todos.aFaire(), todos.selon(Filtre.A_FAIRE))
        assertEquals(todos.terminees(), todos.selon(Filtre.TERMINEES))
        assertEquals(10, Filtre.entries.filter { it != Filtre.TOUTES }.sumOf { todos.selon(it).size })
    }

    // Étape 4

    @Test
    fun taches_groupees_par_utilisateur() {
        val groupes = todos.parUtilisateur()
        assertEquals(setOf(1, 2, 3), groupes.keys)
        assertEquals(listOf("1", "2", "4", "8"), groupes.getValue(1).map { it.id })
        assertEquals(listOf("21", "22", "26"), groupes.getValue(2).map { it.id })
        assertEquals(listOf("41", "43", "45"), groupes.getValue(3).map { it.id })
    }

    @Test
    fun nombre_de_taches_par_utilisateur() {
        assertEquals(mapOf(1 to 4, 2 to 3, 3 to 3), todos.nombreParUtilisateur())
    }

    @Test
    fun utilisateur_le_plus_actif() {
        assertEquals(1, todos.utilisateurLePlusActif())
        assertNull(emptyList<Todo>().utilisateurLePlusActif())
    }

    @Test
    fun tache_au_titre_le_plus_long() {
        assertEquals("41", todos.titreLePlusLong()?.id)
        assertNull(emptyList<Todo>().titreLePlusLong())
    }

    @Test
    fun taches_triees_a_faire_d_abord_puis_par_titre() {
        assertEquals(
            listOf("41", "1", "2", "21", "45", "26", "22", "4", "8", "43"),
            todos.triees().map { it.id },
        )
        // Titres : aliquid…, delectus…, quis…, suscipit…, velit… puis aliquam…, distinctio…, et…, quo…, tempore…
    }

    @Test
    fun index_par_id() {
        val index = todos.indexParId()
        assertEquals(10, index.size)
        assertEquals("tempore ut sint quis recusandae", index["43"]?.titre)
        assertNull(index["3"])
    }

    // Étape 6

    @Test
    fun resume_gere_le_singulier_le_pluriel_et_la_liste_vide() {
        assertEquals("0 tâche, 0 terminée", emptyList<Todo>().resume())
        assertEquals("1 tâche, 1 terminée (100 %)", listOf(todo("1", terminee = true)).resume())
        assertEquals("2 tâches, 0 terminée (0 %)", listOf(todo("1"), todo("2")).resume())
    }

    @Test
    fun resume_arrondit_le_pourcentage() {
        val troisDontDeux = listOf(todo("1", terminee = true), todo("2", terminee = true), todo("3"))
        assertEquals("3 tâches, 2 terminées (67 %)", troisDontDeux.resume())
    }

    @Test
    fun les_extensions_fonctionnent_sur_une_liste_filtree() {
        assertEquals("3 tâches, 2 terminées (67 %)", todos.parUtilisateur().getValue(2).resume())
        assertEquals("5 tâches, 0 terminée (0 %)", todos.selon(Filtre.A_FAIRE).resume())
    }
}
