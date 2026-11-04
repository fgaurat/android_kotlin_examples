package fr.formation.ousuisje.ui

import fr.formation.ousuisje.geo.VILLES_DE_REPLI
import fr.formation.ousuisje.geo.calculHaversine
import fr.formation.ousuisje.localisation.Position
import fr.formation.ousuisje.localisation.SourceLocalisation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Faux FLP : positions pilotées par le test. */
private class FausseSource : SourceLocalisation {
    var prochaine: Position? = null
    var exception: Exception? = null
    val positionsSuivies = MutableSharedFlow<Position>()
    var abonnes = 0

    override suspend fun positionCourante(): Position? {
        exception?.let { throw it }
        return prochaine
    }

    override fun suivrePosition(intervalleMs: Long): Flow<Position> = positionsSuivies
        .onStart { abonnes++ }
        .onCompletion { abonnes-- }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OuSuisJeViewModelTest {

    private val lyon = Position(45.7640, 4.8357, precisionMetres = 12f)
    private lateinit var source: FausseSource

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        source = FausseSource()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(suivi: Boolean = false) =
        OuSuisJeViewModel(source, calculHaversine, suiviDisponible = suivi, intervalleSuiviMs = 1_000)

    @Test
    fun localiser_positionPrecise_distanceEtPrecision() = runTest {
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.etat.collect {} }
        vm.permissionsMisesAJour(accordee = true, precise = true)
        source.prochaine = lyon

        vm.localiser()

        val etat = vm.etat.value as EtatPosition.Trouvee
        assertEquals(Origine.PONCTUELLE, etat.origine)
        assertFalse(etat.approximative)
        assertEquals(392_000f, etat.distanceParisMetres, 3_000f)
    }

    @Test
    fun localiser_seulementApproximative_marqueeApproximative() = runTest {
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.etat.collect {} }
        vm.permissionsMisesAJour(accordee = true, precise = false)
        source.prochaine = lyon.copy(precisionMetres = 3_000f)

        vm.localiser()

        assertTrue((vm.etat.value as EtatPosition.Trouvee).approximative)
    }

    @Test
    fun localiser_positionNulle_localisationDesactivee() = runTest {
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.etat.collect {} }
        vm.localiser()
        assertEquals(EtatPosition.Erreur(CauseErreur.LOCALISATION_DESACTIVEE), vm.etat.value)
    }

    @Test
    fun localiser_securityException_permissionRetiree() = runTest {
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.etat.collect {} }
        source.exception = SecurityException("révoquée")
        vm.localiser()
        assertEquals(EtatPosition.Erreur(CauseErreur.PERMISSION_RETIREE), vm.etat.value)
    }

    @Test
    fun choisirVille_modeDegrade() = runTest {
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.etat.collect {} }
        vm.choisirVille(VILLES_DE_REPLI.first { it.nom == "Brest" })
        val etat = vm.etat.value as EtatPosition.Trouvee
        assertEquals(Origine.VILLE_CHOISIE, etat.origine)
        assertEquals("Brest", etat.nomVille)
    }

    @Test
    fun suivi_indisponibleDansLeFlavorGratuit() = runTest {
        val vm = viewModel(suivi = false)
        vm.permissionsMisesAJour(accordee = true, precise = true)
        vm.basculerSuivi()
        assertFalse(vm.suiviActif.value)
    }

    @Test
    fun suivi_pro_emetPositionsPuisSeDesabonne() = runTest {
        val vm = viewModel(suivi = true)
        val collecte = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.etat.collect {} }
        vm.permissionsMisesAJour(accordee = true, precise = true)

        vm.basculerSuivi()
        assertEquals(EtatPosition.Recherche, vm.etat.value)
        assertEquals(1, source.abonnes)

        source.positionsSuivies.emit(lyon)
        assertEquals(Origine.SUIVI, (vm.etat.value as EtatPosition.Trouvee).origine)

        // Permission retirée (retour au premier plan) : le suivi s'arrête
        vm.permissionsMisesAJour(accordee = false, precise = false)
        assertFalse(vm.suiviActif.value)
        assertEquals(0, source.abonnes)
        collecte.cancel()
    }
}
