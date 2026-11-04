package fr.formation.ficheproduit.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.formation.ficheproduit.R
import fr.formation.ficheproduit.donnees.couleursFictives
import fr.formation.ficheproduit.donnees.produitFictif
import fr.formation.ficheproduit.logique.ajouter
import fr.formation.ficheproduit.logique.avecQuantite
import fr.formation.ficheproduit.logique.formaterPrix
import fr.formation.ficheproduit.logique.nombreArticles
import fr.formation.ficheproduit.logique.peutEtreAjoute
import fr.formation.ficheproduit.modele.LignePanier
import fr.formation.ficheproduit.modele.Produit
import fr.formation.ficheproduit.modele.Taille
import fr.formation.ficheproduit.ui.navigation.NavigationPrincipale
import fr.formation.ficheproduit.ui.navigation.Onglet
import fr.formation.ficheproduit.ui.panier.PanierScreen
import fr.formation.ficheproduit.ui.produit.ActionsProduit
import fr.formation.ficheproduit.ui.produit.ProduitScreen
import fr.formation.ficheproduit.ui.produit.SelectionProduit
import kotlinx.coroutines.launch

/** Sauvegarde du panier pour rememberSaveable (types simples uniquement dans un Bundle). */
private val PanierSaver: Saver<List<LignePanier>, Any> = listSaver(
    save = { lignes ->
        lignes.map { listOf(it.produitId, it.nom, it.taille.name, it.couleur.id, it.prixCentimes, it.quantite) }
    },
    restore = { valeurs ->
        valeurs.map {
            @Suppress("UNCHECKED_CAST")
            val v = it as List<Any>
            LignePanier(
                produitId = v[0] as String,
                nom = v[1] as String,
                taille = Taille.valueOf(v[2] as String),
                couleur = couleursFictives.first { c -> c.id == v[3] },
                prixCentimes = v[4] as Int,
                quantite = v[5] as Int,
            )
        }
    },
)

/**
 * Composable AVEC état : onglet courant, choix sur la fiche, contenu du panier.
 * Les écrans `ProduitScreen` et `PanierScreen` restent sans état.
 */
@Composable
fun FicheProduitApp(modifier: Modifier = Modifier, produit: Produit = produitFictif) {
    var onglet by rememberSaveable { mutableStateOf(Onglet.Accueil) }      // enum : sérialisable
    var taille by rememberSaveable { mutableStateOf<Taille?>(null) }
    var couleurId by rememberSaveable { mutableStateOf(produit.couleurs.first().id) }
    var descriptionDepliee by rememberSaveable { mutableStateOf(false) }
    var panier by rememberSaveable(stateSaver = PanierSaver) { mutableStateOf(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val contexte = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]

    val messageAjout = stringResource(R.string.ajoute_au_panier, produit.nom, taille?.name.orEmpty())
    val voirPanier = stringResource(R.string.voir_panier)
    val merci = stringResource(R.string.commande_simulee)
    val titrePartage = stringResource(R.string.partager_via)
    val textePartage = stringResource(R.string.texte_partage, produit.nom, formaterPrix(produit.prixCentimes, locale))

    when (onglet) {
        Onglet.Accueil -> ProduitScreen(
            produit = produit,
            selection = SelectionProduit(taille, couleurId, descriptionDepliee),
            nombreArticlesPanier = panier.nombreArticles(),
            snackbarHostState = snackbarHostState,
            onOngletClick = { onglet = it },
            modifier = modifier,
            actions = ActionsProduit(
                // Pas d'écran précédent dans cette démo : le retour ferme l'activité
                onRetour = { (contexte as? Activity)?.finish() },
                onPartager = {
                    val envoi = Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, textePartage)
                    contexte.startActivity(Intent.createChooser(envoi, titrePartage))
                },
                onTailleChange = { taille = it },
                onCouleurChange = { couleurId = it.id },
                onDescriptionDeplieeChange = { descriptionDepliee = it },
                onAjouterAuPanier = {
                    val tailleChoisie = taille
                    if (tailleChoisie != null && produit.peutEtreAjoute(tailleChoisie)) {
                        val couleur = produit.couleurs.first { it.id == couleurId }
                        panier = panier.ajouter(
                            LignePanier(produit.id, produit.nom, tailleChoisie, couleur, produit.prixCentimes),
                        )
                        scope.launch {
                            val resultat = snackbarHostState.showSnackbar(messageAjout, actionLabel = voirPanier)
                            if (resultat == SnackbarResult.ActionPerformed) onglet = Onglet.Panier
                        }
                    }
                },
            ),
        )

        Onglet.Panier -> PanierScreen(
            lignes = panier,
            onQuantiteChange = { ligne, quantite -> panier = panier.avecQuantite(ligne.id, quantite) },
            onCommander = {
                panier = emptyList()
                scope.launch { snackbarHostState.showSnackbar(merci) }
            },
            onOngletClick = { onglet = it },
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )

        Onglet.Recherche, Onglet.Profil -> NavigationPrincipale(
            ongletCourant = onglet,
            onOngletClick = { onglet = it },
            nombreArticlesPanier = panier.nombreArticles(),
            modifier = modifier,
        ) {
            EcranAVenir()
        }
    }
}

/** Onglets hors du périmètre du TP. */
@Composable
private fun EcranAVenir(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.a_venir),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
