package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.data.remote.dto.MajTodoDto
import fr.formation.clienttodos.data.remote.dto.PageDto
import fr.formation.clienttodos.data.remote.dto.TodoDto
import fr.formation.clienttodos.data.remote.dto.UtilisateurDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Vérifie la configuration `json` de Reseau.kt sur des réponses réelles de JSON Server 1.x. */
class DtoJsonTest {

    @Test
    fun `décode un tableau de tâches avec des id en chaîne`() {
        val texte = """
            [
              {"userId": 1, "id": "1", "title": "delectus aut autem", "completed": false},
              {"userId": 1, "id": "yWeeNCMABkQ", "title": "Préparer le TP", "completed": true}
            ]
        """.trimIndent()

        val todos: List<TodoDto> = json.decodeFromString(texte)

        assertEquals(listOf("1", "yWeeNCMABkQ"), todos.map { it.id })
        assertEquals(true, todos[1].completed)
    }

    @Test
    fun `completed absent ou null prend la valeur par défaut`() {
        val absent: TodoDto = json.decodeFromString("""{"userId":1,"id":"2","title":"a"}""")
        val nul: TodoDto = json.decodeFromString("""{"userId":1,"id":"3","title":"b","completed":null}""")

        assertFalse(absent.completed)
        assertFalse(nul.completed)          // coerceInputValues
    }

    @Test
    fun `les champs inconnus de users sont ignorés et username devient pseudo`() {
        val texte = """
            {"id": "1", "name": "Leanne Graham", "username": "Bret", "email": "Sincere@april.biz",
             "address": {"street": "Kulas Light", "city": "Gwenborough"}, "phone": "1-770-736-8031"}
        """.trimIndent()

        val utilisateur: UtilisateurDto = json.decodeFromString(texte)

        assertEquals("Bret", utilisateur.pseudo)
    }

    @Test(expected = SerializationException::class)
    fun `sans ignoreUnknownKeys, address fait planter le décodage`() {
        Json.decodeFromString<UtilisateurDto>(
            """{"id":"1","name":"L","username":"B","email":"e","address":{}}"""
        )
    }

    @Test(expected = SerializationException::class)
    fun `un id numérique dans un DTO en String est refusé`() {
        // C'est ce que renverrait json-server 0.17 : il faudrait alors repasser id en Int
        json.decodeFromString<TodoDto>("""{"userId":1,"id":1,"title":"a","completed":false}""")
    }

    @Test
    fun `un PATCH n'envoie que les champs renseignés`() {
        assertEquals("""{"completed":true}""", json.encodeToString(MajTodoDto(completed = true)))
        assertEquals("""{"title":"Nouveau"}""", json.encodeToString(MajTodoDto(title = "Nouveau")))
    }

    @Test
    fun `décode l'enveloppe de pagination`() {
        val texte = """
            {"first":1,"prev":1,"next":3,"last":20,"pages":20,"items":200,
             "data":[{"userId":1,"id":"11","title":"vero rerum temporibus dolor","completed":true}]}
        """.trimIndent()

        val page: PageDto<TodoDto> = json.decodeFromString(texte)

        assertEquals(3, page.next)
        assertEquals("11", page.data.single().id)
    }
}
