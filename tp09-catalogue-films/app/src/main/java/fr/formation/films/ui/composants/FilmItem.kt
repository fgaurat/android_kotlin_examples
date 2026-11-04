package fr.formation.films.ui.composants

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import fr.formation.films.data.filmsFictifs
import fr.formation.films.data.Film
import fr.formation.films.ui.theme.CatalogueFilmsTheme

/** Une ligne de liste : titre, année et réalisateur, cœur si le film est favori. */
@Composable
fun FilmItem(
    film: Film,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    estFavori: Boolean = false,
) {
    ListItem(
        headlineContent = { Text(film.titre) },
        supportingContent = { Text("${film.annee} · ${film.realisateur}") },
        trailingContent = {
            if (estFavori) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Favori",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        modifier = modifier.clickable(onClick = onClick),
    )
}

@Preview(showBackground = true)
@Composable
private fun FilmItemPreview() {
    CatalogueFilmsTheme {
        FilmItem(film = filmsFictifs.first(), onClick = {}, estFavori = true)
    }
}
