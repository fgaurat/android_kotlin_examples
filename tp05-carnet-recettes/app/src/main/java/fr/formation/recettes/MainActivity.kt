package fr.formation.recettes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.formation.recettes.ui.CarnetRecettesApp
import fr.formation.recettes.ui.theme.CarnetRecettesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarnetRecettesTheme {
                CarnetRecettesApp()
            }
        }
    }
}
