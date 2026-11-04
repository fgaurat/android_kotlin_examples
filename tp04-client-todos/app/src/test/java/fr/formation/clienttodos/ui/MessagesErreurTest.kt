package fr.formation.clienttodos.ui

import fr.formation.clienttodos.data.ErreurTodo
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException

class MessagesErreurTest {

    private val http404 = HttpException(Response.error<Any>(404, "".toResponseBody()))

    @Test
    fun `erreur réseau, le message cite la cause technique`() {
        val message = ErreurTodo.Reseau(ConnectException("Failed to connect")).messagePourUtilisateur()

        assertTrue(message.startsWith("Serveur injoignable"))
        assertTrue(message.contains("ConnectException : Failed to connect"))
    }

    @Test
    fun `404 et erreur serveur`() {
        assertEquals("Tâche introuvable (404)", ErreurTodo.Introuvable(http404).messagePourUtilisateur())
        assertEquals("Erreur du serveur (code 500)", ErreurTodo.Serveur(500, http404).messagePourUtilisateur())
    }
}
