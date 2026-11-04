package fr.formation.clienttodos.data

import fr.formation.clienttodos.data.remote.TodoApi
import fr.formation.clienttodos.data.remote.dto.MajTodoDto
import fr.formation.clienttodos.data.remote.dto.NouveauTodoDto
import fr.formation.clienttodos.data.remote.dto.PageDto
import fr.formation.clienttodos.data.remote.dto.TodoDto
import fr.formation.clienttodos.data.remote.dto.UtilisateurDto
import fr.formation.clienttodos.domain.model.Todo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException

class TodoRepositoryImplTest {

    private val api = mockk<TodoApi>()                     // l'interface Retrofit
    private val repository = TodoRepositoryImpl(api)

    private val dto4 = TodoDto(userId = 1, id = "4", title = "et porro tempora", completed = true)

    private fun httpException(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody()))

    @Test
    fun `lister renvoie les tâches converties en domaine`() = runTest {
        coEvery { api.lister(1, null, null) } returns listOf(dto4)

        val resultat = repository.lister(utilisateurId = 1)

        assertEquals(listOf(Todo("4", "et porro tempora", true, 1)), resultat.getOrThrow())
    }

    @Test
    fun `JSON Server arrêté donne ErreurTodo Reseau`() = runTest {
        coEvery { api.lister(any(), any(), any()) } throws ConnectException("Failed to connect to /10.0.2.2:3000")

        val erreur = repository.lister().exceptionOrNull()

        assertTrue(erreur is ErreurTodo.Reseau)
        assertTrue(erreur?.cause is ConnectException)
    }

    @Test
    fun `404 donne Introuvable et 500 donne Serveur`() = runTest {
        coEvery { api.lire("9999") } throws httpException(404)
        coEvery { api.lire("1") } throws httpException(500)

        assertTrue(repository.lire("9999").exceptionOrNull() is ErreurTodo.Introuvable)
        val serveur = repository.lire("1").exceptionOrNull()
        assertTrue(serveur is ErreurTodo.Serveur)
        assertEquals(500, (serveur as ErreurTodo.Serveur).code)
    }

    @Test
    fun `JSON inattendu donne ErreurTodo Donnees`() = runTest {
        coEvery { api.lister(any(), any(), any()) } throws SerializationException("id attendu en String")

        assertTrue(repository.lister().exceptionOrNull() is ErreurTodo.Donnees)
    }

    @Test(expected = CancellationException::class)
    fun `l'annulation traverse le repository`() = runTest {
        coEvery { api.lister(any(), any(), any()) } throws CancellationException("écran quitté")

        repository.lister()
    }

    @Test(expected = CancellationException::class)
    fun `l'annulation traverse aussi resultatDe`() = runTest {
        coEvery { api.lire(any()) } throws CancellationException("écran quitté")

        repository.lire("1")
    }

    @Test
    fun `ajouter envoie une tâche non terminée sans id`() = runTest {
        val corps = slot<NouveauTodoDto>()
        coEvery { api.creer(capture(corps)) } returns
            TodoDto(1, "yWeeNCMABkQ", "Préparer le TP", false)

        val creee = repository.ajouter("Préparer le TP", utilisateurId = 1).getOrThrow()

        assertEquals(NouveauTodoDto(1, "Préparer le TP", completed = false), corps.captured)
        assertEquals("yWeeNCMABkQ", creee.id)
    }

    @Test
    fun `changerStatut envoie un PATCH avec le seul champ completed`() = runTest {
        val maj = slot<MajTodoDto>()                                  // capture d'argument
        coEvery { api.modifier("4", capture(maj)) } returns dto4

        val resultat = repository.changerStatut("4", terminee = true)

        assertEquals(MajTodoDto(completed = true), maj.captured)      // title reste null
        assertTrue(resultat.getOrThrow().terminee)
        coVerify(exactly = 1) { api.modifier("4", any()) }
    }

    @Test
    fun `remplacer envoie un PUT avec tous les champs`() = runTest {
        coEvery { api.remplacer("4", NouveauTodoDto(1, "Nouveau titre", true)) } returns
            dto4.copy(title = "Nouveau titre")

        val resultat = repository.remplacer(Todo("4", "Nouveau titre", true, 1))

        assertEquals("Nouveau titre", resultat.getOrThrow().titre)
    }

    @Test
    fun `supprimer réussit sur 200 et échoue en Introuvable sur 404`() = runTest {
        coEvery { api.supprimer("4") } returns Response.success(Unit)
        coEvery { api.supprimer("9999") } returns Response.error(404, "Not Found".toResponseBody())

        assertTrue(repository.supprimer("4").isSuccess)
        assertTrue(repository.supprimer("9999").exceptionOrNull() is ErreurTodo.Introuvable)
    }

    @OptIn(ExperimentalCoroutinesApi::class)        // testScheduler.currentTime
    @Test
    fun `lireAvecAuteur lance les deux requêtes en parallèle`() = runTest {
        coEvery { api.lire("1") } coAnswers {
            delay(1_000); TodoDto(1, "1", "delectus aut autem", false)
        }
        coEvery { api.utilisateur("1") } coAnswers {
            delay(1_000); UtilisateurDto("1", "Leanne Graham", "Bret", "Sincere@april.biz")
        }

        val resultat = repository.lireAvecAuteur(todoId = "1", auteurId = "1")

        assertEquals("delectus aut autem — Leanne Graham", resultat.getOrThrow().toString())
        assertEquals(1_000, testScheduler.currentTime)        // temps virtuel : 1 s et non 2 s
    }

    @Test
    fun `lireAvecAuteur échoue si l'une des requêtes échoue`() = runTest {
        coEvery { api.lire("1") } returns TodoDto(1, "1", "delectus aut autem", false)
        coEvery { api.utilisateur("1") } throws httpException(404)

        assertTrue(repository.lireAvecAuteur("1", "1").exceptionOrNull() is ErreurTodo.Introuvable)
    }

    @Test
    fun `listerPage convertit l'enveloppe`() = runTest {
        coEvery { api.listerPage(2, 10) } returns
            PageDto(first = 1, prev = 1, next = 3, last = 20, pages = 20, items = 200, data = listOf(dto4))

        val page = repository.listerPage(page = 2).getOrThrow()

        assertEquals(2, page.page)
        assertEquals(1, page.precedente)
        assertEquals(3, page.suivante)
        assertEquals(1, page.todos.size)
    }
}
