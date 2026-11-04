package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Sujet 13 — Thème clair / sombre
// - Un thème = deux palettes (claire et sombre) passées à MaterialTheme.
// - Dans les écrans : uniquement MaterialTheme.colorScheme.xxx, jamais Color.Red en dur.
//   Ainsi, changer de thème ne demande AUCUNE modification des écrans.
// - Par défaut, on suit le réglage du système (isSystemInDarkTheme) ;
//   les interrupteurs permettent de forcer le mode sombre et les couleurs dynamiques.

private val PaletteClaire = lightColorScheme(
    primary = Color(0xFF00658F),
    secondary = Color(0xFF4F616E),
    background = Color(0xFFF8F9FC),
)

private val PaletteSombre = darkColorScheme(
    primary = Color(0xFF85CFFF),
    secondary = Color(0xFFB7C9D8),
    background = Color(0xFF101417),
)

@Composable
private fun MonTheme(
    sombre: Boolean = isSystemInDarkTheme(),
    couleursDynamiques: Boolean = false,                // Android 12+ : couleurs du fond d'écran
    contenu: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val palette = when {
        couleursDynamiques && sombre -> dynamicDarkColorScheme(context)
        couleursDynamiques -> dynamicLightColorScheme(context)
        sombre -> PaletteSombre
        else -> PaletteClaire
    }
    MaterialTheme(colorScheme = palette, content = contenu)
}

class MainActivity13 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemeSombre = isSystemInDarkTheme()
            var sombre by rememberSaveable { mutableStateOf(systemeSombre) }
            var dynamique by rememberSaveable { mutableStateOf(false) }

            MonTheme(sombre = sombre, couleursDynamiques = dynamique) {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {   // fond = colorScheme.surface
                    Ecran(
                        sombre = sombre,
                        onSombreChange = { sombre = it },
                        dynamique = dynamique,
                        onDynamiqueChange = { dynamique = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun Ecran(
    sombre: Boolean,
    onSombreChange: (Boolean) -> Unit,
    dynamique: Boolean,
    onDynamiqueChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Interrupteur("Mode sombre", sombre, onSombreChange)
        Interrupteur("Couleurs dynamiques", dynamique, onDynamiqueChange)

        Text("Titre en couleur primaire", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
        Button(onClick = {}) { Text("Bouton (primary)") }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "Carte en secondaryContainer",
                color = MaterialTheme.colorScheme.onSecondaryContainer,      // texte assorti au fond
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun Interrupteur(libelle: String, actif: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(libelle, modifier = Modifier.weight(1f))
        Switch(checked = actif, onCheckedChange = onChange)
    }
}
