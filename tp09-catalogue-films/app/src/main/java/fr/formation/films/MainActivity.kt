package fr.formation.films

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.formation.films.ui.navigation.FilmsApp
import fr.formation.films.ui.theme.CatalogueFilmsTheme

/** Unique Activity de l'application : elle héberge tous les écrans Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as FilmsApplication).container
        setContent {
            CatalogueFilmsTheme {
                FilmsApp(container = container)
            }
        }
    }
}
