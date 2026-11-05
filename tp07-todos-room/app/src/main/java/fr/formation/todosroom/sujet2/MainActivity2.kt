package fr.formation.todosroom.sujet2

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlinx.coroutines.launch

// Étape 2 — Le DAO renvoie un Flow : l'écran se met à jour tout seul
// Nouveau :
// - observer() renvoie Flow<List<Tache>> : Room réémet la liste À CHAQUE modification de la table
// - plus besoin de relire après un ajout : on écrit, Room prévient, l'écran se redessine
// - @Update et @Delete : cocher et supprimer une tâche
// À essayer : modifier une ligne dans le Database Inspector (décocher « Keep database connections open » si besoin)
//   → l'écran change sans rien toucher.
// ❌ Reste à faire : la logique (insertion, bascule) est encore dans le composable (étape 3)

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titre: String,
    val faite: Boolean = false,
)

@Dao
interface TacheDao {
    @Query("SELECT * FROM taches ORDER BY id")
    fun observer(): Flow<List<Tache>>                        // PAS suspend : un Flow est déjà asynchrone

    @Insert
    suspend fun inserer(tache: Tache)

    @Update
    suspend fun modifier(tache: Tache)                       // retrouve la ligne grâce à la clé primaire

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
                instance ?: Room.databaseBuilder(context.applicationContext, BaseDeDonnees::class.java, "taches-etape2.db")
                    .build()
                    .also { instance = it }
            }
    }
}

// ---------- View ----------

class MainActivity2 : ComponentActivity() {
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
private fun TachesEcran() {
    val context = LocalContext.current
    val dao = remember { BaseDeDonnees.get(context).tacheDao() }
    val scope = rememberCoroutineScope()

    // remember : sinon un nouveau Flow (une nouvelle requête) serait créé à chaque recomposition
    val taches by remember { dao.observer() }.collectAsStateWithLifecycle(initialValue = emptyList())
    var titre by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mes tâches (Room + Flow)", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Nouvelle tâche") },
                singleLine = true, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    // ⚠ Lire le titre AVANT launch : la coroutine démarre plus tard, quand titre vaut déjà ""
                    val nouvelle = Tache(titre = titre.trim())
                    scope.launch { dao.inserer(nouvelle) }      // et c'est tout : pas de relecture
                    titre = ""
                },
                enabled = titre.isNotBlank(),
            ) { Text("Ajouter") }
        }

        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(
                    headlineContent = { Text(tache.titre) },
                    leadingContent = {
                        Checkbox(checked = tache.faite, onCheckedChange = { faite ->
                            scope.launch { dao.modifier(tache.copy(faite = faite)) }
                        })
                    },
                    trailingContent = {
                        IconButton(onClick = { scope.launch { dao.supprimer(tache) } }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                        }
                    },
                )
            }
        }
    }
}
