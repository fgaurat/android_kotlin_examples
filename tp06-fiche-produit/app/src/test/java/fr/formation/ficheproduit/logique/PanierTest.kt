package fr.formation.ficheproduit.logique

import fr.formation.ficheproduit.donnees.couleursFictives
import fr.formation.ficheproduit.donnees.produitFictif
import fr.formation.ficheproduit.modele.LignePanier
import fr.formation.ficheproduit.modele.Taille
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PanierTest {

    private fun ligne(taille: Taille = Taille.M, couleur: Int = 0, quantite: Int = 1) =
        LignePanier(produitFictif.id, produitFictif.nom, taille, couleursFictives[couleur], 2990, quantite)

    @Test
    fun `l'ajout exige une taille choisie et disponible`() {
        assertFalse(produitFictif.peutEtreAjoute(null))
        assertFalse(produitFictif.peutEtreAjoute(Taille.XL))      // épuisée dans les données fictives
        assertTrue(produitFictif.peutEtreAjoute(Taille.M))
    }

    @Test
    fun `ajouter la meme ligne augmente la quantite`() {
        val panier = emptyList<LignePanier>().ajouter(ligne()).ajouter(ligne())
        assertEquals(1, panier.size)
        assertEquals(2, panier.single().quantite)
    }

    @Test
    fun `une autre taille ou un autre coloris cree une nouvelle ligne`() {
        val panier = emptyList<LignePanier>()
            .ajouter(ligne())
            .ajouter(ligne(taille = Taille.L))
            .ajouter(ligne(couleur = 2))
        assertEquals(3, panier.size)
        assertEquals(3, panier.nombreArticles())
    }

    @Test
    fun `le total tient compte des quantites`() {
        val panier = listOf(ligne(quantite = 2), ligne(taille = Taille.S))
        assertEquals(3 * 2990, panier.totalCentimes())
    }

    @Test
    fun `une quantite nulle supprime la ligne`() {
        val l = ligne()
        assertEquals(5, listOf(l).avecQuantite(l.id, 5).single().quantite)
        assertTrue(listOf(l).avecQuantite(l.id, 0).isEmpty())
    }
}
