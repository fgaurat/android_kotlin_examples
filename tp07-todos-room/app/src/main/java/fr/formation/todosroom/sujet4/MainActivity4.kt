package fr.formation.todosroom.sujet4

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Étape 4 — Un Repository entre le ViewModel et Room
// Nouveau :
// - TacheRepository (interface) + TacheRepositoryRoom (implémentation) : le ViewModel ne connaît plus Room.
//   En test, on lui passe un faux repository en mémoire ; demain, la même interface peut cacher Retrofit + Room.
// - Le DAO écrit aussi en SQL : UPDATE et DELETE par id, et un COUNT(*) observé en Flow
// - Le compteur de tâches restantes est calculé PAR LA BASE, plus en Kotlin
// L'écran est identique à l'étape 3.

// ---------- Entity ----------

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titre: String,
    val faite: Boolean = false,
)

// ---------- DAO ----------

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY id")
    fun observer(): Flow<List<Tache>>

    @Query("SELECT COUNT(*) FROM taches WHERE faite = 0")     // Boolean stocké en INTEGER : 0 / 1
    fun observerRestantes(): Flow<Int>

    @Insert
    suspend fun inserer(tache: Tache): Long                   // renvoie l'id généré

    @Query("UPDATE taches SET faite = NOT faite WHERE id = :id")    // :id = le paramètre de la fonction
    suspend fun basculer(id: Long)

    @Query("DELETE FROM taches WHERE id = :id")
    suspend fun supprimer(id: Long)
}

// ---------- Database ----------

@Database(entities = [Tache::class], version = 1, exportSchema = false)
abstract class BaseDeDonnees : RoomDatabase() {
    abstract fun tacheDao(): TacheDao

    companion object {
        @Volatile private var instance: BaseDeDonnees? = null

        fun get(context: Context): BaseDeDonnees =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, BaseDeDonnees::class.java, "taches-etape4.db")
                    .build()
                    .also { instance = it }
            }
    }
}

// ---------- Repository ----------

// Le contrat vu par le ViewModel : aucune trace de Room, de SQL ni de DAO
interface TacheRepository {
    val taches: Flow<List<Tache>>
    val restantes: Flow<Int>
    suspend fun ajouter(titre: String)
    suspend fun basculer(id: Long)
    suspend fun supprimer(id: Long)
}

class TacheRepositoryRoom(private val dao: TacheDao) : TacheRepository {
    override val taches = dao.observer()
    override val restantes = dao.observerRestantes()
    override suspend fun ajouter(titre: String) { dao.inserer(Tache(titre = titre)) }
    override suspend fun basculer(id: Long) = dao.basculer(id)
    override suspend fun supprimer(id: Long) = dao.supprimer(id)
}

// ---------- ViewModel ----------

class TachesViewModel(private val repository: TacheRepository) : ViewModel() {

    val taches: StateFlow<List<Tache>> = repository.taches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val restantes: StateFlow<Int> = repository.restantes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = 0)

    fun ajouter(titre: String) {
        if (titre.isBlank()) return
        viewModelScope.launch { repository.ajouter(titre.trim()) }
    }

    fun basculer(tache: Tache) {
        viewModelScope.launch { repository.basculer(tache.id) }
    }

    fun supprimer(tache: Tache) {
        viewModelScope.launch { repository.supprimer(tache.id) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val dao = BaseDeDonnees.get(this[APPLICATION_KEY]!!).tacheDao()
                TachesViewModel(TacheRepositoryRoom(dao))
            }
        }
    }
}

// ---------- View ----------

class MainActivity4 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    TachesEcran()
                }
            }
        }
    }
}

@Composable
private fun TachesEcran(viewModel: TachesViewModel = viewModel(factory = TachesViewModel.Factory)) {
    val taches by viewModel.taches.collectAsStateWithLifecycle()
    val restantes by viewModel.restantes.collectAsStateWithLifecycle()
    var titre by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mes tâches (Repository)", style = MaterialTheme.typography.titleLarge)
        Text("$restantes tâche(s) restante(s)")

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Nouvelle tâche") },
                singleLine = true, modifier = Modifier.weight(1f))
            Button(onClick = { viewModel.ajouter(titre); titre = "" }, enabled = titre.isNotBlank()) { Text("Ajouter") }
        }

        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = { Checkbox(checked = tache.faite, onCheckedChange = { viewModel.basculer(tache) }) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.supprimer(tache) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                        }
                    },
                )
            }
        }
    }
}
