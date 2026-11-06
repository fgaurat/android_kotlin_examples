package fr.formation.testsujets.sujet0

// Étape 0 — Le CODE testé : des fonctions pures (même entrée → même sortie, aucun effet de bord)
// C'est le code le plus simple à tester : ni Android, ni coroutines, ni réseau.
// Les tests sont dans src/test/java/fr/formation/testsujets/sujet0/TodoReglesTest.kt
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/testsujets/domain/Todo.kt           Todo
// app/src/main/java/fr/formation/testsujets/domain/TodoRegles.kt     TodoRegles
// app/src/test/java/fr/formation/testsujets/domain/TodoReglesTest.kt TodoReglesTest  ← MÊME package que le code
//   src/test/ : tests JVM, rapides, sans émulateur (« Run » dans Android Studio, ou ./gradlew testDebugUnitTest)

data class Todo(val id: String, val titre: String, val terminee: Boolean = false)

object TodoRegles {

    fun restantes(todos: List<Todo>): Int = todos.count { !it.terminee }

    // Division entière : 1 sur 3 → 33
    fun pourcentageTermine(todos: List<Todo>): Int =
        if (todos.isEmpty()) 0 else todos.count { it.terminee } * 100 / todos.size

    fun resume(todos: List<Todo>): String = when (val n = restantes(todos)) {
        0 -> "Tout est fait"
        1 -> "1 tâche restante"
        else -> "$n tâches restantes"
    }

    // Un titre : non vide, 50 caractères au plus (espaces autour ignorés)
    fun titreValide(titre: String): Boolean = titre.isNotBlank() && titre.trim().length <= 50
}
