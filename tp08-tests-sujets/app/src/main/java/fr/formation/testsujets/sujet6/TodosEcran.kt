package fr.formation.testsujets.sujet6

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Étape 6 — Le CODE testé : un écran Compose SANS état (state hoisting)
// Il reçoit un état et prévient par des callbacks : on peut donc le tester sans ViewModel ni réseau,
// en lui passant un FAUX état. Les tests sont dans src/androidTest/ (ils tournent sur l'émulateur).
// Pensé pour être testable… et accessible : ce sont les mêmes informations (textes, rôles, descriptions)
// que lisent TalkBack et les tests.
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// main/…/ui/todos/TodosUiState.kt                    TodosUiState
// main/…/ui/todos/TodosEcran.kt                      TodosEcran (+ @Preview)
// main/…/MainActivity.kt                             MainActivity6 (démo)
// androidTest/…/ui/todos/TodosEcranTest.kt           TodosEcranTest
//   src/androidTest/ : tests instrumentés, sur émulateur ou appareil (./gradlew connectedDebugAndroidTest)

data class Todo(val id: String, val titre: String, val terminee: Boolean = false)

sealed interface TodosUiState {
    data object Chargement : TodosUiState
    data class Succes(val todos: List<Todo>) : TodosUiState
    data class Erreur(val message: String) : TodosUiState
}

// Tag de test : pour un élément sans texte (l'indicateur de chargement)
const val TAG_CHARGEMENT = "chargement"

@Composable
fun TodosEcran(
    etat: TodosUiState,
    onBasculer: (Todo) -> Unit,
    onSupprimer: (Todo) -> Unit,
    onReessayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mes tâches", style = MaterialTheme.typography.headlineMedium)
        when (etat) {
            TodosUiState.Chargement -> CircularProgressIndicator(modifier = Modifier.testTag(TAG_CHARGEMENT))
            is TodosUiState.Erreur -> {
                Text(etat.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = onReessayer) { Text("Réessayer") }
            }
            is TodosUiState.Succes -> {
                val restantes = etat.todos.count { !it.terminee }
                Text(if (restantes == 0) "Tout est fait" else "$restantes à faire")
                LazyColumn {
                    items(etat.todos, key = { it.id }) { todo ->
                        ListItem(
                            headlineContent = { Text(todo.titre) },
                            // La case ne gère pas le clic : toute la ligne est « cochable » (rôle Checkbox).
                            // Le texte et l'état coché sont FUSIONNÉS dans un seul nœud sémantique.
                            leadingContent = { Checkbox(checked = todo.terminee, onCheckedChange = null) },
                            trailingContent = {
                                // Description unique par ligne : « Supprimer Acheter du pain »
                                IconButton(onClick = { onSupprimer(todo) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer ${todo.titre}")
                                }
                            },
                            modifier = Modifier.toggleable(
                                value = todo.terminee,
                                role = Role.Checkbox,
                                onValueChange = { onBasculer(todo) },
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodosEcranPreview() {
    MaterialTheme {
        TodosEcran(
            etat = TodosUiState.Succes(listOf(Todo("1", "Acheter du pain"), Todo("2", "Réviser Kotlin", true))),
            onBasculer = {}, onSupprimer = {}, onReessayer = {},
        )
    }
}

// Démo : l'écran avec un état local, pour le voir tourner (le TP, lui, se lance par les tests)
class MainActivity6 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var todos by remember {
                        mutableStateOf(listOf(Todo("1", "Acheter du pain"), Todo("2", "Réviser Kotlin", true), Todo("3", "Appeler le plombier")))
                    }
                    TodosEcran(
                        etat = TodosUiState.Succes(todos),
                        onBasculer = { t -> todos = todos.map { if (it.id == t.id) it.copy(terminee = !it.terminee) else it } },
                        onSupprimer = { t -> todos = todos - t },
                        onReessayer = {},
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
            }
        }
    }
}
