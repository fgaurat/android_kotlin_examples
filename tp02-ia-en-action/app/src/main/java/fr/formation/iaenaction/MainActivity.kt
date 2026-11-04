package fr.formation.iaenaction

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.iaenaction.todo.Todo
import fr.formation.iaenaction.todo.TodoClient
import fr.formation.iaenaction.todo.resume
import fr.formation.iaenaction.ui.theme.IaEnActionTheme
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "IaEnAction"

/** Données fictives (étape 3) : de quoi vérifier le résumé et ses pluriels à l'écran. */
private val todosExemple = listOf(
    Todo(utilisateurId = 1, id = "1", titre = "Activer Gemini et créer .aiexclude", terminee = true),
    Todo(utilisateurId = 1, id = "2", titre = "Faire expliquer MainActivity.kt", terminee = false),
    Todo(utilisateurId = 1, id = "3", titre = "Vérifier la data class Todo avec curl", terminee = false),
)

/** État de l'écran pour la requête HTTP (étape 5). */
sealed interface EtatChargement {
    data object Inactif : EtatChargement
    data object Chargement : EtatChargement
    data class Succes(val todo: Todo) : EtatChargement
    data class Erreur(val message: String) : EtatChargement
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IaEnActionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EcranIaEnAction(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EcranIaEnAction(
    modifier: Modifier = Modifier,
    client: TodoClient = remember { TodoClient() },
) {
    // Portée liée à la composition : la requête en cours est annulée si l'écran disparaît
    val scope = rememberCoroutineScope()
    var etat by remember { mutableStateOf<EtatChargement>(EtatChargement.Inactif) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.titre_todos), style = MaterialTheme.typography.titleLarge)
        Text(todosExemple.resume(), style = MaterialTheme.typography.titleMedium)
        todosExemple.forEach { LigneTodo(it) }

        CarteTodoDistante(
            etat = etat,
            onCharger = {
                scope.launch {
                    etat = EtatChargement.Chargement
                    etat = try {
                        EtatChargement.Succes(client.lireTodo("1"))
                    } catch (e: IOException) {
                        // Couvre aussi ErreurHttpException (404…) et les réponses illisibles
                        Log.e(TAG, "Échec du chargement de /todos/1", e)
                        EtatChargement.Erreur(e.message ?: e.javaClass.simpleName)
                    }
                }
            },
        )

        BoutonPlantage()
    }
}

@Composable
fun LigneTodo(todo: Todo, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (todo.terminee) "✅" else "⬜", style = MaterialTheme.typography.titleMedium)
        Column {
            Text(
                todo.titre,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (todo.terminee) TextDecoration.LineThrough else null,
            )
            Text(
                stringResource(R.string.todo_details, todo.id, todo.utilisateurId),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun CarteTodoDistante(etat: EtatChargement, onCharger: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.titre_todo_distant), style = MaterialTheme.typography.titleMedium)
            when (etat) {
                EtatChargement.Inactif -> Text(stringResource(R.string.todo_inactif))
                EtatChargement.Chargement ->
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                is EtatChargement.Succes -> {
                    LigneTodo(etat.todo)
                    Text(listOf(etat.todo).resume(), style = MaterialTheme.typography.bodySmall)
                }
                is EtatChargement.Erreur -> Text(
                    stringResource(R.string.todo_erreur, etat.message),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(onClick = onCharger, enabled = etat != EtatChargement.Chargement) {
                Text(stringResource(R.string.bouton_charger))
            }
        }
    }
}

/**
 * Étape 4 : PLANTAGE VOLONTAIRE pour s'exercer à « Ask Gemini » dans Logcat.
 * À ne jamais conserver dans une vraie application.
 */
@Composable
fun BoutonPlantage(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.avertissement_plantage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Button(
                onClick = { throw IllegalStateException("test") }, // plantage volontaire (étape 4)
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(stringResource(R.string.bouton_plantage))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LigneTodoPreview() {
    IaEnActionTheme { LigneTodo(todosExemple.first()) }
}

@Preview(showBackground = true)
@Composable
fun CarteTodoDistantePreview() {
    IaEnActionTheme {
        CarteTodoDistante(
            etat = EtatChargement.Succes(Todo(1, "1", "delectus aut autem", false)),
            onCharger = {},
        )
    }
}
