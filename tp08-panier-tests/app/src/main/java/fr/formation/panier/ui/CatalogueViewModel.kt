package fr.formation.panier.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.formation.panier.data.CatalogueRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// Code fourni par l'énoncé (copié tel quel).
// Bonus : seule une IOException est interceptée, voir bonus/CatalogueViewModelCorrige.kt
class CatalogueViewModel(
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
            } catch (e: IOException) {
                CatalogueUiState.Erreur(e.message)
            }
        }
    }
}
