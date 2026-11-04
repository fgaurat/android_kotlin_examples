package fr.formation.panier.domain

// Code fourni par l'énoncé (copié tel quel)
data class Panier(val lignes: Map<Produit, Int> = emptyMap()) {

    val nombreArticles: Int get() = lignes.values.sum()

    fun ajouter(produit: Produit, quantite: Int = 1): Panier {
        require(quantite > 0) { "Quantité invalide : $quantite" }
        val actuelle = lignes[produit] ?: 0
        return copy(lignes = lignes + (produit to actuelle + quantite))
    }

    fun retirer(produit: Produit): Panier = copy(lignes = lignes - produit)

    // -10 % avec le code "BIENVENUE", à partir de 50 € d'achat
    fun total(codePromo: String? = null): Double {
        val brut = lignes.entries.sumOf { (p, q) -> p.prix * q }
        return if (codePromo == "BIENVENUE" && brut >= 50.0) brut * 0.9
               else brut
    }
}
