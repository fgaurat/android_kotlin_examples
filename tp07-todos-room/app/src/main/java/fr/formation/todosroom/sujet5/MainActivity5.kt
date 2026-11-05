package fr.formation.todosroom.sujet5

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Étape 5 — Des requêtes avec paramètres : filtre et recherche
// Nouveau (sur la base de l'étape 4) :
// - une @Query avec paramètres : LIKE pour la recherche, un filtre optionnel (NULL = pas de filtre), ORDER BY
// - le FILTRAGE est fait par SQLite, plus en Kotlin : utile quand la table contient des milliers de lignes
// - ViewModel : combine(recherche, filtre) → flatMapLatest → une nouvelle requête Room à chaque changement,
//   l'ancienne est annulée
// ⚠ Cette étape et l'étape 6 partagent le même fichier de base (taches-etapes5-6.db) : voir l'étape 6.

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titre: String,
    val faite: Boolean = false,
)

enum class Filtre(val libelle: String, val faite: Boolean?) {
    Toutes("Toutes", null),
    AFaire("À faire", false),
    Faites("Faites", true),
}

// ---------- DAO ----------

@Dao
interface TacheDao {
    // '%' || :texte || '%' : concaténation SQL → « contient texte » ; LIKE ignore la casse (lettres ASCII)
    // (:faite IS NULL OR faite = :faite) : le filtre ne s'applique que si on en passe un
    @Query("""
        SELECT * FROM taches
        WHERE titre LIKE '%' || :texte || '%'
          AND (:faite IS NULL OR faite = :faite)
        ORDER BY faite, titre COLLATE NOCASE
    """)
    fun rechercher(texte: String, faite: Boolean?): Flow<List<Tache>>

    @Insert
    suspend fun inserer(tache: Tache): Long

    @Query("UPDATE taches SET faite = NOT faite WHERE id = :id")
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
                instance ?: Room.databaseBuilder(context.applicationContext, BaseDeDonnees::class.java, "taches-etapes5-6.db")
                    // Revenir à cette étape après l'étape 6 (base en version 2) : on repart d'une base vide
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}

// ---------- Repository ----------

interface TacheRepository {
    fun rechercher(texte: String, filtre: Filtre): Flow<List<Tache>>
    suspend fun ajouter(titre: String)
    suspend fun basculer(id: Long)
    suspend fun supprimer(id: Long)
}

class TacheRepositoryRoom(private val dao: TacheDao) : TacheRepository {
    override fun rechercher(texte: String, filtre: Filtre) = dao.rechercher(texte.trim(), filtre.faite)
    override suspend fun ajouter(titre: String) { dao.inserer(Tache(titre = titre)) }
    override suspend fun basculer(id: Long) = dao.basculer(id)
    override suspend fun supprimer(id: Long) = dao.supprimer(id)
}

// ---------- ViewModel ----------

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)     // debounce, flatMapLatest
class TachesViewModel(private val repository: TacheRepository) : ViewModel() {

    private val _recherche = MutableStateFlow("")
    val recherche: StateFlow<String> = _recherche.asStateFlow()

    private val _filtre = MutableStateFlow(Filtre.Toutes)
    val filtre: StateFlow<Filtre> = _filtre.asStateFlow()

    // Recherche OU filtre change → nouvelle requête SQL (et la précédente est annulée)
    val taches: StateFlow<List<Tache>> =
        combine(_recherche.debounce(300), _filtre) { texte, filtre -> texte to filtre }
            .flatMapLatest { (texte, filtre) -> repository.rechercher(texte, filtre) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    fun onRechercheChange(texte: String) { _recherche.value = texte }
    fun onFiltreChange(filtre: Filtre) { _filtre.value = filtre }

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

class MainActivity5 : ComponentActivity() {
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
    val recherche by viewModel.recherche.collectAsStateWithLifecycle()
    val filtre by viewModel.filtre.collectAsStateWithLifecycle()
    var titre by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mes tâches (requêtes)", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Nouvelle tâche") },
                singleLine = true, modifier = Modifier.weight(1f))
            Button(onClick = { viewModel.ajouter(titre); titre = "" }, enabled = titre.isNotBlank()) { Text("Ajouter") }
        }

        OutlinedTextField(value = recherche, onValueChange = viewModel::onRechercheChange,
            label = { Text("Rechercher") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Filtre.entries.forEach { f ->
                FilterChip(selected = f == filtre, onClick = { viewModel.onFiltreChange(f) }, label = { Text(f.libelle) })
            }
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
