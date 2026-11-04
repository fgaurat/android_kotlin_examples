package fr.formation.hydratation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import fr.formation.hydratation.HydratationApplication
import fr.formation.hydratation.data.PreferencesHydratation
import fr.formation.hydratation.rappels.PlanificationRappels
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HydratationViewModel(
    private val preferences: PreferencesHydratation,
    private val planification: PlanificationRappels,
) : ViewModel() {

    /** Jour courant, rafraîchi au retour au premier plan : le compteur repart à 0 chaque jour. */
    private val jour = MutableStateFlow(preferences.aujourdhui())

    val rappelsActifs: StateFlow<Boolean> = preferences.rappelsActifs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val verresDuJour: StateFlow<Int> = jour
        .flatMapLatest { preferences.verresDuJour(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val etatTravail: StateFlow<WorkInfo?> = planification.etat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        // Interrupteur persistant : s'il était activé, on s'assure que le travail existe
        // (UPDATE ne réinitialise pas la planification en cours).
        viewModelScope.launch {
            if (preferences.rappelsActifs.first()) planification.planifier()
        }
    }

    fun auPremierPlan() {
        jour.value = preferences.aujourdhui()
    }

    /** Étape 4 : appelée APRÈS l'accord de POST_NOTIFICATIONS (ou à la désactivation). */
    fun definirRappels(actifs: Boolean) {
        if (actifs) planification.planifier() else planification.annuler()
        viewModelScope.launch { preferences.definirRappelsActifs(actifs) }
    }

    fun testerMaintenant() {
        planification.testerMaintenant()
    }

    fun ajouterVerre() {
        viewModelScope.launch { preferences.ajouterVerre() }
    }

    fun remettreAZero() {
        viewModelScope.launch { preferences.remettreAZero() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as HydratationApplication
                HydratationViewModel(application.preferences, PlanificationRappels(application))
            }
        }
    }
}
