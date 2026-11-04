package fr.formation.recettes.ui.liste

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import fr.formation.recettes.R
import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.modele.Recette
import fr.formation.recettes.ui.composants.RecetteCard
import fr.formation.recettes.ui.theme.CarnetRecettesTheme

/** Mode d'affichage de la liste : colonne sur téléphone, grille adaptative sur grand écran (bonus). */
enum class Affichage { Liste, Grille }

/** Marge basse pour que le dernier élément ne soit pas masqué par le FAB. */
private val MargeFab = 88.dp

/**
 * Écran de la liste des recettes, SANS état : tout arrive en paramètre,
 * tout événement remonte par un callback. L'état est porté par `CarnetRecettesApp`.
 */
@Composable
fun RecettesScreen(
    recettes: List<Recette>,
    recherche: String,
    onRechercheChange: (String) -> Unit,
    categoriesActives: Set<Categorie>,
    onCategorieClick: (Categorie) -> Unit,
    onRecetteClick: (Recette) -> Unit,
    onFavoriChange: (Recette, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    recetteSelectionneeId: String? = null,
    affichage: Affichage = Affichage.Liste,
) {
    Column(modifier) {
        ChampRecherche(
            texte = recherche,
            onTexteChange = onRechercheChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        FiltresCategories(
            categoriesActives = categoriesActives,
            onCategorieClick = onCategorieClick,
            modifier = Modifier.fillMaxWidth(),
        )
        // Nombre de résultats annoncé par TalkBack quand il change (live region)
        Text(
            text = pluralStringResource(R.plurals.nb_recettes, recettes.size, recettes.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )

        if (recettes.isEmpty()) {
            EtatVide(Modifier.fillMaxSize())
            return@Column
        }

        val marges = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = MargeFab)
        when (affichage) {
            Affichage.Liste -> LazyColumn(
                contentPadding = marges,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TAG_LISTE_RECETTES),
            ) {
                items(
                    items = recettes,
                    key = { it.id },                 // identité stable : conserve l'état et anime les changements
                    contentType = { "recette" },
                ) { recette ->
                    RecetteCard(
                        recette = recette,
                        onClick = { onRecetteClick(recette) },
                        onFavoriChange = { onFavoriChange(recette, it) },
                        selectionnee = recette.id == recetteSelectionneeId,
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            // Bonus : autant de colonnes que la largeur le permet (2 à 4 sur tablette)
            Affichage.Grille -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                contentPadding = marges,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TAG_GRILLE_RECETTES),
            ) {
                items(items = recettes, key = { it.id }, contentType = { "recette" }) { recette ->
                    RecetteCard(
                        recette = recette,
                        onClick = { onRecetteClick(recette) },
                        onFavoriChange = { onFavoriChange(recette, it) },
                        selectionnee = recette.id == recetteSelectionneeId,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

const val TAG_LISTE_RECETTES = "liste_recettes"
const val TAG_GRILLE_RECETTES = "grille_recettes"

@Composable
private fun ChampRecherche(
    texte: String,
    onTexteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = texte,
        onValueChange = onTexteChange,
        label = { Text(stringResource(R.string.rechercher_recette)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (texte.isNotEmpty()) {
                IconButton(onClick = { onTexteChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.effacer_recherche))
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = modifier,
    )
}

@Composable
private fun FiltresCategories(
    categoriesActives: Set<Categorie>,
    onCategorieClick: (Categorie) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Categorie.entries.forEach { categorie ->
            val selectionnee = categorie in categoriesActives
            FilterChip(
                selected = selectionnee,
                onClick = { onCategorieClick(categorie) },
                label = { Text(stringResource(categorie.libelle)) },
                leadingIcon = if (selectionnee) {
                    { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun EtatVide(modifier: Modifier = Modifier) {
    Box(modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.aucune_recette),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun RecettesScreenPreview() {
    CarnetRecettesTheme {
        Surface {
            RecettesScreen(
                recettes = recettesFictives,
                recherche = "",
                onRechercheChange = {},
                categoriesActives = setOf(Categorie.Plat),
                onCategorieClick = {},
                onRecetteClick = {},
                onFavoriChange = { _, _ -> },
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecettesScreenVidePreview() {
    CarnetRecettesTheme {
        Surface {
            RecettesScreen(
                recettes = emptyList(),
                recherche = "homard",
                onRechercheChange = {},
                categoriesActives = emptySet(),
                onCategorieClick = {},
                onRecetteClick = {},
                onFavoriChange = { _, _ -> },
            )
        }
    }
}

@PreviewScreenSizes
@Composable
private fun RecettesScreenGrillePreview() {
    CarnetRecettesTheme {
        Surface {
            RecettesScreen(
                recettes = recettesFictives,
                recherche = "",
                onRechercheChange = {},
                categoriesActives = emptySet(),
                onCategorieClick = {},
                onRecetteClick = {},
                onFavoriChange = { _, _ -> },
                affichage = Affichage.Grille,
            )
        }
    }
}
