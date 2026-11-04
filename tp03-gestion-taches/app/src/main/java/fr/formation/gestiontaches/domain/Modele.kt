package fr.formation.gestiontaches.domain

// Étape 1 : le modèle. Kotlin pur, aucun import Android : tout se teste sur la JVM.

/**
 * Une tâche, avec la structure de `https://jsonplaceholder.typicode.com/todos`
 * (`id`, `title`, `completed`, `userId`), traduite en français.
 *
 * L'`id` est une chaîne, comme avec JSON Server 1.x (`"id": "1"`).
 * Que des `val` : une tâche ne change pas, on en crée une nouvelle avec `copy()`.
 */
data class Todo(
    val id: String,
    val titre: String,
    val terminee: Boolean = false,
    val utilisateurId: Int,
)

/** Un ensemble fini de **valeurs** : une `enum class` suffit. */
enum class Filtre { TOUTES, A_FAIRE, TERMINEES }

/**
 * Un ensemble fini de **types** : seul `Succes` porte une donnée.
 * Le compilateur connaît tous les cas : un `when` sur un `ResultatAction` n'a pas besoin de `else`.
 */
sealed interface ResultatAction {
    data class Succes(val todo: Todo) : ResultatAction
    data object DejaTerminee : ResultatAction
    data object Introuvable : ResultatAction
}
