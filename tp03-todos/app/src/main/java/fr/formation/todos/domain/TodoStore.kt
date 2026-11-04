package fr.formation.todos.domain

/**
 * Étapes 2, 5 et 6 : la liste des tâches de l'application.
 *
 * `object` : une seule instance. La liste modifiable reste privée, seule une `List` en lecture seule
 * est exposée (même principe que `MutableStateFlow` privé / `StateFlow` public au jour 3).
 */
object TodoStore {

    // 10 tâches reprises telles quelles de https://jsonplaceholder.typicode.com/todos
    // (ids 1, 2, 3, 4, 8, 21, 22, 25, 41 et 44) : 3 utilisateurs, 5 tâches terminées.
    private val todosInitiaux = listOf(
        Todo("1", "delectus aut autem", terminee = false, utilisateurId = 1),
        Todo("2", "quis ut nam facilis et officia qui", terminee = false, utilisateurId = 1),
        Todo("3", "fugiat veniam minus", terminee = false, utilisateurId = 1),
        Todo("4", "et porro tempora", terminee = true, utilisateurId = 1),
        Todo("8", "quo adipisci enim quam ut ab", terminee = true, utilisateurId = 1),
        Todo("21", "suscipit repellat esse quibusdam voluptatem incidunt", terminee = false, utilisateurId = 2),
        Todo("22", "distinctio vitae autem nihil ut molestias quo", terminee = true, utilisateurId = 2),
        Todo("25", "voluptas quo tenetur perspiciatis explicabo natus", terminee = true, utilisateurId = 2),
        Todo("41", "aliquid amet impedit consequatur aspernatur placeat eaque fugiat suscipit", terminee = false, utilisateurId = 3),
        Todo("44", "cum debitis quis accusamus doloremque ipsa natus sapiente omnis", terminee = true, utilisateurId = 3),
    )

    private val _todos: MutableList<Todo> = todosInitiaux.toMutableList()

    /** Copie en lecture seule : l'appelant ne peut pas modifier la liste dans notre dos. */
    val todos: List<Todo>
        get() = _todos.toList()

    /** Étape 5 : termine une tâche, si elle existe et si elle n'est pas déjà terminée. */
    fun terminer(id: String): ResultatAction {
        val index = _todos.indexOfFirst { it.id == id }
        if (index == -1) return ResultatAction.Introuvable

        val todo = _todos[index]
        if (todo.terminee) return ResultatAction.DejaTerminee

        val terminee = todo.copy(terminee = true)   // on remplace l'élément, on ne le modifie pas
        _todos[index] = terminee
        return ResultatAction.Succes(terminee)
    }

    /** Remet une tâche « à faire » ; `false` si l'id est inconnu ou si la tâche n'était pas terminée. */
    fun rouvrir(id: String): Boolean {
        val index = _todos.indexOfFirst { it.id == id && it.terminee }
        if (index == -1) return false
        _todos[index] = _todos[index].copy(terminee = false)
        return true
    }

    /** Étape 6 : fonction d'ordre supérieur, le critère est une fonction `(Todo) -> Boolean`. */
    fun filtrer(critere: (Todo) -> Boolean): List<Todo> = _todos.filter(critere)

    // --- Bonus : ajouter et supprimer ----------------------------------------------------------

    /** Ajoute une tâche « à faire » avec l'identifiant suivant, et la renvoie. */
    fun ajouter(titre: String, utilisateurId: Int): Todo {
        val prochainId = (_todos.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
        val todo = Todo(prochainId.toString(), titre, terminee = false, utilisateurId = utilisateurId)
        _todos += todo
        return todo
    }

    /** Supprime la tâche ; `false` si l'id est inconnu. */
    fun supprimer(id: String): Boolean = _todos.removeAll { it.id == id }

    /** Remet la liste dans son état initial (utile pour les tests et la démo). */
    fun reinitialiser() {
        _todos.clear()
        _todos += todosInitiaux
    }
}
