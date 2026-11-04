package fr.formation.films.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/** Un onglet de premier niveau : sa route, son libellé, son icône. */
data class Onglet<T : Any>(val route: T, val libelle: String, val icone: ImageVector)

val onglets = listOf(
    Onglet(Films, "Films", Icons.AutoMirrored.Filled.List),
    Onglet(Favoris, "Favoris", Icons.Filled.Favorite),
    Onglet(ParametresGraphe, "Paramètres", Icons.Filled.Settings),
)

/**
 * Onglet sélectionné : on parcourt la hiérarchie de la destination courante,
 * ainsi l'onglet « Paramètres » reste sélectionné sur APropos (dans ParametresGraphe).
 */
fun Onglet<*>.estSelectionne(destination: NavDestination?): Boolean =
    destination?.hierarchy?.any { it.hasRoute(route::class) } == true

/** Navigation entre onglets, partagée par la NavigationBar et le NavigationSuiteScaffold. */
fun NavHostController.naviguerVersOnglet(onglet: Onglet<*>) {
    navigate(onglet.route) {
        // Dépile jusqu'à la destination de départ (Films) en mémorisant la pile de l'onglet quitté
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true   // re-clic sur l'onglet courant : pas de doublon
        restoreState = true      // retrouve la pile de l'onglet rejoint (ex. APropos)
    }
}
