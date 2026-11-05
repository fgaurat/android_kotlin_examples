package fr.formation.todoapp.data.remote.dto

import kotlinx.serialization.Serializable

/** Réponse de GET /todos et GET /todos/{id}. Colle exactement au JSON de JSON Server. */
@Serializable
data class TodoDto(
    val userId: Int,
    val id: String,                     // chaîne avec JSON Server 1.x ("1", "yWeeNCMABkQ"…)
    val title: String,
    val completed: Boolean = false,     // facultatif dans le JSON
)

/** Corps de POST /todos et PUT /todos/{id} : pas d'id, c'est le serveur qui l'attribue. */
@Serializable
data class NouveauTodoDto(
    val userId: Int,
    val title: String,
    val completed: Boolean,
)

/**
 * Corps de PATCH /todos/{id} : uniquement les champs modifiés.
 * Avec `explicitNulls = false`, les champs à null ne sont pas écrits dans le JSON.
 */
@Serializable
data class MajTodoDto(
    val title: String? = null,
    val completed: Boolean? = null,
)
