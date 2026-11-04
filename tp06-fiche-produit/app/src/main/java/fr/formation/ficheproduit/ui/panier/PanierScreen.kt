package fr.formation.ficheproduit.ui.panier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.formation.ficheproduit.R
import fr.formation.ficheproduit.donnees.couleursFictives
import fr.formation.ficheproduit.donnees.produitFictif
import fr.formation.ficheproduit.logique.formaterPrix
import fr.formation.ficheproduit.logique.totalCentimes
import fr.formation.ficheproduit.modele.LignePanier
import fr.formation.ficheproduit.modele.Taille
import fr.formation.ficheproduit.ui.navigation.NavigationPrincipale
import fr.formation.ficheproduit.ui.navigation.Onglet
import fr.formation.ficheproduit.ui.theme.FicheProduitTheme

/*
 * BONUS — écran de panier transcrit depuis un croquis (voir README) :
 * titre en haut, une ligne par article (pastille couleur, nom, taille · coloris, quantité − / +, prix),
 * total et bouton « Commander » en bas, message si le panier est vide.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanierScreen(
    lignes: List<LignePanier>,
    onQuantiteChange: (LignePanier, Int) -> Unit,
    onCommander: () -> Unit,
    onOngletClick: (Onglet) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val locale = LocalConfiguration.current.locales[0]
    val nombreArticles = lignes.sumOf { it.quantite }
    NavigationPrincipale(
        ongletCourant = Onglet.Panier,
        onOngletClick = onOngletClick,
        nombreArticlesPanier = nombreArticles,
        modifier = modifier,
    ) {
        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.onglet_panier)) }) },
            bottomBar = {
                if (lignes.isNotEmpty()) {
                    BarreTotal(
                        total = formaterPrix(lignes.totalCentimes(), locale),
                        onCommander = onCommander,
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            if (lignes.isEmpty()) {
                Box(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.panier_vide),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(lignes, key = { it.id }) { ligne ->
                        LignePanierItem(
                            ligne = ligne,
                            prix = formaterPrix(ligne.prixCentimes * ligne.quantite, locale),
                            onQuantiteChange = { onQuantiteChange(ligne, it) },
                            modifier = Modifier.animateItem(),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun LignePanierItem(
    ligne: LignePanier,
    prix: String,
    onQuantiteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val couleur = stringResource(ligne.couleur.nom)
    ListItem(
        modifier = modifier,
        leadingContent = {
            Box(
                Modifier
                    .size(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .background(Color(ligne.couleur.teinte), CircleShape)
                    .clearAndSetSemantics { },    // décoratif : le coloris est écrit en dessous
            )
        },
        headlineContent = { Text(ligne.nom, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column {
                Text(stringResource(R.string.details_ligne, ligne.taille.name, couleur))
                Text(prix, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
        },
        trailingContent = {
            SelecteurQuantite(quantite = ligne.quantite, nomArticle = ligne.nom, onQuantiteChange = onQuantiteChange)
        },
    )
}

@Composable
private fun SelecteurQuantite(
    quantite: Int,
    nomArticle: String,
    onQuantiteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val descriptionQuantite = stringResource(R.string.quantite_description, quantite)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onQuantiteChange(quantite - 1) }) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = stringResource(R.string.diminuer_quantite, nomArticle),
            )
        }
        Text(
            text = quantite.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 24.dp)
                .semantics {
                    contentDescription = descriptionQuantite
                    liveRegion = LiveRegionMode.Polite      // nouvelle quantité annoncée
                },
        )
        IconButton(onClick = { onQuantiteChange(quantite + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.augmenter_quantite, nomArticle))
        }
    }
}

@Composable
private fun BarreTotal(total: String, onCommander: () -> Unit, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.total, total),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onCommander, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.commander))
            }
        }
    }
}

private val panierFictif = listOf(
    LignePanier(produitFictif.id, produitFictif.nom, Taille.M, couleursFictives[0], produitFictif.prixCentimes, 2),
    LignePanier(produitFictif.id, produitFictif.nom, Taille.S, couleursFictives[3], produitFictif.prixCentimes),
)

@PreviewLightDark
@Composable
private fun PanierScreenPreview() {
    FicheProduitTheme {
        PanierScreen(lignes = panierFictif, onQuantiteChange = { _, _ -> }, onCommander = {}, onOngletClick = {})
    }
}

@PreviewLightDark
@Composable
private fun PanierScreenVidePreview() {
    FicheProduitTheme {
        PanierScreen(lignes = emptyList(), onQuantiteChange = { _, _ -> }, onCommander = {}, onOngletClick = {})
    }
}
