package fr.formation.hydratation.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class PreferencesHydratationTest {

    @get:Rule
    val dossier = TemporaryFolder()

    private val lundi = LocalDate.of(2026, 11, 2)
    private val mardi = lundi.plusDays(1)

    @Test
    fun compteurJour_logiquePure() {
        assertEquals(3, CompteurJour.valeurDuJour("2026-11-02", 3, lundi))
        assertEquals(0, CompteurJour.valeurDuJour("2026-11-02", 3, mardi))   // nouveau jour
        assertEquals(0, CompteurJour.valeurDuJour(null, 0, lundi))
        assertEquals(4, CompteurJour.apresAjout("2026-11-02", 3, lundi))
        assertEquals(1, CompteurJour.apresAjout("2026-11-02", 3, mardi))
    }

    private fun horloge(jour: LocalDate): Clock {
        val zone = ZoneId.of("Europe/Paris")
        return Clock.fixed(jour.atTime(10, 0).atZone(zone).toInstant(), zone)
    }

    /** DataStore réel (fichier temporaire) : fonctionne sur la JVM, sans émulateur. */
    @Test
    fun dataStore_compteurEtInterrupteurPersistants() = runTest {
        val fichier = dossier.newFile("test.preferences_pb").also { it.delete() }
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) { fichier }

        val lundiPrefs = PreferencesHydratation(dataStore, horloge(lundi))
        assertEquals(false, lundiPrefs.rappelsActifs.first())
        lundiPrefs.definirRappelsActifs(true)
        assertEquals(1, lundiPrefs.ajouterVerre())
        assertEquals(2, lundiPrefs.ajouterVerre())
        assertEquals(true, lundiPrefs.rappelsActifs.first())
        assertEquals(2, lundiPrefs.verresDuJour(lundi).first())

        // Le lendemain : le compteur affiché repart de zéro, l'interrupteur reste activé
        val mardiPrefs = PreferencesHydratation(dataStore, horloge(mardi))
        assertEquals(0, mardiPrefs.verresDuJour(mardi).first())
        assertEquals(1, mardiPrefs.ajouterVerre())
        assertEquals(true, mardiPrefs.rappelsActifs.first())

        mardiPrefs.remettreAZero()
        assertEquals(0, mardiPrefs.verresDuJour(mardi).first())
    }
}
