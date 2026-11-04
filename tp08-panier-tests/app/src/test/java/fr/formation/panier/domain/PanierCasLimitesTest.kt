package fr.formation.panier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Étapes 8 et 9 : branches et cas limites ajoutés après la mesure de couverture et l'aide de l'IA. */
class PanierCasLimitesTest {

    private val stylo = Produit("1", "Stylo", 1.50)

    @Test
    fun `le total d'un panier vide vaut 0`() {
        assertEquals(0.0, Panier().total("BIENVENUE"), 0.0)
    }

    @Test
    fun `le total multiplie le prix par la quantité`() {
        val panier = Panier().ajouter(stylo, 4).ajouter(Produit("2", "Cahier", 3.20), 2)

        assertEquals(12.40, panier.total(), 0.001)
    }

    @Test
    fun `le seuil de 50 € s'applique au total de plusieurs lignes`() {
        val panier = Panier().ajouter(Produit("1", "A", 30.0)).ajouter(Produit("2", "B", 20.0))

        assertEquals(45.0, panier.total("BIENVENUE"), 0.001)
    }

    @Test
    fun `le code promo est sensible à la casse`() {
        val panier = Panier().ajouter(Produit("1", "Casque", 80.0))

        // Comportement actuel, à valider avec le métier : "bienvenue" n'est pas reconnu
        assertEquals(80.0, panier.total("bienvenue"), 0.001)
    }

    @Test
    fun `retirer un produit absent laisse le panier inchangé`() {
        val panier = Panier().ajouter(stylo)

        assertEquals(panier, panier.retirer(Produit("9", "Gomme", 0.80)))
    }

    @Test
    fun `même id mais prix différent donne deux lignes distinctes`() {
        // Produit est une data class : la clé de la Map compare TOUS les champs, pas seulement l'id
        val panier = Panier().ajouter(stylo).ajouter(stylo.copy(prix = 1.80))

        assertEquals(2, panier.lignes.size)
        assertNotEquals(stylo, stylo.copy(prix = 1.80))
    }

    @Test
    fun `les centimes s'additionnent à epsilon près`() {
        val panier = Panier().ajouter(Produit("1", "A", 0.1)).ajouter(Produit("2", "B", 0.2))

        // 0.1 + 0.2 vaut 0.30000000000000004 en Double : jamais d'assertEquals sans delta
        assertNotEquals(0.3, panier.total())
        assertEquals(0.3, panier.total(), 1e-9)
    }
}
