package fr.formation.films.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import fr.formation.films.data.AppContainer
import fr.formation.films.ui.detail.DetailFilmScreen
import fr.formation.films.ui.detail.DetailFilmViewModel
import fr.formation.films.ui.favoris.FavorisScreen
import fr.formation.films.ui.favoris.FavorisViewModel
import fr.formation.films.ui.films.FilmsScreen
import fr.formation.films.ui.films.FilmsViewModel
import fr.formation.films.ui.parametres.AProposScreen
import fr.formation.films.ui.parametres.ParametresScreen
import fr.formation.films.ui.parametres.ParametresViewModel

/**
 * Le graphe de navigation. C'est le seul endroit qui connaît le [NavHostController] :
 * les écrans émettent des événements (callbacks), le NavHost choisit la destination.
 */
@Composable
fun FilmsNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = Films, modifier = modifier) {

        composable<Films> {
            val viewModel: FilmsViewModel =
                viewModel(factory = FilmsViewModel.factory(container.filmsRepository))
            val etat by viewModel.etat.collectAsStateWithLifecycle()
            FilmsScreen(
                etat = etat,
                onFilmClick = { film -> navController.navigate(film.versDetail()) },
            )
        }

        composable<DetailFilm>(
            // films://detail/{id} : l'argument obligatoire devient un segment du chemin
            deepLinks = listOf(navDeepLink<DetailFilm>(basePath = BASE_DEEP_LINK_DETAIL)),
        ) {
            // ViewModel lié à cette entrée de la pile ; il lit l'id dans son SavedStateHandle
            val viewModel: DetailFilmViewModel =
                viewModel(factory = DetailFilmViewModel.factory(container.filmsRepository))
            val etat by viewModel.etat.collectAsStateWithLifecycle()
            DetailFilmScreen(
                etat = etat,
                onRetour = { navController.navigateUp() },
                onBasculerFavori = viewModel::basculerFavori,
            )
        }

        composable<Favoris> {
            val viewModel: FavorisViewModel =
                viewModel(factory = FavorisViewModel.factory(container.filmsRepository))
            val favoris by viewModel.favoris.collectAsStateWithLifecycle()
            FavorisScreen(
                favoris = favoris,
                onFilmClick = { film -> navController.navigate(film.versDetail()) },
            )
        }

        parametresGraphe(navController, container)
    }
}

/** Sous-graphe des paramètres, découpé en fonction d'extension. */
private fun NavGraphBuilder.parametresGraphe(
    navController: NavHostController,
    container: AppContainer,
) {
    navigation<ParametresGraphe>(startDestination = Parametres) {
        composable<Parametres> {
            val viewModel: ParametresViewModel =
                viewModel(factory = ParametresViewModel.factory(container.filmsRepository))
            val nombreFavoris by viewModel.nombreFavoris.collectAsStateWithLifecycle()
            ParametresScreen(
                nombreFavoris = nombreFavoris,
                onViderFavoris = viewModel::viderFavoris,
                onAProposClick = { navController.navigate(APropos) { launchSingleTop = true } },
            )
        }
        composable<APropos> {
            AProposScreen(onRetour = { navController.navigateUp() })
        }
    }
}
