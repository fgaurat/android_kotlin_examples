package fr.formation.iaenaction.todo

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Correspondance JSON -> Kotlin, faite à la main avec `org.json` (fourni par Android).
 *
 * | JSON        | Kotlin          |
 * |-------------|-----------------|
 * | `userId`    | `utilisateurId` |
 * | `id`        | `id` (texte)    |
 * | `title`     | `titre`         |
 * | `completed` | `terminee`      |
 *
 * Fonctions pures, testables sans réseau ni appareil (voir TodoJsonTest).
 */
object TodoJson {

    /**
     * Analyse un objet tâche, par exemple la réponse de `/todos/1` :
     * `{"userId": 1, "id": 1, "title": "delectus aut autem", "completed": false}`.
     *
     * @throws JSONException si le JSON est invalide, si un champ manque ou a un type inattendu.
     */
    fun analyserTodo(json: String): Todo = depuis(JSONObject(json))

    /**
     * Analyse un tableau de tâches, par exemple la réponse de `/todos`.
     *
     * @throws JSONException si le JSON est invalide ou si une tâche est incomplète.
     */
    fun analyserTodos(json: String): List<Todo> {
        val tableau = JSONArray(json)
        return List(tableau.length()) { i -> depuis(tableau.getJSONObject(i)) }
    }

    /** @throws JSONException si un champ manque ou a un type inattendu. */
    fun depuis(objet: JSONObject): Todo = Todo(
        utilisateurId = objet.getInt("userId"),
        id = identifiant(objet),
        titre = objet.getString("title"),
        terminee = objet.getBoolean("completed"),
    )

    /**
     * `id` accepté sous deux formes : nombre entier (`1`, JSONPlaceholder) ou chaîne (`"1"`, JSON Server 1.x).
     * Un nombre décimal, un booléen ou une chaîne vide sont refusés.
     */
    private fun identifiant(objet: JSONObject): String =
        when (val valeur = objet.get("id")) {
            is String -> valeur.ifBlank { throw JSONException("Champ id vide") }
            is Int, is Long -> valeur.toString()
            else -> throw JSONException("Champ id inattendu : $valeur")
        }
}
