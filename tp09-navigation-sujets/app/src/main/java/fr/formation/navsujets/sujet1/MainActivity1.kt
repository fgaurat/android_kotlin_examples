package fr.formation.navsujets.sujet1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.serialization.Serializable

// Étape 1 — Navigation Compose : NavHost, routes, navigate, navigateUp
// Nouveau :
// - une ROUTE = un type @Serializable (data object quand il n'y a pas d'argument) ; plus de chaînes "apropos"
// - rememberNavController() : le NavController garde la PILE DE RETOUR (et survit à la rotation)
// - NavHost(startDestination = Todos) { composable<Todos> { … } } : à chaque route, son écran
// - navigate(APropos) empile l'écran ; navigateUp() le dépile (flèche de la barre du haut)
// - les écrans ne connaissent PAS le NavController : ils reçoivent des callbacks (onAProposClick, onRetour)
// ✅ Ouvrir « À propos » (icône ⓘ), puis Retour système : on revient à la liste, l'app ne se ferme plus.
// ❌ Reste à faire : ouvrir UNE tâche précise → passer un argument à la route (étape 2)

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/navsujets/ (ou …/navsujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1
// ├── data/
// │   └── Todo.kt                  Todo + todosEnMemoire
// └── ui/
//     ├── navigation/
//     │   ├── Routes.kt            Todos, APropos
//     │   └── TodoNavHost.kt       TodoApp, TodoNavHost (le SEUL endroit qui connaît le NavController)
//     ├── todos/
//     │   └── TodosEcran.kt        TodosEcran
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
@Serializable data object APropos

// ---------- Navigation ----------

// 📄 MainActivity.kt
class MainActivity1 : ComponentActivity() {
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
            TodosEcran(todos = todosEnMemoire, onAProposClick = { navController.navigate(APropos) })
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
private fun TodosEcran(todos: List<Todo>, onAProposClick: () -> Unit) {
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
                )
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
            Text("Étape 1 : NavHost et routes sans argument.")
        }
    }
}
