package fr.formation.hydratation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.formation.hydratation.ui.HydratationScreen
import fr.formation.hydratation.ui.theme.HydratationTheme

/**
 * Ouverte par le clic sur la notification (PendingIntent + FLAG_IMMUTABLE).
 * launchMode="singleTop" + FLAG_ACTIVITY_SINGLE_TOP : si elle est déjà affichée,
 * elle n'est pas recréée (onNewIntent) ; l'écran relit DataStore de toute façon.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HydratationTheme {
                HydratationScreen()
            }
        }
    }
}
