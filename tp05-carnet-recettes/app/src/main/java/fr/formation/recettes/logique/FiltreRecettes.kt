package fr.formation.recettes.logique

import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.modele.Recette

/**
 * Filtre les recettes : la recherche porte sur le nom et les ingrédients (casse ignorée),
 * et un ensemble de catégories vide signifie « toutes les catégories ».
 * Fonction pure : testée en test unitaire, sans Android.
 */
fun filtrerRecettes(
    recettes: List<Recette>,
    recherche: String,
    categories: Set<Categorie>,
): List<Recette> {
    val texte = recherche.trim()
    return recettes.filter { recette ->
        val categorieOk = categories.isEmpty() || recette.categorie in categories
        val texteOk = texte.isEmpty() ||
            recette.nom.contains(texte, ignoreCase = true) ||
            recette.ingredients.any { it.contains(texte, ignoreCase = true) }
        categorieOk && texteOk
    }
}

/** Ajoute ou retire une catégorie de l'ensemble (clic sur un `FilterChip`). */
fun Set<Categorie>.basculer(categorie: Categorie): Set<Categorie> =
    if (categorie in this) this - categorie else this + categorie
