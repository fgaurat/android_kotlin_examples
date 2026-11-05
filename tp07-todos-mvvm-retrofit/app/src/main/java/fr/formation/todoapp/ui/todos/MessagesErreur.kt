package fr.formation.todoapp.ui.todos

import fr.formation.todoapp.data.ErreurTodo

/** Traduit une erreur du domaine en message lisible ; la cause technique reste visible pour le TP. */
fun Throwable.messagePourUtilisateur(): String = when (this) {
    is ErreurTodo.Reseau ->
        "Serveur injoignable : JSON Server est-il lancé ? (${cause.descriptionCourte()})"
    is ErreurTodo.Introuvable -> "Tâche introuvable (404)"
    is ErreurTodo.Serveur -> "Erreur du serveur (code $code)"
    is ErreurTodo.Donnees -> "Réponse inattendue du serveur (${cause.descriptionCourte()})"
    else -> message ?: javaClass.simpleName
}

private fun Throwable?.descriptionCourte(): String =
    if (this == null) "cause inconnue" else "${javaClass.simpleName} : $message"
