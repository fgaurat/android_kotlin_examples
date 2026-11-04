package fr.formation.films.ui.films

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import fr.formation.films.data.Film
import fr.formation.films.data.filmsFictifs
import fr.formation.films.ui.composants.FilmItem
import fr.formation.films.ui.composants.FilmsTopBar
import fr.formation.films.ui.theme.CatalogueFilmsTheme

/**
 * Liste du catalogue. L'écran ne connaît pas le NavController :
 * il signale simplement le film choisi via [onFilmClick].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmsScreen(
    etat: FilmsUiState,
    onFilmClick: (Film) -> Unit,
    modifier: Modifier = Modifier,
) {
    // La barre disparaît quand on descend dans la liste et revient dès qu'on remonte
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { FilmsTopBar(titre = "Catalogue", scrollBehavior = scrollBehavior) },
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(etat.films, key = { it.id }) { film ->
                FilmItem(
                    film = film,
                    estFavori = film.id in etat.idsFavoris,
                    onClick = { onFilmClick(film) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilmsScreenPreview() {
    CatalogueFilmsTheme {
        FilmsScreen(etat = FilmsUiState(filmsFictifs, setOf(2, 5)), onFilmClick = {})
    }
}
