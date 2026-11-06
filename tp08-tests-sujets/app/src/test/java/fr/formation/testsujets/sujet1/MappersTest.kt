package fr.formation.testsujets.sujet1

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

// Étape 1 — Tester le mapping DTO → domaine, avec @Before
// - @Before s'exécute AVANT CHAQUE test : chaque test repart d'un objet neuf (pas d'état partagé entre tests)
// - un test par règle du mapping : id converti, titre nettoyé, priorité absente → NORMALE
class MappersTest {

    private lateinit var dto: TodoDto

    @Before
    fun preparer() {
        dto = TodoDto(id = 4, title = "  et porro tempora ", completed = true)
    }

    @Test
    fun `l'id numérique devient une chaîne`() {
        assertEquals("4", dto.versDomaine().id)
    }

    @Test
    fun `le titre est nettoyé et l'état recopié`() {
        val todo = dto.versDomaine()
        assertEquals("et porro tempora", todo.titre)
        assertEquals(true, todo.terminee)
    }

    @Test
    fun `sans priorité, la tâche est NORMALE`() {
        assertEquals(Priorite.NORMALE, dto.versDomaine().priorite)
    }

    @Test
    fun `l'objet complet est celui attendu`() {
        // data class : equals compare tous les champs, un seul assert suffit
        assertEquals(
            Todo(id = "4", titre = "et porro tempora", terminee = true, priorite = Priorite.HAUTE),
            dto.copy(priority = "haute").versDomaine(),
        )
    }
}
