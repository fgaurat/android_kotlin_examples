package fr.formation.ousuisje.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.formation.ousuisje.BuildConfig
import fr.formation.ousuisje.R
import fr.formation.ousuisje.geo.Lieu
import fr.formation.ousuisje.geo.VILLES_DE_REPLI
import fr.formation.ousuisje.geo.formaterDistance
import fr.formation.ousuisje.geo.formaterLatitude
import fr.formation.ousuisje.geo.formaterLongitude
import fr.formation.ousuisje.geo.formaterPrecision
import fr.formation.ousuisje.permissions.localisationAccordee
import fr.formation.ousuisje.permissions.localisationPreciseAccordee
import fr.formation.ousuisje.permissions.ouvrirParametresApp
import fr.formation.ousuisje.permissions.ouvrirReglagesLocalisation
import fr.formation.ousuisje.permissions.rememberDemandeLocalisation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OuSuisJeScreen(viewModel: OuSuisJeViewModel = viewModel(factory = OuSuisJeViewModel.Factory)) {
    val context = LocalContext.current
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val permission by viewModel.permission.collectAsStateWithLifecycle()
    val suiviActif by viewModel.suiviActif.collectAsStateWithLifecycle()

    val rafraichirPermissions = {
        viewModel.permissionsMisesAJour(
            accordee = context.localisationAccordee(),
            precise = context.localisationPreciseAccordee(),
        )
    }
    val demande = rememberDemandeLocalisation(
        onResultat = rafraichirPermissions,
        onAccordee = viewModel::localiser,
    )

    // Revérification au retour au premier plan (ex. après un passage dans les paramètres
    // ou un « adb shell pm revoke »)
    LifecycleResumeEffect(Unit) {
        rafraichirPermissions()
        if (context.localisationAccordee()) demande.refusDefinitif = false
        onPauseOrDispose { }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BanniereOffre()      // classe différente dans src/gratuit et src/pro
            if (BuildConfig.ENVIRONNEMENT != "prod") {
                AssistChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.environnement, BuildConfig.ENVIRONNEMENT)) },
                )
            }

            CartePermission(
                permission = permission,
                refusDefinitif = demande.refusDefinitif,
                onLocaliser = demande.lancer,
                onPrecise = demande.demanderPrecise,
                onParametres = { context.ouvrirParametresApp() },
            )

            if (viewModel.suiviDisponible && permission.accordee) {
                CarteSuivi(actif = suiviActif, onBasculer = viewModel::basculerSuivi)
            }

            CarteResultat(etat = etat, onReglages = { context.ouvrirReglagesLocalisation() })

            if (!permission.accordee) {
                ModeDegrade(onChoisir = viewModel::choisirVille)
            }
        }
    }

    if (demande.afficherExplication) {
        AlertDialog(
            onDismissRequest = { demande.afficherExplication = false },
            title = { Text(stringResource(R.string.permission_titre)) },
            text = { Text(stringResource(R.string.permission_explication)) },
            confirmButton = {
                TextButton(onClick = demande.confirmer) { Text(stringResource(R.string.permission_continuer)) }
            },
            dismissButton = {
                TextButton(onClick = { demande.afficherExplication = false }) {
                    Text(stringResource(R.string.permission_non_merci))
                }
            },
        )
    }
}

@Composable
private fun CartePermission(
    permission: EtatPermission,
    refusDefinitif: Boolean,
    onLocaliser: () -> Unit,
    onPrecise: () -> Unit,
    onParametres: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(
                    when {
                        permission.precise -> R.string.etat_permission_precise
                        permission.accordee -> R.string.etat_permission_approximative
                        else -> R.string.etat_permission_aucune
                    },
                ),
                style = MaterialTheme.typography.titleMedium,
            )
            Button(onClick = onLocaliser, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.bouton_localiser))
            }
            // Android 12+ : on peut proposer de passer d'approximative à précise
            if (permission.accordee && !permission.precise) {
                OutlinedButton(onClick = onPrecise, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.passer_en_precise))
                }
            }
            if (refusDefinitif && !permission.accordee) {
                Text(stringResource(R.string.permission_refus_definitif))
                TextButton(onClick = onParametres) { Text(stringResource(R.string.ouvrir_parametres)) }
            }
        }
    }
}

@Composable
private fun CarteSuivi(actif: Boolean, onBasculer: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(stringResource(R.string.suivi_titre), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(if (actif) R.string.suivi_arreter else R.string.suivi_demarrer))
            }
            Switch(checked = actif, onCheckedChange = { onBasculer() })
        }
    }
}

@Composable
private fun CarteResultat(etat: EtatPosition, onReglages: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (etat) {
                EtatPosition.Initial -> Text(stringResource(R.string.aucune_position))
                EtatPosition.Recherche -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.recherche_position))
                }
                is EtatPosition.Erreur -> {
                    Text(
                        stringResource(
                            when (etat.cause) {
                                CauseErreur.LOCALISATION_DESACTIVEE -> R.string.erreur_localisation_desactivee
                                CauseErreur.PERMISSION_RETIREE -> R.string.erreur_permission_retiree
                                CauseErreur.ECHEC -> R.string.erreur_echec
                            },
                        ),
                        color = MaterialTheme.colorScheme.error,
                    )
                    if (etat.cause == CauseErreur.LOCALISATION_DESACTIVEE) {
                        TextButton(onClick = onReglages) { Text(stringResource(R.string.ouvrir_reglages_localisation)) }
                    }
                }
                is EtatPosition.Trouvee -> {
                    Text(
                        when (etat.origine) {
                            Origine.PONCTUELLE -> stringResource(R.string.origine_ponctuelle)
                            Origine.SUIVI -> stringResource(R.string.origine_suivi)
                            Origine.VILLE_CHOISIE -> stringResource(R.string.origine_ville, etat.nomVille.orEmpty())
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Ligne(stringResource(R.string.libelle_latitude), formaterLatitude(etat.position.latitude))
                    Ligne(stringResource(R.string.libelle_longitude), formaterLongitude(etat.position.longitude))
                    if (etat.origine != Origine.VILLE_CHOISIE) {
                        Ligne(stringResource(R.string.libelle_precision), formaterPrecision(etat.position.precisionMetres))
                    }
                    Ligne(
                        stringResource(R.string.libelle_distance_paris),
                        formaterDistance(etat.distanceParisMetres, etat.approximative),
                    )
                    if (etat.approximative) {
                        Text(
                            stringResource(R.string.badge_approximative),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Ligne(libelle: String, valeur: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(libelle, style = MaterialTheme.typography.bodyMedium)
        Text(valeur, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ModeDegrade(onChoisir: (Lieu) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.mode_degrade_titre), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VILLES_DE_REPLI.forEach { lieu ->
                AssistChip(onClick = { onChoisir(lieu) }, label = { Text(lieu.nom) })
            }
        }
    }
}
