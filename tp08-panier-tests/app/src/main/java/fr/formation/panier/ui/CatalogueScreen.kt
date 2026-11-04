package fr.formation.panier.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.panier.domain.Panier
import fr.formation.panier.domain.Produit
import fr.formation.panier.ui.theme.PanierTheme

/** Écran sans état : catalogue + résumé du panier. */
@Composable
fun CatalogueScreen(
    etat: CatalogueUiState,
    panier: Panier,
    codePromo: String,
    onAjouter: (Produit) -> Unit,
    onCodePromoChange: (String) -> Unit,
    onReessayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Catalogue", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (etat) {
                CatalogueUiState.Chargement ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                is CatalogueUiState.Erreur -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(etat.message ?: "Erreur inconnue")
                    Button(onClick = onReessayer) { Text("Réessayer") }
                }

                is CatalogueUiState.Succes -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(etat.produits, key = { it.id }) { produit ->
                        ProduitItem(produit, onAjouter = { onAjouter(produit) })
                    }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            value = codePromo,
            onValueChange = onCodePromoChange,
            label = { Text("Code promo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("champCodePromo"),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Articles : ${panier.nombreArticles} — Total : %.2f €".format(panier.total(codePromo)),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("resumePanier"),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogueScreenPreview() {
    val clavier = Produit("1", "Clavier mécanique", 79.90)
    PanierTheme {
        CatalogueScreen(
            etat = CatalogueUiState.Succes(listOf(clavier, Produit("2", "Souris sans fil", 24.99))),
            panier = Panier().ajouter(clavier),
            codePromo = "BIENVENUE",
            onAjouter = {}, onCodePromoChange = {}, onReessayer = {},
        )
    }
}
