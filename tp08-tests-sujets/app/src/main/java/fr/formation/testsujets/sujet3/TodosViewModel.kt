package fr.formation.testsujets.sujet3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// Étape 3 — Le CODE testé : un ViewModel (viewModelScope, StateFlow, état sealed)
// Les tests sont dans src/test/java/fr/formation/testsujets/sujet3/
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// main/…/data/TodoRepository.kt              TodoRepository
// main/…/ui/todos/TodosUiState.kt            TodosUiState
// main/…/ui/todos/TodosViewModel.kt          TodosViewModel
// test/…/MainDispatcherRule.kt               MainDispatcherRule (à la racine des tests : partagée)
// test/…/data/FakeTodoRepository.kt          FakeTodoRepository
// test/…/ui/todos/TodosViewModelTest.kt      TodosViewModelTest

data class Todo(val id: String, val titre: String, val terminee: Boolean = false)

interface TodoRepository {
    suspend fun lister(): List<Todo>
    suspend fun modifier(todo: Todo): Todo
}

sealed interface TodosUiState {
    data object Chargement : TodosUiState
    data class Succes(val todos: List<Todo>) : TodosUiState
    data class Erreur(val message: String) : TodosUiState
}

class TodosViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    init {
        charger()
    }

    fun charger() {
        _etat.value = TodosUiState.Chargement
        viewModelScope.launch {
            _etat.value = try {
                TodosUiState.Succes(repository.lister())
            } catch (e: IOException) {
                TodosUiState.Erreur("Serveur injoignable")
            }
        }
    }

    // La liste est mise à jour avec la tâche RENVOYÉE par le serveur ; en cas d'échec, rien ne change
    fun basculer(todo: Todo) {
        viewModelScope.launch {
            try {
                val maj = repository.modifier(todo.copy(terminee = !todo.terminee))
                _etat.update { e ->
                    if (e is TodosUiState.Succes) e.copy(todos = e.todos.map { if (it.id == maj.id) maj else it }) else e
                }
            } catch (e: IOException) {
                // volontairement ignoré ici : l'étape 5 affiche un message
            }
        }
    }
}
