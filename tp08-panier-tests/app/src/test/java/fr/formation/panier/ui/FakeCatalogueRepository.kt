package fr.formation.panier.ui

import fr.formation.panier.data.CatalogueRepository
import fr.formation.panier.domain.Produit
import kotlinx.coroutines.delay
import java.io.IOException

/** Étape 6 : fake en mémoire, avec une latence simulée d'une seconde (temps virtuel en test). */
class FakeCatalogueRepository(
    var produits: List<Produit> = emptyList(),
    var erreur: Exception? = null,          // simule un serveur injoignable
) : CatalogueRepository {

    var nombreAppels = 0
        private set

    override suspend fun produits(): List<Produit> {
        nombreAppels++
        delay(1_000)
        erreur?.let { throw it }
        return produits
    }

    companion object {
        fun horsLigne() = FakeCatalogueRepository(erreur = IOException("Serveur injoignable"))
    }
}
