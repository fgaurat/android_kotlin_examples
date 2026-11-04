package fr.formation.parametres

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Écrit à l'étape 1, AVANT la migration, et non modifié ensuite :
 * seule la couche vue change (XML + View Binding → Compose).
 */
class ParametresViewModel : ViewModel() {

    private val _etat = MutableStateFlow(ParametresUiState())
    val etat: StateFlow<ParametresUiState> = _etat.asStateFlow()

    fun choisirUnite(unite: Unite) {
        _etat.update { it.copy(unite = unite) }
    }

    fun activerThemeSombre(actif: Boolean) {
        _etat.update { it.copy(themeSombre = actif) }
    }
}
