package fr.formation.todoapp.data

import fr.formation.todoapp.data.remote.TodoApi
import fr.formation.todoapp.data.remote.dto.MajTodoDto
import fr.formation.todoapp.data.remote.dto.TodoDto
import fr.formation.todoapp.domain.Todo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException

/** Le repository est testé seul, avec une fausse TodoApi (MockK) : pas de serveur. */
class TodoRepositoryImplTest {

    private val api = mockk<TodoApi>()
    private val repository = TodoRepositoryImpl(api)

    private val dto4 = TodoDto(userId = 1, id = "4", title = "et porro tempora", completed = true)

    @Test
    fun `lister convertit les DTO et omet la recherche vide`() = runTest {
        coEvery { api.lister(1, null) } returns listOf(dto4)

        val resultat = repository.lister(utilisateurId = 1, recherche = "  ")

        assertEquals(listOf(Todo("4", "et porro tempora", true, 1)), resultat.getOrThrow())
        coVerify { api.lister(1, null) }
    }

    @Test
    fun `lister transmet la recherche au serveur`() = runTest {
        coEvery { api.lister(1, "porro") } returns listOf(dto4)

        repository.lister(utilisateurId = 1, recherche = "porro")

        coVerify { api.lister(1, "porro") }
    }

    @Test
    fun `changerStatut envoie un PATCH avec le seul champ completed`() = runTest {
        coEvery { api.modifier("4", MajTodoDto(completed = false)) } returns dto4.copy(completed = false)

        val maj = repository.changerStatut("4", terminee = false).getOrThrow()

        assertEquals(false, maj.terminee)
    }

    @Test
    fun `JSON Server arrêté donne ErreurTodo Reseau`() = runTest {
        coEvery { api.lister(any(), any()) } throws ConnectException("Failed to connect to /10.0.2.2:3000")

        assertTrue(repository.lister(1).exceptionOrNull() is ErreurTodo.Reseau)
    }

    @Test
    fun `supprimer une tâche inexistante donne Introuvable`() = runTest {
        coEvery { api.supprimer("9999") } returns Response.error(404, "".toResponseBody())

        assertTrue(repository.supprimer("9999").exceptionOrNull() is ErreurTodo.Introuvable)
    }

    @Test(expected = CancellationException::class)
    fun `l'annulation traverse le repository`() = runTest {
        coEvery { api.lister(any(), any()) } throws CancellationException("écran quitté")

        repository.lister(1)
    }
}
