package fr.formation.navsujets.sujet3

import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.Button
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

// Étape 3 — L'argument lu dans un ViewModel (SavedStateHandle)
// Nouveau :
// - TodosRepository : UNE source de vérité, partagée par les écrans (StateFlow<List<Todo>>)
// - DetailViewModel lit l'id avec savedStateHandle.toRoute<DetailTodo>() : le NavHost n'a plus à le faire
// - viewModel() dans composable<DetailTodo> : le ViewModel est lié à CETTE ENTRÉE de la pile,
//   créé à l'ouverture du détail et détruit (onCleared) quand on la quitte par Retour
// - le détail peut modifier la tâche (« Marquer comme faite ») : la liste est à jour au retour,
//   preuve qu'on a bien fait de passer l'id et pas une copie de l'objet
// À essayer : Logcat, filtre « NavSujets » : « DetailViewModel créé (id=…) » / « détruit » à chaque aller-retour ;
//   tourner l'écran sur le détail : PAS de nouveau ViewModel.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// app/src/main/java/fr/formation/navsujets/ (ou …/navsujets/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1
// ├── data/
// │   ├── Todo.kt                  Todo
// │   └── TodosRepository.kt       TodosRepository (la liste en mémoire, en StateFlow)
// └── ui/
//     ├── navigation/
//     │   ├── Routes.kt            Todos, DetailTodo, APropos
//     │   └── TodoNavHost.kt       TodoApp, TodoNavHost (le SEUL endroit qui connaît le NavController)
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
class MainActivity3 : ComponentActivity() {
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
            val todos by TodosRepository.todos.collectAsStateWithLifecycle()
            TodosEcran(
                todos = todos,
                onTodoClick = { todo -> navController.navigate(DetailTodo(id = todo.id)) },   // on passe l'id
                onAProposClick = { navController.navigate(APropos) },
            )
        }
        composable<DetailTodo> {
            // Plus de toRoute() ici : c'est le ViewModel qui lit l'id
            val viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory)
            val todo by viewModel.todo.collectAsStateWithLifecycle()
            DetailEcran(
                todo = todo,
                onBasculer = viewModel::basculer,
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
            Text("Étape 3 : l'argument lu dans un ViewModel.")
        }
    }
}
