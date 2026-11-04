package fr.formation.panier.ui

import fr.formation.panier.domain.Produit

// Code fourni par l'énoncé (copié tel quel)
sealed interface CatalogueUiState {
    data object Chargement : CatalogueUiState
    data class Succes(val produits: List<Produit>) : CatalogueUiState
    data class Erreur(val message: String?) : CatalogueUiState
}
