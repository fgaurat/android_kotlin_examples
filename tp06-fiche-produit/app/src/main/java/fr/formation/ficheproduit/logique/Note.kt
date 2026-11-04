package fr.formation.ficheproduit.logique

import kotlin.math.roundToInt

enum class EtatEtoile { Pleine, Demie, Vide }

/** Décompose une note (0 à [max]) en étoiles, arrondie à la demi-étoile la plus proche. */
fun etoilesPourNote(note: Float, max: Int = 5): List<EtatEtoile> {
    val demiEtoiles = (note.coerceIn(0f, max.toFloat()) * 2).roundToInt()
    return List(max) { indice ->
        val seuil = (indice + 1) * 2
        when {
            demiEtoiles >= seuil -> EtatEtoile.Pleine
            demiEtoiles == seuil - 1 -> EtatEtoile.Demie
            else -> EtatEtoile.Vide
        }
    }
}
