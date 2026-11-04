package fr.formation.panier.data

import fr.formation.panier.domain.Produit
import kotlinx.coroutines.delay

/** Implémentation de démonstration utilisée par l'application (pas de réseau). */
class CatalogueRepositoryEnMemoire : CatalogueRepository {

    override suspend fun produits(): List<Produit> {
        delay(1_000) // simule la latence d'un serveur
        return listOf(
            Produit("1", "Clavier mécanique", 79.90),
            Produit("2", "Souris sans fil", 24.99),
            Produit("3", "Tapis de souris", 9.50),
            Produit("4", "Casque audio", 49.99),
            Produit("5", "Câble USB-C", 0.01),
        )
    }
}
