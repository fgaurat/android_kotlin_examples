package fr.formation.panier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.formation.panier.data.CatalogueRepositoryEnMemoire
import fr.formation.panier.domain.Panier
import fr.formation.panier.ui.CatalogueScreen
import fr.formation.panier.ui.CatalogueUiState
import fr.formation.panier.ui.CatalogueViewModel
import fr.formation.panier.ui.theme.PanierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PanierTheme {
                // Le ViewModel fourni n'a pas de Factory : on passe le repository via un initializer
                val viewModel = viewModel { CatalogueViewModel(CatalogueRepositoryEnMemoire()) }
                val etat by viewModel.etat.collectAsStateWithLifecycle()
                // Panier immuable gardé en mémoire de composition (démo, non sauvegardé)
                var panier by remember { mutableStateOf(Panier()) }
                var codePromo by rememberSaveable { mutableStateOf("") }

                // Le ViewModel fourni ne charge rien dans son init : on déclenche le chargement,
                // sans recharger après une rotation si le catalogue est déjà là
                LaunchedEffect(viewModel) {
                    if (viewModel.etat.value is CatalogueUiState.Chargement) viewModel.charger()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CatalogueScreen(
                        etat = etat,
                        panier = panier,
                        codePromo = codePromo,
                        onAjouter = { panier = panier.ajouter(it) },
                        onCodePromoChange = { codePromo = it },
                        onReessayer = viewModel::charger,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
