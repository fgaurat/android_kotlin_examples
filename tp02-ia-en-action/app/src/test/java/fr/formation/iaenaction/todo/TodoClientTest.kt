package fr.formation.iaenaction.todo

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

/**
 * Tests du client HTTP contre un mini-serveur local (ServerSocket) : aucun accès à Internet,
 * résultats reproductibles. On vérifie surtout les cas d'erreur.
 */
class TodoClientTest {

    private val serveur = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
    private val urlBase = "http://127.0.0.1:${serveur.localPort}"

    @After
    fun arreter() {
        serveur.close()
    }

    /** Répond une seule fois avec le statut et le corps donnés ; mémorise la ligne de requête. */
    private fun repondre(statut: String, corps: String): () -> String {
        var ligneRequete = ""
        val t = thread {
            serveur.accept().use { socket ->
                val lecteur = socket.getInputStream().bufferedReader()
                ligneRequete = lecteur.readLine()
                while (lecteur.readLine().orEmpty().isNotEmpty()) { /* en-têtes ignorés */ }
                val octets = corps.toByteArray()
                socket.getOutputStream().apply {
                    write(
                        ("HTTP/1.1 $statut\r\nContent-Type: application/json; charset=utf-8\r\n" +
                            "Content-Length: ${octets.size}\r\nConnection: close\r\n\r\n").toByteArray(),
                    )
                    write(octets)
                    flush()
                }
            }
        }
        return { t.join(); ligneRequete }
    }

    @Test
    fun url_de_la_tache_1() {
        assertEquals("https://jsonplaceholder.typicode.com/todos/1", TodoClient().urlTodo("1"))
    }

    @Test
    fun url_encode_l_identifiant() {
        assertEquals("https://jsonplaceholder.typicode.com/todos/a%2Fb", TodoClient().urlTodo("a/b"))
    }

    @Test
    fun lit_une_tache() = runBlocking<Unit> {
        val requete = repondre(
            "200 OK",
            """{"userId": 1, "id": 1, "title": "delectus aut autem", "completed": false}""",
        )

        val todo = TodoClient(urlBase).lireTodo("1")

        assertEquals(Todo(1, "1", "delectus aut autem", false), todo)
        assertEquals("GET /todos/1 HTTP/1.1", requete())
    }

    @Test
    fun tache_inexistante_leve_ErreurHttpException_404() = runBlocking<Unit> {
        // JSONPlaceholder répond 404 avec un corps « {} » pour /todos/999
        repondre("404 Not Found", "{}")

        try {
            TodoClient(urlBase).lireTodo("999")
            fail("ErreurHttpException attendue")
        } catch (e: ErreurHttpException) {
            assertEquals(404, e.code)
        }
    }

    @Test
    fun erreur_serveur_leve_ErreurHttpException_500() = runBlocking<Unit> {
        repondre("500 Internal Server Error", """{"error":"boom"}""")

        try {
            TodoClient(urlBase).lireTodo("1")
            fail("ErreurHttpException attendue")
        } catch (e: ErreurHttpException) {
            assertEquals(500, e.code)
        }
    }

    @Test
    fun reponse_illisible_levee_en_IOException() = runBlocking<Unit> {
        repondre("200 OK", "<html>maintenance</html>")

        try {
            TodoClient(urlBase).lireTodo("1")
            fail("IOException attendue")
        } catch (e: IOException) {
            assertTrue(e.message!!.startsWith("Réponse illisible"))
        }
    }

    @Test
    fun serveur_muet_delai_depasse() = runBlocking<Unit> {
        // Le serveur accepte la connexion mais ne répond jamais
        val muet = thread { runCatching { serveur.accept().use { Thread.sleep(2_000) } } }

        try {
            TodoClient(urlBase, delaiMs = 200).lireTodo("1")
            fail("SocketTimeoutException attendue")
        } catch (e: SocketTimeoutException) {
            // attendu : readTimeout dépassé
        } finally {
            serveur.close()
            muet.interrupt()
        }
    }

    @Test
    fun serveur_injoignable_leve_IOException() = runBlocking<Unit> {
        val port = serveur.localPort
        serveur.close() // plus personne n'écoute sur ce port

        try {
            TodoClient("http://127.0.0.1:$port", delaiMs = 1_000).lireTodo("1")
            fail("IOException attendue")
        } catch (e: IOException) {
            // attendu : ConnectException (connexion refusée)
        }
    }
}
