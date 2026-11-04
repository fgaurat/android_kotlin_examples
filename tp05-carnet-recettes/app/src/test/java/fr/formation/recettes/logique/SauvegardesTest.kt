package fr.formation.recettes.logique

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.modele.Categorie
import org.junit.Assert.assertEquals
import org.junit.Test

/** Aller-retour save → restore : ce que rememberSaveable fait lors d'une rotation. */
class SauvegardesTest {

    private val scope = object : SaverScope {
        override fun canBeSaved(value: Any) = true
    }

    private fun <T : Any> allerRetour(saver: Saver<T, Any>, valeur: T): T =
        saver.restore(with(saver) { scope.save(valeur) }!!)!!

    @Test
    fun `les recettes survivent a la sauvegarde`() {
        assertEquals(recettesFictives, allerRetour(RecettesSaver, recettesFictives))
    }

    @Test
    fun `les categories survivent a la sauvegarde`() {
        val categories = setOf(Categorie.Plat, Categorie.Dessert)
        assertEquals(categories, allerRetour(CategoriesSaver, categories))
    }

    @Test
    fun `le formulaire survit a la sauvegarde`() {
        val formulaire = FormulaireRecette("Crêpes", "30", 4f, Categorie.Dessert, "farine\nlait", "mélanger")
        assertEquals(formulaire, allerRetour(FormulaireSaver, formulaire))
    }
}
