package fr.formation.todoapp.ui.todos

import fr.formation.todoapp.domain.Todo

/** Ce que l'écran affiche : un seul des trois états à la fois. */
sealed interface TodosUiState {
    data object Chargement : TodosUiState

    data class Succes(val todos: List<Todo>) : TodosUiState {
        val restantes: Int get() = todos.count { !it.terminee }      // valeur dérivée, jamais stockée
    }

    data class Erreur(val message: String) : TodosUiState
}
