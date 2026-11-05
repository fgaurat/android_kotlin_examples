package fr.formation.todosroom.sujet3

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
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Étape 3 — Un ViewModel entre l'écran et le DAO
// Nouveau : TachesViewModel
// - taches : le Flow du DAO transformé en StateFlow avec stateIn (valeur initiale + partage)
// - WhileSubscribed(5_000) : la requête s'arrête 5 s après que l'écran ne regarde plus (arrière-plan),
//   mais PAS pendant une rotation (quelques millisecondes)
// - les écritures (ajouter, basculer, supprimer) passent par viewModelScope
// - restantes : une valeur dérivée, calculée avec map sur le même Flow
// - la Factory récupère le Context de l'application (APPLICATION_KEY) pour ouvrir la base
// L'écran ne connaît plus Room : il affiche l'état et appelle des fonctions.

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titre: String,
    val faite: Boolean = false,
)

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY id")
    fun observer(): Flow<List<Tache>>

    @Insert
    suspend fun inserer(tache: Tache)

    @Update
    suspend fun modifier(tache: Tache)

    @Delete
    suspend fun supprimer(tache: Tache)
}

@Database(entities = [Tache::class], version = 1, exportSchema = false)
abstract class BaseDeDonnees : RoomDatabase() {
    abstract fun tacheDao(): TacheDao

    companion object {
        @Volatile private var instance: BaseDeDonnees? = null

        fun get(context: Context): BaseDeDonnees =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, BaseDeDonnees::class.java, "taches-etape3.db")
                    .build()
                    .also { instance = it }
            }
    }
}

// ---------- ViewModel ----------

class TachesViewModel(private val dao: TacheDao) : ViewModel() {

    val taches: StateFlow<List<Tache>> = dao.observer()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    val restantes: StateFlow<Int> = dao.observer()
        .map { liste -> liste.count { !it.faite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = 0)

    fun ajouter(titre: String) {
        if (titre.isBlank()) return                          // la règle est ici, plus dans l'écran
        viewModelScope.launch { dao.inserer(Tache(titre = titre.trim())) }
    }

    fun basculer(tache: Tache) {
        viewModelScope.launch { dao.modifier(tache.copy(faite = !tache.faite)) }
    }

    fun supprimer(tache: Tache) {
        viewModelScope.launch { dao.supprimer(tache) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!          // le Context de l'application
                TachesViewModel(BaseDeDonnees.get(application).tacheDao())
            }
        }
    }
}

// ---------- View ----------

class MainActivity3 : ComponentActivity() {
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
        Text("Mes tâches (ViewModel)", style = MaterialTheme.typography.titleLarge)
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
