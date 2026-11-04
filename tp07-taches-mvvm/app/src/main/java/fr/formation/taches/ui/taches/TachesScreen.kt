package fr.formation.taches.ui.taches

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.taches.R
import fr.formation.taches.data.TacheRepositoryEnMemoire
import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import fr.formation.taches.ui.theme.TachesTheme

/**
 * Écran sans ViewModel : un état en entrée, des callbacks en sortie.
 * Prévisualisable et testable avec n'importe quel état.
 */
@Composable
fun TachesScreen(
    etat: TachesUiState,
    recherche: String,
    restantes: Int,
    onRechercheChange: (String) -> Unit,
    onFiltreChange: (Filtre) -> Unit,
    onAjouter: (String, Priorite) -> Unit,
    onBasculer: (Tache) -> Unit,
    onSupprimer: (Tache) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.titre_ecran), style = MaterialTheme.typography.headlineSmall)
        Text(
            pluralStringResource(R.plurals.taches_restantes, restantes, restantes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.testTag("restantes"),
        )

        FormulaireAjout(onAjouter = onAjouter)
        HorizontalDivider()

        OutlinedTextField(
            value = recherche,
            onValueChange = onRechercheChange,
            label = { Text(stringResource(R.string.rechercher_tache)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("champRecherche"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Filtre.entries.forEach { filtre ->
                FilterChip(
                    selected = etat.filtre == filtre,
                    onClick = { onFiltreChange(filtre) },
                    label = { Text(stringResource(filtre.libelle)) },
                )
            }
        }

        when {
            etat.chargement -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            etat.taches.isEmpty() -> Text(
                if (etat.recherche.isEmpty()) stringResource(R.string.aucune_tache)
                else stringResource(R.string.aucun_resultat, etat.recherche),
                modifier = Modifier.padding(top = 16.dp),
            )
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(etat.taches, key = { it.id }) { tache ->       // clé stable : animations et état conservés
                    LigneTache(tache, onBasculer = { onBasculer(tache) }, onSupprimer = { onSupprimer(tache) })
                }
            }
        }
    }
}

/** Formulaire d'ajout. Le texte en cours de saisie est un état d'UI éphémère : rememberSaveable suffit. */
@Composable
private fun FormulaireAjout(onAjouter: (String, Priorite) -> Unit) {
    var titre by rememberSaveable { mutableStateOf("") }
    var priorite by rememberSaveable { mutableStateOf(Priorite.Normale) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = titre,
            onValueChange = { titre = it },
            label = { Text(stringResource(R.string.nouvelle_tache)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("champTitre"),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Priorite.entries.forEach { p ->
                FilterChip(
                    selected = priorite == p,
                    onClick = { priorite = p },
                    label = { Text(libelle(p)) },
                )
            }
            Button(
                onClick = {
                    onAjouter(titre, priorite)
                    titre = ""
                },
                enabled = titre.isNotBlank(),
            ) { Text(stringResource(R.string.ajouter)) }
        }
    }
}

@Composable
private fun LigneTache(tache: Tache, onBasculer: () -> Unit, onSupprimer: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Checkbox(checked = tache.faite, onCheckedChange = { onBasculer() })
        Text(
            text = tache.titre,
            textDecoration = if (tache.faite) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
        )
        AssistChip(onClick = {}, label = { Text(libelle(tache.priorite)) })
        IconButton(onClick = onSupprimer) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.supprimer_tache, tache.titre))
        }
    }
}

@Composable
private fun libelle(priorite: Priorite): String = stringResource(
    when (priorite) {
        Priorite.Basse -> R.string.priorite_basse
        Priorite.Normale -> R.string.priorite_normale
        Priorite.Haute -> R.string.priorite_haute
    }
)

@Preview(showBackground = true)
@Composable
private fun TachesScreenPreview() {
    TachesTheme {
        TachesScreen(
            etat = TachesUiState(taches = TacheRepositoryEnMemoire.TACHES_EXEMPLE, filtre = Filtre.Toutes),
            recherche = "",
            restantes = 4,
            onRechercheChange = {}, onFiltreChange = {}, onAjouter = { _, _ -> },
            onBasculer = {}, onSupprimer = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TachesScreenRechercheVidePreview() {
    TachesTheme {
        TachesScreen(
            etat = TachesUiState(filtre = Filtre.Faites, recherche = "xyz"),
            recherche = "xyz",
            restantes = 0,
            onRechercheChange = {}, onFiltreChange = {}, onAjouter = { _, _ -> },
            onBasculer = {}, onSupprimer = {},
        )
    }
}
