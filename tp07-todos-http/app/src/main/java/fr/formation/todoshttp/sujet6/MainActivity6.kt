package fr.formation.todoshttp.sujet6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import fr.formation.todoshttp.BuildConfig
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
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
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Étape 6 — Recherche côté serveur, réactive
// Nouveau (sur la base de l'étape 4, sans les actions de l'étape 5 pour rester court) :
// - TodoApi : paramètre title:contains (filtre de JSON Server 1.x)
// - la recherche est un StateFlow lu dans le SavedStateHandle : survit à la rotation ET à la mort du processus
// - debounce(300) : une seule requête quand l'utilisateur fait une pause, pas une par lettre
// - une nouvelle recherche ANNULE la requête précédente (Job.cancel) : jamais de résultat périmé
// + Ajouter (bouton flottant) et supprimer (corbeille), comme à l'étape 4 ; state hoisting du champ de recherche
// À essayer : taper vite « quis » → un seul GET dans Logcat ;
//   touche Accueil, tuer le processus (Logcat > Terminate Application), revenir : la recherche est restaurée.

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// Ici tout est dans un seul fichier pour la lecture. Découpé, l'étape 6 donnerait, dans
// app/src/main/java/fr/formation/todoshttp/ (ou …/todoshttp/sujet6/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity6
// ├── data/
// │   ├── TodoRepository.kt        TodoRepository (interface)
// │   ├── TodoRepositoryHttp.kt    TodoRepositoryHttp
// │   ├── model/
// │   │   └── Todo.kt              Todo
// │   └── remote/
// │       ├── Reseau.kt            Reseau (Json, OkHttp, Retrofit)
// │       ├── TodoApi.kt           TodoApi (interface Retrofit)
// │       └── dto/
// │           └── NouveauTodo.kt   NouveauTodo (corps du POST)
// └── ui/
//     └── todos/
//         ├── DialogueAjout.kt     DialogueAjout
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
// Corps du POST : pas d'id, c'est le serveur qui le choisit.
// Pas de valeur par défaut : kotlinx.serialization n'écrit PAS les valeurs par défaut dans le JSON.
@Serializable
data class NouveauTodo(val userId: Int, val title: String, val completed: Boolean)

// ---------- Data : la source réseau ----------

// 📄 data/remote/TodoApi.kt
interface TodoApi {
    @GET("todos")
    suspend fun lister(
        @Query("userId") userId: Int,
        // json-server 1.x : "title:contains" ; json-server 0.17 : "title_like" (sinon le filtre est ignoré)
        @Query("title:contains") recherche: String? = null,     // null → paramètre absent de l'URL
    ): List<Todo>

    @POST("todos")
    suspend fun creer(@Body todo: NouveauTodo): Todo                  // le serveur renvoie la tâche avec son id

    @DELETE("todos/{id}")
    suspend fun supprimer(@Path("id") id: String): Response<Unit>     // DELETE /todos/3, pas de corps utile
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
    suspend fun lister(userId: Int, recherche: String): Result<List<Todo>>
    suspend fun ajouter(titre: String, userId: Int): Result<Todo>
    suspend fun supprimer(id: String): Result<Unit>
}

// 📄 data/TodoRepositoryHttp.kt
class TodoRepositoryHttp(private val api: TodoApi) : TodoRepository {

    override suspend fun lister(userId: Int, recherche: String): Result<List<Todo>> =
        try {
            Result.success(api.lister(userId, recherche.ifBlank { null }))
        } catch (e: CancellationException) {
            throw e                         // ici, c'est fréquent : chaque nouvelle recherche annule l'ancienne
        } catch (e: IOException) {
            Result.failure(Exception("Serveur injoignable : JSON Server est-il lancé ?", e))
        } catch (e: HttpException) {
            Result.failure(Exception("Erreur du serveur (code ${e.code()})", e))
        } catch (e: SerializationException) {
            Result.failure(Exception("Réponse inattendue du serveur", e))
        }

    override suspend fun ajouter(titre: String, userId: Int): Result<Todo> =
        try {
            Result.success(api.creer(NouveauTodo(userId = userId, title = titre, completed = false)))
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(Exception("Serveur injoignable : JSON Server est-il lancé ?", e))
        } catch (e: HttpException) {
            Result.failure(Exception("Erreur du serveur (code ${e.code()})", e))
        } catch (e: SerializationException) {
            Result.failure(Exception("Réponse inattendue du serveur", e))
        }

    override suspend fun supprimer(id: String): Result<Unit> =
        try {
            val reponse = api.supprimer(id)
            if (!reponse.isSuccessful) throw HttpException(reponse)    // Response<Unit> ne lève rien tout seul
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.failure(Exception("Serveur injoignable : JSON Server est-il lancé ?", e))
        } catch (e: HttpException) {
            Result.failure(Exception("Erreur du serveur (code ${e.code()})", e))
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
@OptIn(FlowPreview::class)              // debounce
class TodosViewModel(
    private val repository: TodoRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    // Le texte du champ : lu et écrit dans le SavedStateHandle
    val recherche: StateFlow<String> = savedStateHandle.getStateFlow("recherche", "")

    private var requeteEnCours: Job? = null

    init {
        viewModelScope.launch {
            recherche
                .debounce(300)                                // attendre 300 ms sans frappe
                .onStart { emit(recherche.value) }            // … sauf la première fois : on charge tout de suite
                .distinctUntilChanged()                       // et on ne relance pas la même recherche
                .collect { texte -> charger(texte) }
        }
    }

    fun onRechercheChange(texte: String) {
        savedStateHandle["recherche"] = texte                 // met à jour le StateFlow recherche
    }

    fun reessayer() = charger(recherche.value)

    private fun charger(texte: String) {
        requeteEnCours?.cancel()                              // la recherche précédente ne sert plus à rien
        requeteEnCours = viewModelScope.launch {
            _etat.value = TodosUiState.Chargement
            _etat.value = repository.lister(userId = 1, recherche = texte.trim()).fold(
                onSuccess = { TodosUiState.Succes(it) },
                onFailure = { TodosUiState.Erreur(it.message ?: "Erreur inconnue") },
            )
        }
    }

    fun ajouter(titre: String) {
        viewModelScope.launch {
            repository.ajouter(titre, userId = 1)
                .onSuccess { cree -> _etat.update { e -> if (e is TodosUiState.Succes) e.copy(todos = e.todos + cree) else e } }
                .onFailure { Log.e("TodosHttp", "Ajout impossible", it) }          // Snackbar : étape 5
        }
    }

    fun supprimer(todo: Todo) {
        viewModelScope.launch {
            repository.supprimer(todo.id)
                .onSuccess { _etat.update { e -> if (e is TodosUiState.Succes) e.copy(todos = e.todos - todo) else e } }
                .onFailure { Log.e("TodosHttp", "Suppression impossible", it) }    // Snackbar : étape 5
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodosViewModel(
                    repository = TodoRepositoryHttp(Reseau.api),
                    savedStateHandle = createSavedStateHandle(),    // fourni par le système
                )
            }
        }
    }
}

// ---------- View ----------

// 📄 MainActivity.kt
class MainActivity6 : ComponentActivity() {
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
// Composable AVEC état : il récupère le ViewModel, lit son StateFlow et lui transmet les événements
@Composable
private fun TodosRoute(modifier: Modifier = Modifier, viewModel: TodosViewModel = viewModel(factory = TodosViewModel.Factory)) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    val recherche by viewModel.recherche.collectAsStateWithLifecycle()
    TodosEcran(
        etat = etat,
        recherche = recherche,
        onRechercheChange = viewModel::onRechercheChange,
        onReessayer = viewModel::reessayer,
        onAjouter = viewModel::ajouter,
        onSupprimer = viewModel::supprimer,
        modifier = modifier,
    )
}

// 📄 ui/todos/TodosEcran.kt
// Composable SANS état (state hoisting) : il reçoit ce qu'il affiche et remonte les événements (onXxx).
// Il ne connaît ni le ViewModel ni le réseau → on peut l'afficher dans un @Preview.
@Composable
private fun TodosEcran(
    etat: TodosUiState,
    recherche: String,
    onRechercheChange: (String) -> Unit,
    onReessayer: () -> Unit,
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
            OutlinedTextField(
                value = recherche,                        // le champ suit le StateFlow immédiatement
                onValueChange = onRechercheChange,
                label = { Text("Rechercher (côté serveur)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            when (val e = etat) {
                TodosUiState.Chargement -> CircularProgressIndicator()
                is TodosUiState.Erreur -> {
                    Text(e.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReessayer) { Text("Réessayer") }
                }
                is TodosUiState.Succes -> {
                    Text("${e.todos.size} résultat(s)")
                    // contentPadding : la dernière ligne n'est pas cachée par le bouton flottant
                    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(e.todos, key = { it.id }) { todo ->
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
        TodosEcran(etat = TodosUiState.Succes(exemples), recherche = "", onRechercheChange = {}, onReessayer = {}, onAjouter = {}, onSupprimer = {})
    }
}
