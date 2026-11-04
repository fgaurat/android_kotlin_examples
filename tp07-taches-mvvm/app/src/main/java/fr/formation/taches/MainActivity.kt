package fr.formation.taches

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import fr.formation.taches.ui.taches.TachesRoute
import fr.formation.taches.ui.theme.TachesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TachesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TachesRoute(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
