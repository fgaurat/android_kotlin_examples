package fr.formation.films.ui.favoris

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.films.data.Film
import fr.formation.films.data.filmsFictifs
import fr.formation.films.ui.composants.FilmItem
import fr.formation.films.ui.composants.FilmsTopBar
import fr.formation.films.ui.theme.CatalogueFilmsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavorisScreen(
    favoris: List<Film>,
    onFilmClick: (Film) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Barre fixe qui change de couleur quand la liste défile dessous
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { FilmsTopBar(titre = "Favoris", scrollBehavior = scrollBehavior) },
    ) { innerPadding ->
        if (favoris.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aucun favori pour l'instant. Touchez le cœur sur la fiche d'un film.")
            }
        } else {
            LazyColumn(contentPadding = innerPadding) {
                items(favoris, key = { it.id }) { film ->
                    FilmItem(film = film, estFavori = true, onClick = { onFilmClick(film) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavorisScreenPreview() {
    CatalogueFilmsTheme {
        FavorisScreen(favoris = filmsFictifs.take(3), onFilmClick = {})
    }
}
