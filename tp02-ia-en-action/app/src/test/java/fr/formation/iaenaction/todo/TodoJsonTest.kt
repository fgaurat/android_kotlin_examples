package fr.formation.iaenaction.todo

import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Analyse JSON -> Todo, sans réseau.
 * org.json est ajouté en testImplementation car l'android.jar des tests JVM n'en contient que des bouchons.
 */
class TodoJsonTest {

    @Test
    fun reponse_reelle_de_jsonplaceholder_id_entier() {
        // Réponse obtenue le 01/11/2026 : curl https://jsonplaceholder.typicode.com/todos/1
        val json = """
            {
              "userId": 1,
              "id": 1,
              "title": "delectus aut autem",
              "completed": false
            }
        """.trimIndent()

        assertEquals(
            Todo(utilisateurId = 1, id = "1", titre = "delectus aut autem", terminee = false),
            TodoJson.analyserTodo(json),
        )
    }

    @Test
    fun id_chaine_comme_json_server_1() {
        val json = """{"userId": 2, "id": "1", "title": "Réviser Kotlin", "completed": true}"""

        assertEquals(Todo(2, "1", "Réviser Kotlin", true), TodoJson.analyserTodo(json))
    }

    @Test
    fun id_chaine_non_numerique_genere_par_json_server() {
        val json = """{"userId": 1, "id": "a3f9", "title": "Nouvelle tâche", "completed": false}"""

        assertEquals("a3f9", TodoJson.analyserTodo(json).id)
    }

    @Test
    fun tableau_de_taches() {
        val json = """
            [
              {"userId": 1, "id": 1, "title": "delectus aut autem", "completed": false},
              {"userId": 1, "id": 4, "title": "et porro tempora", "completed": true}
            ]
        """.trimIndent()

        val todos = TodoJson.analyserTodos(json)

        assertEquals(listOf("1", "4"), todos.map { it.id })
        assertEquals("2 tâches, 1 terminée", todos.resume())
    }

    @Test(expected = JSONException::class)
    fun champ_title_manquant() {
        TodoJson.analyserTodo("""{"userId": 1, "id": 1, "completed": false}""")
    }

    @Test(expected = JSONException::class)
    fun champ_id_manquant() {
        TodoJson.analyserTodo("""{"userId": 1, "title": "x", "completed": false}""")
    }

    @Test(expected = JSONException::class)
    fun champ_completed_manquant() {
        TodoJson.analyserTodo("""{"userId": 1, "id": 1, "title": "x"}""")
    }

    @Test(expected = JSONException::class)
    fun id_decimal_refuse() {
        TodoJson.analyserTodo("""{"userId": 1, "id": 1.5, "title": "x", "completed": false}""")
    }

    @Test(expected = JSONException::class)
    fun id_vide_refuse() {
        TodoJson.analyserTodo("""{"userId": 1, "id": "", "title": "x", "completed": false}""")
    }

    @Test(expected = JSONException::class)
    fun json_invalide() {
        TodoJson.analyserTodo("<html>Not found</html>")
    }
}
