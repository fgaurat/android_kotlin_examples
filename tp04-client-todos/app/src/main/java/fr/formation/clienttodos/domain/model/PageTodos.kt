package fr.formation.clienttodos.domain.model

/** Bonus : une page de tâches, construite à partir de l'enveloppe de JSON Server. */
data class PageTodos(
    val todos: List<Todo>,
    val page: Int,
    val nombreDePages: Int,
    val total: Int,
    val precedente: Int?,
    val suivante: Int?,
)
