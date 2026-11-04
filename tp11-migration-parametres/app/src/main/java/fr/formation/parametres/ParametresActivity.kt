package fr.formation.parametres

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.formation.parametres.ui.theme.ParametresTheme

/**
 * Après migration : ComponentActivity + setContent.
 * Plus de layout XML, plus de binding, plus de repeatOnLifecycle manuel.
 */
class ParametresActivity : ComponentActivity() {

    // ViewModel INCHANGÉ
    private val viewModel: ParametresViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Équivalent de repeatOnLifecycle(STARTED) { collect { … } }
            val etat by viewModel.etat.collectAsStateWithLifecycle()

            // Barres système accordées au thème choisi (icônes claires sur fond sombre et inversement)
            DisposableEffect(etat.themeSombre) {
                val style = if (etat.themeSombre) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            // Le thème sombre choisi s'applique réellement à tout l'écran
            ParametresTheme(darkTheme = etat.themeSombre) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ParametresScreen(
                        etat = etat,
                        onUniteChoisie = viewModel::choisirUnite,
                        onThemeSombreChange = viewModel::activerThemeSombre,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
