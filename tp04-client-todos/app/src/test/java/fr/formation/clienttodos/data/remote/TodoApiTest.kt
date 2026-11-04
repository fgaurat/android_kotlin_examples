package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.data.remote.dto.MajTodoDto
import fr.formation.clienttodos.data.remote.dto.NouveauTodoDto
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * Vérifie les requêtes réellement émises par Retrofit (méthode, URL, corps)
 * grâce à un faux serveur HTTP local : MockWebServer.
 */
class TodoApiTest {

    private val serveur = MockWebServer()
    private lateinit var api: TodoApi

    @Before
    fun setUp() {
        serveur.start()
        api = creerTodoApi(serveur.url("/").toString(), OkHttpClient())
    }

    @After
    fun tearDown() = serveur.shutdown()

    private fun repondre(corps: String, code: Int = 200) =
        serveur.enqueue(MockResponse().setResponseCode(code).setBody(corps)
            .addHeader("Content-Type", "application/json"))

    @Test
    fun `lister construit les paramètres de requête et omet les null`() = runTest {
        repondre("[]")
        api.lister(utilisateurId = 1, terminee = false)
        assertEquals("/todos?userId=1&completed=false", serveur.takeRequest().path)

        repondre("[]")
        api.lister()
        assertEquals("/todos", serveur.takeRequest().path)

        repondre("[]")
        api.lister(recherche = "dolor")
        // Retrofit encode « : » en %3A (/todos?title%3Acontains=dolor) : JSON Server le décode très bien
        val url = serveur.takeRequest().requestUrl!!
        assertEquals("dolor", url.queryParameter("title:contains"))
    }

    @Test
    fun `creer envoie un POST sans id et lit l'id attribué par le serveur`() = runTest {
        repondre("""{"userId":1,"title":"Préparer le TP","completed":false,"id":"yWeeNCMABkQ"}""", code = 201)

        val creee = api.creer(NouveauTodoDto(userId = 1, title = "Préparer le TP", completed = false))

        val requete = serveur.takeRequest()
        assertEquals("POST", requete.method)
        assertEquals("/todos", requete.path)
        assertEquals("""{"userId":1,"title":"Préparer le TP","completed":false}""", requete.body.readUtf8())
        assertEquals("yWeeNCMABkQ", creee.id)
    }

    @Test
    fun `modifier envoie un PATCH ne contenant que completed`() = runTest {
        repondre("""{"userId":1,"id":"4","title":"et porro tempora","completed":false}""")

        api.modifier("4", MajTodoDto(completed = false))

        val requete = serveur.takeRequest()
        assertEquals("PATCH", requete.method)
        assertEquals("/todos/4", requete.path)
        assertEquals("""{"completed":false}""", requete.body.readUtf8())
    }

    @Test
    fun `remplacer envoie un PUT avec la tâche complète`() = runTest {
        repondre("""{"userId":1,"id":"4","title":"Autre","completed":true}""")

        api.remplacer("4", NouveauTodoDto(1, "Autre", true))

        val requete = serveur.takeRequest()
        assertEquals("PUT", requete.method)
        assertEquals("""{"userId":1,"title":"Autre","completed":true}""", requete.body.readUtf8())
    }

    @Test
    fun `supprimer renvoie le code HTTP sans lever d'exception`() = runTest {
        repondre("", code = 404)

        val reponse = api.supprimer("9999")

        assertEquals("DELETE", serveur.takeRequest().method)
        assertFalse(reponse.isSuccessful)
        assertEquals(404, reponse.code())
    }

    @Test
    fun `listerPage envoie _page et _per_page et décode l'enveloppe`() = runTest {
        repondre("""{"first":1,"prev":null,"next":2,"last":20,"pages":20,"items":200,"data":[]}""")

        val page = api.listerPage(page = 1, parPage = 10)

        assertEquals("/todos?_page=1&_per_page=10", serveur.takeRequest().path)
        assertEquals(20, page.pages)
    }
}
