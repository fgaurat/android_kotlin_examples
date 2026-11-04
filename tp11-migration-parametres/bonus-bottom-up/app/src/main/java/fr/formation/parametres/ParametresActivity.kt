package fr.formation.parametres

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import fr.formation.parametres.databinding.ActivityParametresBinding
import fr.formation.parametres.ui.theme.ParametresTheme
import kotlinx.coroutines.launch

/**
 * BONUS « bottom-up » : étape intermédiaire de la migration.
 * Le RadioGroup reste en XML ; seul l'interrupteur est un composable, inséré via une ComposeView.
 */
class ParametresActivity : AppCompatActivity() {
    private lateinit var binding: ActivityParametresBinding
    private val viewModel: ParametresViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParametresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appliquerMargesSysteme()

        binding.groupeUnite.setOnCheckedChangeListener { _, id ->
            viewModel.choisirUnite(
                if (id == R.id.radioFahrenheit) Unite.FAHRENHEIT
                else Unite.CELSIUS
            )
        }

        // Dans une Activity, la stratégie par défaut (libération au détachement de la fenêtre) convient.
        // Dans un Fragment : setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed).
        binding.composeThemeSombre.setContent {
            ParametresTheme {
                // Le composable observe lui-même le ViewModel
                val etat by viewModel.etat.collectAsStateWithLifecycle()
                // Le MÊME composable que dans l'écran final (ParametresScreen.kt)
                LigneThemeSombre(
                    actif = etat.themeSombre,
                    onChange = viewModel::activerThemeSombre,
                )
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.etat.collect { etat ->
                    binding.groupeUnite.check(
                        if (etat.unite == Unite.FAHRENHEIT) R.id.radioFahrenheit
                        else R.id.radioCelsius
                    )
                    // Plus de binding.switchThemeSombre : l'interrupteur se met à jour tout seul
                }
            }
        }
    }

    // Ajout au code fourni : avec targetSdk 35+, l'affichage bord à bord est imposé.
    // Sans cela, le contenu passe sous la barre d'état (le modèle Empty Views Activity fait de même).
    private fun appliquerMargesSysteme() {
        val marge = binding.root.paddingTop      // les 16dp du layout
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { vue, insets ->
            val barres = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            vue.updatePadding(
                left = marge + barres.left,
                top = marge + barres.top,
                right = marge + barres.right,
                bottom = marge + barres.bottom,
            )
            insets
        }
    }
}
