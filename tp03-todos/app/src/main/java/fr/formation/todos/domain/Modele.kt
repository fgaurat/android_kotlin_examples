package fr.formation.todos.domain

// Étape 1 : le modèle du domaine. Kotlin pur, aucun import Android : ce code se teste sur la JVM.
// Mêmes informations que https://jsonplaceholder.typicode.com/todos (userId, id, title, completed),
// avec des noms français : c'est exactement le modèle `Todo` du TP04, qui ajoutera le réseau.

/** Une tâche. Immuable : on la « modifie » en créant une copie avec `copy()`. */
data class Todo(
    val id: String,            // String comme dans le TP04 (JSON Server génère des identifiants texte)
    val titre: String,
    val terminee: Boolean,
    val utilisateurId: Int,
)

/** Filtres d'affichage : un ensemble fini de **valeurs** → `enum class`. */
enum class Filtre(val libelle: String) {
    TOUTES("Toutes"),
    A_FAIRE("À faire"),
    TERMINEES("Terminées"),
}

/**
 * Résultat de l'action « terminer une tâche » : un ensemble fini de **types**,
 * dont un seul porte une donnée → `sealed interface`. Un `when` sur ce type est exhaustif sans `else`.
 */
sealed interface ResultatAction {
    data class Succes(val todo: Todo) : ResultatAction
    data object DejaTerminee : ResultatAction
    data object Introuvable : ResultatAction
}

/** Étape 5 : un message adapté à chaque résultat, avec un `when` exhaustif. */
fun ResultatAction.message(): String = when (this) {
    is ResultatAction.Succes -> "Bravo, « ${todo.titre} » est terminée"   // smart cast : `todo` est accessible
    ResultatAction.DejaTerminee -> "Cette tâche était déjà terminée"
    ResultatAction.Introuvable -> "Aucune tâche ne porte cet identifiant"
}
