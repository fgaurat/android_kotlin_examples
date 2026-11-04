package fr.formation.ficheproduit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.formation.ficheproduit.ui.FicheProduitApp
import fr.formation.ficheproduit.ui.theme.FicheProduitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FicheProduitTheme {
                FicheProduitApp()
            }
        }
    }
}
