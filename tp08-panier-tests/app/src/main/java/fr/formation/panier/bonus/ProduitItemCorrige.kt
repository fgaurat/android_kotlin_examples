package fr.formation.panier.bonus

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.formation.panier.domain.Produit

/** Bonus : ProduitItem corrigé, le prix ne dépend plus de la locale de l'appareil. */
@Composable
fun ProduitItemCorrige(produit: Produit, onAjouter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(produit.nom, Modifier.weight(1f))
        Text(formaterPrix(produit.prix))
        Button(onClick = onAjouter) { Text("Ajouter") }
    }
}
