package fr.formation.panier.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/** Étape 3 : total() sur plusieurs jeux de données, valeurs limites comprises. */
@RunWith(Parameterized::class)
class PanierTotalParametreTest(
    private val description: String,
    private val montantBrut: Double,
    private val codePromo: String?,
    private val attendu: Double,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} : {1} € avec {2} → {3} €")
        fun donnees() = listOf(
            arrayOf<Any?>("sans code", 80.0, null, 80.0),
            arrayOf<Any?>("code invalide", 80.0, "PROMO", 80.0),
            arrayOf<Any?>("BIENVENUE sous le seuil", 49.99, "BIENVENUE", 49.99),
            arrayOf<Any?>("BIENVENUE à 50 € pile", 50.0, "BIENVENUE", 45.0),
            arrayOf<Any?>("BIENVENUE au-dessus du seuil", 80.0, "BIENVENUE", 72.0),
        )
    }

    @Test
    fun `total avec ou sans code promo`() {
        val panier = Panier().ajouter(Produit("1", "Article", montantBrut))

        // Des Double : comparaison à epsilon près
        assertEquals(attendu, panier.total(codePromo), 0.001)
    }
}
