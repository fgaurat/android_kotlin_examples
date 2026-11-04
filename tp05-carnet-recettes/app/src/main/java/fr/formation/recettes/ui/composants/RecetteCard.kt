package fr.formation.recettes.ui.composants

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.formation.recettes.R
import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.modele.Recette
import fr.formation.recettes.ui.theme.CarnetRecettesTheme

/**
 * Carte d'une recette dans la liste. Sans état : elle affiche la recette
 * et signale les clics (`onClick`, `onFavoriChange`) à son parent.
 */
@Composable
fun RecetteCard(
    recette: Recette,
    onClick: () -> Unit,
    onFavoriChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    selectionnee: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selectionnee) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PastilleCategorie(recette.categorie)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = recette.nom,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.resume_recette,
                        stringResource(recette.categorie.libelle),
                        texteDuree(recette.dureeMinutes),
                        pluralStringResource(R.plurals.nb_ingredients, recette.ingredients.size, recette.ingredients.size),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BoutonFavori(favori = recette.favori, onFavoriChange = onFavoriChange)
        }
    }
}

/** Pastille décorative : initiale de la catégorie (la catégorie est lue dans le résumé). */
@Composable
private fun PastilleCategorie(categorie: Categorie, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(categorie.libelle).take(1),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * Bouton favori : la couleur de l'icône est animée avec `animateColorAsState`.
 * `IconToggleButton` garantit une cible de 48 dp et le rôle « case à cocher » pour TalkBack.
 */
@Composable
fun BoutonFavori(
    favori: Boolean,
    onFavoriChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val couleur by animateColorAsState(
        targetValue = if (favori) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 400),
        label = "couleur favori",
    )
    IconToggleButton(checked = favori, onCheckedChange = onFavoriChange, modifier = modifier) {
        Icon(
            imageVector = if (favori) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(R.string.favori),
            tint = couleur,
        )
    }
}

/** Durée lisible : « 40 min », « 2 h », « 3 h 30 ». */
@Composable
fun texteDuree(minutes: Int): String {
    val heures = minutes / 60
    val reste = minutes % 60
    return when {
        heures == 0 -> stringResource(R.string.duree_minutes, minutes)
        reste == 0 -> stringResource(R.string.duree_heures, heures)
        else -> stringResource(R.string.duree_heures_minutes, heures, reste)
    }
}

/** Cas de preview : recette standard, favorite, nom très long, un seul ingrédient (pluriel « one »). */
class RecetteProvider : PreviewParameterProvider<Recette> {
    override val values = sequenceOf(
        recettesFictives[0],
        recettesFictives[4],
        recettesFictives[2].copy(
            nom = "Bœuf bourguignon de grand-mère mijoté toute la nuit avec ses carottes fondantes et ses lardons",
        ),
        recettesFictives[5].copy(ingredients = listOf("200 g de chocolat noir")),
    )
}

@PreviewLightDark
@Composable
private fun RecetteCardPreview(@PreviewParameter(RecetteProvider::class) recette: Recette) {
    CarnetRecettesTheme {
        RecetteCard(recette = recette, onClick = {}, onFavoriChange = {}, modifier = Modifier.padding(8.dp))
    }
}

@PreviewLightDark
@Composable
private fun RecetteCardSelectionneePreview() {
    CarnetRecettesTheme {
        RecetteCard(
            recette = recettesFictives[1],
            onClick = {},
            onFavoriChange = {},
            selectionnee = true,
            modifier = Modifier.padding(8.dp),
        )
    }
}
