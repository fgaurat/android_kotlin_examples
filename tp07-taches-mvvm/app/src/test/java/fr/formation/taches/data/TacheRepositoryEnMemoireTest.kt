package fr.formation.taches.data

import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TacheRepositoryEnMemoireTest {

    private val lire = Tache(1, "Lire", faite = false, priorite = Priorite.Normale)
    private val coder = Tache(2, "Coder", faite = true, priorite = Priorite.Haute)
    private val repository = TacheRepositoryEnMemoire(listOf(lire, coder))

    @Test
    fun `la première émission arrive après la latence simulée`() = runTest {
        val taches = repository.taches.first()

        assertEquals(listOf(lire, coder), taches)
        assertEquals(300, testScheduler.currentTime)      // temps virtuel : aucun vrai délai
    }

    @Test
    fun `ajouter attribue un nouvel id après 300 ms`() = runTest {
        val debut = testScheduler.currentTime
        repository.ajouter("Tester", Priorite.Basse)

        assertEquals(300, testScheduler.currentTime - debut)
        assertEquals(Tache(3, "Tester", faite = false, priorite = Priorite.Basse), repository.taches.first().last())
    }

    @Test
    fun `basculer inverse faite`() = runTest {
        repository.basculer(1)
        repository.basculer(2)

        val taches = repository.taches.first()
        assertTrue(taches.first { it.id == 1L }.faite)
        assertFalse(taches.first { it.id == 2L }.faite)
    }

    @Test
    fun `supprimer retire la tâche, un id inconnu ne change rien`() = runTest {
        repository.supprimer(1)
        repository.supprimer(42)

        assertEquals(listOf(coder), repository.taches.first())
    }

    @Test
    fun `les ids ne sont jamais réutilisés après une suppression`() = runTest {
        repository.supprimer(2)
        repository.ajouter("Nouvelle", Priorite.Normale)

        assertEquals(3L, repository.taches.first().last().id)
    }
}
