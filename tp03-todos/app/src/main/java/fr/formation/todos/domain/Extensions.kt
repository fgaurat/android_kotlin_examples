package fr.formation.todos.domain

// Étape 3 : extensions sur le modèle, puis étapes 4 et 6 : calculs sur les collections.

/** Titre tronqué à 20 caractères (propriété d'extension, recalculée à chaque accès). */
val Todo.titreCourt: String
    get() = if (titre.length <= 20) titre else titre.take(19).trimEnd() + "…"

/** Les tâches qui restent à faire. */
fun List<Todo>.aFaire(): List<Todo> = filter { !it.terminee }

/** Les tâches terminées (référence de propriété : `Todo::terminee` est une fonction `(Todo) -> Boolean`). */
fun List<Todo>.terminees(): List<Todo> = filter(Todo::terminee)

/** Applique un [Filtre] : `when` exhaustif sur une enum, sans `else`. */
fun List<Todo>.selon(filtre: Filtre): List<Todo> = when (filtre) {
    Filtre.TOUTES -> this
    Filtre.A_FAIRE -> aFaire()
    Filtre.TERMINEES -> terminees()
}

/** Critère réutilisable, passé par référence de fonction : `TodoStore.filtrer(::aUnTitreLong)`. */
fun aUnTitreLong(todo: Todo): Boolean = todo.titre.length > 40

// --- Étape 4 : fonctions de collections ---------------------------------------------------------

/** Tâches groupées par utilisateur : `Map<Int, List<Todo>>`. */
fun List<Todo>.parUtilisateur(): Map<Int, List<Todo>> = groupBy { it.utilisateurId }

/** Nombre de tâches par utilisateur. `groupingBy { }.eachCount()` compte sans créer les listes. */
fun List<Todo>.nombreParUtilisateur(): Map<Int, Int> = groupingBy { it.utilisateurId }.eachCount()

/** Utilisateur qui a le plus de tâches, `null` si la liste est vide. */
fun List<Todo>.utilisateurLePlusActif(): Int? = nombreParUtilisateur().maxByOrNull { it.value }?.key

/** Tâche au titre le plus long, `null` si la liste est vide. */
fun List<Todo>.titreLePlusLong(): Todo? = maxByOrNull { it.titre.length }

/** Tâches à faire d'abord (`false` < `true`), puis par ordre alphabétique. */
fun List<Todo>.triees(): List<Todo> = sortedWith(compareBy(Todo::terminee, Todo::titre))

/** Index par identifiant : accès direct `index["4"]`. */
fun List<Todo>.indexParId(): Map<String, Todo> = associateBy { it.id }

// --- Étape 6 : résumé texte ---------------------------------------------------------------------

fun List<Todo>.pourcentageTerminees(): Int = if (isEmpty()) 0 else terminees().size * 100 / size

/** Par exemple : `10 tâches, 5 terminées (50 %), utilisateur le plus actif : 1`. */
fun List<Todo>.resume(): String = buildString {
    append("$size tâches, ${terminees().size} terminées (${pourcentageTerminees()} %)")
    utilisateurLePlusActif()?.let { append(", utilisateur le plus actif : $it") }
}
