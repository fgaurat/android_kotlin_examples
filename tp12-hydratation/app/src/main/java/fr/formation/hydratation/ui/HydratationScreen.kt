package fr.formation.hydratation.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import fr.formation.hydratation.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydratationScreen(viewModel: HydratationViewModel = viewModel(factory = HydratationViewModel.Factory)) {
    val rappelsActifs by viewModel.rappelsActifs.collectAsStateWithLifecycle()
    val verres by viewModel.verresDuJour.collectAsStateWithLifecycle()
    val etatTravail by viewModel.etatTravail.collectAsStateWithLifecycle()
    val modeAvion by rememberModeAvion()

    LifecycleResumeEffect(Unit) {
        viewModel.auPremierPlan()
        onPauseOrDispose { }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            if (modeAvion) BandeauModeAvion()

            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CarteCompteur(
                    verres = verres,
                    onAjouter = viewModel::ajouterVerre,
                    onRemettreAZero = viewModel::remettreAZero,
                )
                CarteRappels(
                    rappelsActifs = rappelsActifs,
                    etatTravail = etatTravail,
                    onChange = viewModel::definirRappels,
                    onTester = viewModel::testerMaintenant,
                )
            }
        }
    }
}

/** Étape 7 : bandeau affiché tant que le mode avion est actif. */
@Composable
private fun BandeauModeAvion() {
    Text(
        text = stringResource(R.string.bandeau_mode_avion),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
        color = MaterialTheme.colorScheme.onErrorContainer,
        textAlign = TextAlign.Center,
    )
}

/** Bonus : compteur du jour lu dans DataStore. */
@Composable
private fun CarteCompteur(verres: Int, onAjouter: () -> Unit, onRemettreAZero: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.compteur_titre), style = MaterialTheme.typography.titleMedium)
            Text("$verres 💧", style = MaterialTheme.typography.displayMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAjouter) { Text(stringResource(R.string.ajouter_verre)) }
                OutlinedButton(onClick = onRemettreAZero) { Text(stringResource(R.string.remettre_a_zero)) }
            }
        }
    }
}

@Composable
private fun CarteRappels(
    rappelsActifs: Boolean,
    etatTravail: WorkInfo?,
    onChange: (Boolean) -> Unit,
    onTester: () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var afficherExplication by remember { mutableStateOf(false) }
    var refusDefinitif by remember { mutableStateOf(false) }
    var notificationsPossibles by remember { mutableStateOf(context.notificationsPossibles()) }

    // Étape 2 : permission demandée EN CONTEXTE, quand l'utilisateur active les rappels
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { accordee ->
        notificationsPossibles = context.notificationsPossibles()
        if (accordee) {
            refusDefinitif = false
            onChange(true)
        } else {
            refusDefinitif = activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Revérification au retour des réglages (permission ou canal modifiés)
    LifecycleResumeEffect(Unit) {
        notificationsPossibles = context.notificationsPossibles()
        if (notificationsPossibles) refusDefinitif = false
        onPauseOrDispose { }
    }

    val demanderActivation = {
        when {
            context.aLaPermissionNotifications() -> onChange(true)
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.POST_NOTIFICATIONS,
            ) -> afficherExplication = true
            else -> launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.rappels_actives), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.rappels_explication), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = rappelsActifs,
                    onCheckedChange = { actif -> if (actif) demanderActivation() else onChange(false) },
                )
            }

            Text(libelleEtatTravail(etatTravail), style = MaterialTheme.typography.bodyMedium)

            if (refusDefinitif || (rappelsActifs && !notificationsPossibles)) {
                Text(
                    stringResource(R.string.notifications_bloquees),
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = { context.ouvrirReglagesNotifications() }) {
                    Text(stringResource(R.string.ouvrir_reglages))
                }
            }

            OutlinedButton(onClick = onTester, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.tester_maintenant))
            }
        }
    }

    if (afficherExplication) {
        AlertDialog(
            onDismissRequest = { afficherExplication = false },
            title = { Text(stringResource(R.string.permission_titre)) },
            text = { Text(stringResource(R.string.permission_explication)) },
            confirmButton = {
                TextButton(onClick = {
                    afficherExplication = false
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text(stringResource(R.string.continuer)) }
            },
            dismissButton = {
                TextButton(onClick = { afficherExplication = false }) { Text(stringResource(R.string.non_merci)) }
            },
        )
    }
}

@Composable
private fun libelleEtatTravail(info: WorkInfo?): String {
    if (info == null) return stringResource(R.string.travail_aucun)
    val etat = when (info.state) {
        WorkInfo.State.ENQUEUED -> stringResource(R.string.travail_planifie)
        WorkInfo.State.RUNNING -> stringResource(R.string.travail_en_cours)
        WorkInfo.State.CANCELLED -> stringResource(R.string.travail_annule)
        else -> info.state.name
    }
    val prochain = info.nextScheduleTimeMillis
    return if (info.state == WorkInfo.State.ENQUEUED && prochain != Long.MAX_VALUE) {
        val heure = Instant.ofEpochMilli(prochain).atZone(ZoneId.systemDefault()).format(FORMAT_HEURE)
        stringResource(R.string.travail_prochain, etat, heure)
    } else {
        etat
    }
}

private val FORMAT_HEURE = DateTimeFormatter.ofPattern("HH:mm")

private fun Context.aLaPermissionNotifications() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

/** Permission accordée ET notifications non coupées dans les réglages. */
private fun Context.notificationsPossibles() =
    aLaPermissionNotifications() && NotificationManagerCompat.from(this).areNotificationsEnabled()

private fun Context.ouvrirReglagesNotifications() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
    )
}
