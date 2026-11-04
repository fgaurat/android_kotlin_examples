package fr.formation.ousuisje.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Source set du flavor « gratuit » : une classe ne peut pas être redéfinie,
 * elle existe donc dans src/gratuit ET src/pro, jamais dans src/main.
 */
@Composable
fun BanniereOffre() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            "Version gratuite : position ponctuelle. Le suivi continu est réservé à la version Pro.",
            modifier = Modifier.padding(16.dp),
        )
    }
}
