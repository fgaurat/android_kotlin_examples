package fr.formation.hydratation.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

/** Une seule instance de DataStore par fichier et par processus : délégué au niveau du fichier. */
val Context.dataStoreHydratation: DataStore<Preferences> by preferencesDataStore(name = "hydratation")

/**
 * Logique pure du compteur journalier (bonus) : on mémorise la date du compteur,
 * un compteur d'un autre jour vaut 0. Aucune tâche de remise à zéro n'est nécessaire.
 */
object CompteurJour {
    fun valeurDuJour(dateCompteur: String?, valeur: Int, aujourdhui: LocalDate): Int =
        if (dateCompteur == aujourdhui.toString()) valeur else 0

    fun apresAjout(dateCompteur: String?, valeur: Int, aujourdhui: LocalDate): Int =
        valeurDuJour(dateCompteur, valeur, aujourdhui) + 1
}

/** Étape 2 (état de l'interrupteur) et bonus (compteur), persistés dans DataStore. */
class PreferencesHydratation(
    private val dataStore: DataStore<Preferences>,
    private val horloge: Clock = Clock.systemDefaultZone(),
) {
    private object Cles {
        val RAPPELS_ACTIFS = booleanPreferencesKey("rappels_actifs")
        val DATE_COMPTEUR = stringPreferencesKey("date_compteur")
        val VERRES = intPreferencesKey("verres")
    }

    val rappelsActifs: Flow<Boolean> = dataStore.data.map { it[Cles.RAPPELS_ACTIFS] ?: false }

    /** Le jour est passé en paramètre pour que l'écran puisse le rafraîchir au retour au premier plan. */
    fun verresDuJour(aujourdhui: LocalDate): Flow<Int> = dataStore.data.map {
        CompteurJour.valeurDuJour(it[Cles.DATE_COMPTEUR], it[Cles.VERRES] ?: 0, aujourdhui)
    }

    fun aujourdhui(): LocalDate = LocalDate.now(horloge)

    suspend fun definirRappelsActifs(actifs: Boolean) {
        dataStore.edit { it[Cles.RAPPELS_ACTIFS] = actifs }
    }

    /** Étape 6 : appelée par le receiver de l'action « C'est fait ». Transaction atomique. */
    suspend fun ajouterVerre(): Int {
        val jour = aujourdhui()
        var nouvelleValeur = 0
        dataStore.edit {
            nouvelleValeur = CompteurJour.apresAjout(it[Cles.DATE_COMPTEUR], it[Cles.VERRES] ?: 0, jour)
            it[Cles.DATE_COMPTEUR] = jour.toString()
            it[Cles.VERRES] = nouvelleValeur
        }
        return nouvelleValeur
    }

    suspend fun remettreAZero() {
        dataStore.edit {
            it[Cles.DATE_COMPTEUR] = aujourdhui().toString()
            it[Cles.VERRES] = 0
        }
    }
}
