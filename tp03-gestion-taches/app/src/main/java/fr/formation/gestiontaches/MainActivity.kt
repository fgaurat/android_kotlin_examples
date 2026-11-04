package fr.formation.gestiontaches

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.gestiontaches.domain.Filtre
import fr.formation.gestiontaches.domain.ResultatAction
import fr.formation.gestiontaches.domain.Todo
import fr.formation.gestiontaches.domain.TodoStore
import fr.formation.gestiontaches.domain.estAFaire
import fr.formation.gestiontaches.domain.genererTodos
import fr.formation.gestiontaches.domain.nombreParUtilisateur
import fr.formation.gestiontaches.domain.parUtilisateur
import fr.formation.gestiontaches.domain.premieresAFaire
import fr.formation.gestiontaches.domain.resume
import fr.formation.gestiontaches.domain.selon
import fr.formation.gestiontaches.domain.titreCourt
import fr.formation.gestiontaches.domain.titreLePlusLong
import fr.formation.gestiontaches.domain.triees
import fr.formation.gestiontaches.domain.utilisateurLePlusActif
import fr.formation.gestiontaches.ui.theme.GestionTachesTheme
import kotlinx.coroutines.launch
import kotlin.time.measureTimedValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionTachesTheme {
                EcranTaches()
            }
        }
    }
}

/** Utilisateurs proposés pour une nouvelle tâche : ceux des données initiales. */
private val UTILISATEURS = listOf(1, 2, 3)

/** Étape 5 : un message adapté à chaque cas, avec un `when` exhaustif (pas de `else`). */
fun messagePour(resultat: ResultatAction, ressources: Resources): String = when (resultat) {
    is ResultatAction.Succes -> ressources.getString(R.string.message_succes, resultat.todo.titreCourt)
    ResultatAction.DejaTerminee -> ressources.getString(R.string.message_deja_terminee)
    ResultatAction.Introuvable -> ressources.getString(R.string.message_introuvable)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcranTaches() {
    // TodoStore n'est pas observable : on garde une copie dans un état Compose,
    // rafraîchie après chaque action (TodoStore.todos renvoie une nouvelle liste).
    var todos by remember { mutableStateOf(TodoStore.todos) }
    var filtre by rememberSaveable { mutableStateOf(Filtre.TOUTES) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val ressources = LocalResources.current

    fun afficher(message: String) {
        todos = TodoStore.todos
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    // Étapes 3 et 4 : filtre, tri (à faire d'abord, puis par titre), puis regroupement par utilisateur
    val groupes = todos.selon(filtre).triees().parUtilisateur().toSortedMap()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding),
        ) {
            item { CarteResume(todos) }
            item { CarteRecherches(todos) }
            item { CarteSequence() }
            item {
                CarteAjout(onAjouter = { titre, utilisateurId ->
                    val todo = TodoStore.ajouter(titre, utilisateurId)
                    afficher(ressources.getString(R.string.message_ajout, todo.titreCourt, utilisateurId))
                })
            }
            item {
                EnteteTaches(
                    filtre = filtre,
                    onFiltre = { filtre = it },
                    onIdInconnu = { afficher(messagePour(TodoStore.terminer("0"), ressources)) },
                    onReinitialiser = {
                        TodoStore.reinitialiser()
                        afficher(ressources.getString(R.string.message_reinitialisation))
                    },
                )
            }
            if (groupes.isEmpty()) {
                item { Text(stringResource(R.string.aucune_tache)) }
            }
            groupes.forEach { (utilisateurId, taches) ->
                item(key = "utilisateur-$utilisateurId") {
                    Text(
                        pluralStringResource(R.plurals.entete_utilisateur, taches.size, utilisateurId, taches.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                items(taches, key = Todo::id) { todo ->
                    LigneTodo(
                        todo = todo,
                        onTerminer = { afficher(messagePour(TodoStore.terminer(todo.id), ressources)) },
                        onSupprimer = {
                            if (TodoStore.supprimer(todo.id)) {
                                afficher(ressources.getString(R.string.message_suppression, todo.titreCourt))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun Carte(titre: String, modifier: Modifier = Modifier, contenu: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titre, style = MaterialTheme.typography.titleMedium)
            contenu()
        }
    }
}

/** Étapes 4 et 6 : résumé buildString, utilisateur le plus actif, titre le plus long, comptage. */
@Composable
fun CarteResume(todos: List<Todo>) {
    val aucune = stringResource(R.string.aucune_donnee)
    Carte(stringResource(R.string.titre_resume)) {
        Text(todos.resume(), fontWeight = FontWeight.Bold)
        Text(
            todos.utilisateurLePlusActif()?.let { stringResource(R.string.utilisateur_plus_actif, it) } ?: aucune,
        )
        todos.titreLePlusLong()?.let {
            Text(stringResource(R.string.titre_plus_long, it.titreCourt, it.id))
        }
        Text(
            stringResource(
                R.string.nombre_par_utilisateur,
                todos.nombreParUtilisateur().toSortedMap().entries.joinToString { "${it.key} → ${it.value}" },
            ),
        )
    }
}

/** Étape 6 : la même fonction d'ordre supérieur, avec une lambda puis avec une référence de fonction. */
@Composable
fun CarteRecherches(todos: List<Todo>) {
    // todos est lu pour recomposer la carte après chaque action ; le calcul passe par TodoStore.filtrer.
    val parLambda = remember(todos) { TodoStore.filtrer { it.utilisateurId == 2 } }
    val parReference = remember(todos) { TodoStore.filtrer(::estAFaire) }
    Carte(stringResource(R.string.titre_recherches)) {
        Text(stringResource(R.string.recherche_lambda), fontWeight = FontWeight.Bold)
        Text(parLambda.joinToString { it.titreCourt }, style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.recherche_reference), fontWeight = FontWeight.Bold)
        Text(parReference.joinToString { it.titreCourt }, style = MaterialTheme.typography.bodySmall)
    }
}

/** Bonus : 100 000 tâches traitées avec une Sequence, calculées une seule fois. */
@Composable
fun CarteSequence() {
    val (resultat, duree) = remember {
        measureTimedValue {
            val sequence = genererTodos(100_000)
            sequence.resume() to sequence.premieresAFaire(utilisateurId = 4)
        }
    }
    val (resume, premieres) = resultat
    Carte(stringResource(R.string.titre_sequence)) {
        Text(resume, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.sequence_duree, duree.inWholeMilliseconds))
        Text(stringResource(R.string.sequence_premieres, premieres.joinToString { it.id }))
    }
}

/** Bonus : ajout d'une tâche (titre + utilisateur). */
@Composable
fun CarteAjout(onAjouter: (titre: String, utilisateurId: Int) -> Unit) {
    var titre by rememberSaveable { mutableStateOf("") }
    var utilisateurId by rememberSaveable { mutableIntStateOf(UTILISATEURS.first()) }
    Carte(stringResource(R.string.titre_ajout)) {
        OutlinedTextField(
            value = titre,
            onValueChange = { titre = it },
            label = { Text(stringResource(R.string.champ_titre)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UTILISATEURS.forEach { id ->
                FilterChip(
                    selected = id == utilisateurId,
                    onClick = { utilisateurId = id },
                    label = { Text(stringResource(R.string.utilisateur_court, id)) },
                )
            }
        }
        Button(
            onClick = {
                onAjouter(titre, utilisateurId)
                titre = ""
            },
            enabled = titre.isNotBlank(),   // TodoStore.ajouter refuse un titre vide
        ) {
            Text(stringResource(R.string.bouton_ajouter))
        }
    }
}

@Composable
fun Filtre.libelle(): String = stringResource(
    when (this) {
        Filtre.TOUTES -> R.string.filtre_toutes
        Filtre.A_FAIRE -> R.string.filtre_a_faire
        Filtre.TERMINEES -> R.string.filtre_terminees
    },
)

/** Titre de la liste, choix du filtre et actions de démonstration. */
@Composable
fun EnteteTaches(
    filtre: Filtre,
    onFiltre: (Filtre) -> Unit,
    onIdInconnu: () -> Unit,
    onReinitialiser: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.titre_taches), style = MaterialTheme.typography.titleLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Filtre.entries.forEach { choix ->
                FilterChip(
                    selected = choix == filtre,
                    onClick = { onFiltre(choix) },
                    label = { Text(choix.libelle()) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onIdInconnu) { Text(stringResource(R.string.bouton_id_inconnu)) }
            TextButton(onClick = onReinitialiser) { Text(stringResource(R.string.bouton_reinitialiser)) }
        }
    }
}

@Composable
fun LigneTodo(todo: Todo, onTerminer: () -> Unit, onSupprimer: () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = todo.titre,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (todo.terminee) TextDecoration.LineThrough else null,
                color = if (todo.terminee) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            // Le bouton reste actif sur une tâche terminée : il montre le cas DejaTerminee du when.
            OutlinedButton(onClick = onTerminer) { Text(stringResource(R.string.bouton_terminer)) }
            TextButton(onClick = onSupprimer) { Text(stringResource(R.string.bouton_supprimer)) }
        }
        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun CarteResumePreview() {
    GestionTachesTheme { CarteResume(TodoStore.todos) }
}
