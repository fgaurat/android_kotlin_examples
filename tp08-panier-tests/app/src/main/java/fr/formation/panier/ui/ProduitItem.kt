package fr.formation.panier.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.formation.panier.domain.Produit

// Code fourni par l'énoncé (copié tel quel).
// Bonus : "%.2f €".format(...) dépend de la locale de l'appareil, voir bonus/ProduitItemCorrige.kt
@Composable
fun ProduitItem(produit: Produit, onAjouter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(produit.nom, Modifier.weight(1f))
        Text("%.2f €".format(produit.prix))
        Button(onClick = onAjouter) { Text("Ajouter") }
    }
}
