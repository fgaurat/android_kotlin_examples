package fr.formation.panier.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Étape 2 : comportement du panier. */
class PanierTest {

    private val stylo = Produit("1", "Stylo", 1.50)
    private val cahier = Produit("2", "Cahier", 3.20)

    @Test
    fun `un panier neuf est vide`() {
        val panier = Panier()

        assertTrue(panier.lignes.isEmpty())
        assertEquals(0, panier.nombreArticles)
    }

    @Test
    fun `ajouter un produit crée une ligne de quantité 1 par défaut`() {
        val panier = Panier().ajouter(stylo)

        assertEquals(mapOf(stylo to 1), panier.lignes)
    }

    @Test
    fun `ajouter deux fois le même produit cumule les quantités`() {
        val panier = Panier().ajouter(stylo, 2).ajouter(stylo, 3)

        assertEquals(1, panier.lignes.size)
        assertEquals(5, panier.lignes[stylo])
    }

    @Test
    fun `une quantité nulle lève une IllegalArgumentException`() {
        val erreur = assertThrows(IllegalArgumentException::class.java) {
            Panier().ajouter(stylo, 0)
        }
        assertEquals("Quantité invalide : 0", erreur.message)
    }

    @Test
    fun `une quantité négative lève une IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) { Panier().ajouter(stylo, -2) }
    }

    @Test
    fun `retirer supprime toute la ligne du produit`() {
        val panier = Panier().ajouter(stylo, 3).ajouter(cahier).retirer(stylo)

        assertFalse(stylo in panier.lignes)
        assertEquals(mapOf(cahier to 1), panier.lignes)
    }

    @Test
    fun `nombreArticles additionne les quantités de toutes les lignes`() {
        val panier = Panier().ajouter(stylo, 3).ajouter(cahier, 2)

        assertEquals(5, panier.nombreArticles)
    }

    @Test
    fun `le panier est immuable, ajouter renvoie une nouvelle instance`() {
        val vide = Panier()

        val plein = vide.ajouter(stylo)

        assertTrue(vide.lignes.isEmpty())
        assertEquals(1, plein.nombreArticles)
    }
}
