package fr.formation.ficheproduit.logique

import fr.formation.ficheproduit.logique.EtatEtoile.Demie
import fr.formation.ficheproduit.logique.EtatEtoile.Pleine
import fr.formation.ficheproduit.logique.EtatEtoile.Vide
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTest {

    @Test
    fun `une note entiere donne des etoiles pleines puis vides`() {
        assertEquals(listOf(Pleine, Pleine, Pleine, Vide, Vide), etoilesPourNote(3f))
    }

    @Test
    fun `une demi-note donne une demi-etoile`() {
        assertEquals(listOf(Pleine, Pleine, Pleine, Pleine, Demie), etoilesPourNote(4.5f))
    }

    @Test
    fun `la note est arrondie a la demi-etoile la plus proche`() {
        assertEquals(listOf(Pleine, Pleine, Demie, Vide, Vide), etoilesPourNote(2.3f))
        assertEquals(listOf(Pleine, Pleine, Pleine, Vide, Vide), etoilesPourNote(2.8f))
    }

    @Test
    fun `les notes hors bornes sont ramenees entre 0 et 5`() {
        assertEquals(List(5) { Vide }, etoilesPourNote(-1f))
        assertEquals(List(5) { Pleine }, etoilesPourNote(7f))
    }
}
