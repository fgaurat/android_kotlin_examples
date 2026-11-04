package fr.formation.ousuisje.ui

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import fr.formation.ousuisje.BuildConfig
import fr.formation.ousuisje.geo.CalculDistance
import fr.formation.ousuisje.geo.Lieu
import fr.formation.ousuisje.geo.PARIS
import fr.formation.ousuisje.geo.estApproximative
import fr.formation.ousuisje.localisation.LocalisationDataSource
import fr.formation.ousuisje.localisation.Position
import fr.formation.ousuisje.localisation.SourceLocalisation
import fr.formation.ousuisje.localisation.calculDistanceAndroid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Origine { PONCTUELLE, SUIVI, VILLE_CHOISIE }

enum class CauseErreur { LOCALISATION_DESACTIVEE, PERMISSION_RETIREE, ECHEC }

sealed interface EtatPosition {
    data object Initial : EtatPosition
    data object Recherche : EtatPosition
    data class Trouvee(
        val position: Position,
        val distanceParisMetres: Float,
        val approximative: Boolean,
        val origine: Origine,
        val nomVille: String? = null,
    ) : EtatPosition
    data class Erreur(val cause: CauseErreur) : EtatPosition
}

data class EtatPermission(val accordee: Boolean = false, val precise: Boolean = false)

class OuSuisJeViewModel(
    private val source: SourceLocalisation,
    private val calculDistance: CalculDistance,
    /** Flavor pro : BuildConfig.SUIVI_CONTINU. */
    val suiviDisponible: Boolean,
    private val intervalleSuiviMs: Long,
) : ViewModel() {

    private val _permission = MutableStateFlow(EtatPermission())
    val permission: StateFlow<EtatPermission> = _permission.asStateFlow()

    private val _suiviActif = MutableStateFlow(false)
    val suiviActif: StateFlow<Boolean> = _suiviActif.asStateFlow()

    private val ponctuel = MutableStateFlow<EtatPosition>(EtatPosition.Initial)

    /**
     * Suivi continu : la collecte (donc l'abonnement au FLP) ne vit que tant que l'UI
     * collecte `etat` (collectAsStateWithLifecycle + WhileSubscribed) : jamais en arrière-plan.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")    // activé uniquement quand la permission est accordée
    private val suivi: Flow<EtatPosition?> = _suiviActif.flatMapLatest { actif ->
        if (!actif) {
            flowOf(null)
        } else {
            source.suivrePosition(intervalleSuiviMs)
                .map<Position, EtatPosition?> { versEtat(it, Origine.SUIVI) }
                .onStart { emit(EtatPosition.Recherche) }
                .catch { e ->
                    ponctuel.value = EtatPosition.Erreur(causeDe(e))
                    _suiviActif.value = false
                }
        }
    }

    val etat: StateFlow<EtatPosition> = combine(ponctuel, suivi) { p, s -> s ?: p }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EtatPosition.Initial)

    /** Appelée au retour au premier plan et après chaque réponse à la demande. */
    fun permissionsMisesAJour(accordee: Boolean, precise: Boolean) {
        _permission.value = EtatPermission(accordee, precise)
        if (!accordee) _suiviActif.value = false     // permission retirée dans les paramètres
    }

    /** Étape 2 : position ponctuelle (les deux flavors). */
    @SuppressLint("MissingPermission")    // appelée uniquement après accord (onAccordee)
    fun localiser() {
        _suiviActif.value = false
        viewModelScope.launch {
            ponctuel.value = EtatPosition.Recherche
            ponctuel.value = try {
                source.positionCourante()
                    ?.let { versEtat(it, Origine.PONCTUELLE) }
                    ?: EtatPosition.Erreur(CauseErreur.LOCALISATION_DESACTIVEE)
            } catch (e: CancellationException) {
                throw e                                   // ne jamais avaler l'annulation
            } catch (e: Exception) {
                EtatPosition.Erreur(causeDe(e))
            }
        }
    }

    /** Étape 9 : suivi continu, flavor pro uniquement. */
    fun basculerSuivi() {
        if (suiviDisponible && _permission.value.accordee) _suiviActif.value = !_suiviActif.value
    }

    /** Mode dégradé : sans position, l'app reste utilisable avec une ville choisie. */
    fun choisirVille(lieu: Lieu) {
        _suiviActif.value = false
        ponctuel.value = EtatPosition.Trouvee(
            position = Position(lieu.latitude, lieu.longitude, precisionMetres = null),
            distanceParisMetres = distanceJusquAParis(lieu.latitude, lieu.longitude),
            approximative = false,
            origine = Origine.VILLE_CHOISIE,
            nomVille = lieu.nom,
        )
    }

    private fun versEtat(position: Position, origine: Origine) = EtatPosition.Trouvee(
        position = position,
        distanceParisMetres = distanceJusquAParis(position.latitude, position.longitude),
        approximative = estApproximative(_permission.value.precise, position.precisionMetres),
        origine = origine,
    )

    private fun distanceJusquAParis(latitude: Double, longitude: Double): Float =
        calculDistance.metres(latitude, longitude, PARIS.latitude, PARIS.longitude)

    private fun causeDe(e: Throwable) = when (e) {
        is SecurityException -> CauseErreur.PERMISSION_RETIREE   // retirée entre-temps
        else -> CauseErreur.ECHEC
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY]!!
                OuSuisJeViewModel(
                    source = LocalisationDataSource(application),
                    calculDistance = calculDistanceAndroid,
                    suiviDisponible = BuildConfig.SUIVI_CONTINU,
                    intervalleSuiviMs = BuildConfig.INTERVALLE_SUIVI_MS,
                )
            }
        }
    }
}
