package fr.formation.gestiontaches.domain

/**
 * Étapes 2, 5, 6 et bonus : le magasin de tâches.
 *
 * `object` : une seule instance dans l'application. La liste modifiable reste privée,
 * seule une `List` en lecture seule est exposée (même principe que `MutableStateFlow` / `StateFlow`).
 */
object TodoStore {

    // Étape 2 : 10 tâches copiées de https://jsonplaceholder.typicode.com/todos
    // (utilisateurs 1, 2 et 3 ; 5 terminées). Pas d'appel réseau : les données sont en dur.
    private val todosInitiaux = listOf(
        Todo("1", "delectus aut autem", terminee = false, utilisateurId = 1),
        Todo("2", "quis ut nam facilis et officia qui", terminee = false, utilisateurId = 1),
        Todo("4", "et porro tempora", terminee = true, utilisateurId = 1),
        Todo("8", "quo adipisci enim quam ut ab", terminee = true, utilisateurId = 1),
        Todo("21", "suscipit repellat esse quibusdam voluptatem incidunt", terminee = false, utilisateurId = 2),
        Todo("22", "distinctio vitae autem nihil ut molestias quo", terminee = true, utilisateurId = 2),
        Todo("26", "aliquam aut quasi", terminee = true, utilisateurId = 2),
        Todo("41", "aliquid amet impedit consequatur aspernatur placeat eaque fugiat suscipit", terminee = false, utilisateurId = 3),
        Todo("43", "tempore ut sint quis recusandae", terminee = true, utilisateurId = 3),
        Todo("45", "velit soluta adipisci molestias reiciendis harum", terminee = false, utilisateurId = 3),
    )

    private val _todos: MutableList<Todo> = todosInitiaux.toMutableList()

    /** Copie en lecture seule : l'appelant ne peut pas modifier le magasin, ni voir la liste changer sous ses yeux. */
    val todos: List<Todo>
        get() = _todos.toList()

    /** Étape 5 : termine une tâche, si elle existe et si elle n'est pas déjà terminée. */
    fun terminer(id: String): ResultatAction {
        val index = _todos.indexOfFirst { it.id == id }
        if (index == -1) return ResultatAction.Introuvable

        val todo = _todos[index]
        if (todo.terminee) return ResultatAction.DejaTerminee

        val terminee = todo.copy(terminee = true)   // nouvelle instance, l'ancienne n'est pas modifiée
        _todos[index] = terminee
        return ResultatAction.Succes(terminee)
    }

    /** Étape 6 : fonction d'ordre supérieur, le critère est une fonction `(Todo) -> Boolean`. */
    fun filtrer(critere: (Todo) -> Boolean): List<Todo> = _todos.filter(critere)

    /**
     * Bonus : ajoute une tâche à faire et la renvoie.
     * L'id suit le plus grand id numérique existant (JSON Server générerait un id aléatoire).
     */
    fun ajouter(titre: String, utilisateurId: Int): Todo {
        require(titre.isNotBlank()) { "Le titre ne peut pas être vide" }
        val prochainId = (_todos.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
        val todo = Todo(prochainId.toString(), titre.trim(), terminee = false, utilisateurId = utilisateurId)
        _todos += todo
        return todo
    }

    /** Bonus : supprime la tâche portant cet id ; `false` si elle n'existe pas. */
    fun supprimer(id: String): Boolean = _todos.removeAll { it.id == id }

    /** Remet le magasin dans son état initial (utile pour les tests et la démo). */
    fun reinitialiser() {
        _todos.clear()
        _todos += todosInitiaux
    }
}
