package fr.formation.clienttodos.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Réponse de GET /users/{id} : `address`, `phone`, `company`… sont ignorés (ignoreUnknownKeys). */
@Serializable
data class UtilisateurDto(
    val id: String,
    val name: String,
    @SerialName("username") val pseudo: String,
    val email: String,
)
