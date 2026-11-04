package fr.formation.recettes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.window.core.layout.WindowSizeClass
import fr.formation.recettes.R
import fr.formation.recettes.donnees.recettesFictives
import fr.formation.recettes.logique.CategoriesSaver
import fr.formation.recettes.logique.RecettesSaver
import fr.formation.recettes.logique.basculer
import fr.formation.recettes.logique.filtrerRecettes
import fr.formation.recettes.modele.Recette
import fr.formation.recettes.ui.ajout.AjoutRecetteRoute
import fr.formation.recettes.ui.detail.AucuneRecetteChoisie
import fr.formation.recettes.ui.detail.RecetteDetailRoute
import fr.formation.recettes.ui.liste.Affichage
import fr.formation.recettes.ui.liste.RecettesScreen
import fr.formation.recettes.ui.theme.CarnetRecettesTheme
import java.util.UUID

/**
 * Composable AVEC état de l'application (le « Route ») : il détient la liste des recettes,
 * la recherche, les filtres et la recette choisie, puis les passe aux écrans sans état.
 * Au jour 3, cet état migrera dans un ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarnetRecettesApp(modifier: Modifier = Modifier) {
    var recettes by rememberSaveable(stateSaver = RecettesSaver) { mutableStateOf(recettesFictives) }
    var recherche by rememberSaveable { mutableStateOf("") }
    var categories by rememberSaveable(stateSaver = CategoriesSaver) { mutableStateOf(emptySet()) }
    var recetteChoisieId by rememberSaveable { mutableStateOf<String?>(null) }
    var ajoutEnCours by rememberSaveable { mutableStateOf(false) }

    val recettesFiltrees = remember(recettes, recherche, categories) {
        filtrerRecettes(recettes, recherche, categories)
    }
    val recetteChoisie = recettes.find { it.id == recetteChoisieId }

    // Taille de la FENÊTRE : Compact < 600 dp ≤ Medium < 840 dp ≤ Expanded
    val classe = currentWindowAdaptiveInfo().windowSizeClass
    val deuxVolets = classe.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    val grille = classe.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    val detailPleinEcran = !deuxVolets && recetteChoisie != null

    // Retour arrière : ferme le formulaire, puis revient du détail à la liste (un seul volet)
    BackHandler(enabled = ajoutEnCours) { ajoutEnCours = false }
    BackHandler(enabled = !ajoutEnCours && detailPleinEcran) { recetteChoisieId = null }

    val changerFavori: (Recette, Boolean) -> Unit = { recette, favori ->
        recettes = recettes.map { if (it.id == recette.id) it.copy(favori = favori) else it }   // jamais de mutation
    }

    val titre = when {
        ajoutEnCours -> stringResource(R.string.titre_ajout)
        detailPleinEcran -> recetteChoisie.nom
        else -> stringResource(R.string.app_name)
    }
    val onRetour: (() -> Unit)? = when {
        ajoutEnCours -> { { ajoutEnCours = false } }
        detailPleinEcran -> { { recetteChoisieId = null } }
        else -> null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(titre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onRetour != null) {
                        IconButton(onClick = onRetour) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.retour))
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (!ajoutEnCours && !detailPleinEcran) {
                ExtendedFloatingActionButton(
                    onClick = { ajoutEnCours = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.ajouter_recette)) },
                )
            }
        },
    ) { innerPadding ->
        val contenu = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .fillMaxSize()

        // Liste réutilisée dans les deux dispositions
        val liste: @Composable (Modifier, Affichage) -> Unit = { mod, affichage ->
            RecettesScreen(
                recettes = recettesFiltrees,
                recherche = recherche,
                onRechercheChange = { recherche = it },
                categoriesActives = categories,
                onCategorieClick = { categories = categories.basculer(it) },
                onRecetteClick = { recetteChoisieId = it.id },
                onFavoriChange = changerFavori,
                recetteSelectionneeId = if (deuxVolets) recetteChoisieId else null,
                affichage = affichage,
                modifier = mod,
            )
        }

        when {
            ajoutEnCours -> AjoutRecetteRoute(
                onEnregistrer = { formulaire ->
                    val nouvelle = formulaire.versRecette(id = UUID.randomUUID().toString())
                    recettes = recettes + nouvelle
                    recetteChoisieId = nouvelle.id
                    ajoutEnCours = false
                },
                modifier = contenu,
            )

            // Expanded : liste (ou grille) et détail côte à côte
            deuxVolets -> Row(contenu) {
                liste(Modifier.weight(1f), Affichage.Grille)
                VerticalDivider()
                if (recetteChoisie != null) {
                    RecetteDetailRoute(
                        recette = recetteChoisie,
                        onFavoriChange = { changerFavori(recetteChoisie, it) },
                        modifier = Modifier.weight(1.4f).fillMaxHeight(),
                    )
                } else {
                    AucuneRecetteChoisie(Modifier.weight(1.4f).fillMaxHeight())
                }
            }

            // Compact / Medium : un seul volet
            recetteChoisie != null -> RecetteDetailRoute(
                recette = recetteChoisie,
                onFavoriChange = { changerFavori(recetteChoisie, it) },
                modifier = contenu,
            )

            else -> liste(contenu, if (grille) Affichage.Grille else Affichage.Liste)
        }
    }
}

@PreviewScreenSizes
@Composable
private fun CarnetRecettesAppPreview() {
    CarnetRecettesTheme { CarnetRecettesApp() }
}
