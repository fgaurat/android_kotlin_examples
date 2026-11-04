package fr.formation.todos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.todos.domain.Filtre
import fr.formation.todos.domain.Todo
import fr.formation.todos.domain.TodoStore
import fr.formation.todos.domain.aUnTitreLong
import fr.formation.todos.domain.message
import fr.formation.todos.domain.nombreParUtilisateur
import fr.formation.todos.domain.resume
import fr.formation.todos.domain.selon
import fr.formation.todos.domain.titreCourt
import fr.formation.todos.domain.titreLePlusLong
import fr.formation.todos.domain.triees
import fr.formation.todos.ui.theme.TodosTheme
import kotlinx.coroutines.launch

// Le TP se fait en Kotlin pur (package domain + tests). Cet écran sert seulement à « voir » le résultat :
// Compose est présenté au jour 2, l'état (remember / mutableStateOf) aussi.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodosTheme {
                EcranTodos()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranTodos() {
    // TodoStore n'est pas observable : on garde une copie dans un état Compose,
    // rafraîchie après chaque action (TodoStore.todos renvoie une nouvelle liste).
    var todos by remember { mutableStateOf(TodoStore.todos) }
    var filtre by remember { mutableStateOf(Filtre.TOUTES) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun rafraichir(message: String? = null) {
        todos = TodoStore.todos
        if (message != null) {
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(innerPadding),
        ) {
            item { CarteResume(todos) }
            item { LigneFiltres(filtre, onChoisir = { filtre = it }) }
            // Étapes 3 et 4 : filtre (enum + when) puis tri, calculés à partir de la liste courante
            items(todos.selon(filtre).triees(), key = Todo::id) { todo ->
                LigneTodo(
                    todo = todo,
                    onCocher = { cochee ->
                        if (cochee) {
                            rafraichir(TodoStore.terminer(todo.id).message())   // étape 5
                        } else {
                            TodoStore.rouvrir(todo.id)
                            rafraichir()
                        }
                    },
                )
            }
        }
    }
}

/** Étapes 4 et 6 : résumé, regroupement par utilisateur, recherche par référence de fonction. */
@Composable
fun CarteResume(todos: List<Todo>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(todos.resume(), fontWeight = FontWeight.Bold)
            Text(
                "Par utilisateur : " +
                    todos.nombreParUtilisateur().entries.joinToString { "n°${it.key} → ${it.value}" },
            )
            todos.titreLePlusLong()?.let { Text("Titre le plus long : ${it.titreCourt}") }
            Text(
                "filtrer(::aUnTitreLong) : " + TodoStore.filtrer(::aUnTitreLong).joinToString { it.id },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun LigneFiltres(filtre: Filtre, onChoisir: (Filtre) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Filtre.entries.forEach { choix ->
            FilterChip(
                selected = choix == filtre,
                onClick = { onChoisir(choix) },
                label = { Text(choix.libelle) },
            )
        }
    }
}

@Composable
fun LigneTodo(todo: Todo, onCocher: (Boolean) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = todo.terminee, onCheckedChange = onCocher)
            Column(Modifier.weight(1f)) {
                Text(
                    todo.titre,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (todo.terminee) TextDecoration.LineThrough else null,
                )
                Text(
                    "Utilisateur ${todo.utilisateurId} · tâche n°${todo.id}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        HorizontalDivider(Modifier.padding(top = 4.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun EcranTodosPreview() {
    TodosTheme { EcranTodos() }
}
