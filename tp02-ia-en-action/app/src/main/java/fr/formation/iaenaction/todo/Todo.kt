package fr.formation.iaenaction.todo

/*
 * Étape 3 du TP : data class Todo et fonction resume(), générées par l'IA puis corrigées.
 *
 * Prompt utilisé (celui du support) :
 *   « Écris une data class Kotlin `Todo` qui correspond au JSON de
 *     https://jsonplaceholder.typicode.com/todos (userId, id, title, completed),
 *     avec des propriétés en français. Ajoute une fonction d'extension qui résume
 *     une liste : « 3 tâches, 1 terminée ». »
 *
 * Réponse type de l'IA (À NE PAS UTILISER TELLE QUELLE) :
 *
 *   data class Todo(
 *       val id: Int,
 *       val titre: String,
 *       val terminee: Boolean,
 *       val utilisateurId: Int,
 *   )
 *
 *   fun List<Todo>.resume(): String {
 *       val terminees = count { it.terminee }
 *       return "$size tâches, $terminees terminées"
 *   }
 *
 * Ce qui a été vérifié (curl https://jsonplaceholder.typicode.com/todos/1, le 01/11/2026) :
 *   {"userId": 1, "id": 1, "title": "delectus aut autem", "completed": false}
 *
 * Ce qui a été corrigé :
 *   1. `id: Int` -> `id: String` : JSONPlaceholder renvoie 1, mais JSON Server 1.x (utilisé plus tard
 *      dans la formation) renvoie "1" et génère des identifiants texte ("a3f9") pour les créations.
 *      Une chaîne accepte les deux ; le parseur (TodoJson.kt) convertit l'entier en "1".
 *   2. Pluriel : « 1 terminées », « 0 tâches », « 1 tâches » sont faux. En français, 0 et 1 sont au
 *      singulier : « 0 tâche, 0 terminée », « 1 tâche, 1 terminée », « 3 tâches, 2 terminées ».
 *   3. `title` -> `titre`, `completed` -> `terminee`, `userId` -> `utilisateurId` : renommer les
 *      propriétés casse le décodage automatique. La correspondance est faite à la main dans
 *      TodoJson.kt (au jour 3, avec kotlinx.serialization : @SerialName("title")).
 *   4. Ordre des propriétés aligné sur le JSON, KDoc ajoutée.
 */

/**
 * Une tâche de `https://jsonplaceholder.typicode.com/todos`.
 *
 * @property utilisateurId champ JSON `userId`
 * @property id champ JSON `id` ; texte pour accepter `1` (JSONPlaceholder) comme `"1"` (JSON Server 1.x)
 * @property titre champ JSON `title`
 * @property terminee champ JSON `completed`
 */
data class Todo(
    val utilisateurId: Int,
    val id: String,
    val titre: String,
    val terminee: Boolean,
)

/**
 * Résumé d'une liste de tâches, par exemple « 3 tâches, 1 terminée ».
 *
 * Accord en français : 0 et 1 au singulier, à partir de 2 au pluriel.
 */
fun List<Todo>.resume(): String {
    val terminees = count { it.terminee }
    return "${accorder(size, "tâche", "tâches")}, ${accorder(terminees, "terminée", "terminées")}"
}

/** « 0 tâche », « 1 tâche », « 2 tâches » : en français, le pluriel commence à 2. */
internal fun accorder(nombre: Int, singulier: String, pluriel: String): String =
    "$nombre ${if (nombre >= 2) pluriel else singulier}"
