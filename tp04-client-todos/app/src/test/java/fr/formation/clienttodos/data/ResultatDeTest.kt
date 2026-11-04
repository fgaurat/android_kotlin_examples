package fr.formation.clienttodos.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ResultatDeTest {

    @Test
    fun `un succès est enveloppé dans Result success`() = runTest {
        assertEquals(Result.success(42), resultatDe { 42 })
    }

    @Test
    fun `une exception devient un Result en échec`() = runTest {
        val resultat = resultatDe { throw IOException("coupure") }

        assertTrue(resultat.exceptionOrNull() is IOException)
    }

    @Test(expected = CancellationException::class)
    fun `CancellationException est relancée, jamais avalée`() = runTest {
        resultatDe { throw CancellationException("écran quitté") }
    }

    @Test
    fun `runCatching, lui, avale l'annulation`() = runTest {
        // Le piège montré en cours : la coroutine annulée continuerait comme si de rien n'était
        val resultat = runCatching { throw CancellationException("écran quitté") }

        assertTrue(resultat.isFailure)
    }
}
