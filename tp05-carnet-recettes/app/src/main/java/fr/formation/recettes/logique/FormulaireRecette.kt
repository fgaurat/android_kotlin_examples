package fr.formation.recettes.logique

import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.modele.Recette

const val NOM_LONGUEUR_MAX = 60
const val DUREE_MAX_MINUTES = 24 * 60
const val DIFFICULTE_MIN = 1
const val DIFFICULTE_MAX = 5

enum class ErreurNom { Obligatoire, TropLong }
enum class ErreurDuree { Obligatoire, NonNumerique, HorsLimites }

/**
 * État du formulaire d'ajout, tel que saisi (la durée reste une chaîne tant qu'elle n'est pas validée).
 * Conservé par `rememberSaveable` (rotation, mort du processus) grâce à `FormulaireSaver`.
 */
data class FormulaireRecette(
    val nom: String = "",
    val duree: String = "",
    val difficulte: Float = 2f,
    val categorie: Categorie = Categorie.Plat,
    val ingredients: String = "",     // un ingrédient par ligne
    val etapes: String = "",          // une étape par ligne
) {

    val erreurNom: ErreurNom?
        get() = when {
            nom.isBlank() -> ErreurNom.Obligatoire
            nom.trim().length > NOM_LONGUEUR_MAX -> ErreurNom.TropLong
            else -> null
        }

    val erreurDuree: ErreurDuree?
        get() {
            if (duree.isBlank()) return ErreurDuree.Obligatoire
            val minutes = duree.trim().toIntOrNull() ?: return ErreurDuree.NonNumerique
            return if (minutes in 1..DUREE_MAX_MINUTES) null else ErreurDuree.HorsLimites
        }

    val estValide: Boolean
        get() = erreurNom == null && erreurDuree == null

    /** Convertit le formulaire (supposé valide) en recette. */
    fun versRecette(id: String): Recette {
        check(estValide) { "Formulaire invalide" }
        return Recette(
            id = id,
            nom = nom.trim(),
            categorie = categorie,
            dureeMinutes = duree.trim().toInt(),
            ingredients = ingredients.lignesNonVides(),
            etapes = etapes.lignesNonVides(),
            difficulte = difficulte.toInt().coerceIn(DIFFICULTE_MIN, DIFFICULTE_MAX),
        )
    }
}

private fun String.lignesNonVides(): List<String> =
    lines().map { it.trim() }.filter { it.isNotEmpty() }
