package fr.formation.todosroom.sujet6

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Étape 6 — Faire évoluer la base : une migration
// Nouveau : une colonne « priorite » dans la table taches
// - la base passe en version 2 et une Migration(1, 2) dit à Room comment transformer l'ancienne base
// - sans migration, Room refuse d'ouvrir la base (IllegalStateException) : il ne détruit JAMAIS
//   les données de l'utilisateur sans qu'on le lui demande (fallbackToDestructiveMigration)
// À essayer, dans l'ordre :
// 1. Lancer l'ÉTAPE 5 et créer quelques tâches (base version 1, sans priorité)
// 2. Lancer cette étape 6 (même fichier de base) : les tâches sont toujours là, avec la priorité « Normale »
// 3. Pour voir l'erreur : commenter .addMigrations(MIGRATION_1_2), réinstaller l'app et refaire 1 puis 2
// Basé sur l'étape 4 (sans recherche ni filtre) pour rester court.

// ---------- Entity (version 2) ----------

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titre: String,
    val faite: Boolean = false,
    @ColumnInfo(defaultValue = "1") val priorite: Int = 1,     // NOUVEAU : 0 basse, 1 normale, 2 haute
)

val LIBELLES_PRIORITE = listOf("Basse", "Normale", "Haute")

// ---------- Migration ----------

// Le SQL doit produire EXACTEMENT le schéma attendu par l'entité (Room le vérifie à l'ouverture)
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE taches ADD COLUMN priorite INTEGER NOT NULL DEFAULT 1")
    }
}

// ---------- DAO ----------

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY faite, priorite DESC, titre COLLATE NOCASE")
    fun observer(): Flow<List<Tache>>

    @Insert
    suspend fun inserer(tache: Tache): Long

    @Query("UPDATE taches SET faite = NOT faite WHERE id = :id")
    suspend fun basculer(id: Long)

    @Query("DELETE FROM taches WHERE id = :id")
    suspend fun supprimer(id: Long)
}

// ---------- Database (version 2) ----------

@Database(entities = [Tache::class], version = 2, exportSchema = false)
abstract class BaseDeDonnees : RoomDatabase() {
    abstract fun tacheDao(): TacheDao

    companion object {
        @Volatile private var instance: BaseDeDonnees? = null

        fun get(context: Context): BaseDeDonnees =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, BaseDeDonnees::class.java, "taches-etapes5-6.db")
                    .addMigrations(MIGRATION_1_2)            // version 1 (étape 5) → version 2
                    .build()
                    .also { instance = it }
            }
    }
}

// ---------- Repository ----------

interface TacheRepository {
    val taches: Flow<List<Tache>>
    suspend fun ajouter(titre: String, priorite: Int)
    suspend fun basculer(id: Long)
    suspend fun supprimer(id: Long)
}

class TacheRepositoryRoom(private val dao: TacheDao) : TacheRepository {
    override val taches = dao.observer()
    override suspend fun ajouter(titre: String, priorite: Int) { dao.inserer(Tache(titre = titre, priorite = priorite)) }
    override suspend fun basculer(id: Long) = dao.basculer(id)
    override suspend fun supprimer(id: Long) = dao.supprimer(id)
}

// ---------- ViewModel ----------

class TachesViewModel(private val repository: TacheRepository) : ViewModel() {

    val taches: StateFlow<List<Tache>> = repository.taches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    fun ajouter(titre: String, priorite: Int) {
        if (titre.isBlank()) return
        viewModelScope.launch { repository.ajouter(titre.trim(), priorite) }
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

class MainActivity6 : ComponentActivity() {
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
    var titre by rememberSaveable { mutableStateOf("") }
    var priorite by rememberSaveable { mutableIntStateOf(1) }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mes tâches (base version 2)", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Nouvelle tâche") },
                singleLine = true, modifier = Modifier.weight(1f))
            Button(onClick = { viewModel.ajouter(titre, priorite); titre = "" }, enabled = titre.isNotBlank()) {
                Text("Ajouter")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LIBELLES_PRIORITE.forEachIndexed { index, libelle ->
                FilterChip(selected = priorite == index, onClick = { priorite = index }, label = { Text(libelle) })
            }
        }

        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = { Checkbox(checked = tache.faite, onCheckedChange = { viewModel.basculer(tache) }) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(onClick = {}, label = { Text(LIBELLES_PRIORITE[tache.priorite]) })
                            IconButton(onClick = { viewModel.supprimer(tache) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    },
                )
            }
        }
    }
}
