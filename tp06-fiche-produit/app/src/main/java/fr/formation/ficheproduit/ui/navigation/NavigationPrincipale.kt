package fr.formation.ficheproduit.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import fr.formation.ficheproduit.R

/** Onglets de la navigation principale : libellé traduisible et icône. */
enum class Onglet(@param:StringRes val libelle: Int, val icone: ImageVector) {
    Accueil(R.string.onglet_accueil, Icons.Filled.Home),
    Recherche(R.string.onglet_recherche, Icons.Filled.Search),
    Panier(R.string.onglet_panier, Icons.Filled.ShoppingCart),
    Profil(R.string.onglet_profil, Icons.Filled.Person),
}

/**
 * Navigation à 4 onglets, sans état. `NavigationSuiteScaffold` choisit seul une barre du bas
 * (téléphone) ou un rail (tablette, paysage). Le contenu de l'onglet est un slot.
 */
@Composable
fun NavigationPrincipale(
    ongletCourant: Onglet,
    onOngletClick: (Onglet) -> Unit,
    nombreArticlesPanier: Int,
    modifier: Modifier = Modifier,
    contenu: @Composable () -> Unit,
) {
    // navigationSuiteItems n'est pas composable : les textes sont résolus avant
    val libelles = Onglet.entries.associateWith { stringResource(it.libelle) }
    // NavigationBarItem efface la sémantique de l'icône (et donc du badge) quand le libellé
    // est affiché : on décrit l'onglet entier, « Panier, 2 articles dans le panier ».
    val descriptionPanier = stringResource(
        R.string.onglet_avec_badge,
        libelles.getValue(Onglet.Panier),
        pluralStringResource(R.plurals.articles_panier, nombreArticlesPanier, nombreArticlesPanier),
    )

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            Onglet.entries.forEach { onglet ->
                val badge = onglet == Onglet.Panier && nombreArticlesPanier > 0
                val libelle = libelles.getValue(onglet)
                item(
                    selected = onglet == ongletCourant,
                    onClick = { onOngletClick(onglet) },
                    icon = { Icon(onglet.icone, contentDescription = null) },   // le libellé visible est lu
                    label = { Text(libelle) },
                    modifier = if (badge) {
                        Modifier.semantics { contentDescription = descriptionPanier }
                    } else {
                        Modifier
                    },
                    badge = if (badge) {
                        {
                            Badge {
                                // Le chiffre brut n'est pas lu : la description de l'onglet le dit déjà
                                Text(
                                    text = if (nombreArticlesPanier > 99) "99+" else nombreArticlesPanier.toString(),
                                    modifier = Modifier.clearAndSetSemantics { },
                                )
                            }
                        }
                    } else {
                        null
                    },
                )
            }
        },
        content = contenu,
    )
}
