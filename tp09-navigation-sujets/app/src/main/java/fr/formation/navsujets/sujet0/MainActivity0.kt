package fr.formation.navsujets.sujet0

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Étape 0 — Deux écrans SANS bibliothèque de navigation
// Un simple état dit quel écran afficher : l'id de la tâche ouverte, ou null pour la liste.
// ✅ Ça marche pour deux écrans, et l'état survit à la rotation (rememberSaveable)
// ❌ Ouvrir une tâche, puis faire le geste / bouton Retour du système : l'APPLICATION SE FERME.
//    Il n'y a pas de pile de retour : seule la flèche de la barre du haut sait revenir.
// ❌ Avec 5 écrans, il faudrait écrire soi-même la pile, les arguments, la sauvegarde…
// ➡ Étape 1 : Navigation Compose fait tout cela.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/navsujets/ (ou …/navsujets/sujet0/ pour rester dans ce projet) :
//
// ├── MainActivity.kt         MainActivity0
// ├── data/
// │   └── Todo.kt             Todo + todosEnMemoire
// └── ui/
//     ├── TodoApp.kt          TodoApp (choisit l'écran à afficher)
//     ├── todos/
//     │   └── TodosEcran.kt   TodosEcran
//     └── detail/
//         └── DetailEcran.kt  DetailEcran
//
// - un sous-dossier par écran dans ui/ ; le dossier donne le package (ui/detail → …navsujets.ui.detail)
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/Todo.kt
data class Todo(val id: String, val titre: String, val faite: Boolean = false)

// 📄 data/Todo.kt
private val todosEnMemoire = listOf(
    Todo("1", "Acheter du pain"),
    Todo("2", "Réviser Navigation Compose"),
    Todo("3", "Appeler le plombier", faite = true),
    Todo("4", "Préparer la démo du TP"),
    Todo("5", "Arroser les plantes"),
)

// 📄 MainActivity.kt
class MainActivity0 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { TodoApp() }
        }
    }
}

// 📄 ui/TodoApp.kt
@Composable
private fun TodoApp() {
    // La « navigation » : null = la liste, sinon l'id de la tâche affichée
    var idOuvert by rememberSaveable { mutableStateOf<String?>(null) }

    val id = idOuvert
    if (id == null) {
        TodosEcran(todos = todosEnMemoire, onTodoClick = { todo -> idOuvert = todo.id })
    } else {
        DetailEcran(todo = todosEnMemoire.first { it.id == id }, onRetour = { idOuvert = null })
    }
}

// 📄 ui/todos/TodosEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodosEcran(todos: List<Todo>, onTodoClick: (Todo) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Mes tâches") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(todos, key = { it.id }) { todo ->
                ListItem(
                    headlineContent = { Text(todo.titre) },
                    supportingContent = { Text(if (todo.faite) "Faite" else "À faire") },
                    modifier = Modifier.clickable { onTodoClick(todo) },     // l'écran prévient, c'est tout
                )
            }
        }
    }
}

// 📄 ui/detail/DetailEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailEcran(todo: Todo, onRetour: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tâche n° ${todo.id}") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(todo.titre, style = MaterialTheme.typography.headlineSmall)
            Text(if (todo.faite) "✅ Faite" else "⏳ À faire")
        }
    }
}
