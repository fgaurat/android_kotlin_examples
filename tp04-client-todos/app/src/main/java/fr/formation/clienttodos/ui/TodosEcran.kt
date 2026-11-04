package fr.formation.clienttodos.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.formation.clienttodos.data.TodoRepository
import fr.formation.clienttodos.domain.model.PageTodos
import fr.formation.clienttodos.domain.model.Todo
import kotlinx.coroutines.launch
import kotlin.time.measureTimedValue

private const val TAG = "TodosEcran"
private const val UTILISATEUR_ID = 1

/**
 * Écran unique du TP (niveau jour 1 : pas encore de ViewModel).
 * L'état vit dans des `remember`, les appels réseau dans `LaunchedEffect` et `rememberCoroutineScope`.
 */
@Composable
fun TodosEcran(repository: TodoRepository, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()        // coroutines annulées quand l'écran quitte la composition

    // Étape 5 : la liste de l'utilisateur 1 dans un simple Text
    var texte by remember { mutableStateOf("Chargement…") }
    var rechargement by remember { mutableIntStateOf(0) }   // incrémenté → LaunchedEffect relancé
    LaunchedEffect(rechargement) {
        texte = repository.lister(utilisateurId = UTILISATEUR_ID).fold(
            onSuccess = { liste -> liste.joinToString("\n") { (if (it.terminee) "☑ " else "☐ ") + it.titre } },
            onFailure = {
                Log.e(TAG, "Échec du chargement", it)
                "Erreur : ${it.messagePourUtilisateur()}"
            },
        )
    }

    // Étapes 6 et 7 : actions et retour d'information
    var titre by remember { mutableStateOf("Préparer le TP") }
    var derniere by remember { mutableStateOf<Todo?>(null) }   // dernière tâche créée
    var message by remember { mutableStateOf("") }

    // Lance une action, affiche le résultat dans `message`, puis recharge la liste
    fun action(libelle: String, bloc: suspend () -> Result<String>) {
        scope.launch {
            message = "$libelle…"
            message = bloc().fold(
                onSuccess = { "$libelle : $it" },
                onFailure = {
                    Log.e(TAG, libelle, it)
                    "$libelle — ${it.messagePourUtilisateur()}"
                },
            )
            rechargement++
        }
    }

    // Bonus : pagination
    var page by remember { mutableIntStateOf(1) }
    var pageTodos by remember { mutableStateOf<PageTodos?>(null) }
    var messagePage by remember { mutableStateOf("") }
    LaunchedEffect(page) {
        repository.listerPage(page, parPage = 10)
            .onSuccess { pageTodos = it; messagePage = "" }
            .onFailure { messagePage = "Erreur : ${it.messagePourUtilisateur()}" }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Tâches de l'utilisateur $UTILISATEUR_ID", style = MaterialTheme.typography.titleLarge)
        Text(texte)
        OutlinedButton(onClick = { rechargement++ }) { Text("Recharger") }

        HorizontalDivider()
        Text("Ajouter, terminer, supprimer", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = titre,
            onValueChange = { titre = it },
            label = { Text("Titre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                action("POST") {
                    repository.ajouter(titre, UTILISATEUR_ID)
                        .onSuccess { derniere = it }
                        .map { "créée avec l'id ${it.id}" }
                }
            }) { Text("Ajouter") }

            Button(enabled = derniere != null, onClick = {
                val todo = derniere ?: return@Button
                action("PATCH") {
                    repository.changerStatut(todo.id, terminee = !todo.terminee)
                        .onSuccess { derniere = it }
                        .map { "terminee = ${it.terminee}" }
                }
            }) { Text("Basculer") }

            Button(enabled = derniere != null, onClick = {
                val todo = derniere ?: return@Button
                action("PUT") {
                    repository.remplacer(todo.copy(titre = titre))
                        .onSuccess { derniere = it }
                        .map { "titre = « ${it.titre} »" }
                }
            }) { Text("Renommer") }

            Button(enabled = derniere != null, onClick = {
                val todo = derniere ?: return@Button
                action("DELETE") {
                    repository.supprimer(todo.id)
                        .onSuccess { derniere = null }
                        .map { "tâche ${todo.id} supprimée" }
                }
            }) { Text("Supprimer") }
        }
        derniere?.let { Text("Dernière tâche créée : id ${it.id}, terminee = ${it.terminee}") }

        HorizontalDivider()
        Text("Cas d'erreur", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                action("GET /todos/9999") { repository.lire("9999").map { it.titre } }
            }) { Text("Lire 9999") }
            OutlinedButton(onClick = {
                action("DELETE /todos/9999") { repository.supprimer("9999").map { "supprimée" } }
            }) { Text("Supprimer 9999") }
        }
        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }

        HorizontalDivider()
        Text("Bonus : chargement parallèle", style = MaterialTheme.typography.titleMedium)
        var titreEtAuteur by remember { mutableStateOf("") }
        Button(onClick = {
            scope.launch {
                titreEtAuteur = "Chargement…"
                val (resultat, duree) = measureTimedValue {
                    repository.lireAvecAuteur(todoId = "1", auteurId = "1")
                }
                titreEtAuteur = resultat.fold(
                    onSuccess = { "$it (${duree.inWholeMilliseconds} ms)" },
                    onFailure = { "Erreur : ${it.messagePourUtilisateur()}" },
                )
            }
        }) { Text("Tâche 1 + auteur (async)") }
        if (titreEtAuteur.isNotEmpty()) Text(titreEtAuteur)

        HorizontalDivider()
        Text("Bonus : pagination (_page, _per_page)", style = MaterialTheme.typography.titleMedium)
        val p = pageTodos
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = p?.precedente != null, onClick = { p?.precedente?.let { page = it } }) {
                Text("◀")
            }
            Text(if (p == null) "Page $page" else "Page ${p.page} / ${p.nombreDePages} (${p.total} tâches)")
            OutlinedButton(enabled = p?.suivante != null, onClick = { p?.suivante?.let { page = it } }) {
                Text("▶")
            }
        }
        if (messagePage.isNotEmpty()) Text(messagePage)
        p?.todos?.forEach { Text("${it.id}. " + (if (it.terminee) "☑ " else "☐ ") + it.titre) }
    }
}
