package fr.formation.taches.ui.taches

import fr.formation.taches.domain.Tache
import org.junit.Assert.assertEquals
import org.junit.Test

class FiltreTest {

    private val aFaire = Tache(1, "À faire", faite = false)
    private val faite = Tache(2, "Faite", faite = true)
    private val toutes = listOf(aFaire, faite)

    @Test
    fun `chaque filtre ne garde que les tâches attendues`() {
        assertEquals(toutes, toutes.filter { Filtre.Toutes.accepte(it) })
        assertEquals(listOf(aFaire), toutes.filter { Filtre.AFaire.accepte(it) })
        assertEquals(listOf(faite), toutes.filter { Filtre.Faites.accepte(it) })
    }
}
