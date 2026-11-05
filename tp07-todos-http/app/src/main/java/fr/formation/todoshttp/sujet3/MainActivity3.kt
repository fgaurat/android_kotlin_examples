package fr.formation.todoshttp.sujet3

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.formation.todoshttp.BuildConfig
import java.io.IOException
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
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Étape 3 — Un état d'écran : Chargement, Succès ou Erreur
// Nouveau : TodosUiState (sealed interface)
// - l'écran est TOUJOURS dans un seul de ces trois états, impossible d'afficher « chargement » ET « erreur »
// - when (etat) dans le composable : le compilateur vérifie que les trois cas sont traités
// - charger() est public : le bouton « Réessayer » le rappelle
// + Ajouter et supprimer : la liste de l'état Succes n'est modifiée qu'APRÈS la réponse du serveur
// + state hoisting : TodosEcran reçoit maintenant un TodosUiState ; le @Preview montre l'état Succes
//   (en cas d'échec : seulement Logcat, la Snackbar arrive à l'étape 5)
// À essayer : arrêter JSON Server (Ctrl+C), relancer l'app → Erreur ; relancer le serveur → « Réessayer »

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// Ici tout est dans un seul fichier pour la lecture. Découpé, l'étape 3 donnerait, dans
// app/src/main/java/fr/formation/todoshttp/ (ou …/todoshttp/sujet3/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity3
// ├── data/
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

// 📄 data/model/Todo.kt
@Serializable
data class Todo(val id: String, val userId: Int, val title: String, val completed: Boolean = false)

// 📄 data/remote/dto/NouveauTodo.kt
// Corps du POST : pas d'id, c'est le serveur qui le choisit.
// Pas de valeur par défaut : kotlinx.serialization n'écrit PAS les valeurs par défaut dans le JSON.
@Serializable
data class NouveauTodo(val userId: Int, val title: String, val completed: Boolean)

// 📄 data/remote/TodoApi.kt
interface TodoApi {
    @GET("todos")
    suspend fun lister(@Query("userId") userId: Int): List<Todo>

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

// ---------- État de l'écran ----------

// 📄 ui/todos/TodosUiState.kt
sealed interface TodosUiState {
    data object Chargement : TodosUiState
    data class Succes(val todos: List<Todo>) : TodosUiState
    data class Erreur(val message: String) : TodosUiState
}

// ---------- ViewModel ----------

// 📄 ui/todos/TodosViewModel.kt
class TodosViewModel : ViewModel() {

    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    init {
        charger()
    }

    fun charger() {
        _etat.value = TodosUiState.Chargement
        viewModelScope.launch {
            _etat.value = try {
                TodosUiState.Succes(Reseau.api.lister(userId = 1))
            } catch (e: IOException) {
                TodosUiState.Erreur("Serveur injoignable : JSON Server est-il lancé ?")
            } catch (e: HttpException) {
                TodosUiState.Erreur("Erreur du serveur (code ${e.code()})")
            } catch (e: SerializationException) {
                TodosUiState.Erreur("Réponse inattendue du serveur")
            }
        }
    }

    fun ajouter(titre: String) {
        viewModelScope.launch {
            try {
                val cree = Reseau.api.creer(NouveauTodo(userId = 1, title = titre, completed = false))
                _etat.update { e -> if (e is TodosUiState.Succes) e.copy(todos = e.todos + cree) else e }
            } catch (e: IOException) {
                Log.e("TodosHttp", "Ajout : réseau", e)
            } catch (e: HttpException) {
                Log.e("TodosHttp", "Ajout : HTTP ${e.code()}", e)
            } catch (e: SerializationException) {
                Log.e("TodosHttp", "Ajout : JSON inattendu", e)
            }
        }
    }

    fun supprimer(todo: Todo) {
        viewModelScope.launch {
            try {
                val reponse = Reseau.api.supprimer(todo.id)
                if (!reponse.isSuccessful) throw HttpException(reponse)
                // On ne modifie la liste que si l'écran en affiche une
                _etat.update { e -> if (e is TodosUiState.Succes) e.copy(todos = e.todos - todo) else e }
            } catch (e: IOException) {
                Log.e("TodosHttp", "Suppression : réseau", e)
            } catch (e: HttpException) {
                Log.e("TodosHttp", "Suppression : HTTP ${e.code()}", e)
            }
        }
    }
}

// ---------- View ----------

// 📄 MainActivity.kt
class MainActivity3 : ComponentActivity() {
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
private fun TodosRoute(modifier: Modifier = Modifier, viewModel: TodosViewModel = viewModel()) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()
    TodosEcran(
        etat = etat,
        onReessayer = viewModel::charger,
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
            when (val e = etat) {
                TodosUiState.Chargement -> CircularProgressIndicator()
                is TodosUiState.Erreur -> {
                    Text(e.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReessayer) { Text("Réessayer") }
                }
                is TodosUiState.Succes ->
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
        TodosEcran(etat = TodosUiState.Succes(exemples), onReessayer = {}, onAjouter = {}, onSupprimer = {})
    }
}
