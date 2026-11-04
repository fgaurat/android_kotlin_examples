package fr.formation.gestiontaches.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class GrandesListesTest {

    @Test
    fun la_sequence_genere_le_nombre_de_taches_demande() {
        assertEquals(100_000, genererTodos(100_000).count())
        assertEquals(Todo("1", "Tâche n°1", terminee = false, utilisateurId = 1), genererTodos(100_000).first())
    }

    @Test
    fun rien_n_est_calcule_sans_operation_terminale() {
        var tachesCreees = 0
        genererTodos(100_000)
            .onEach { tachesCreees++ }
            .filter { !it.terminee }
            .map { it.titre }   // pas d'opération terminale : la chaîne est seulement décrite
        assertEquals(0, tachesCreees)
    }

    @Test
    fun la_sequence_est_paresseuse_et_s_arrete_des_que_possible() {
        var tachesCreees = 0
        val premieres = genererTodos(100_000)
            .onEach { tachesCreees++ }
            .premieresAFaire(utilisateurId = 4)

        // Utilisateur 4 : tâches 4, 14, 24, 34… ; celles dont le numéro est multiple de 4 sont terminées
        assertEquals(listOf("14", "34", "54", "74", "94"), premieres.map { it.id })
        // Seules 94 tâches sur 100 000 ont été construites et filtrées
        assertEquals(94, tachesCreees)
    }

    @Test
    fun les_operations_sont_appliquees_element_par_element() {
        val journal = mutableListOf<String>()
        genererTodos(100_000)
            .map { journal += "map ${it.id}"; it }
            .filter { journal += "filter ${it.id}"; it.terminee }
            .first()
        // Avec une List, tous les map auraient lieu avant le premier filter
        assertEquals(
            listOf("map 1", "filter 1", "map 2", "filter 2", "map 3", "filter 3", "map 4", "filter 4"),
            journal,
        )
    }

    @Test
    fun resume_sur_100_000_taches_en_un_seul_passage() {
        assertEquals("100000 tâches, 25000 terminées (25 %)", genererTodos(100_000).resume())
    }

    @Test
    fun la_version_Sequence_et_la_version_List_donnent_le_meme_resume() {
        val sequence = genererTodos(1_000)
        assertEquals(sequence.toList().resume(), sequence.resume())
    }

    @Test
    fun les_extensions_de_List_fonctionnent_aussi_sur_100_000_taches() {
        val todos = genererTodos(100_000).toList()
        assertEquals(75_000, todos.aFaire().size)
        assertEquals((1..10).associateWith { 10_000 }, todos.nombreParUtilisateur())
        assertEquals("100000", todos.titreLePlusLong()?.id)   // « Tâche n°100000 » : le seul numéro à 6 chiffres
    }
}
