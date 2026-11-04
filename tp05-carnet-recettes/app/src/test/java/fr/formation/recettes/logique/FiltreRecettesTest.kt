package fr.formation.recettes.logique

import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.modele.Categorie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltreRecettesTest {

    @Test
    fun `sans recherche ni categorie, toutes les recettes sont gardees`() {
        assertEquals(recettesFictives, filtrerRecettes(recettesFictives, "", emptySet()))
    }

    @Test
    fun `les donnees fictives comptent 6 recettes et 3 categories`() {
        assertEquals(6, recettesFictives.size)
        assertEquals(Categorie.entries.toSet(), recettesFictives.map { it.categorie }.toSet())
    }

    @Test
    fun `la recherche ignore la casse et les espaces autour`() {
        val resultat = filtrerRecettes(recettesFictives, "  TARTE ", emptySet())
        assertEquals(listOf("Tarte Tatin"), resultat.map { it.nom })
    }

    @Test
    fun `la recherche porte aussi sur les ingredients`() {
        val resultat = filtrerRecettes(recettesFictives, "champignons", emptySet())
        assertEquals(setOf("3", "4"), resultat.map { it.id }.toSet())
    }

    @Test
    fun `le filtre par categorie garde uniquement les categories choisies`() {
        val resultat = filtrerRecettes(recettesFictives, "", setOf(Categorie.Dessert))
        assertTrue(resultat.isNotEmpty())
        assertTrue(resultat.all { it.categorie == Categorie.Dessert })
    }

    @Test
    fun `plusieurs categories se cumulent`() {
        val resultat = filtrerRecettes(recettesFictives, "", setOf(Categorie.Entree, Categorie.Dessert))
        assertEquals(4, resultat.size)
    }

    @Test
    fun `recherche et categorie se combinent`() {
        val resultat = filtrerRecettes(recettesFictives, "champignons", setOf(Categorie.Entree))
        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `basculer ajoute puis retire une categorie`() {
        val avec = emptySet<Categorie>().basculer(Categorie.Plat)
        assertEquals(setOf(Categorie.Plat), avec)
        assertEquals(emptySet<Categorie>(), avec.basculer(Categorie.Plat))
    }
}
