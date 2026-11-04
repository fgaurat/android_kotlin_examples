package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Sujet 17 — Utiliser Material 3
// Scaffold = la structure d'un écran Material, avec des emplacements prévus :
// topBar, bottomBar, floatingActionButton, snackbarHost, et le contenu.
// Le contenu reçoit « innerPadding » : à appliquer pour ne pas passer sous les barres.

class MainActivity17 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Ecran()
            }
        }
    }
}

private val onglets = listOf("Accueil" to Icons.Filled.Home, "Panier" to Icons.Filled.ShoppingCart, "Profil" to Icons.Filled.Person)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Ecran() {
    var ongletChoisi by remember { mutableIntStateOf(0) }
    var dialogueVisible by remember { mutableStateOf(false) }
    var filtrePromo by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boutique") },
                actions = {
                    IconButton(onClick = { dialogueVisible = true }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Paramètres")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                onglets.forEachIndexed { index, (libelle, icone) ->
                    NavigationBarItem(
                        selected = ongletChoisi == index,
                        onClick = { ongletChoisi = index },
                        label = { Text(libelle) },
                        icon = {
                            // Un badge sur l'onglet Panier
                            BadgedBox(badge = { if (libelle == "Panier") Badge { Text("3") } }) {
                                Icon(icone, contentDescription = null)
                            }
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { scope.launch { snackbar.showSnackbar("Article ajouté") } }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un article")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Onglet : ${onglets[ongletChoisi].first}", style = MaterialTheme.typography.headlineSmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filtrePromo, onClick = { filtrePromo = !filtrePromo }, label = { Text("Promotions") })
            }

            Text("Progression de la commande")
            LinearProgressIndicator(progress = { 0.6f })

            Button(onClick = { dialogueVisible = true }) { Text("Ouvrir une boîte de dialogue") }
        }
    }

    if (dialogueVisible) {
        AlertDialog(
            onDismissRequest = { dialogueVisible = false },
            title = { Text("Vider le panier ?") },
            text = { Text("Les 3 articles seront retirés.") },
            confirmButton = { TextButton(onClick = { dialogueVisible = false }) { Text("Vider") } },
            dismissButton = { TextButton(onClick = { dialogueVisible = false }) { Text("Annuler") } },
        )
    }
}
