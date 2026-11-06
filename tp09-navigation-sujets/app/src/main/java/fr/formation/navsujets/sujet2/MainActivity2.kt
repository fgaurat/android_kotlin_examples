package fr.formation.navsujets.sujet2

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

// Étape 2 — Une route avec argument : DetailTodo(id)
// Nouveau :
// - @Serializable data class DetailTodo(val id: String) : l'argument fait partie du TYPE de la route
// - navigate(DetailTodo(todo.id)) : le compilateur vérifie qu'on passe bien un id, et du bon type
// - dans composable<DetailTodo> { entree -> entree.toRoute<DetailTodo>() } : on relit l'argument
// - on passe l'IDENTIFIANT, pas l'objet Todo : la destination retrouve elle-même la tâche
//   (les arguments sont stockés dans un Bundle : taille limitée, données figées au moment du clic)
// À essayer : ouvrir une tâche, tourner l'écran (l'argument est conservé), Retour système.
// ❌ Reste à faire : la recherche de la tâche est faite dans le NavHost → un ViewModel (étape 3)

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/navsujets/ (ou …/navsujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1
// ├── data/
// │   └── Todo.kt                  Todo + todosEnMemoire
// └── ui/
//     ├── navigation/
//     │   ├── Routes.kt            Todos, DetailTodo, APropos
//     │   └── TodoNavHost.kt       TodoApp, TodoNavHost (le SEUL endroit qui connaît le NavController)
//     ├── todos/
//     │   └── TodosEcran.kt        TodosEcran
//     ├── detail/
//     │   └── DetailEcran.kt       DetailEcran
//     └── apropos/
//         └── AProposEcran.kt      AProposEcran
//
// - un sous-dossier par écran dans ui/, et ui/navigation/ pour les routes et le graphe
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

// ---------- Routes ----------

// 📄 ui/navigation/Routes.kt
@Serializable data object Todos

// 📄 ui/navigation/Routes.kt
@Serializable data class DetailTodo(val id: String)       // un argument obligatoire, typé

// 📄 ui/navigation/Routes.kt
@Serializable data object APropos

// ---------- Navigation ----------

// 📄 MainActivity.kt
class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { TodoApp() }
        }
    }
}

// 📄 ui/navigation/TodoNavHost.kt
@Composable
private fun TodoApp() {
    val navController = rememberNavController()

    // Le graphe : quelles routes existent, et quel écran pour chacune
    NavHost(navController = navController, startDestination = Todos) {
        composable<Todos> {
            TodosEcran(
                todos = todosEnMemoire,
                onTodoClick = { todo -> navController.navigate(DetailTodo(id = todo.id)) },   // on passe l'id
                onAProposClick = { navController.navigate(APropos) },
            )
        }
        composable<DetailTodo> { entree ->
            val route = entree.toRoute<DetailTodo>()                      // arguments typés
            DetailEcran(
                todo = todosEnMemoire.find { it.id == route.id },         // null si l'id n'existe pas
                onRetour = { navController.navigateUp() },
            )
        }
        composable<APropos> {
            AProposEcran(onRetour = { navController.navigateUp() })
        }
    }
}

// ---------- Écrans ----------

// 📄 ui/todos/TodosEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodosEcran(todos: List<Todo>, onTodoClick: (Todo) -> Unit, onAProposClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes tâches") },
                actions = {
                    IconButton(onClick = onAProposClick) {
                        Icon(Icons.Filled.Info, contentDescription = "À propos")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(todos, key = { it.id }) { todo ->
                ListItem(
                    headlineContent = { Text(todo.titre) },
                    supportingContent = { Text(if (todo.faite) "Faite" else "À faire") },
                    modifier = Modifier.clickable { onTodoClick(todo) },
                )
            }
        }
    }
}

// 📄 ui/detail/DetailEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailEcran(todo: Todo?, onRetour: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (todo != null) "Tâche n° ${todo.id}" else "Tâche introuvable") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (todo == null) {
                Text("Cette tâche n'existe pas (ou plus).")
            } else {
                Text(todo.titre, style = MaterialTheme.typography.headlineSmall)
                Text(if (todo.faite) "✅ Faite" else "⏳ À faire")
            }
        }
    }
}

// 📄 ui/apropos/AProposEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AProposEcran(onRetour: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("À propos") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("TodoApp — TP navigation", style = MaterialTheme.typography.headlineSmall)
            Text("Étape 2 : une route avec argument.")
        }
    }
}
