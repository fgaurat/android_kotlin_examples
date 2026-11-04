package fr.formation.taches.domain

/** Modèle métier en Kotlin pur : aucun import android.* dans ce package. */
data class Tache(
    val id: Long,
    val titre: String,
    val faite: Boolean = false,
    val priorite: Priorite = Priorite.Normale,
)

enum class Priorite { Basse, Normale, Haute }
