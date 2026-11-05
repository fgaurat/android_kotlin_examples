package fr.formation.todoshttp.sujet5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import fr.formation.todoshttp.BuildConfig
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Étape 5 — Modifier les données : POST, PATCH, DELETE
// Nouveau (sur la base de l'étape 4) :
// - TodoApi : changerStatut (PATCH) ; creer (POST) et supprimer (DELETE) existent depuis l'étape 1
// - TodoRepository : le try / catch est écrit UNE fois (appel { … }) pour toutes les opérations
// - ViewModel : ajouter / basculer / supprimer ; en cas de succès, la liste affichée est modifiée
//   avec la RÉPONSE du serveur (pas besoin de tout recharger)
// - en cas d'échec : un message, affiché par l'écran en Snackbar puis effacé (messageAffiche)
// - supprimer : corbeille OU glissement vers la gauche (SwipeToDismissBox), puis « Annuler » dans la Snackbar
//   La tâche disparaît tout de suite de l'écran, mais le DELETE part qu'à la disparition de la Snackbar :
//   « Annuler » n'a donc rien à demander au serveur.
//   Le swipe est un raccourci : la corbeille reste, car un geste ne se voit pas et TalkBack ne le propose pas.
// - state hoisting : TodosRoute s'occupe du ViewModel et des Snackbar ; TodosEcran affiche et prévient.
// À essayer : cocher, ajouter, supprimer, et regarder db.json changer ;
//   supprimer puis « Annuler » : aucun DELETE dans Logcat ;
//   puis arrêter JSON Server et cocher une tâche → Snackbar, la liste ne bouge pas.
// Limite : fermer l'écran pendant la Snackbar → le DELETE n'est jamais envoyé, la tâche revient au prochain
//   lancement (dans une vraie app : WorkManager, ou DELETE immédiat et re-création sur « Annuler »).

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// Ici tout est dans un seul fichier pour la lecture. Découpé, l'étape 5 donnerait, dans
// app/src/main/java/fr/formation/todoshttp/ (ou …/todoshttp/sujet5/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity5
// ├── data/
// │   ├── TodoRepository.kt        TodoRepository (interface)
// │   ├── TodoRepositoryHttp.kt    TodoRepositoryHttp
// │   ├── model/
// │   │   └── Todo.kt              Todo
// │   └── remote/
// │       ├── Reseau.kt            Reseau (Json, OkHttp, Retrofit)
// │       ├── TodoApi.kt           TodoApi (interface Retrofit)
// │       └── dto/
// │           ├── MajStatut.kt     MajStatut (corps du PATCH)
// │           └── NouveauTodo.kt   NouveauTodo (corps du POST)
// └── ui/
//     └── todos/
//         ├── DialogueAjout.kt     DialogueAjout
//         ├── LigneTodo.kt         LigneTodo (ligne qu'on peut glisser)
//         ├── TodosEcran.kt        TodosEcran (sans état) + exemples + @Preview
//         ├── TodosRoute.kt        TodosRoute (avec état)
//         ├── TodosUiState.kt      TodosUiState
//         └── TodosViewModel.kt    TodosViewModel
//
// - un fichier par classe, nommé comme elle ; le dossier donne le package :
//   ui/todos/TodosRoute.kt → package fr.formation.todoshttp.ui.todos
// - data/ = d'où viennent les données ; ui/ = ce qui s'affiche, un sous-dossier par écran
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer le private des composables
//   appelés depuis un autre fichier (TodosRoute, TodosEcran, DialogueAjout…) et de la liste partagée
// - Reseau est un object global pour rester simple ; dans tp07-todos-mvvm-retrofit, c'est un AppContainer
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// ---------- Model ----------

// 📄 data/model/Todo.kt
@Serializable
data class Todo(val id: String, val userId: Int, val title: String, val completed: Boolean = false)

// 📄 data/remote/dto/NouveauTodo.kt
// Corps des requêtes : POST n'a pas d'id (c'est le serveur qui le choisit), PATCH n'envoie que ce qui change.
// Pas de valeur par défaut ici : kotlinx.serialization n'écrit PAS les valeurs par défaut dans le JSON.
@Serializable
data class NouveauTodo(val userId: Int, val title: String, val completed: Boolean)

// 📄 data/remote/dto/MajStatut.kt
@Serializable
data class MajStatut(val completed: Boolean)

// ---------- Data : la source réseau ----------

// 📄 data/remote/TodoApi.kt
interface TodoApi {
    @GET("todos")
    suspend fun lister(@Query("userId") userId: Int): List<Todo>

    @POST("todos")
    suspend fun creer(@Body todo: NouveauTodo): Todo                          // renvoie la tâche avec son id

    @PATCH("todos/{id}")
    suspend fun changerStatut(@Path("id") id: String, @Body maj: MajStatut): Todo

    @DELETE("todos/{id}")
    suspend fun supprimer(@Path("id") id: String): Response<Unit>             // pas de corps utile
}

// 📄 data/remote/Reseau.kt
object Reseau {
    private val json = Json {
        ignoreUnknownKeys = true      // ignore les champs du JSON absents de Todo
        isLenient = true              // accepte "id": 1 (json-server 0.17) comme "id": "1" (json-server 1.x)
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: TodoApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}

// ---------- Data : le repository ----------

// 📄 data/TodoRepository.kt
interface TodoRepository {
    suspend fun lister(userId: Int): Result<List<Todo>>
    suspend fun ajouter(titre: String, userId: Int): Result<Todo>
    suspend fun changerStatut(id: String, termine: Boolean): Result<Todo>
    suspend fun supprimer(id: String): Result<Unit>
}

// 📄 data/TodoRepositoryHttp.kt
class TodoRepositoryHttp(private val api: TodoApi) : TodoRepository {

    override suspend fun lister(userId: Int) = appel { api.lister(userId) }

    override suspend fun ajouter(titre: String, userId: Int) = appel { api.creer(NouveauTodo(userId, titre, completed = false)) }

    override suspend fun changerStatut(id: String, termine: Boolean) =
        appel { api.changerStatut(id, MajStatut(completed = termine)) }

    override suspend fun supprimer(id: String) = appel {
        val reponse = api.supprimer(id)
        if (!reponse.isSuccessful) throw HttpException(reponse)    // Response<Unit> ne lève rien tout seul
    }

    // Le try / catch de l'étape 4, écrit une seule fois pour toutes les opérations
    private suspend fun <T> appel(bloc: suspend () -> T): Result<T> =
        try {
            Result.success(bloc())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(Exception("Serveur injoignable : JSON Server est-il lancé ?", e))
        } catch (e: HttpException) {
            Result.failure(Exception("Erreur du serveur (code ${e.code()})", e))
        } catch (e: SerializationException) {
            Result.failure(Exception("Réponse inattendue du serveur", e))
        }
}

// ---------- État de l'écran ----------

// 📄 ui/todos/TodosUiState.kt
sealed interface TodosUiState {
    data object Chargement : TodosUiState
    data class Succes(val todos: List<Todo>) : TodosUiState
    data class Erreur(val message: String) : TodosUiState
}

// ---------- ViewModel ----------

// 📄 ui/todos/TodosViewModel.kt
class TodosViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    // Échec d'une action : affiché une fois par l'écran, puis effacé
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // Suppression en attente : la tâche est retirée de l'écran, mais le serveur n'est pas encore prévenu
    data class Suppression(val todo: Todo, val position: Int)
    private val _suppression = MutableStateFlow<Suppression?>(null)
    val suppression: StateFlow<Suppression?> = _suppression.asStateFlow()

    init {
        charger()
    }

    fun charger() {
        _etat.value = TodosUiState.Chargement
        viewModelScope.launch {
            _etat.value = repository.lister(userId = 1).fold(
                onSuccess = { TodosUiState.Succes(it) },
                onFailure = { TodosUiState.Erreur(it.message ?: "Erreur inconnue") },
            )
        }
    }

    fun ajouter(titre: String) {
        if (titre.isBlank()) return
        viewModelScope.launch {
            repository.ajouter(titre.trim(), userId = 1)
                .onSuccess { cree -> modifierListe { it + cree } }
                .onFailure { _message.value = "Ajout impossible : ${it.message}" }
        }
    }

    fun basculer(todo: Todo) {
        viewModelScope.launch {
            repository.changerStatut(todo.id, !todo.completed)
                .onSuccess { maj -> modifierListe { liste -> liste.map { if (it.id == maj.id) maj else it } } }
                .onFailure { _message.value = "Modification impossible : ${it.message}" }
        }
    }

    // 1. Corbeille ou swipe : on retire la tâche de l'écran et on la garde de côté
    fun supprimer(todo: Todo) {
        val liste = (_etat.value as? TodosUiState.Succes)?.todos ?: return
        val position = liste.indexOfFirst { it.id == todo.id }
        if (position == -1) return                     // déjà retirée (le swipe peut prévenir deux fois)
        confirmerSuppression()                         // une suppression encore en attente part maintenant
        modifierListe { it.filterNot { t -> t.id == todo.id } }
        _suppression.value = Suppression(todo, position)
    }

    // 2a. « Annuler » : on remet la tâche à sa place, sans appel réseau
    fun annulerSuppression() {
        val attente = _suppression.value ?: return
        _suppression.value = null
        remettre(attente)
    }

    // 2b. La Snackbar a disparu sans « Annuler » : maintenant, on envoie le DELETE
    fun confirmerSuppression() {
        val attente = _suppression.value ?: return
        _suppression.value = null
        viewModelScope.launch {
            repository.supprimer(attente.todo.id)
                .onFailure {
                    remettre(attente)                  // le serveur a refusé : la tâche revient
                    _message.value = "Suppression impossible : ${it.message}"
                }
        }
    }

    private fun remettre(attente: Suppression) {
        modifierListe { liste ->
            liste.toMutableList().apply { add(attente.position.coerceAtMost(size), attente.todo) }
        }
    }

    fun messageAffiche() {
        _message.value = null
    }

    // Modifie la liste seulement si l'écran affiche une liste (état Succès)
    private fun modifierListe(transformation: (List<Todo>) -> List<Todo>) {
        _etat.update { e -> if (e is TodosUiState.Succes) e.copy(todos = transformation(e.todos)) else e }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TodosViewModel(TodoRepositoryHttp(Reseau.api)) }
        }
    }
}

// ---------- View ----------

// 📄 MainActivity.kt
class MainActivity5 : ComponentActivity() {
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
// Composable AVEC état : le ViewModel, et les effets (Snackbar) qui dépendent de ses messages
@Composable
private fun TodosRoute(modifier: Modifier = Modifier, viewModel: TodosViewModel = viewModel(factory = TodosViewModel.Factory)) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val suppression by viewModel.suppression.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }

    // Afficher le message en Snackbar, puis prévenir le ViewModel qu'il a été vu
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.messageAffiche()
        }
    }

    // Une suppression en attente : Snackbar avec « Annuler ».
    // Si une autre suppression arrive, ce LaunchedEffect redémarre : la Snackbar précédente est fermée,
    // et supprimer() a déjà envoyé le DELETE précédent.
    LaunchedEffect(suppression) {
        if (suppression == null) return@LaunchedEffect
        val resultat = snackbar.showSnackbar(
            message = "Tâche supprimée",
            actionLabel = "Annuler",
            duration = SnackbarDuration.Long,          // avec une action, la durée par défaut est infinie
        )
        when (resultat) {
            SnackbarResult.ActionPerformed -> viewModel.annulerSuppression()
            SnackbarResult.Dismissed -> viewModel.confirmerSuppression()
        }
    }

    TodosEcran(
        etat = etat,
        snackbar = snackbar,
        onReessayer = viewModel::charger,
        onAjouter = viewModel::ajouter,
        onBasculer = viewModel::basculer,
        onSupprimer = viewModel::supprimer,
        modifier = modifier,
    )
}

// 📄 ui/todos/TodosEcran.kt
// Composable SANS état (state hoisting) : il reçoit ce qu'il affiche et remonte les événements (onXxx).
// Il ne connaît ni le ViewModel ni le réseau → on peut l'afficher dans un @Preview.
// La SnackbarHostState vient de TodosRoute : c'est lui qui décide QUAND afficher un message.
@Composable
private fun TodosEcran(
    etat: TodosUiState,
    snackbar: SnackbarHostState,
    onReessayer: () -> Unit,
    onAjouter: (String) -> Unit,
    onBasculer: (Todo) -> Unit,
    onSupprimer: (Todo) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ouvert ou fermé : un état d'UI pur, que personne d'autre n'a besoin de connaître → il peut rester ici
    var dialogueOuvert by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
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
            when (val e = etat) {
                TodosUiState.Chargement -> CircularProgressIndicator()
                is TodosUiState.Erreur -> {
                    Text(e.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReessayer) { Text("Réessayer") }
                }
                // contentPadding : la dernière ligne n'est pas cachée par le bouton flottant
                is TodosUiState.Succes -> LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(e.todos, key = { it.id }) { todo ->
                        LigneTodo(
                            todo = todo,
                            onBasculer = { onBasculer(todo) },
                            onSupprimer = { onSupprimer(todo) },
                        )
                    }
                }
            }
        }
    }

    if (dialogueOuvert) {
        DialogueAjout(onAjouter = onAjouter, onFermer = { dialogueOuvert = false })
    }
}

// 📄 ui/todos/LigneTodo.kt
// Une ligne qu'on peut glisser vers la gauche pour la supprimer
@Composable
private fun LigneTodo(todo: Todo, onBasculer: () -> Unit, onSupprimer: () -> Unit) {
    val etatSwipe = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = etatSwipe,
        enableDismissFromStartToEnd = false,           // seulement de droite à gauche
        onDismiss = { onSupprimer() },                 // appelé quand la ligne est sortie de l'écran
        // Ce qui apparaît SOUS la ligne pendant le glissement
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null,      // décoratif : la corbeille a le texte
                    tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
    ) {
        // ListItem a un fond opaque : il cache le rouge tant qu'on ne glisse pas
        ListItem(
            headlineContent = { Text(todo.title) },
            leadingContent = { Checkbox(checked = todo.completed, onCheckedChange = { onBasculer() }) },
            trailingContent = {
                IconButton(onClick = onSupprimer) {
                    Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                }
            },
        )
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
private val exemples = listOf(
    Todo(id = "1", userId = 1, title = "Acheter du pain"),
    Todo(id = "2", userId = 1, title = "Réviser Compose", completed = true),
)

// 📄 ui/todos/TodosEcran.kt (même fichier que TodosEcran)
// Aperçu dans Android Studio : possible parce que TodosEcran ne dépend que de ses paramètres
@Preview(showBackground = true)
@Composable
private fun TodosEcranPreview() {
    MaterialTheme {
        TodosEcran(
            etat = TodosUiState.Succes(exemples),
            snackbar = remember { SnackbarHostState() },
            onReessayer = {}, onAjouter = {}, onBasculer = {}, onSupprimer = {},
        )
    }
}
