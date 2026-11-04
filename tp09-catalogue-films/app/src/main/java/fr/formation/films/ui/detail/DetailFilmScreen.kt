package fr.formation.films.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.films.data.Film
import fr.formation.films.data.filmsFictifs
import fr.formation.films.ui.theme.CatalogueFilmsTheme

/**
 * Détail d'un film : grand titre repliable, flèche de retour et action Favori.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailFilmScreen(
    etat: DetailFilmUiState,
    onRetour: () -> Unit,
    onBasculerFavori: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Grand titre qui se replie quand on fait défiler le contenu
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val titre = when (etat) {
        is DetailFilmUiState.Affiche -> etat.film.titre
        is DetailFilmUiState.Introuvable -> "Film introuvable"
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(titre, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (etat is DetailFilmUiState.Affiche) {
                        IconButton(onClick = onBasculerFavori) {
                            Icon(
                                imageVector = if (etat.estFavori) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (etat.estFavori) "Retirer des favoris" else "Ajouter aux favoris",
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        when (etat) {
            is DetailFilmUiState.Affiche -> DetailFilmContenu(
                film = etat.film,
                modifier = Modifier.padding(innerPadding),
            )
            is DetailFilmUiState.Introuvable -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aucun film ne porte l'identifiant ${etat.id}.")
            }
        }
    }
}

@Composable
private fun DetailFilmContenu(film: Film, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("${film.annee} · ${film.realisateur}", style = MaterialTheme.typography.titleMedium)
        Text(film.synopsis, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailFilmScreenPreview() {
    CatalogueFilmsTheme {
        DetailFilmScreen(
            etat = DetailFilmUiState.Affiche(filmsFictifs[3], estFavori = true),
            onRetour = {},
            onBasculerFavori = {},
        )
    }
}
