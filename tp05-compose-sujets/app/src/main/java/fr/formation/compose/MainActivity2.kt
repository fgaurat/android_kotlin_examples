package fr.formation.compose

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

// Sujet 2 — Utiliser la Preview
// Ouvrir ce fichier en mode « Split » ou « Design » : chaque @Preview est dessinée
// sans lancer l'application. Une preview ne peut afficher qu'un composable
// qui reçoit ses données en paramètres.

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Column {
                        CarteFilm(titre = "Le Voyage de Chihiro", annee = 2001)
                    }
                }
            }
        }
    }
}

@Composable
private fun CarteFilm(titre: String, annee: Int) {
    Card(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titre, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Sorti en $annee", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// 1. Preview simple
@Preview(showBackground = true)
@Composable
private fun CarteFilmPreview() {
    MaterialTheme { CarteFilm(titre = "Le Voyage de Chihiro", annee = 2001) }
}

// 2. Paramètres de la preview : nom, largeur, mode sombre, taille de police
@Preview(name = "Étroit, sombre", widthDp = 200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Grande police", fontScale = 1.5f, showBackground = true)
@Composable
private fun CarteFilmVariantesPreview() {
    MaterialTheme { CarteFilm(titre = "Le Voyage de Chihiro", annee = 2001) }
}

// 3. Multipreview fournie par Compose : clair + sombre en une annotation
@PreviewLightDark
@Composable
private fun CarteFilmClairSombrePreview() {
    MaterialTheme { CarteFilm(titre = "Mon voisin Totoro", annee = 1988) }
}

// 4. Une preview par jeu de données, dont les cas limites (titre très long)
private class TitresProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf(
        "Ponyo",
        "Nausicaä de la Vallée du Vent, version restaurée et remastérisée",
    )
}

@Preview(showBackground = true)
@Composable
private fun CarteFilmTitresPreview(@PreviewParameter(TitresProvider::class) titre: String) {
    MaterialTheme { CarteFilm(titre = titre, annee = 1984) }
}
