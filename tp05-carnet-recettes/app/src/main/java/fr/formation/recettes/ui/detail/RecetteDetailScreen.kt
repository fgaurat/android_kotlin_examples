package fr.formation.recettes.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import fr.formation.recettes.R
import fr.formation.recettes.modele.Recette
import fr.formation.recettes.ui.composants.BoutonFavori
import fr.formation.recettes.ui.composants.RecetteProvider
import fr.formation.recettes.ui.composants.texteDuree
import fr.formation.recettes.ui.theme.CarnetRecettesTheme

/** Sauvegarde d'un ensemble d'indices (ingrédients cochés) dans le `Bundle`. */
private val IndicesSaver = Saver<Set<Int>, IntArray>(
    save = { it.toIntArray() },
    restore = { it.toSet() },
)

/**
 * Version AVEC état du détail : ingrédients cochés et étapes affichées,
 * réinitialisés quand on change de recette (clé `recette.id`).
 */
@Composable
fun RecetteDetailRoute(
    recette: Recette,
    onFavoriChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var coches by rememberSaveable(recette.id, stateSaver = IndicesSaver) { mutableStateOf(emptySet()) }
    var etapesVisibles by rememberSaveable(recette.id) { mutableStateOf(false) }

    RecetteDetailScreen(
        recette = recette,
        ingredientsCoches = coches,
        onIngredientCocheChange = { indice, coche -> coches = if (coche) coches + indice else coches - indice },
        etapesVisibles = etapesVisibles,
        onEtapesVisiblesChange = { etapesVisibles = it },
        onFavoriChange = onFavoriChange,
        modifier = modifier,
    )
}

/** Détail d'une recette, SANS état. */
@Composable
fun RecetteDetailScreen(
    recette: Recette,
    ingredientsCoches: Set<Int>,
    onIngredientCocheChange: (Int, Boolean) -> Unit,
    etapesVisibles: Boolean,
    onEtapesVisiblesChange: (Boolean) -> Unit,
    onFavoriChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.testTag(TAG_DETAIL_RECETTE),
        contentPadding = PaddingValues(16.dp),
    ) {
        item(key = "entete") {
            EnteteRecette(recette = recette, onFavoriChange = onFavoriChange)
        }
        item(key = "titre-ingredients") {
            TitreSection(
                titre = stringResource(R.string.ingredients),
                detail = pluralStringResource(R.plurals.nb_ingredients, recette.ingredients.size, recette.ingredients.size),
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
            )
        }
        if (recette.ingredients.isEmpty()) {
            item(key = "aucun-ingredient") { TexteSecondaire(stringResource(R.string.aucun_ingredient)) }
        }
        itemsIndexed(recette.ingredients, key = { indice, _ -> "ingredient-$indice" }) { indice, ingredient ->
            LigneIngredient(
                ingredient = ingredient,
                coche = indice in ingredientsCoches,
                onCocheChange = { onIngredientCocheChange(indice, it) },
            )
        }
        item(key = "titre-etapes") {
            Row(
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TitreSection(titre = stringResource(R.string.etapes), modifier = Modifier.weight(1f))
                TextButton(onClick = { onEtapesVisiblesChange(!etapesVisibles) }) {
                    Text(stringResource(if (etapesVisibles) R.string.masquer_etapes else R.string.afficher_etapes))
                    Icon(
                        imageVector = if (etapesVisibles) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                    )
                }
            }
        }
        item(key = "etapes") {
            // Apparition animée des étapes
            AnimatedVisibility(
                visible = etapesVisibles,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (recette.etapes.isEmpty()) TexteSecondaire(stringResource(R.string.aucune_etape))
                    recette.etapes.forEachIndexed { indice, etape ->
                        EtapeNumerotee(numero = indice + 1, texte = etape)
                    }
                }
            }
        }
    }
}

const val TAG_DETAIL_RECETTE = "detail_recette"

@Composable
private fun EnteteRecette(
    recette: Recette,
    onFavoriChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val niveaux = stringArrayResource(R.array.niveaux_difficulte)
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = recette.nom,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            BoutonFavori(favori = recette.favori, onFavoriChange = onFavoriChange)
        }
        Spacer(Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiquette(stringResource(recette.categorie.libelle))
            Etiquette(texteDuree(recette.dureeMinutes))
        }
        Text(
            text = stringResource(
                R.string.difficulte_valeur,
                recette.difficulte,
                niveaux.getOrElse(recette.difficulte - 1) { "" },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(Modifier.padding(top = 16.dp))
    }
}

/** Étiquette d'information non cliquable (une puce désactivée aurait un contraste insuffisant). */
@Composable
private fun Etiquette(texte: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = texte,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TitreSection(titre: String, modifier: Modifier = Modifier, detail: String? = null) {
    Row(modifier.semantics(mergeDescendants = true) { heading() }, verticalAlignment = Alignment.Bottom) {
        Text(titre, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        if (detail != null) {
            Spacer(Modifier.width(8.dp))
            Text(detail, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Ingrédient à cocher : toute la ligne est cliquable (cible ≥ 48 dp, rôle « case à cocher »). */
@Composable
private fun LigneIngredient(
    ingredient: String,
    coche: Boolean,
    onCocheChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = coche, onValueChange = onCocheChange, role = Role.Checkbox)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = coche, onCheckedChange = null)   // la ligne gère le clic
        Spacer(Modifier.width(12.dp))
        Text(
            text = ingredient,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (coche) TextDecoration.LineThrough else null,
            color = if (coche) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Étape numérotée, lue « Étape 2 : … » par TalkBack. */
@Composable
private fun EtapeNumerotee(numero: Int, texte: String, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.etape_description, numero, texte)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = numero.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(texte, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TexteSecondaire(texte: String) {
    Text(texte, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Volet affiché à droite en largeur Expanded tant qu'aucune recette n'est choisie. */
@Composable
fun AucuneRecetteChoisie(modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.choisir_recette),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 320.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun RecetteDetailScreenPreview(@PreviewParameter(RecetteProvider::class) recette: Recette) {
    CarnetRecettesTheme {
        Surface {
            RecetteDetailScreen(
                recette = recette,
                ingredientsCoches = setOf(0, 2),
                onIngredientCocheChange = { _, _ -> },
                etapesVisibles = true,
                onEtapesVisiblesChange = {},
                onFavoriChange = {},
            )
        }
    }
}
