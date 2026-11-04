package fr.formation.films.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.formation.films.data.AppContainer

/**
 * Racine de l'application.
 *
 * - [adaptatif] = true (bonus) : `NavigationSuiteScaffold` choisit barre, rail ou tiroir
 *   selon la taille de la fenêtre (barre sur téléphone, rail sur tablette).
 * - [adaptatif] = false (étape 5) : `Scaffold` + `NavigationBar` classique.
 *
 * Le [navController] est un paramètre pour pouvoir injecter un `TestNavHostController`.
 */
@Composable
fun FilmsApp(
    container: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    adaptatif: Boolean = true,
) {
    // Recompose à chaque navigation : sert à sélectionner l'onglet et à masquer la barre
    val entree by navController.currentBackStackEntryAsState()
    val destination = entree?.destination
    val barreVisible = destination?.hasRoute<DetailFilm>() != true

    if (adaptatif) {
        FilmsAppAdaptative(navController, container, destination, barreVisible, modifier)
    } else {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (barreVisible) FilmsBottomBar(navController, destination)
            },
        ) { innerPadding ->
            FilmsNavHost(
                navController = navController,
                container = container,
                // consumeWindowInsets : le Scaffold de chaque écran ne rajoute pas la marge de la barre système
                modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
            )
        }
    }
}

/** Étape 5 : la barre d'onglets du bas. */
@Composable
fun FilmsBottomBar(navController: NavHostController, destination: NavDestination?) {
    NavigationBar {
        onglets.forEach { onglet ->
            NavigationBarItem(
                selected = onglet.estSelectionne(destination),
                onClick = { navController.naviguerVersOnglet(onglet) },
                icon = { Icon(onglet.icone, contentDescription = null) },  // le libellé suffit
                label = { Text(onglet.libelle) },
            )
        }
    }
}

/** Bonus : même navigation, présentation adaptée à la taille d'écran. */
@Composable
private fun FilmsAppAdaptative(
    navController: NavHostController,
    container: AppContainer,
    destination: NavDestination?,
    barreVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val typeCalcule = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
        currentWindowAdaptiveInfo()
    )
    // Pas de navigation sur le détail, comme sur téléphone avec la NavigationBar
    val type = if (barreVisible) typeCalcule else NavigationSuiteType.None

    NavigationSuiteScaffold(
        modifier = modifier,
        layoutType = type,
        navigationSuiteItems = {
            onglets.forEach { onglet ->
                item(
                    selected = onglet.estSelectionne(destination),
                    onClick = { navController.naviguerVersOnglet(onglet) },
                    icon = { Icon(onglet.icone, contentDescription = null) },
                    label = { Text(onglet.libelle) },
                )
            }
        },
    ) {
        // La barre du bas occupe déjà la zone de navigation système : on la consomme
        // pour que le Scaffold de chaque écran n'ajoute pas une seconde marge.
        val contenuModifier = if (type == NavigationSuiteType.NavigationBar) {
            Modifier.consumeWindowInsets(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
        } else {
            Modifier
        }
        FilmsNavHost(navController = navController, container = container, modifier = contenuModifier)
    }
}
