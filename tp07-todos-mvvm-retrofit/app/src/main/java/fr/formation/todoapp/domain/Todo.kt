package fr.formation.todoapp.domain

/** Modèle métier en Kotlin pur : aucune dépendance vers Android, Retrofit ou kotlinx.serialization. */
data class Todo(
    val id: String,
    val titre: String,
    val terminee: Boolean,
    val utilisateurId: Int,
)
