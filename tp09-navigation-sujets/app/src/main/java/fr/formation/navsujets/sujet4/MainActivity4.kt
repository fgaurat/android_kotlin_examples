package fr.formation.navsujets.sujet4

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

// Étape 4 — Maîtriser la pile de retour
// Nouveau (sur la base de l'étape 3) :
// - écran de Connexion au démarrage : navigate(Todos) { popUpTo<Connexion> { inclusive = true } }
//   → Connexion est RETIRÉ de la pile : Retour depuis la liste quitte l'app, sans revenir à la connexion
// - « Tâche suivante » sur le détail : navigate(DetailTodo(suivant)) EMPILE un détail de plus
// - « Retour à la liste » : popBackStack<Todos>(inclusive = false) dépile TOUS les détails d'un coup
// - « À propos » avec launchSingleTop = true : pas de doublon au sommet (double clic sur ⓘ)
// - un bandeau en bas affiche la pile en direct (navController.currentBackStack)
// À essayer : Connexion → tâche 1 → Suivante → Suivante : la pile montre 3 détails ;
//   Retour système = un cran ; « Retour à la liste » = tout d'un coup ;
//   enlever launchSingleTop et toucher ⓘ deux fois très vite : deux « À propos » dans la pile.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/navsujets/ (ou …/navsujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1
// ├── data/
// │   ├── Todo.kt                  Todo
// │   └── TodosRepository.kt       TodosRepository (la liste en mémoire, en StateFlow)
// └── ui/
//     ├── navigation/
//     │   ├── Routes.kt            Connexion, Todos, DetailTodo, APropos
//     │   ├── TodoNavHost.kt       TodoApp, TodoNavHost (le SEUL endroit qui connaît le NavController)
//     │   └── PileDeRetour.kt      PileDeRetour, libelle() (affichage de la pile, pour le TP)
//     ├── connexion/
//     │   └── ConnexionEcran.kt    ConnexionEcran
//     ├── todos/
//     │   └── TodosEcran.kt        TodosEcran
//     ├── detail/
//     │   ├── DetailViewModel.kt   DetailViewModel (+ Factory)
//     │   └── DetailEcran.kt       DetailEcran
//     └── apropos/
//         └── AProposEcran.kt      AProposEcran
//
// - un sous-dossier par écran dans ui/, et ui/navigation/ pour les routes et le graphe
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer les private
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/Todo.kt
data class Todo(val id: String, val titre: String, val faite: Boolean = false)

// 📄 data/TodosRepository.kt
// Un object pour rester simple ; dans un vrai projet, une classe créée dans un AppContainer
object TodosRepository {
    private val _todos = MutableStateFlow(
        listOf(
            Todo("1", "Acheter du pain"),
            Todo("2", "Réviser Navigation Compose"),
            Todo("3", "Appeler le plombier", faite = true),
            Todo("4", "Préparer la démo du TP"),
            Todo("5", "Arroser les plantes"),
        )
    )
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()

    fun observer(id: String): Flow<Todo?> = todos.map { liste -> liste.find { it.id == id } }

    // L'id de la tâche suivante (la première après la dernière), pour le bouton « Tâche suivante »
    fun suivant(id: String): String {
        val liste = _todos.value
        val position = liste.indexOfFirst { it.id == id }
        return liste[(position + 1) % liste.size].id
    }

    fun basculer(id: String) {
        _todos.update { liste -> liste.map { if (it.id == id) it.copy(faite = !it.faite) else it } }
    }
}

// ---------- Routes ----------

// 📄 ui/navigation/Routes.kt
@Serializable data object Connexion

// 📄 ui/navigation/Routes.kt
@Serializable data object Todos

// 📄 ui/navigation/Routes.kt
@Serializable data class DetailTodo(val id: String)       // un argument obligatoire, typé

// 📄 ui/navigation/Routes.kt
@Serializable data object APropos

// ---------- ViewModel ----------

// 📄 ui/detail/DetailViewModel.kt
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TodosRepository,
) : ViewModel() {

    // Les arguments de la route sont DANS le SavedStateHandle : toRoute() les décode
    private val id: String = savedStateHandle.toRoute<DetailTodo>().id

    val todo: StateFlow<Todo?> = repository.observer(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = null)

    init {
        Log.d("NavSujets", "DetailViewModel créé (id=$id)")
    }

    fun basculer() = repository.basculer(id)

    fun idSuivant(): String = repository.suivant(id)

    override fun onCleared() {
        Log.d("NavSujets", "DetailViewModel détruit (id=$id)")      // on a quitté l'entrée DetailTodo
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // createSavedStateHandle() : celui de l'entrée de la pile, qui contient l'id
            initializer { DetailViewModel(createSavedStateHandle(), TodosRepository) }
        }
    }
}

// ---------- Navigation ----------

// 📄 MainActivity.kt
class MainActivity4 : ComponentActivity() {
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

    // bottomBar : le bandeau qui montre la pile ; chaque écran garde son propre Scaffold
    Scaffold(bottomBar = { PileDeRetour(navController) }) { padding ->
        NavHost(
            navController = navController,
            startDestination = Connexion,
            // consumeWindowInsets : les Scaffold des écrans ne rajoutent pas la marge déjà prise ici
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<Connexion> {
                ConnexionEcran(onConnecte = {
                    navController.navigate(Todos) {
                        popUpTo<Connexion> { inclusive = true }       // Connexion quitte la pile
                    }
                })
            }
            composable<Todos> {
                val todos by TodosRepository.todos.collectAsStateWithLifecycle()
                TodosEcran(
                    todos = todos,
                    onTodoClick = { todo -> navController.navigate(DetailTodo(id = todo.id)) },   // on passe l'id
                    onAProposClick = {
                        navController.navigate(APropos) { launchSingleTop = true }   // pas deux À propos d'affilée
                    },
                )
            }
            composable<DetailTodo> {
                // Plus de toRoute() ici : c'est le ViewModel qui lit l'id
                val viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
                val todo by viewModel.todo.collectAsStateWithLifecycle()
                DetailEcran(
                    todo = todo,
                    onBasculer = viewModel::basculer,
                    onSuivant = { navController.navigate(DetailTodo(viewModel.idSuivant())) },   // empile
                    onRetourListe = { navController.popBackStack<Todos>(inclusive = false) },    // dépile tout
                    onRetour = { navController.navigateUp() },
                )
            }
            composable<APropos> {
                AProposEcran(onRetour = { navController.navigateUp() })
            }
        }
    }
}

// 📄 ui/navigation/PileDeRetour.kt
// La pile, de bas en haut : « Liste › Détail 1 › Détail 2 »
@Composable
private fun PileDeRetour(navController: NavHostController) {
    val pile by navController.currentBackStack.collectAsStateWithLifecycle()
    Text(
        text = "Pile : " + pile.filter { it.destination !is NavGraph }       // la 1re entrée est le graphe lui-même
            .joinToString(" › ") { it.libelle() },
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .navigationBarsPadding()
            .padding(8.dp),
    )
}

// 📄 ui/navigation/PileDeRetour.kt
private fun NavBackStackEntry.libelle(): String = when {
    destination.hasRoute<Connexion>() -> "Connexion"
    destination.hasRoute<Todos>() -> "Liste"
    destination.hasRoute<DetailTodo>() -> "Détail ${toRoute<DetailTodo>().id}"
    destination.hasRoute<APropos>() -> "À propos"
    else -> "?"
}

// ---------- Écrans ----------

// 📄 ui/connexion/ConnexionEcran.kt
@Composable
private fun ConnexionEcran(onConnecte: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Bienvenue", style = MaterialTheme.typography.headlineMedium)
            Text("Pas de vrai mot de passe : on s'intéresse seulement à la pile.")
            Button(onClick = onConnecte) { Text("Se connecter") }
        }
    }
}

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
private fun DetailEcran(
    todo: Todo?,
    onBasculer: () -> Unit,
    onSuivant: () -> Unit,
    onRetourListe: () -> Unit,
    onRetour: () -> Unit,
) {
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
                Button(onClick = onBasculer) {
                    Text(if (todo.faite) "Marquer à faire" else "Marquer comme faite")
                }
                OutlinedButton(onClick = onSuivant) { Text("Tâche suivante") }
                OutlinedButton(onClick = onRetourListe) { Text("Retour à la liste") }
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
            Text("Étape 4 : la pile de retour.")
        }
    }
}
