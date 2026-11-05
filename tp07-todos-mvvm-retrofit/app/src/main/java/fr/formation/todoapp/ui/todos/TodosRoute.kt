package fr.formation.todoapp.ui.todos

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Le seul composable qui connaît le ViewModel : il collecte l'état et branche les callbacks.
 * `viewModel()` renvoie la même instance tant que l'écran existe (rotation comprise).
 */
@Composable
fun TodosRoute(
    modifier: Modifier = Modifier,
    viewModel: TodosViewModel = viewModel(factory = TodosViewModel.Factory),
) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val recherche by viewModel.recherche.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    // Message ponctuel : affiché une fois en Snackbar, puis signalé comme consommé au ViewModel
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageAffiche()
        }
    }

    TodosScreen(
        etat = etat,
        recherche = recherche,
        snackbarHostState = snackbarHostState,
        onRechercheChange = viewModel::onRechercheChange,
        onRafraichir = viewModel::rafraichir,
        onAjouter = viewModel::ajouter,
        onBasculer = viewModel::basculer,
        onSupprimer = viewModel::supprimer,
        modifier = modifier,
    )
}
