package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

// Sujet 16 — Gestion des différentes tailles d'écran
// On raisonne sur la largeur de la FENÊTRE (pas sur le type d'appareil) :
// - Compact  (< 600 dp)  : téléphone en portrait → une seule colonne
// - Medium   (600-840)   : pliable, petite tablette
// - Expanded (≥ 840 dp)  : tablette, téléphone en paysage → liste et détail côte à côte
// Tester : rotation, émulateur « Resizable » ou « Pixel Tablet ».

private val notes = listOf("Courses", "Réunion de lundi", "Idées de vacances", "Livres à lire", "Recettes")

class MainActivity16 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Ecran()
                }
            }
        }
    }
}

@Composable
private fun Ecran() {
    val classe = currentWindowAdaptiveInfo().windowSizeClass
    val large = classe.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    var noteChoisie by rememberSaveable { mutableStateOf<String?>(null) }

    if (large) {
        // Grand écran : les deux volets en même temps
        Row {
            ListeNotes(onNoteClick = { noteChoisie = it }, modifier = Modifier.width(300.dp))
            VerticalDivider()
            DetailNote(note = noteChoisie)
        }
    } else {
        // Petit écran : la liste OU le détail ; le bouton retour revient à la liste
        BackHandler(enabled = noteChoisie != null) { noteChoisie = null }
        val note = noteChoisie
        if (note == null) {
            ListeNotes(onNoteClick = { noteChoisie = it })
        } else {
            DetailNote(note = note)
        }
    }
}

@Composable
private fun ListeNotes(onNoteClick: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxHeight()) {
        items(notes) { note ->
            ListItem(
                headlineContent = { Text(note) },
                modifier = Modifier.clickable { onNoteClick(note) },
            )
        }
    }
}

@Composable
private fun DetailNote(note: String?) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(note ?: "Choisissez une note", style = MaterialTheme.typography.headlineMedium)
    }
}
