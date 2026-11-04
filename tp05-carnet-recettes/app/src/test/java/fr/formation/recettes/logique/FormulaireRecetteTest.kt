package fr.formation.recettes.logique

import fr.formation.recettes.modele.Categorie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormulaireRecetteTest {

    private val valide = FormulaireRecette(nom = "Crêpes", duree = "30", difficulte = 2f)

    @Test
    fun `un formulaire vierge est invalide`() {
        val formulaire = FormulaireRecette()
        assertEquals(ErreurNom.Obligatoire, formulaire.erreurNom)
        assertEquals(ErreurDuree.Obligatoire, formulaire.erreurDuree)
        assertFalse(formulaire.estValide)
    }

    @Test
    fun `un nom et une duree corrects rendent le formulaire valide`() {
        assertNull(valide.erreurNom)
        assertNull(valide.erreurDuree)
        assertTrue(valide.estValide)
    }

    @Test
    fun `un nom compose d'espaces est refuse`() {
        assertEquals(ErreurNom.Obligatoire, valide.copy(nom = "   ").erreurNom)
    }

    @Test
    fun `un nom trop long est refuse`() {
        assertEquals(ErreurNom.TropLong, valide.copy(nom = "a".repeat(NOM_LONGUEUR_MAX + 1)).erreurNom)
        assertNull(valide.copy(nom = "a".repeat(NOM_LONGUEUR_MAX)).erreurNom)
    }

    @Test
    fun `une duree non numerique est refusee`() {
        assertEquals(ErreurDuree.NonNumerique, valide.copy(duree = "12a").erreurDuree)
        assertEquals(ErreurDuree.NonNumerique, valide.copy(duree = "1.5").erreurDuree)
    }

    @Test
    fun `une duree hors limites est refusee`() {
        assertEquals(ErreurDuree.HorsLimites, valide.copy(duree = "0").erreurDuree)
        assertEquals(ErreurDuree.HorsLimites, valide.copy(duree = "-5").erreurDuree)
        assertEquals(ErreurDuree.HorsLimites, valide.copy(duree = "${DUREE_MAX_MINUTES + 1}").erreurDuree)
        assertNull(valide.copy(duree = "$DUREE_MAX_MINUTES").erreurDuree)
    }

    @Test
    fun `la conversion en recette nettoie la saisie`() {
        val recette = FormulaireRecette(
            nom = "  Crêpes ",
            duree = " 30 ",
            difficulte = 3.4f,
            categorie = Categorie.Dessert,
            ingredients = "250 g de farine\n\n  3 œufs \n",
            etapes = "Mélanger\nCuire",
        ).versRecette(id = "42")

        assertEquals("42", recette.id)
        assertEquals("Crêpes", recette.nom)
        assertEquals(30, recette.dureeMinutes)
        assertEquals(3, recette.difficulte)
        assertEquals(Categorie.Dessert, recette.categorie)
        assertEquals(listOf("250 g de farine", "3 œufs"), recette.ingredients)
        assertEquals(listOf("Mélanger", "Cuire"), recette.etapes)
        assertFalse(recette.favori)
    }

    @Test(expected = IllegalStateException::class)
    fun `convertir un formulaire invalide leve une exception`() {
        FormulaireRecette().versRecette(id = "1")
    }
}
