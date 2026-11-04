package fr.formation.panier.data

import fr.formation.panier.domain.Produit

// Code fourni par l'énoncé (copié tel quel)
interface CatalogueRepository {
    suspend fun produits(): List<Produit>
}
