package fr.formation.testsujets.sujet3

import kotlinx.coroutines.delay
import java.io.IOException

// Un fake qui simule aussi la LENTEUR du réseau : delay(latence).
// Dans runTest, ce delay ne dure RIEN en vrai : l'horloge est virtuelle.
class FakeTodoRepository(private val latence: Long = 1_000) : TodoRepository {

    val todos = mutableListOf<Todo>()
    var erreur: IOException? = null

    override suspend fun lister(): List<Todo> {
        delay(latence)
        erreur?.let { throw it }
        return todos.toList()
    }

    override suspend fun modifier(todo: Todo): Todo {
        delay(latence)
        erreur?.let { throw it }
        todos.replaceAll { if (it.id == todo.id) todo else it }
        return todo
    }
}
