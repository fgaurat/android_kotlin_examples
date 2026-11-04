package fr.formation.todos.domain

// Bonus : traiter une très grande liste avec une Sequence (évaluation paresseuse).

/**
 * Génère [nombre] tâches fictives, **à la demande** : aucune tâche n'est créée tant qu'une
 * opération terminale (`toList`, `first`, `count`…) ne la réclame.
 */
fun genererTodos(nombre: Int): Sequence<Todo> =
    generateSequence(1) { it + 1 }
        .take(nombre)
        .map { n ->
            Todo(
                id = n.toString(),
                titre = "Tâche n°$n",
                terminee = n % 3 == 0,
                utilisateurId = n % 10 + 1,
            )
        }

/** Les [nombre] premiers titres à faire de l'utilisateur [utilisateurId] : on s'arrête dès qu'on les a. */
fun Sequence<Todo>.premieresAFaire(utilisateurId: Int, nombre: Int = 5): List<String> =
    filter { it.utilisateurId == utilisateurId && !it.terminee }
        .map { it.titre }
        .take(nombre)
        .toList()

/** Statistiques calculées en un seul passage, sans liste intermédiaire. */
data class Statistiques(val total: Int, val terminees: Int, val parUtilisateur: Map<Int, Int>)

fun Sequence<Todo>.statistiques(): Statistiques {
    var total = 0
    var terminees = 0
    val parUtilisateur = mutableMapOf<Int, Int>()
    forEach { todo ->
        total++
        if (todo.terminee) terminees++
        parUtilisateur.merge(todo.utilisateurId, 1, Int::plus)
    }
    return Statistiques(total, terminees, parUtilisateur)
}
