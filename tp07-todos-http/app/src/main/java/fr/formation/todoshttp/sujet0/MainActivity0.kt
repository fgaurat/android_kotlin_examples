package fr.formation.todoshttp.sujet0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

// Étape 0 — Afficher une liste de tâches écrite « en dur » (pas de réseau)
// On se concentre sur l'affichage :
// - une data class Todo, avec les mêmes champs que le JSON de JSON Server
// - LazyColumn + items(…, key = …) : une ligne par tâche, seules les lignes visibles sont composées
// - ListItem + Checkbox (onCheckedChange = null : case en lecture seule)
// - la liste est un ÉTAT (mutableStateOf) : la remplacer par une nouvelle liste redessine l'écran
// - ajouter : bouton flottant (FloatingActionButton du Scaffold) → boîte de dialogue (AlertDialog)
// - supprimer : une corbeille sur chaque ligne
// - state hoisting : TodosRoute GARDE l'état et sait le modifier ; TodosEcran ne fait qu'AFFICHER ce qu'il
//   reçoit et PRÉVENIR (onAjouter, onSupprimer). Résultat : TodosEcran s'affiche dans un @Preview.
// ❌ Tourner l'écran après un ajout : la tâche disparaît (remember ne survit pas à la rotation)
// Pas besoin de JSON Server pour cette étape.
// ➡ Étape 1 : on garde CE MÊME écran, et on remplace la liste en dur par un appel Retrofit.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// Ici tout est dans un seul fichier pour la lecture. Découpé, l'étape 0 donnerait, dans
// app/src/main/java/fr/formation/todoshttp/ (ou …/todoshttp/sujet0/ pour rester dans ce projet) :
//
// ├── MainActivity.kt            MainActivity0
// ├── data/
// │   ├── TodosEnDur.kt          todosEnDur
// │   └── model/
// │       └── Todo.kt            Todo
// └── ui/
//     └── todos/
//         ├── DialogueAjout.kt   DialogueAjout
//         ├── TodosEcran.kt      TodosEcran (sans état) + @Preview
//         └── TodosRoute.kt      TodosRoute (avec état)
//
// - un fichier par classe, nommé comme elle ; le dossier donne le package :
//   ui/todos/TodosRoute.kt → package fr.formation.todoshttp.ui.todos
// - data/ = d'où viennent les données ; ui/ = ce qui s'affiche, un sous-dossier par écran
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer le private de todosEnDur
//   et des composables appelés depuis un autre fichier (TodosRoute, TodosEcran, DialogueAjout)
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/model/Todo.kt
// 1. Les données (à l'étape 1, on ajoutera seulement @Serializable)
data class Todo(val id: String, val userId: Int, val title: String, val completed: Boolean = false)

// 📄 data/TodosEnDur.kt
// 2. Une liste en dur, copiée de json-server/db.json
private val todosEnDur = listOf(
    Todo(id = "1", userId = 1, title = "delectus aut autem"),
    Todo(id = "2", userId = 1, title = "quis ut nam facilis et officia qui"),
    Todo(id = "3", userId = 1, title = "fugiat veniam minus"),
    Todo(id = "4", userId = 1, title = "et porro tempora", completed = true),
    Todo(id = "5", userId = 1, title = "laboriosam mollitia et enim quasi adipisci quia provident illum"),
    Todo(id = "6", userId = 1, title = "qui ullam ratione quibusdam voluptatem quia omnis"),
    Todo(id = "7", userId = 1, title = "illo expedita consequatur quia in"),
    Todo(id = "8", userId = 1, title = "quo adipisci enim quam ut ab", completed = true),
)

// 📄 MainActivity.kt
class MainActivity0 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TodosRoute(modifier = Modifier.safeDrawingPadding())
            }
        }
    }
}

// 📄 ui/todos/TodosRoute.kt
// 3. Composable AVEC état : il possède la liste et sait la modifier
@Composable
private fun TodosRoute(modifier: Modifier = Modifier) {
    var todos by remember { mutableStateOf(todosEnDur) }

    TodosEcran(
        todos = todos,
        onAjouter = { titre ->
            val id = (todos.maxOfOrNull { it.id.toInt() } ?: 0) + 1           // à l'étape 1, c'est le serveur
            todos = todos + Todo(id = id.toString(), userId = 1, title = titre)  // nouvelle liste → recomposition
        },
        onSupprimer = { todo -> todos = todos - todo },
        modifier = modifier,
    )
}

// 📄 ui/todos/TodosEcran.kt
// 4. Composable SANS état (state hoisting) : il reçoit ce qu'il affiche et remonte les événements (onXxx).
// Il ne connaît ni le ViewModel ni le réseau → on peut l'afficher dans un @Preview.
@Composable
private fun TodosEcran(
    todos: List<Todo>,
    onAjouter: (String) -> Unit,
    onSupprimer: (Todo) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ouvert ou fermé : un état d'UI pur, que personne d'autre n'a besoin de connaître → il peut rester ici
    var dialogueOuvert by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { dialogueOuvert = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une tâche")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Tâches de l'utilisateur 1", style = MaterialTheme.typography.titleLarge)
            // contentPadding : la dernière ligne n'est pas cachée par le bouton flottant
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(todos, key = { it.id }) { todo ->
                    ListItem(
                        headlineContent = { Text(todo.title) },
                        leadingContent = { Checkbox(checked = todo.completed, onCheckedChange = null) },
                        trailingContent = {
                            IconButton(onClick = { onSupprimer(todo) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        },
                    )
                }
            }
        }
    }

    if (dialogueOuvert) {
        DialogueAjout(onAjouter = onAjouter, onFermer = { dialogueOuvert = false })
    }
}

// 📄 ui/todos/DialogueAjout.kt
// Boîte de dialogue d'ajout. Le texte en cours de saisie ne concerne qu'elle : il reste ici.
@Composable
private fun DialogueAjout(onAjouter: (String) -> Unit, onFermer: () -> Unit) {
    var titre by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onFermer,                   // toucher à côté, ou bouton Retour
        title = { Text("Nouvelle tâche") },
        text = {
            OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Titre") }, singleLine = true)
        },
        confirmButton = {
            TextButton(onClick = { onAjouter(titre.trim()); onFermer() }, enabled = titre.isNotBlank()) {
                Text("Ajouter")
            }
        },
        dismissButton = { TextButton(onClick = onFermer) { Text("Annuler") } },
    )
}

// 📄 ui/todos/TodosEcran.kt (même fichier que TodosEcran)
// Aperçu dans Android Studio : possible parce que TodosEcran ne dépend que de ses paramètres
@Preview(showBackground = true)
@Composable
private fun TodosEcranPreview() {
    MaterialTheme {
        TodosEcran(todos = todosEnDur, onAjouter = {}, onSupprimer = {})
    }
}
