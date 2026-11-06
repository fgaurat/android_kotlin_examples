package fr.formation.testsujets.sujet2

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

// Étape 2 — Tester du code suspend avec un FAKE
// - runTest { … } : un test peut appeler des fonctions suspend (et les delay() y sont instantanés, étape 3)
// - le fake est une vraie classe : on lui met des données, on agit, on regarde son état
class AjouterTodoFakeTest {

    private val repository = FakeTodoRepository()
    private val ajouterTodo = AjouterTodo(repository)

    @Test
    fun `un titre valide est ajouté, sans les espaces`() = runTest {
        val resultat = ajouterTodo("  Acheter du pain ")

        assertEquals(ResultatAjout.Ajoutee(Todo("1", "Acheter du pain")), resultat)
        assertEquals(listOf("Acheter du pain"), repository.todos.map { it.titre })   // l'état du fake
    }

    @Test
    fun `un titre déjà présent est refusé, casse ignorée`() = runTest {
        repository.todos += Todo("1", "Acheter du pain")

        assertEquals(ResultatAjout.Doublon, ajouterTodo("ACHETER DU PAIN"))
        assertEquals(0, repository.appelsAjouter)
    }

    @Test
    fun `un titre vide ne contacte même pas le repository`() = runTest {
        repository.erreur = IOException()      // si le repository était appelé, on aurait Echec

        assertEquals(ResultatAjout.TitreInvalide, ajouterTodo("   "))
    }

    @Test
    fun `une erreur réseau donne Echec`() = runTest {
        repository.erreur = IOException("timeout")

        assertEquals(ResultatAjout.Echec("Serveur injoignable"), ajouterTodo("Réviser Kotlin"))
    }
}
