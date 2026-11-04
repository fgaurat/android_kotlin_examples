package fr.formation.gestiontaches.domain

import kotlin.math.roundToInt

// Étapes 3, 4 et 6 : extensions sur Todo et List<Todo>.
// Des extensions plutôt que des méthodes de TodoStore : les mêmes calculs servent pour le magasin,
// une liste filtrée, une liste vide de test ou les 100 000 tâches générées.

const val LONGUEUR_TITRE_COURT = 20

/** Étape 3 : propriété d'extension, le titre tronqué à 20 caractères au plus (points de suspension compris). */
val Todo.titreCourt: String
    get() = if (titre.length <= LONGUEUR_TITRE_COURT) titre
    else titre.take(LONGUEUR_TITRE_COURT - 1).trimEnd() + "…"

/** Étape 3 : les tâches à faire. */
fun List<Todo>.aFaire(): List<Todo> = filterNot { it.terminee }

/** Étape 3 : les tâches terminées (une propriété `Boolean` sert de prédicat). */
fun List<Todo>.terminees(): List<Todo> = filter(Todo::terminee)

/** Étape 3 : `when` exhaustif sur l'enum, sans `else`. */
fun List<Todo>.selon(filtre: Filtre): List<Todo> = when (filtre) {
    Filtre.TOUTES -> this
    Filtre.A_FAIRE -> aFaire()
    Filtre.TERMINEES -> terminees()
}

/** Étape 4 : les tâches groupées par utilisateur (`groupBy`). */
fun List<Todo>.parUtilisateur(): Map<Int, List<Todo>> = groupBy { it.utilisateurId }

/** Étape 4 : le nombre de tâches par utilisateur (`groupingBy` compte sans construire les listes). */
fun List<Todo>.nombreParUtilisateur(): Map<Int, Int> = groupingBy { it.utilisateurId }.eachCount()

/** Étape 4 : l'utilisateur qui a le plus de tâches ; `null` si la liste est vide. */
fun List<Todo>.utilisateurLePlusActif(): Int? = nombreParUtilisateur().maxByOrNull { it.value }?.key

/** Étape 4 : la tâche au titre le plus long ; `null` si la liste est vide. */
fun List<Todo>.titreLePlusLong(): Todo? = maxByOrNull { it.titre.length }

/** Étape 4 : à faire d'abord (`false` < `true`), puis par titre. */
fun List<Todo>.triees(): List<Todo> = sortedWith(compareBy(Todo::terminee, Todo::titre))

/** Étape 4 : accès direct par id (`associateBy`). */
fun List<Todo>.indexParId(): Map<String, Todo> = associateBy { it.id }

/** Étape 6 : fonction ordinaire, passée à `filtrer` par référence (`TodoStore.filtrer(::estAFaire)`). */
fun estAFaire(todo: Todo): Boolean = !todo.terminee

/** Étape 6 : résumé du type `10 tâches, 5 terminées (50 %)`. */
fun List<Todo>.resume(): String = formaterResume(total = size, terminees = count { it.terminee })

/** Construit le résumé avec `buildString` ; partagé avec la version `Sequence`. */
internal fun formaterResume(total: Int, terminees: Int): String = buildString {
    append(total).append(if (total > 1) " tâches" else " tâche")
    append(", ")
    append(terminees).append(if (terminees > 1) " terminées" else " terminée")
    if (total > 0) {
        append(" (").append((terminees * 100.0 / total).roundToInt()).append(" %)")
    }
}
