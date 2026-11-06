package fr.formation.testsujets.sujet5

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// Étape 5 — Le CODE testé : le ViewModel de l'étape 3, plus des MESSAGES ponctuels (Snackbar)
// - etat : un StateFlow (toujours une valeur, la dernière)
// - messages : un Channel lu en Flow (chaque message est reçu UNE fois, puis disparaît)
// Les tests sont dans src/test/java/fr/formation/testsujets/sujet5/
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// main/…/data/TodoRepository.kt              TodoRepository
// main/…/ui/todos/TodosUiState.kt            TodosUiState
// main/…/ui/todos/TodosViewModel.kt          TodosViewModel
// test/…/data/FakeTodoRepository.kt          FakeTodoRepository
// test/…/ui/todos/TodosViewModelTurbineTest.kt

data class Todo(val id: String, val titre: String, val terminee: Boolean = false)

interface TodoRepository {
    suspend fun lister(): List<Todo>
    suspend fun supprimer(id: String)
}

sealed interface TodosUiState {
    data object Chargement : TodosUiState
    data class Succes(val todos: List<Todo>) : TodosUiState
    data class Erreur(val message: String) : TodosUiState
}

class TodosViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

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

    fun supprimer(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.supprimer(todo.id)
                _etat.update { e ->
                    if (e is TodosUiState.Succes) e.copy(todos = e.todos.filterNot { it.id == todo.id }) else e
                }
                _messages.send("« ${todo.titre} » supprimée")
            } catch (e: IOException) {
                _messages.send("Suppression impossible")
            }
        }
    }
}
