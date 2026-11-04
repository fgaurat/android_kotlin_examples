package fr.formation.parametres

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Étape 2 : écrits AVANT la migration.
 * Étape 6 : ils doivent rester verts APRÈS la migration, sans aucune modification.
 */
class ParametresViewModelTest {

    private val viewModel = ParametresViewModel()

    @Test
    fun `l'état initial est Celsius et thème clair`() {
        assertEquals(ParametresUiState(Unite.CELSIUS, themeSombre = false), viewModel.etat.value)
    }

    @Test
    fun `choisir Fahrenheit change l'unité`() {
        viewModel.choisirUnite(Unite.FAHRENHEIT)

        assertEquals(Unite.FAHRENHEIT, viewModel.etat.value.unite)
    }

    @Test
    fun `revenir à Celsius après Fahrenheit`() {
        viewModel.choisirUnite(Unite.FAHRENHEIT)
        viewModel.choisirUnite(Unite.CELSIUS)

        assertEquals(Unite.CELSIUS, viewModel.etat.value.unite)
    }

    @Test
    fun `activer le thème sombre`() {
        viewModel.activerThemeSombre(true)

        assertTrue(viewModel.etat.value.themeSombre)
    }

    @Test
    fun `désactiver le thème sombre`() {
        viewModel.activerThemeSombre(true)
        viewModel.activerThemeSombre(false)

        assertFalse(viewModel.etat.value.themeSombre)
    }

    @Test
    fun `changer l'unité ne touche pas au thème, et inversement`() {
        viewModel.activerThemeSombre(true)
        viewModel.choisirUnite(Unite.FAHRENHEIT)

        assertEquals(ParametresUiState(Unite.FAHRENHEIT, themeSombre = true), viewModel.etat.value)
    }
}
