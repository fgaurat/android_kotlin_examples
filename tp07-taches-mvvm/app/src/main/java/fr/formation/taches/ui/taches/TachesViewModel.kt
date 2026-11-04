package fr.formation.taches.ui.taches

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.formation.taches.TachesApplication
import fr.formation.taches.data.TacheRepository
import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)            // debounce
class TachesViewModel(
    private val repository: TacheRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Filtre : survit à la rotation ET à la mort du processus (Bundle sauvegardé par le système)
    private val filtre: StateFlow<Filtre> = savedStateHandle.getStateFlow(CLE_FILTRE, Filtre.Toutes)

    // Texte saisi : exposé tel quel au champ de saisie, pour qu'il réagisse sans délai
    private val _recherche = MutableStateFlow("")
    val recherche: StateFlow<String> = _recherche.asStateFlow()

    // Texte appliqué au filtrage : seulement après 300 ms sans frappe
    private val rechercheAppliquee = _recherche
        .map { it.trim() }
        .debounce(300)
        .onStart { emit(_recherche.value.trim()) }   // la valeur initiale, elle, n'attend pas 300 ms
        .distinctUntilChanged()

    /** État de l'écran : recalculé dès que les tâches, le filtre OU la recherche changent. */
    val etat: StateFlow<TachesUiState> = combine(
        repository.taches,
        filtre,
        rechercheAppliquee,
    ) { taches, filtre, recherche ->
        TachesUiState(
            taches = taches
                .filter { filtre.accepte(it) }
                .filter { recherche.isEmpty() || it.titre.contains(recherche, ignoreCase = true) },
            filtre = filtre,
            recherche = recherche,
        )
    }.stateIn(
        scope = viewModelScope,                            // vit avec le ViewModel
        started = SharingStarted.WhileSubscribed(5_000),   // survit aux quelques ms d'une rotation
        initialValue = TachesUiState(filtre = filtre.value, chargement = true),
    )

    // Bonus : le même calcul exposé en LiveData…
    val restantes: LiveData<Int> = repository.taches
        .map { liste -> liste.count { !it.faite } }
        .asLiveData()

    // … et en StateFlow, pour comparer
    val restantesEnStateFlow: StateFlow<Int> = repository.taches
        .map { liste -> liste.count { !it.faite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Événements remontés par l'UI

    fun onFiltreChange(nouveau: Filtre) {
        savedStateHandle[CLE_FILTRE] = nouveau
    }

    fun onRechercheChange(texte: String) {
        _recherche.value = texte
    }

    fun ajouter(titre: String, priorite: Priorite) {
        val propre = titre.trim()
        if (propre.isEmpty()) return               // règle de validation : testable sans UI
        viewModelScope.launch { repository.ajouter(propre, priorite) }
    }

    fun basculer(tache: Tache) {
        viewModelScope.launch { repository.basculer(tache.id) }
    }

    fun supprimer(tache: Tache) {
        viewModelScope.launch { repository.supprimer(tache.id) }
    }

    companion object {
        internal const val CLE_FILTRE = "filtre"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TachesApplication
                TachesViewModel(
                    repository = app.container.tacheRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
