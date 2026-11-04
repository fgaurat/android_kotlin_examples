package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.data.ErreurTodo
import fr.formation.clienttodos.data.TodoRepositoryImpl
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Test d'intégration contre un VRAI JSON Server sur http://localhost:3000/.
 * Ignoré (assumption) si le serveur n'est pas lancé : il ne casse donc jamais le build.
 * Il crée, modifie puis supprime sa propre tâche : db.json revient à son contenu initial.
 */
class JsonServerIntegrationTest {

    private val repository = TodoRepositoryImpl(creerTodoApi("http://localhost:3000/", OkHttpClient()))

    @Before
    fun serveurLance() {
        val disponible = runCatching {
            Socket().use { it.connect(InetSocketAddress("localhost", 3000), 300) }
        }.isSuccess
        assumeTrue("JSON Server n'est pas lancé sur le port 3000", disponible)
    }

    @Test
    fun `cycle complet POST, PATCH, PUT, DELETE`() = runTest {
        val creee = repository.ajouter("Test d'intégration", utilisateurId = 1).getOrThrow()
        assertFalse(creee.terminee)

        val terminee = repository.changerStatut(creee.id, terminee = true).getOrThrow()
        assertTrue(terminee.terminee)
        assertEquals("Test d'intégration", terminee.titre)      // le PATCH n'a pas effacé le titre

        val renommee = repository.remplacer(terminee.copy(titre = "Renommée")).getOrThrow()
        assertEquals(creee.id, renommee.id)

        assertTrue(repository.supprimer(creee.id).isSuccess)
        assertTrue(repository.lire(creee.id).exceptionOrNull() is ErreurTodo.Introuvable)
        assertTrue(repository.supprimer(creee.id).exceptionOrNull() is ErreurTodo.Introuvable)
    }

    @Test
    fun `lecture, filtres, recherche et pagination`() = runTest {
        val todos = repository.lister(utilisateurId = 1).getOrThrow()
        assertTrue(todos.isNotEmpty())
        assertTrue(todos.all { it.utilisateurId == 1 })

        val api = creerTodoApi("http://localhost:3000/", OkHttpClient())
        assertTrue(api.lister(utilisateurId = 1, terminee = false).none { it.completed })
        assertTrue(api.lister(recherche = "dolor").all { "dolor" in it.title })

        val page = repository.listerPage(page = 2, parPage = 10).getOrThrow()
        assertEquals(10, page.todos.size)
        assertEquals(1, page.precedente)

        assertEquals("delectus aut autem — Leanne Graham",
            repository.lireAvecAuteur("1", "1").getOrThrow().toString())
    }
}
