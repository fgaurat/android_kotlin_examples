package fr.formation.gestiontaches.domain

// Bonus : traiter 100 000 tâches avec une Sequence (évaluation paresseuse).

/**
 * Génère [nombre] tâches fictives **à la demande** : aucune tâche n'est créée tant qu'une opération
 * terminale (`toList`, `first`, `count`…) ne la réclame.
 * Une tâche sur quatre est terminée ; les tâches sont réparties entre 10 utilisateurs.
 */
fun genererTodos(nombre: Int): Sequence<Todo> =
    (1..nombre).asSequence().map { n ->
        Todo(
            id = n.toString(),
            titre = "Tâche n°$n",
            terminee = n % 4 == 0,
            utilisateurId = (n - 1) % 10 + 1,
        )
    }

/** Les [nombre] premières tâches à faire de [utilisateurId] : le parcours s'arrête dès qu'elles sont trouvées. */
fun Sequence<Todo>.premieresAFaire(utilisateurId: Int, nombre: Int = 5): List<Todo> =
    filter { it.utilisateurId == utilisateurId && !it.terminee }
        .take(nombre)
        .toList()

/** Le résumé calculé en un seul passage, sans liste intermédiaire. */
fun Sequence<Todo>.resume(): String {
    var total = 0
    var terminees = 0
    forEach { todo ->
        total++
        if (todo.terminee) terminees++
    }
    return formaterResume(total, terminees)
}
