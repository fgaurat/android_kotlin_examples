package fr.formation.todoapp.ui.todos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.formation.todoapp.TodoApplication
import fr.formation.todoapp.data.TodoRepository
import fr.formation.todoapp.domain.Todo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de l'écran de liste.
 * - Reçoit le repository par son constructeur : aucune trace de Retrofit ici, testable en JVM pur.
 * - Expose l'état de l'écran en StateFlow (lecture seule) ; l'écran appelle des fonctions (événements).
 */
@OptIn(FlowPreview::class)            // debounce
class TodosViewModel(
    private val repository: TodoRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // 1. État de l'écran : mutable en privé, exposé en lecture seule
    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    // 2. Texte de recherche : dans le SavedStateHandle, il survit à la rotation ET à la mort du processus
    val recherche: StateFlow<String> = savedStateHandle.getStateFlow(CLE_RECHERCHE, "")

    // 3. Message ponctuel (échec d'un ajout, d'une suppression…) : l'écran l'affiche en Snackbar,
    //    puis appelle messageAffiche(). C'est un état « consommé », pas un événement perdu ou rejoué.
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var chargementEnCours: Job? = null

    init {
        // Chaque nouvelle recherche (après 300 ms sans frappe) relance une requête GET /todos.
        // La première valeur n'attend pas : onStart l'émet tout de suite, distinctUntilChanged
        // ignore ensuite la même valeur quand debounce la laisse passer 300 ms plus tard.
        viewModelScope.launch {
            recherche
                .map { it.trim() }
                .debounce(300)
                .onStart { emit(recherche.value.trim()) }
                .distinctUntilChanged()
                .collect { texte -> charger(texte) }
        }
    }

    // ---------- Événements remontés par l'écran ----------

    fun onRechercheChange(texte: String) {
        savedStateHandle[CLE_RECHERCHE] = texte
    }

    /** Bouton « Réessayer » et bouton « Recharger » de la barre d'application. */
    fun rafraichir() {
        charger(recherche.value.trim())
    }

    fun ajouter(titre: String) {
        val propre = titre.trim()
        if (propre.isEmpty()) return                       // règle de validation : testable sans UI
        viewModelScope.launch {
            repository.ajouter(propre, UTILISATEUR_ID)     // POST
                .onSuccess { cree -> modifierListe { it + cree } }
                .onFailure { _message.value = "Ajout impossible : ${it.messagePourUtilisateur()}" }
        }
    }

    fun basculer(todo: Todo) {
        viewModelScope.launch {
            repository.changerStatut(todo.id, !todo.terminee)    // PATCH
                // On remplace par la tâche RENVOYÉE par le serveur : c'est lui la source de vérité
                .onSuccess { maj -> modifierListe { liste -> liste.map { if (it.id == maj.id) maj else it } } }
                .onFailure { _message.value = "Modification impossible : ${it.messagePourUtilisateur()}" }
        }
    }

    fun supprimer(todo: Todo) {
        viewModelScope.launch {
            repository.supprimer(todo.id)                  // DELETE
                .onSuccess { modifierListe { liste -> liste.filterNot { it.id == todo.id } } }
                .onFailure { _message.value = "Suppression impossible : ${it.messagePourUtilisateur()}" }
        }
    }

    fun messageAffiche() {
        _message.value = null
    }

    // ---------- Fonctions internes ----------

    /** GET /todos : Chargement, puis Succès ou Erreur. Une nouvelle requête annule la précédente. */
    private fun charger(texte: String) {
        chargementEnCours?.cancel()
        chargementEnCours = viewModelScope.launch {        // annulée aussi dans onCleared()
            _etat.value = TodosUiState.Chargement
            _etat.value = repository.lister(UTILISATEUR_ID, texte).fold(
                onSuccess = { TodosUiState.Succes(it) },
                onFailure = { TodosUiState.Erreur(it.messagePourUtilisateur()) },
            )
        }
    }

    /** Modifie la liste affichée, seulement si l'écran est dans l'état Succès. */
    private fun modifierListe(transformation: (List<Todo>) -> List<Todo>) {
        _etat.update { etat ->
            if (etat is TodosUiState.Succes) etat.copy(todos = transformation(etat.todos)) else etat
        }
    }

    companion object {
        /** L'application affiche les tâches d'un seul utilisateur de JSON Server. */
        const val UTILISATEUR_ID = 1
        internal const val CLE_RECHERCHE = "recherche"

        /** Fabrique : va chercher le repository dans l'AppContainer de l'Application. */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TodoApplication
                TodosViewModel(
                    repository = app.container.todoRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
