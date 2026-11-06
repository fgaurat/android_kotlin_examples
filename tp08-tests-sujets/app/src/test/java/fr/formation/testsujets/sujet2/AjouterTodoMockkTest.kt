package fr.formation.testsujets.sujet2

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

// Étape 2 — Les mêmes règles, avec un MOCK (MockK)
// - mockk<TodoRepository>() : un objet généré, qui ne sait rien faire tant qu'on ne le programme pas
//   (un appel non programmé lève une exception : le mock est « strict »)
// - coEvery { … } returns / throws : programmer une fonction SUSPEND (every pour une fonction normale)
// - coVerify { … } : vérifier les APPELS (exactly = 0 : jamais appelé)
// Fake ou mock ? Fake : réutilisable, vérifie l'état, robuste aux changements internes.
//   Mock : rapide à écrire pour un cas précis, vérifie les interactions (« a-t-on appelé ajouter ? »).
class AjouterTodoMockkTest {

    private val repository = mockk<TodoRepository>()
    private val ajouterTodo = AjouterTodo(repository)

    @Test
    fun `un titre valide est vérifié puis ajouté`() = runTest {
        val cree = Todo("7", "Acheter du pain")
        coEvery { repository.lister() } returns emptyList()
        coEvery { repository.ajouter("Acheter du pain") } returns cree

        assertEquals(ResultatAjout.Ajoutee(cree), ajouterTodo("Acheter du pain"))
        coVerifyOrder {                         // l'ordre compte : d'abord vérifier, ensuite ajouter
            repository.lister()
            repository.ajouter("Acheter du pain")
        }
    }

    @Test
    fun `un doublon n'appelle jamais ajouter`() = runTest {
        coEvery { repository.lister() } returns listOf(Todo("1", "Acheter du pain"))

        assertEquals(ResultatAjout.Doublon, ajouterTodo("acheter du pain"))
        coVerify(exactly = 0) { repository.ajouter(any()) }
    }

    @Test
    fun `un titre vide n'appelle aucune fonction du repository`() = runTest {
        assertEquals(ResultatAjout.TitreInvalide, ajouterTodo(""))
        coVerify(exactly = 0) { repository.lister() }          // rien n'est programmé : un appel aurait échoué
    }

    @Test
    fun `une IOException donne Echec`() = runTest {
        coEvery { repository.lister() } throws IOException("timeout")

        assertEquals(ResultatAjout.Echec("Serveur injoignable"), ajouterTodo("Réviser Kotlin"))
    }
}
