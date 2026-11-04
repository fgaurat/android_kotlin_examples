package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Sujet 11 — Bonnes pratiques d'implémentation
// ① Séparer l'écran AVEC état (ProduitsEcran) de l'affichage SANS état (ListeProduits)
// ② Toujours proposer « modifier: Modifier = Modifier » et l'appliquer à la racine
// ③ Aucun effet de bord dans la composition : chargement, réseau → LaunchedEffect (ou ViewModel)
// ④ Calcul coûteux → remember(cle) : refait seulement si la clé change
// ⑤ Listes : items(..., key = { it.id })
// Une fonction @Composable peut être appelée très souvent : elle doit rester rapide et sans surprise.

private data class Produit(val id: Int, val nom: String, val prix: Double)

class MainActivity11 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    ProduitsEcran()
                }
            }
        }
    }
}

// ① Avec état
@Composable
private fun ProduitsEcran() {
    var produits by remember { mutableStateOf<List<Produit>?>(null) }
    var trierParPrix by rememberSaveable { mutableStateOf(false) }

    // ③ ❌ produits = chargerProduits() ici serait rappelé à chaque recomposition
    //    ✅ LaunchedEffect(Unit) : exécuté une seule fois, à l'arrivée à l'écran
    LaunchedEffect(Unit) {
        produits = chargerProduits()
    }

    val liste = produits
    if (liste == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        ListeProduits(
            produits = liste,
            trierParPrix = trierParPrix,
            onTrierParPrixChange = { trierParPrix = it },
            modifier = Modifier.padding(16.dp),
        )
    }
}

// ① Sans état ② avec un paramètre modifier
@Composable
private fun ListeProduits(
    produits: List<Produit>,
    trierParPrix: Boolean,
    onTrierParPrixChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ④ Le tri n'est refait que si la liste ou le critère change
    val produitsAffiches = remember(produits, trierParPrix) {
        if (trierParPrix) produits.sortedBy { it.prix } else produits.sortedBy { it.nom }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = trierParPrix,
            onClick = { onTrierParPrixChange(!trierParPrix) },
            label = { Text("Trier par prix") },
        )
        LazyColumn {
            items(produitsAffiches, key = { it.id }) { produit ->       // ⑤
                ListItem(
                    headlineContent = { Text(produit.nom) },
                    trailingContent = { Text("%.2f €".format(produit.prix)) },
                )
            }
        }
    }
}

// Simule un appel réseau
private suspend fun chargerProduits(): List<Produit> {
    delay(1_000)
    return listOf(
        Produit(1, "Clavier", 49.90),
        Produit(2, "Souris", 19.90),
        Produit(3, "Écran", 189.00),
        Produit(4, "Casque", 79.00),
    )
}
