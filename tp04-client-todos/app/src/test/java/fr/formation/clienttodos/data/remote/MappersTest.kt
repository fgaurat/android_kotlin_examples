package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.data.remote.dto.NouveauTodoDto
import fr.formation.clienttodos.data.remote.dto.PageDto
import fr.formation.clienttodos.data.remote.dto.TodoDto
import fr.formation.clienttodos.data.remote.dto.UtilisateurDto
import fr.formation.clienttodos.domain.model.Todo
import fr.formation.clienttodos.domain.model.Utilisateur
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    @Test
    fun `TodoDto vers Todo renomme les champs`() {
        val dto = TodoDto(userId = 1, id = "4", title = "et porro tempora", completed = true)

        assertEquals(Todo(id = "4", titre = "et porro tempora", terminee = true, utilisateurId = 1), dto.toDomain())
    }

    @Test
    fun `Todo vers NouveauTodoDto ne transmet pas l'id`() {
        val todo = Todo(id = "yWeeNCMABkQ", titre = "Préparer le TP", terminee = false, utilisateurId = 1)

        assertEquals(NouveauTodoDto(userId = 1, title = "Préparer le TP", completed = false), todo.toNouveauDto())
    }

    @Test
    fun `UtilisateurDto vers Utilisateur`() {
        val dto = UtilisateurDto(id = "1", name = "Leanne Graham", pseudo = "Bret", email = "Sincere@april.biz")

        assertEquals(Utilisateur("1", "Leanne Graham", "Bret", "Sincere@april.biz"), dto.toDomain())
    }

    @Test
    fun `l'enveloppe de pagination devient une PageTodos`() {
        val dto = PageDto(
            first = 1, prev = null, next = 2, last = 20, pages = 20, items = 200,
            data = listOf(TodoDto(1, "1", "delectus aut autem", false)),
        )

        val page = dto.toDomain(page = 1)

        assertEquals(1, page.page)
        assertEquals(20, page.nombreDePages)
        assertEquals(200, page.total)
        assertNull(page.precedente)
        assertEquals(2, page.suivante)
        assertEquals("delectus aut autem", page.todos.single().titre)
    }
}
