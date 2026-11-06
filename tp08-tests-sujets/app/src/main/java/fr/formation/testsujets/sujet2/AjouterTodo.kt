package fr.formation.testsujets.sujet2

import java.io.IOException

// Étape 2 — Le CODE testé : une classe qui DÉPEND d'un repository (fonctions suspend)
// Pour la tester sans réseau, on lui donne une DOUBLURE du repository : un fake ou un mock.
// Les tests sont dans src/test/java/fr/formation/testsujets/sujet2/
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// main/…/domain/Todo.kt                  Todo
// main/…/data/TodoRepository.kt          TodoRepository (interface : c'est elle qui rend la doublure possible)
// main/…/domain/AjouterTodo.kt           AjouterTodo, ResultatAjout
// test/…/data/FakeTodoRepository.kt      FakeTodoRepository (réutilisable par tous les tests)
// test/…/domain/AjouterTodoFakeTest.kt
// test/…/domain/AjouterTodoMockkTest.kt

data class Todo(val id: String, val titre: String, val terminee: Boolean = false)

interface TodoRepository {
    suspend fun lister(): List<Todo>
    suspend fun ajouter(titre: String): Todo
}

sealed interface ResultatAjout {
    data class Ajoutee(val todo: Todo) : ResultatAjout
    data object TitreInvalide : ResultatAjout
    data object Doublon : ResultatAjout
    data class Echec(val message: String) : ResultatAjout
}

// Règles : titre non vide, pas deux tâches de même titre (casse ignorée), erreur réseau → Echec
class AjouterTodo(private val repository: TodoRepository) {

    suspend operator fun invoke(titre: String): ResultatAjout {
        val propre = titre.trim()
        if (propre.isBlank()) return ResultatAjout.TitreInvalide
        return try {
            if (repository.lister().any { it.titre.equals(propre, ignoreCase = true) }) {
                ResultatAjout.Doublon
            } else {
                ResultatAjout.Ajoutee(repository.ajouter(propre))
            }
        } catch (e: IOException) {
            ResultatAjout.Echec("Serveur injoignable")
        }
    }
}
