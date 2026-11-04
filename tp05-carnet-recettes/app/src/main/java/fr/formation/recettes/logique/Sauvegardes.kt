package fr.formation.recettes.logique

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.modele.Recette

/*
 * Savers pour rememberSaveable : un Bundle n'accepte que des types simples
 * (String, Int, Boolean, listes…). On décompose donc nos objets en valeurs simples.
 * Alternative vue en cours : @Parcelize (plugin kotlin-parcelize).
 */

/** Recette ↔ liste de valeurs simples (l'ordre des éléments fait office de format). */
val RecetteSaver: Saver<Recette, Any> = listSaver(
    save = {
        listOf(
            it.id, it.nom, it.categorie.name, it.dureeMinutes,
            ArrayList(it.ingredients), ArrayList(it.etapes), it.difficulte, it.favori,
        )
    },
    restore = {
        @Suppress("UNCHECKED_CAST")
        Recette(
            id = it[0] as String,
            nom = it[1] as String,
            categorie = Categorie.valueOf(it[2] as String),
            dureeMinutes = it[3] as Int,
            ingredients = it[4] as List<String>,
            etapes = it[5] as List<String>,
            difficulte = it[6] as Int,
            favori = it[7] as Boolean,
        )
    },
)

/** Liste de recettes : chaque élément est sauvegardé avec [RecetteSaver]. */
val RecettesSaver: Saver<List<Recette>, Any> = listSaver(
    save = { recettes -> recettes.map { recette -> with(RecetteSaver) { save(recette)!! } } },
    restore = { valeurs -> valeurs.map { RecetteSaver.restore(it)!! } },
)

/** Ensemble de catégories, stocké par noms. */
val CategoriesSaver: Saver<Set<Categorie>, Any> = listSaver(
    save = { categories -> categories.map { it.name } },
    restore = { noms -> noms.map(Categorie::valueOf).toSet() },
)

/** Formulaire d'ajout en cours de saisie. */
val FormulaireSaver: Saver<FormulaireRecette, Any> = listSaver(
    save = { listOf(it.nom, it.duree, it.difficulte, it.categorie.name, it.ingredients, it.etapes) },
    restore = {
        FormulaireRecette(
            nom = it[0] as String,
            duree = it[1] as String,
            difficulte = it[2] as Float,
            categorie = Categorie.valueOf(it[3] as String),
            ingredients = it[4] as String,
            etapes = it[5] as String,
        )
    },
)
