package fr.formation.clienttodos.domain.model

data class Utilisateur(
    val id: String,
    val nom: String,
    val pseudo: String,
    val email: String,
)

/** Bonus : une tâche et son auteur, chargés en parallèle. */
data class TodoAvecAuteur(
    val todo: Todo,
    val auteur: Utilisateur,
) {
    override fun toString() = "${todo.titre} — ${auteur.nom}"
}
