package fr.formation.todossplash.sujet2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import fr.formation.todossplash.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

// Étape 2 — Garder le splash pendant le chargement des données
// Nouveau :
// - le ViewModel lance GET /todos dès sa création (init), avec un état Chargement / Succès / Erreur
// - setKeepOnScreenCondition { … } : le splash reste affiché TANT QUE la condition est vraie.
//   Elle est réévaluée à chaque image : elle doit être rapide (lire une valeur, rien de plus).
// - le ViewModel est créé dans l'Activity (by viewModels()) : la condition doit pouvoir le lire avant setContent
// À essayer :
// - JSON Server lancé : le splash reste ~1 s (DELAI_DEMO), puis la liste apparaît directement, sans écran de chargement
// - JSON Server arrêté : le splash disparaît, l'erreur s'affiche avec « Réessayer »
// Prérequis : cd json-server && npx json-server db.json

/** Délai ajouté pour voir le splash : JSON Server en local répond en quelques millisecondes. Mettre 0 en vrai. */
private const val DELAI_DEMO = 1_000L

@Serializable
data class Todo(val id: String, val userId: Int, val title: String, val completed: Boolean = false)

interface TodoApi {
    @GET("todos")
    suspend fun lister(@Query("userId") userId: Int): List<Todo>
}

object Reseau {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true              // accepte "id": 1 (json-server 0.17) comme "id": "1" (json-server 1.x)
    }

    val api: TodoApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()
}

// ---------- État et ViewModel ----------

sealed interface TodosUiState {
    data object Chargement : TodosUiState
    data class Succes(val todos: List<Todo>) : TodosUiState
    data class Erreur(val message: String) : TodosUiState
}

class TodosViewModel : ViewModel() {

    private val _etat = MutableStateFlow<TodosUiState>(TodosUiState.Chargement)
    val etat: StateFlow<TodosUiState> = _etat.asStateFlow()

    init {
        charger()                                       // le chargement commence pendant le splash
    }

    fun charger() {
        _etat.value = TodosUiState.Chargement
        viewModelScope.launch {
            delay(DELAI_DEMO)
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
}

// ---------- View ----------

class MainActivity2 : ComponentActivity() {

    private val viewModel: TodosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Le splash reste tant que les données ne sont pas arrivées (succès OU erreur)
        splash.setKeepOnScreenCondition { viewModel.etat.value is TodosUiState.Chargement }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    TodosEcran(viewModel)
                }
            }
        }
    }
}

@Composable
private fun TodosEcran(viewModel: TodosViewModel) {
    val etat by viewModel.etat.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tâches de l'utilisateur 1", style = MaterialTheme.typography.titleLarge)

        when (val e = etat) {
            // Au démarrage, on ne voit jamais cet état (le splash le cache) ; seulement après « Réessayer »
            TodosUiState.Chargement -> CircularProgressIndicator()

            is TodosUiState.Erreur -> {
                Text(e.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::charger) { Text("Réessayer") }
            }

            is TodosUiState.Succes -> LazyColumn {
                items(e.todos, key = { it.id }) { todo ->
                    ListItem(
                        headlineContent = { Text(todo.title) },
                        leadingContent = { Checkbox(checked = todo.completed, onCheckedChange = null) },
                    )
                }
            }
        }
    }
}
