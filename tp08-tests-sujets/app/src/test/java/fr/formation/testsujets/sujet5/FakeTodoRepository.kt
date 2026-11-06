package fr.formation.testsujets.sujet5

import kotlinx.coroutines.delay
import java.io.IOException

class FakeTodoRepository(private val latence: Long = 1_000) : TodoRepository {

    val todos = mutableListOf<Todo>()
    var erreur: IOException? = null

    override suspend fun lister(): List<Todo> {
        delay(latence)
        erreur?.let { throw it }
        return todos.toList()
    }

    override suspend fun supprimer(id: String) {
        delay(latence)
        erreur?.let { throw it }
        todos.removeAll { it.id == id }
    }
}
