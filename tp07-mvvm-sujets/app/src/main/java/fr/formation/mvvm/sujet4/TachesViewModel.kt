package fr.formation.mvvm.sujet4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Couche UI, partie logique : prépare l'état de l'écran et traite ses actions.
// Le repository est REÇU dans le constructeur (injection) : en test, on passe un faux repository.
class TachesViewModel(private val repository: TacheRepository) : ViewModel() {

    // Flow du repository → StateFlow pour l'écran (valeur initiale : liste vide)
    val taches: StateFlow<List<Tache>> = repository.taches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun ajouter(titre: String) {
        if (titre.isBlank()) return
        viewModelScope.launch { repository.ajouter(titre.trim()) }
    }

    fun basculer(id: Int) {
        viewModelScope.launch { repository.basculer(id) }
    }

    // viewModel() ne sait pas fabriquer un ViewModel qui a des paramètres : on lui explique comment.
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TachesViewModel(Dependances.tacheRepository) }
        }
    }
}
