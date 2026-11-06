package fr.formation.testsujets.sujet2

import java.io.IOException

// Un FAKE : une VRAIE implémentation, simplifiée (une liste en mémoire au lieu du réseau).
// On l'écrit une fois, on le réutilise partout ; les tests vérifient l'ÉTAT final.
class FakeTodoRepository : TodoRepository {

    val todos = mutableListOf<Todo>()
    var erreur: IOException? = null          // si non null : chaque appel la lève
    var appelsAjouter = 0                    // de quoi vérifier « n'a pas été appelé »

    override suspend fun lister(): List<Todo> {
        erreur?.let { throw it }
        return todos.toList()
    }

    override suspend fun ajouter(titre: String): Todo {
        erreur?.let { throw it }
        appelsAjouter++
        return Todo(id = (todos.size + 1).toString(), titre = titre).also { todos += it }
    }
}
