package fr.formation.ficheproduit.ui.produit

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import fr.formation.ficheproduit.R
import fr.formation.ficheproduit.donnees.produitFictif
import fr.formation.ficheproduit.donnees.produitNomLong
import fr.formation.ficheproduit.donnees.produitNoteBasse
import fr.formation.ficheproduit.donnees.produitSansAvis
import fr.formation.ficheproduit.logique.EtatEtoile
import fr.formation.ficheproduit.logique.etoilesPourNote
import fr.formation.ficheproduit.logique.formaterNote
import fr.formation.ficheproduit.logique.formaterPrix
import fr.formation.ficheproduit.logique.peutEtreAjoute
import fr.formation.ficheproduit.modele.CouleurProduit
import fr.formation.ficheproduit.modele.Produit
import fr.formation.ficheproduit.modele.Taille
import fr.formation.ficheproduit.ui.navigation.NavigationPrincipale
import fr.formation.ficheproduit.ui.navigation.Onglet
import fr.formation.ficheproduit.ui.theme.FicheProduitTheme
import java.util.Locale

/** Choix de l'utilisateur sur la fiche (état d'interface, porté par la route). */
data class SelectionProduit(
    val taille: Taille? = null,
    val couleurId: String? = null,
    val descriptionDepliee: Boolean = false,
)

/** Événements de la fiche, regroupés pour alléger la signature de l'écran. */
data class ActionsProduit(
    val onRetour: () -> Unit = {},
    val onPartager: () -> Unit = {},
    val onTailleChange: (Taille) -> Unit = {},
    val onCouleurChange: (CouleurProduit) -> Unit = {},
    val onDescriptionDeplieeChange: (Boolean) -> Unit = {},
    val onAjouterAuPanier: () -> Unit = {},
)

const val TAG_BOUTON_AJOUTER = "bouton_ajouter_panier"
const val TAG_NOTE = "note_etoiles"

/**
 * Fiche produit complète, SANS état : barre du haut (retour, partage), photo, nom, prix, note,
 * taille, coloris, description repliable, bouton « Ajouter au panier » fixé en bas,
 * navigation principale à 4 onglets avec badge sur le panier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduitScreen(
    produit: Produit,
    selection: SelectionProduit,
    nombreArticlesPanier: Int,
    actions: ActionsProduit,
    onOngletClick: (Onglet) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val couleur = produit.couleurs.find { it.id == selection.couleurId } ?: produit.couleurs.first()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    NavigationPrincipale(
        ongletCourant = Onglet.Accueil,
        onOngletClick = onOngletClick,
        nombreArticlesPanier = nombreArticlesPanier,
        modifier = modifier,
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                ProduitTopBar(
                    titre = produit.marque,
                    onRetour = actions.onRetour,
                    onPartager = actions.onPartager,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                BarreAjoutPanier(
                    prix = formaterPrix(produit.prixCentimes, localeCourante()),
                    actif = produit.peutEtreAjoute(selection.taille),
                    onAjouter = actions.onAjouterAuPanier,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            FicheProduitContenu(
                produit = produit,
                couleur = couleur,
                selection = selection,
                actions = actions,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProduitTopBar(
    titre: String,
    onRetour: () -> Unit,
    onPartager: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(titre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onRetour) {
                // Icône auto-miroir : la flèche s'inverse en arabe ou en hébreu
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.retour))
            }
        },
        actions = {
            IconButton(onClick = onPartager) {
                Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.partager))
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}

/** Photo + informations ; côte à côte dès 600 dp de large, l'une sous l'autre sinon. */
@Composable
private fun FicheProduitContenu(
    produit: Produit,
    couleur: CouleurProduit,
    selection: SelectionProduit,
    actions: ActionsProduit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        if (maxWidth >= 600.dp) {
            Row(Modifier.fillMaxSize()) {
                PhotoProduit(
                    produit = produit,
                    couleur = couleur,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                )
                InformationsProduit(
                    produit = produit,
                    couleur = couleur,
                    selection = selection,
                    actions = actions,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                )
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                PhotoProduit(produit = produit, couleur = couleur, modifier = Modifier.fillMaxWidth())
                InformationsProduit(
                    produit = produit,
                    couleur = couleur,
                    selection = selection,
                    actions = actions,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun PhotoProduit(produit: Produit, couleur: CouleurProduit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)                          // carré, quelle que soit la largeur : pas de hauteur fixe
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(produit.image),
            contentDescription = stringResource(R.string.photo_produit, produit.nom, stringResource(couleur.nom)),
            // Illustration blanche « multipliée » par la couleur du tissu : ombres conservées
            colorFilter = ColorFilter.tint(Color(couleur.teinte), BlendMode.Modulate),
            modifier = Modifier.fillMaxSize(0.8f),
        )
    }
}

@Composable
private fun InformationsProduit(
    produit: Produit,
    couleur: CouleurProduit,
    selection: SelectionProduit,
    actions: ActionsProduit,
    modifier: Modifier = Modifier,
) {
    val locale = localeCourante()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = produit.marque,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = produit.nom,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() },
            )
            LignePrix(
                prix = formaterPrix(produit.prixCentimes, locale),
                prixBarre = produit.prixBarreCentimes?.let { formaterPrix(it, locale) },
            )
            NoteEtoiles(note = produit.note, nombreAvis = produit.nombreAvis)
        }
        SelecteurTaille(
            tailles = produit.tailles,
            epuisees = produit.taillesEpuisees,
            choisie = selection.taille,
            onTailleChange = actions.onTailleChange,
        )
        SelecteurCouleur(
            couleurs = produit.couleurs,
            choisie = couleur,
            onCouleurChange = actions.onCouleurChange,
        )
        DescriptionRepliable(
            description = produit.description,
            depliee = selection.descriptionDepliee,
            onDeplieeChange = actions.onDescriptionDeplieeChange,
        )
    }
}

@Composable
private fun LignePrix(prix: String, prixBarre: String?, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(prix, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        if (prixBarre != null) {
            Spacer(Modifier.width(8.dp))
            val description = stringResource(R.string.prix_barre_description, prixBarre)
            Text(
                text = prixBarre,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
                // Le texte barré seul serait lu comme un second prix
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

/**
 * Note en étoiles accessible : l'ensemble est lu en une phrase
 * (« Noté 4,5 sur 5, 128 avis ») au lieu de cinq icônes anonymes.
 */
@Composable
fun NoteEtoiles(note: Float, nombreAvis: Int, modifier: Modifier = Modifier) {
    val locale = localeCourante()
    val avis = pluralStringResource(R.plurals.nb_avis, nombreAvis, nombreAvis)
    val description = if (nombreAvis == 0) {
        stringResource(R.string.aucun_avis)
    } else {
        stringResource(R.string.note_description, formaterNote(note, locale), avis)
    }
    Row(
        modifier = modifier
            .testTag(TAG_NOTE)
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        etoilesPourNote(note).forEach { etat -> Etoile(etat) }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (nombreAvis == 0) description else "${formaterNote(note, locale)} · $avis",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Etoile(etat: EtatEtoile, modifier: Modifier = Modifier) {
    val pleine = MaterialTheme.colorScheme.tertiary
    Box(modifier.size(20.dp)) {
        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant)
        when (etat) {
            EtatEtoile.Pleine -> Icon(Icons.Filled.Star, contentDescription = null, tint = pleine)
            // Demi-étoile : on ne dessine que la moitié « de début » (à droite en RTL)
            EtatEtoile.Demie -> Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = pleine,
                modifier = Modifier.drawWithContent {
                    val milieu = size.width / 2
                    if (layoutDirection == LayoutDirection.Ltr) {
                        clipRect(right = milieu) { this@drawWithContent.drawContent() }
                    } else {
                        clipRect(left = milieu) { this@drawWithContent.drawContent() }
                    }
                },
            )
            EtatEtoile.Vide -> Unit
        }
    }
}

/** Tailles : boutons segmentés à choix unique (rôle « bouton radio » pour TalkBack). */
@Composable
private fun SelecteurTaille(
    tailles: List<Taille>,
    epuisees: Set<Taille>,
    choisie: Taille?,
    onTailleChange: (Taille) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (choisie != null) stringResource(R.string.taille_choisie, choisie.name) else stringResource(R.string.taille),
            style = MaterialTheme.typography.titleMedium,
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            tailles.forEachIndexed { indice, taille ->
                SegmentedButton(
                    selected = taille == choisie,
                    onClick = { onTailleChange(taille) },
                    shape = SegmentedButtonDefaults.itemShape(index = indice, count = tailles.size),
                    enabled = taille !in epuisees,
                    label = { Text(taille.name) },
                )
            }
        }
        // Information doublée en texte : un bouton grisé seul ne dit pas POURQUOI il l'est
        val messages = buildList {
            if (choisie == null) add(stringResource(R.string.choisir_taille))
            epuisees.forEach { add(stringResource(R.string.taille_epuisee, it.name)) }
        }
        if (messages.isNotEmpty()) {
            Text(
                text = messages.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Coloris : pastilles de 48 dp, sélection exclusive annoncée avec le nom de la couleur. */
@Composable
private fun SelecteurCouleur(
    couleurs: List<CouleurProduit>,
    choisie: CouleurProduit,
    onCouleurChange: (CouleurProduit) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.couleur_choisie, stringResource(choisie.nom)),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            couleurs.forEach { couleur ->
                PastilleCouleur(
                    couleur = couleur,
                    selectionnee = couleur.id == choisie.id,
                    onClick = { onCouleurChange(couleur) },
                )
            }
        }
    }
}

@Composable
private fun PastilleCouleur(
    couleur: CouleurProduit,
    selectionnee: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nom = stringResource(couleur.nom)
    val teinte = Color(couleur.teinte)
    Box(
        modifier = modifier
            .size(48.dp)                                          // cible tactile minimale
            .clip(CircleShape)
            .selectable(selected = selectionnee, onClick = onClick, role = Role.RadioButton)
            .semantics { contentDescription = nom }
            .border(
                width = if (selectionnee) 3.dp else 1.dp,
                color = if (selectionnee) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
            )
            .padding(6.dp)
            .background(teinte, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selectionnee) {
            // Coche contrastée avec la couleur du TISSU (donnée), d'où le noir / blanc explicite
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = if (teinte.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Description repliable : aperçu de 3 lignes, en-tête cliquable qui annonce son état. */
@Composable
private fun DescriptionRepliable(
    description: String,
    depliee: Boolean,
    onDeplieeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(if (depliee) 180f else 0f, label = "rotation chevron")
    val etat = stringResource(if (depliee) R.string.etat_deplie else R.string.etat_replie)
    Column(modifier.animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(
                    onClickLabel = stringResource(if (depliee) R.string.replier else R.string.deplier),
                    role = Role.Button,
                ) { onDeplieeChange(!depliee) }
                .semantics { stateDescription = etat },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.description),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.rotate(rotation))
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (depliee) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Barre fixée en bas : prix rappelé et action principale. */
@Composable
private fun BarreAjoutPanier(
    prix: String,
    actif: Boolean,
    onAjouter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = prix,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = onAjouter,
                enabled = actif,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .widthIn(min = 180.dp)
                    .testTag(TAG_BOUTON_AJOUTER),
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.ajouter_au_panier))
            }
        }
    }
}

/** Langue courante (séparateur décimal, format monétaire). */
@Composable
private fun localeCourante(): Locale = LocalConfiguration.current.locales[0]

// ---------------------------------------------------------------- Previews

/** Cas limites : produit standard, nom très long, aucun avis, note basse. */
class ProduitProvider : PreviewParameterProvider<Produit> {
    override val values = sequenceOf(produitFictif, produitNomLong, produitSansAvis, produitNoteBasse)
}

@PreviewLightDark
@Composable
private fun ProduitScreenPreview(@PreviewParameter(ProduitProvider::class) produit: Produit) {
    FicheProduitTheme {
        ProduitScreen(
            produit = produit,
            selection = SelectionProduit(taille = Taille.M),
            nombreArticlesPanier = 2,
            actions = ActionsProduit(),
            onOngletClick = {},
        )
    }
}

@PreviewScreenSizes
@Composable
private fun ProduitScreenTaillesPreview() {
    FicheProduitTheme {
        ProduitScreen(
            produit = produitFictif,
            selection = SelectionProduit(taille = Taille.L, couleurId = "sauge", descriptionDepliee = true),
            nombreArticlesPanier = 3,
            actions = ActionsProduit(),
            onOngletClick = {},
        )
    }
}

@PreviewFontScale
@Composable
private fun ProduitScreenPolicePreview() {
    FicheProduitTheme {
        ProduitScreen(
            produit = produitFictif,
            selection = SelectionProduit(),
            nombreArticlesPanier = 0,
            actions = ActionsProduit(),
            onOngletClick = {},
        )
    }
}

@Preview(name = "Français", locale = "fr", showSystemUi = true)
@Preview(name = "Arabe (RTL)", locale = "ar", showSystemUi = true)
@Preview(name = "Sombre, français", locale = "fr", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
@Composable
private fun ProduitScreenLanguesPreview() {
    FicheProduitTheme {
        ProduitScreen(
            produit = produitFictif,
            selection = SelectionProduit(taille = Taille.S, couleurId = "terracotta"),
            nombreArticlesPanier = 1,
            actions = ActionsProduit(),
            onOngletClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun NoteEtoilesPreview() {
    FicheProduitTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5f, 4.5f, 3.2f, 0.4f).forEach { NoteEtoiles(note = it, nombreAvis = 12) }
                NoteEtoiles(note = 0f, nombreAvis = 0)
            }
        }
    }
}
