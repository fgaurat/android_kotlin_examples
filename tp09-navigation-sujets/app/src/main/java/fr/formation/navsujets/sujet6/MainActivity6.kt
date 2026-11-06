package fr.formation.navsujets.sujet6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
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

// Étape 6 — Graphe imbriqué et deep link
// Nouveau (sur la base de l'étape 5) :
// - navigation<ParametresGraphe>(startDestination = Parametres) { Parametres, APropos } :
//   un SOUS-GRAPHE ; l'onglet navigue vers le graphe, qui ouvre sa destination de départ
// - le sous-graphe est déclaré dans une fonction d'extension NavGraphBuilder.parametresGraphe()
// - onglet sélectionné avec destination.hierarchy : « Paramètres » reste allumé sur « À propos »
// - deep link : composable<DetailTodo>(deepLinks = …) + un intent-filter dans le manifest
//   todoapp://detail/3 ouvre directement le détail ; Retour mène à la liste (pile recréée)
// À essayer :
//   Paramètres → À propos : l'onglet Paramètres reste sélectionné (❌ à l'étape 5)
//   l'app fermée : adb shell am start -a android.intent.action.VIEW -d "todoapp://detail/3"
//   puis Retour → la liste ; essayer aussi todoapp://detail/42 → « Tâche introuvable »

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/navsujets/ (ou …/navsujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1
// ├── data/
// │   ├── Todo.kt                  Todo
// │   └── TodosRepository.kt       TodosRepository (la liste en mémoire, en StateFlow)
// └── ui/
//     ├── navigation/
//     │   ├── Routes.kt            Todos, DetailTodo, Recherche, ParametresGraphe, Parametres, APropos
//     │   ├── Onglets.kt           Onglet, onglets
//     │   ├── TodoApp.kt           TodoApp (Scaffold + barre d'onglets)
//     │   ├── TodoBottomBar.kt     TodoBottomBar
//     │   ├── TodoNavHost.kt       TodoNavHost (le SEUL endroit qui connaît le NavController)
//     │   └── ParametresGraphe.kt  NavGraphBuilder.parametresGraphe() (le sous-graphe)
//     ├── todos/
//     │   └── TodosEcran.kt        TodosEcran
//     ├── detail/
//     │   ├── DetailViewModel.kt   DetailViewModel (+ Factory)
//     │   └── DetailEcran.kt       DetailEcran
//     ├── recherche/
//     │   └── RechercheEcran.kt    RechercheEcran
//     ├── parametres/
//     │   └── ParametresEcran.kt   ParametresEcran
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

    fun basculer(id: String) {
        _todos.update { liste -> liste.map { if (it.id == id) it.copy(faite = !it.faite) else it } }
    }
}

// ---------- Routes ----------

// 📄 ui/navigation/Routes.kt
@Serializable data object Todos

// 📄 ui/navigation/Routes.kt
@Serializable data class DetailTodo(val id: String)       // un argument obligatoire, typé

// 📄 ui/navigation/Routes.kt
// Doit correspondre au <data android:scheme="todoapp" android:host="detail" /> du manifest
private const val BASE_DEEP_LINK = "todoapp://detail"

// 📄 ui/navigation/Routes.kt
@Serializable data object Recherche

// 📄 ui/navigation/Routes.kt
@Serializable data object ParametresGraphe                  // le sous-graphe : une route, sans écran

// 📄 ui/navigation/Routes.kt
@Serializable data object Parametres

// 📄 ui/navigation/Routes.kt
@Serializable data object APropos

// 📄 ui/navigation/Onglets.kt
// T : le type de la route (Todos, Recherche…) ; T : Any car une route peut être n'importe quel type
data class Onglet<T : Any>(val route: T, val libelle: String, val icone: ImageVector)

// 📄 ui/navigation/Onglets.kt
private val onglets = listOf(
    Onglet(Todos, "Tâches", Icons.AutoMirrored.Filled.List),
    Onglet(Recherche, "Recherche", Icons.Filled.Search),
    Onglet(ParametresGraphe, "Paramètres", Icons.Filled.Settings),     // l'onglet vise le GRAPHE
)

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
class MainActivity5 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { TodoApp() }
        }
    }
}

// 📄 ui/navigation/TodoApp.kt
@Composable
private fun TodoApp() {
    val navController = rememberNavController()
    val entree by navController.currentBackStackEntryAsState()     // recompose à chaque navigation
    val destination = entree?.destination

    Scaffold(
        bottomBar = {
            if (destination?.hasRoute<DetailTodo>() != true) {     // masquée sur le détail
                TodoBottomBar(navController, destination)
            }
        },
    ) { padding ->
        TodoNavHost(
            navController = navController,
            // consumeWindowInsets : les Scaffold des écrans ne rajoutent pas la marge déjà prise ici
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        )
    }
}

// 📄 ui/navigation/TodoBottomBar.kt
@Composable
private fun TodoBottomBar(navController: NavHostController, destination: NavDestination?) {
    NavigationBar {
        onglets.forEach { onglet ->
            NavigationBarItem(
                // hierarchy : la destination ET ses graphes parents (APropos → ParametresGraphe → racine)
                selected = destination?.hierarchy?.any { it.hasRoute(onglet.route::class) } == true,
                onClick = {
                    navController.navigate(onglet.route) {
                        // dépiler jusqu'au départ (Todos) en MÉMORISANT la pile de l'onglet quitté
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true           // re-cliquer sur l'onglet courant : pas de doublon
                        restoreState = true              // retrouver la pile mémorisée de l'onglet rejoint
                    }
                },
                icon = { Icon(onglet.icone, contentDescription = null) },   // le libellé suffit
                label = { Text(onglet.libelle) },
            )
        }
    }
}

// 📄 ui/navigation/TodoNavHost.kt
@Composable
private fun TodoNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Todos, modifier = modifier) {
        composable<Todos> {
            val todos by TodosRepository.todos.collectAsStateWithLifecycle()
            TodosEcran(todos = todos, onTodoClick = { todo -> navController.navigate(DetailTodo(todo.id)) })
        }
        composable<DetailTodo>(
            // L'argument obligatoire devient un segment du chemin : todoapp://detail/{id}
            deepLinks = listOf(navDeepLink<DetailTodo>(basePath = BASE_DEEP_LINK)),
        ) {
            val viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
            val todo by viewModel.todo.collectAsStateWithLifecycle()
            DetailEcran(todo = todo, onBasculer = viewModel::basculer, onRetour = { navController.navigateUp() })
        }
        composable<Recherche> {
            val todos by TodosRepository.todos.collectAsStateWithLifecycle()
            RechercheEcran(todos = todos, onTodoClick = { todo -> navController.navigate(DetailTodo(todo.id)) })
        }
        parametresGraphe(navController)                         // le sous-graphe, déclaré à part
    }
}

// 📄 ui/navigation/ParametresGraphe.kt
// Une fonction d'extension par fonctionnalité : le NavHost reste court
private fun NavGraphBuilder.parametresGraphe(navController: NavHostController) {
    navigation<ParametresGraphe>(startDestination = Parametres) {
        composable<Parametres> {
            ParametresEcran(onAProposClick = { navController.navigate(APropos) })
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
private fun TodosEcran(todos: List<Todo>, onTodoClick: (Todo) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Mes tâches") }) }) { padding ->
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
private fun DetailEcran(todo: Todo?, onBasculer: () -> Unit, onRetour: () -> Unit) {
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
            }
        }
    }
}

// 📄 ui/recherche/RechercheEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RechercheEcran(todos: List<Todo>, onTodoClick: (Todo) -> Unit) {
    // rememberSaveable : conservé quand on change d'onglet (grâce à saveState / restoreState)
    var texte by rememberSaveable { mutableStateOf("") }
    val resultats = todos.filter { it.titre.contains(texte.trim(), ignoreCase = true) }

    Scaffold(topBar = { TopAppBar(title = { Text("Recherche") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = texte,
                onValueChange = { texte = it },
                label = { Text("Titre contient…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn {
                items(resultats, key = { it.id }) { todo ->
                    ListItem(headlineContent = { Text(todo.titre) }, modifier = Modifier.clickable { onTodoClick(todo) })
                }
            }
        }
    }
}

// 📄 ui/parametres/ParametresEcran.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParametresEcran(onAProposClick: () -> Unit) {
    var notifications by rememberSaveable { mutableStateOf(true) }    // lui aussi survit au changement d'onglet

    Scaffold(topBar = { TopAppBar(title = { Text("Paramètres") }) }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ListItem(
                headlineContent = { Text("Notifications") },
                trailingContent = { Switch(checked = notifications, onCheckedChange = { notifications = it }) },
            )
            ListItem(
                headlineContent = { Text("À propos") },
                leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onAProposClick),
            )
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
            Text("Étape 6 : graphe imbriqué et deep link.")
        }
    }
}
