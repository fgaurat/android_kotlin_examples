package fr.formation.taches.ui.taches

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Le seul composable qui connaît le ViewModel : il collecte l'état et branche les callbacks.
 * `viewModel()` renvoie la même instance tant que l'écran existe (rotation comprise).
 */
@Composable
fun TachesRoute(
    modifier: Modifier = Modifier,
    viewModel: TachesViewModel = viewModel(factory = TachesViewModel.Factory),
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val recherche by viewModel.recherche.collectAsStateWithLifecycle()
    // Bonus : LiveData observée depuis Compose (valeur initiale obligatoire : LiveData peut être vide)
    val restantes by viewModel.restantes.observeAsState(initial = 0)

    TachesScreen(
        etat = etat,
        recherche = recherche,
        restantes = restantes,
        onRechercheChange = viewModel::onRechercheChange,
        onFiltreChange = viewModel::onFiltreChange,
        onAjouter = viewModel::ajouter,
        onBasculer = viewModel::basculer,
        onSupprimer = viewModel::supprimer,
        modifier = modifier,
    )
}
