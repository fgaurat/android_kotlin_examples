package fr.formation.testsujets.sujet1

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Étape 1 — Un test PARAMÉTRÉ : le même test, rejoué pour chaque ligne d'un tableau
// - @RunWith(Parameterized::class) + une fonction @Parameters qui renvoie les lignes
// - le constructeur reçoit les valeurs d'une ligne ; name = "{0} → {1}" apparaît dans le rapport
// À essayer : ajouter une ligne ("Haute", HAUTE) ; puis une ligne fausse, et lire quel cas échoue
@RunWith(Parameterized::class)
class PrioriteParametreTest(private val code: String?, private val attendue: Priorite) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "\"{0}\" → {1}")
        fun cas(): List<Array<Any?>> = listOf(
            arrayOf("HAUTE", Priorite.HAUTE),
            arrayOf("haute", Priorite.HAUTE),       // casse ignorée
            arrayOf(" basse ", Priorite.BASSE),     // espaces ignorés
            arrayOf("urgente", Priorite.NORMALE),   // inconnue → défaut
            arrayOf("", Priorite.NORMALE),
            arrayOf(null, Priorite.NORMALE),        // absente du JSON
        )
    }

    @Test
    fun `depuis convertit le code`() {
        assertEquals(attendue, Priorite.depuis(code))
    }
}
