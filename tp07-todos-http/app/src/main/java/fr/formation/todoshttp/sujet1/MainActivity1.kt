package fr.formation.todoshttp.sujet1

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.todoshttp.BuildConfig
import java.io.IOException
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

// Étape 1 — Retrofit directement dans le composable (comme au jour 1)
// Ça marche… mais :
// ❌ Tourner l'écran relance la requête : regarder Logcat (filtre « okhttp »), un nouveau GET à chaque rotation
// ❌ L'appel réseau, la gestion d'erreur et l'affichage sont mélangés dans la même fonction
// + Ajouter (bouton flottant → POST /todos) et supprimer (corbeille → DELETE /todos/{id}), lancés depuis le
//   composable avec rememberCoroutineScope
// + state hoisting, comme à l'étape 0 : TodosEcran n'a pas changé (sauf l'erreur), seul TodosRoute a changé
// ❌ Impossible de tester la logique sans lancer l'écran et JSON Server
// Prérequis : JSON Server lancé (cd json-server && npx json-server db.json)

// ---------- Organisation en fichiers (dans un vrai projet) ----------
// Ici tout est dans un seul fichier pour la lecture. Découpé, l'étape 1 donnerait, dans
// app/src/main/java/fr/formation/todoshttp/ (ou …/todoshttp/sujet1/ pour rester dans ce projet) :
//
// ├── MainActivity.kt              MainActivity1
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
//         └── TodosRoute.kt        TodosRoute (avec état)
//
// - un fichier par classe, nommé comme elle ; le dossier donne le package :
//   ui/todos/TodosRoute.kt → package fr.formation.todoshttp.ui.todos
// - data/ = d'où viennent les données ; ui/ = ce qui s'affiche, un sous-dossier par écran
// - ⚠ private = visible dans CE fichier seulement : en découpant, retirer le private des composables
//   appelés depuis un autre fichier (TodosRoute, TodosEcran, DialogueAjout…) et de la liste partagée
// - Reseau est un object global pour rester simple ; dans tp07-todos-mvvm-retrofit, c'est un AppContainer
// - chaque ligne « 📄 » ci-dessous indique le fichier de la déclaration qui suit

// 📄 data/model/Todo.kt
// 1. Les données, telles que JSON Server les renvoie
@Serializable
data class Todo(val id: String, val userId: Int, val title: String, val completed: Boolean = false)

// 📄 data/remote/dto/NouveauTodo.kt
// Corps du POST : pas d'id, c'est le serveur qui le choisit.
// Pas de valeur par défaut : kotlinx.serialization n'écrit PAS les valeurs par défaut dans le JSON.
@Serializable
data class NouveauTodo(val userId: Int, val title: String, val completed: Boolean)

// 📄 data/remote/TodoApi.kt
// 2. L'API : Retrofit génère l'implémentation de cette interface
interface TodoApi {
    @GET("todos")
    suspend fun lister(@Query("userId") userId: Int): List<Todo>     // GET /todos?userId=1

    @POST("todos")
    suspend fun creer(@Body todo: NouveauTodo): Todo                  // le serveur renvoie la tâche avec son id

    @DELETE("todos/{id}")
    suspend fun supprimer(@Path("id") id: String): Response<Unit>     // DELETE /todos/3, pas de corps utile
}

// 📄 data/remote/Reseau.kt
// 3. Une seule instance de Retrofit pour toute l'application
object Reseau {
    private val json = Json {
        ignoreUnknownKeys = true      // ignore les champs du JSON absents de Todo
        isLenient = true              // accepte "id": 1 (json-server 0.17) comme "id": "1" (json-server 1.x)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })  // Logcat
        .build()

    val api: TodoApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)                     // http://10.0.2.2:3000/ = le poste de dev
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}

// 📄 MainActivity.kt
class MainActivity1 : ComponentActivity() {
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
// Composable AVEC état : la liste, l'erreur… et les appels réseau
// ❌ tout ça vit dans la composition : à chaque rotation, l'état est perdu et le GET repart
@Composable
private fun TodosRoute(modifier: Modifier = Modifier) {
    var todos by remember { mutableStateOf<List<Todo>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()                  // pour lancer un appel suspend depuis un clic

    // Lancé à l'entrée dans la composition… donc à nouveau après chaque rotation
    LaunchedEffect(Unit) {
        Log.d("TodosHttp", "LaunchedEffect : appel réseau")
        try {
            todos = Reseau.api.lister(userId = 1)
        } catch (e: IOException) {               // serveur injoignable
            erreur = "Réseau : ${e.message}"
        } catch (e: HttpException) {             // code HTTP 4xx / 5xx
            erreur = "HTTP ${e.code()}"
        } catch (e: SerializationException) {    // JSON qui ne correspond pas à la classe Todo
            erreur = "JSON inattendu : ${e.message}"
        }
    }

    // Le même try / catch, recopié pour chaque action : rien n'est partagé
    fun ajouter(titre: String) {
        scope.launch {
            try {
                todos = todos + Reseau.api.creer(NouveauTodo(userId = 1, title = titre, completed = false))
            } catch (e: IOException) {
                erreur = "Réseau : ${e.message}"
            } catch (e: HttpException) {
                erreur = "HTTP ${e.code()}"
            } catch (e: SerializationException) {
                erreur = "JSON inattendu : ${e.message}"
            }
        }
    }

    fun supprimer(todo: Todo) {
        scope.launch {
            try {
                val reponse = Reseau.api.supprimer(todo.id)
                if (!reponse.isSuccessful) throw HttpException(reponse)    // Response<Unit> ne lève rien tout seul
                todos = todos - todo                                       // le serveur a accepté : on retire
            } catch (e: IOException) {
                erreur = "Réseau : ${e.message}"
            } catch (e: HttpException) {
                erreur = "HTTP ${e.code()}"
            }
        }
    }

    TodosEcran(
        todos = todos,
        erreur = erreur,
        onAjouter = { ajouter(it) },
        onSupprimer = { supprimer(it) },
        modifier = modifier,
    )
}

// 📄 ui/todos/TodosEcran.kt
// Composable SANS état (state hoisting) : il reçoit ce qu'il affiche et remonte les événements (onXxx).
// Il ne connaît ni le ViewModel ni le réseau → on peut l'afficher dans un @Preview.
@Composable
private fun TodosEcran(
    todos: List<Todo>,
    erreur: String?,
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
            erreur?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
        TodosEcran(todos = exemples, erreur = null, onAjouter = {}, onSupprimer = {})
    }
}
