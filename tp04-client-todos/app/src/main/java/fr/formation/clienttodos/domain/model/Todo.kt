package fr.formation.clienttodos.domain.model

/** Modèle métier : noms explicites, aucune dépendance vers Retrofit ou kotlinx.serialization. */
data class Todo(
    val id: String,
    val titre: String,
    val terminee: Boolean,
    val utilisateurId: Int,
)
