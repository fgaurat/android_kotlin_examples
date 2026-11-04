package fr.formation.recettes.modele

import androidx.annotation.StringRes
import fr.formation.recettes.R

/** Catégorie d'une recette : le libellé vient des ressources (traduisible). */
enum class Categorie(@param:StringRes val libelle: Int) {
    Entree(R.string.categorie_entree),
    Plat(R.string.categorie_plat),
    Dessert(R.string.categorie_dessert),
}

/**
 * Recette du carnet. Classe immuable : on la modifie avec `copy()`.
 * Sa sauvegarde par `rememberSaveable` passe par `RecetteSaver` (logique/Sauvegardes.kt).
 */
data class Recette(
    val id: String,
    val nom: String,
    val categorie: Categorie,
    val dureeMinutes: Int,
    val ingredients: List<String>,
    val etapes: List<String>,
    val difficulte: Int = 2,          // de 1 (facile) à 5 (difficile)
    val favori: Boolean = false,
)
