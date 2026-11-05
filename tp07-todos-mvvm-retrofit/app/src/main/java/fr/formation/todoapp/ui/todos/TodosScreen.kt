package fr.formation.todoapp.ui.todos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.todoapp.domain.Todo
import fr.formation.todoapp.ui.theme.TodoAppTheme

/**
 * Écran sans ViewModel : un état en entrée, des callbacks en sortie.
 * Prévisualisable et testable avec n'importe quel état, sans réseau.
 */
@OptIn(ExperimentalMaterial3Api::class)               // TopAppBar
@Composable
fun TodosScreen(
    etat: TodosUiState,
    recherche: String,
    snackbarHostState: SnackbarHostState,
    onRechercheChange: (String) -> Unit,
    onRafraichir: () -> Unit,
    onAjouter: (String) -> Unit,
    onBasculer: (Todo) -> Unit,
    onSupprimer: (Todo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tâches de l'utilisateur ${TodosViewModel.UTILISATEUR_ID}") },
                actions = {
                    IconButton(onClick = onRafraichir) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Recharger")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FormulaireAjout(onAjouter = onAjouter)
            OutlinedTextField(
                value = recherche,
                onValueChange = onRechercheChange,
                label = { Text("Rechercher (côté serveur)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider()

            // Un affichage par état : le compilateur vérifie que les trois cas sont traités
            when (etat) {
                TodosUiState.Chargement -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is TodosUiState.Erreur -> Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(etat.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRafraichir) { Text("Réessayer") }
                }
                is TodosUiState.Succes -> ListeTodos(etat, recherche, onBasculer, onSupprimer)
            }
        }
    }
}

/** Le texte en cours de saisie est un état d'UI éphémère : rememberSaveable suffit, pas besoin du ViewModel. */
@Composable
private fun FormulaireAjout(onAjouter: (String) -> Unit) {
    var titre by rememberSaveable { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = titre,
            onValueChange = { titre = it },
            label = { Text("Nouvelle tâche") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Button(
            onClick = {
                onAjouter(titre)
                titre = ""
            },
            enabled = titre.isNotBlank(),
        ) { Text("Ajouter") }
    }
}

@Composable
private fun ListeTodos(
    etat: TodosUiState.Succes,
    recherche: String,
    onBasculer: (Todo) -> Unit,
    onSupprimer: (Todo) -> Unit,
) {
    Text("${etat.restantes} tâche(s) restante(s) sur ${etat.todos.size}", style = MaterialTheme.typography.bodyMedium)

    if (etat.todos.isEmpty()) {
        Text(if (recherche.isBlank()) "Aucune tâche" else "Aucune tâche ne contient « $recherche »")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(etat.todos, key = { it.id }) { todo ->       // clé stable : l'id du serveur
            LigneTodo(todo, onBasculer = { onBasculer(todo) }, onSupprimer = { onSupprimer(todo) })
        }
    }
}

@Composable
private fun LigneTodo(todo: Todo, onBasculer: () -> Unit, onSupprimer: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = todo.terminee, onCheckedChange = { onBasculer() })
        Text(
            text = todo.titre,
            textDecoration = if (todo.terminee) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onSupprimer) {
            Icon(Icons.Filled.Delete, contentDescription = "Supprimer « ${todo.titre} »")
        }
    }
}

// ---------- Prévisualisations : un état par preview, aucun réseau ----------

private val TODOS_EXEMPLE = listOf(
    Todo("1", "delectus aut autem", terminee = false, utilisateurId = 1),
    Todo("4", "et porro tempora", terminee = true, utilisateurId = 1),
    Todo("8", "quo adipisci enim quam ut ab", terminee = true, utilisateurId = 1),
)

@Composable
private fun Apercu(etat: TodosUiState, recherche: String = "") {
    TodoAppTheme {
        TodosScreen(
            etat = etat,
            recherche = recherche,
            snackbarHostState = remember { SnackbarHostState() },
            onRechercheChange = {}, onRafraichir = {}, onAjouter = {}, onBasculer = {}, onSupprimer = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodosScreenSuccesPreview() = Apercu(TodosUiState.Succes(TODOS_EXEMPLE))

@Preview(showBackground = true)
@Composable
private fun TodosScreenChargementPreview() = Apercu(TodosUiState.Chargement)

@Preview(showBackground = true)
@Composable
private fun TodosScreenErreurPreview() =
    Apercu(TodosUiState.Erreur("Serveur injoignable : JSON Server est-il lancé ?"))

@Preview(showBackground = true)
@Composable
private fun TodosScreenRechercheVidePreview() = Apercu(TodosUiState.Succes(emptyList()), recherche = "xyz")
