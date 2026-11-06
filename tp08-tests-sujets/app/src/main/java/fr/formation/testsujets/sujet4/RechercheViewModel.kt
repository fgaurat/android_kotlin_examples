package fr.formation.testsujets.sujet4

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

// Étape 4 — Le CODE testé : une recherche avec debounce (300 ms) et stateIn(WhileSubscribed)
// Les tests sont dans src/test/java/fr/formation/testsujets/sujet4/
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// main/…/data/TodoRepository.kt                  TodoRepository
// main/…/ui/recherche/RechercheViewModel.kt      RechercheViewModel
// test/…/ui/recherche/RechercheViewModelTest.kt  RechercheViewModelTest

data class Todo(val id: String, val titre: String, val terminee: Boolean = false)

interface TodoRepository {
    suspend fun rechercher(texte: String): Result<List<Todo>>
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RechercheViewModel(private val repository: TodoRepository) : ViewModel() {

    private val recherche = MutableStateFlow("")

    val resultats: StateFlow<List<Todo>> = recherche
        .debounce(300)                                // attendre 300 ms sans frappe
        .map { it.trim() }
        .distinctUntilChanged()                       // « kotlin » puis « kotlin  » : une seule recherche
        .mapLatest { texte ->
            if (texte.isBlank()) emptyList() else repository.rechercher(texte).getOrDefault(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onRechercheChange(texte: String) {
        recherche.value = texte
    }
}
