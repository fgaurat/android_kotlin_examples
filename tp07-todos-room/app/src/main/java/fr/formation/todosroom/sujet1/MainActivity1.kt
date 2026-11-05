package fr.formation.todosroom.sujet1

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
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch

// Étape 1 — Room : les trois briques
// - @Entity   : une classe = une table, une propriété = une colonne
// - @Dao      : les requêtes, écrites en SQL ou générées (@Insert)
// - @Database : la base elle-même, qui donne accès aux DAO
// KSP génère le code à la compilation (build/generated/ksp) : on n'écrit jamais l'implémentation.
// Ici, le DAO est appelé directement depuis le composable, et la liste est relue À LA MAIN après chaque ajout.
// À essayer :
// ✅ Ajouter des tâches, fermer l'application (balayer dans les récents), la relancer : les tâches sont toujours là
// ✅ Android Studio > App Inspection > Database Inspector : la table « taches » en direct
// ❌ Oublier de relire la liste après l'ajout (supprimer la ligne marquée ⚠) : l'écran ne bouge plus

// ---------- Entity : la table ----------

@Entity(tableName = "taches")
data class Tache(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,     // 0 = « à générer » par SQLite
    val titre: String,
    val faite: Boolean = false,
)

// ---------- DAO : les requêtes ----------

@Dao
interface TacheDao {
    @Insert
    suspend fun inserer(tache: Tache)                        // suspend : Room l'exécute hors du thread principal

    @Query("SELECT * FROM taches ORDER BY id")
    suspend fun lister(): List<Tache>                        // une lecture ponctuelle (pas de mise à jour auto)
}

// ---------- Database : la base ----------

@Database(entities = [Tache::class], version = 1, exportSchema = false)
abstract class BaseDeDonnees : RoomDatabase() {
    abstract fun tacheDao(): TacheDao

    companion object {
        @Volatile private var instance: BaseDeDonnees? = null

        // Une seule instance pour toute l'application : ouvrir une base coûte cher
        fun get(context: Context): BaseDeDonnees =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(context.applicationContext, BaseDeDonnees::class.java, "taches-etape1.db")
                    .build()
                    .also { instance = it }
            }
    }
}

// ---------- View ----------

class MainActivity1 : ComponentActivity() {
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
    val dao = BaseDeDonnees.get(LocalContext.current).tacheDao()
    val scope = rememberCoroutineScope()

    var taches by remember { mutableStateOf<List<Tache>>(emptyList()) }
    var titre by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        taches = dao.lister()                                // lecture au démarrage
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Mes tâches (Room)", style = MaterialTheme.typography.titleLarge)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = titre, onValueChange = { titre = it }, label = { Text("Nouvelle tâche") },
                singleLine = true, modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    scope.launch {
                        dao.inserer(Tache(titre = titre.trim()))
                        taches = dao.lister()                // ⚠ relire à la main : la liste ne se met pas à jour seule
                        titre = ""
                    }
                },
                enabled = titre.isNotBlank(),
            ) { Text("Ajouter") }
        }

        Text("${taches.size} tâche(s) en base")
        LazyColumn {
            items(taches, key = { it.id }) { tache ->
                ListItem(headlineContent = { Text("#${tache.id}  ${tache.titre}") })
            }
        }
    }
}
