package fr.formation.panier.bonus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.formation.panier.data.CatalogueRepository
import fr.formation.panier.ui.CatalogueUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Bonus : CatalogueViewModel corrigé.
 *
 * Le ViewModel fourni n'intercepte que IOException. Or l'HttpException de Retrofit
 * (réponse 404, 500…) hérite de RuntimeException : elle traverse le try/catch,
 * remonte jusqu'à viewModelScope et fait planter l'application.
 *
 * Correction : intercepter toute Exception, sauf CancellationException qu'il faut
 * relancer pour ne pas casser l'annulation structurée des coroutines.
 */
class CatalogueViewModelCorrige(
    private val repository: CatalogueRepository,
) : ViewModel() {

    private val _etat =
        MutableStateFlow<CatalogueUiState>(CatalogueUiState.Chargement)
    val etat: StateFlow<CatalogueUiState> = _etat.asStateFlow()

    fun charger() {
        viewModelScope.launch {
            _etat.value = CatalogueUiState.Chargement
            _etat.value = try {
                CatalogueUiState.Succes(repository.produits())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                CatalogueUiState.Erreur(e.message)
            }
        }
    }
}
