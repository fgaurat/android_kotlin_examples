package fr.formation.ousuisje

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.formation.ousuisje.ui.OuSuisJeScreen
import fr.formation.ousuisje.ui.theme.OuSuisJeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OuSuisJeTheme {
                OuSuisJeScreen()
            }
        }
    }
}
