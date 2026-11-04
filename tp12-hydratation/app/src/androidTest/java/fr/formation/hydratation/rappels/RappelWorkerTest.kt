package fr.formation.hydratation.rappels

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Tests instrumentés avec work-testing (exécutés sur émulateur : ./gradlew connectedDebugAndroidTest).
 * runBlocking plutôt que runTest : kotlinx-coroutines-test devrait sinon avoir exactement
 * la version de kotlinx-coroutines embarquée dans l'app (NoSuchMethodError à l'exécution).
 */
@RunWith(AndroidJUnit4::class)
class RappelWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    /** WorkerFactory de test : injecte une horloge figée dans RappelWorker. */
    private fun fabriqueA(heure: Int) = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
        ): ListenableWorker {
            val zone = ZoneId.systemDefault()
            val horloge = Clock.fixed(LocalDateTime.of(2026, 11, 1, heure, 0).atZone(zone).toInstant(), zone)
            return RappelWorker(appContext, workerParameters, horloge)
        }
    }

    @Test
    fun nuit_succesSansNotification() = runBlocking {
        val worker = TestListenableWorkerBuilder<RappelWorker>(context)
            .setWorkerFactory(fabriqueA(23))
            .build()

        val resultat = worker.doWork()

        assertEquals(ListenableWorker.Result.success(workDataOf(RappelWorker.CLE_NOTIFIE to false)), resultat)
    }

    @Test
    fun forcer_laNuit_tenteLaNotification() = runBlocking {
        val worker = TestListenableWorkerBuilder<RappelWorker>(context)
            .setWorkerFactory(fabriqueA(3))
            .setInputData(workDataOf(RappelWorker.CLE_FORCER to true))
            .build()

        val resultat = worker.doWork()

        // notifie vaut true ou false selon que POST_NOTIFICATIONS est accordée sur l'appareil
        assertEquals(ListenableWorker.Result.Success::class, resultat::class)
    }

    @Test
    fun planifier_puisAnnuler_travailUnique() {
        val planification = PlanificationRappels(context)
        val workManager = WorkManager.getInstance(context)

        planification.planifier()
        planification.planifier()     // pas de doublon grâce au nom unique

        val infos = workManager.getWorkInfosForUniqueWork(TRAVAIL_RAPPEL_HYDRATATION).get()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
        assertNotNull(infos.single().periodicityInfo)
        assertEquals(60 * 60 * 1000L, infos.single().periodicityInfo!!.repeatIntervalMillis)

        planification.annuler()
        val apres = workManager.getWorkInfosForUniqueWork(TRAVAIL_RAPPEL_HYDRATATION).get()
        assertEquals(WorkInfo.State.CANCELLED, apres.single().state)
    }
}
