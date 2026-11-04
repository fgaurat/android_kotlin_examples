package fr.formation.hydratation.rappels

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit

const val TRAVAIL_RAPPEL_HYDRATATION = "rappel_hydratation"
const val TAG_TEST_RAPPEL = "test_rappel"

/** Étapes 4 et 5 : planification WorkManager. */
class PlanificationRappels(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /** Étape 4 : une requête périodique d'1 heure, unique par son nom. */
    fun planifier() {
        val requete = PeriodicWorkRequestBuilder<RappelWorker>(1, TimeUnit.HOURS)
            .build()      // aucune contrainte : pas besoin de réseau pour notifier
        // Appeler plusieurs fois ne crée pas de doublon ; UPDATE conserve la planification
        workManager.enqueueUniquePeriodicWork(
            TRAVAIL_RAPPEL_HYDRATATION,
            ExistingPeriodicWorkPolicy.UPDATE,
            requete,
        )
    }

    fun annuler() {
        workManager.cancelUniqueWork(TRAVAIL_RAPPEL_HYDRATATION)
    }

    /** Étape 5 : exécution immédiate, à suivre dans le Background Task Inspector. */
    fun testerMaintenant(): UUID {
        val test = OneTimeWorkRequestBuilder<RappelWorker>()
            .setInputData(workDataOf(RappelWorker.CLE_FORCER to true))
            .addTag(TAG_TEST_RAPPEL)
            .build()
        workManager.enqueue(test)
        return test.id
    }

    /** État du travail périodique (null s'il n'existe pas). */
    val etat: Flow<WorkInfo?> = workManager
        .getWorkInfosForUniqueWorkFlow(TRAVAIL_RAPPEL_HYDRATATION)
        .map { infos -> infos.firstOrNull { !it.state.isFinished } ?: infos.firstOrNull() }
}
