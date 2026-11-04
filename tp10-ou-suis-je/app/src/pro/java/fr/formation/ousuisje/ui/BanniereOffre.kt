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
import fr.formation.ousuisje.BuildConfig

/** Source set du flavor « pro ». */
@Composable
fun BanniereOffre() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Text(
            "Version Pro : suivi continu (une mise à jour toutes les ${BuildConfig.INTERVALLE_SUIVI_MS / 1000} s).",
            modifier = Modifier.padding(16.dp),
        )
    }
}
