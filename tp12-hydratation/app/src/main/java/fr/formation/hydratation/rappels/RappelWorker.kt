package fr.formation.hydratation.rappels

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import fr.formation.hydratation.notifications.NotificationsRappel
import java.time.Clock

/**
 * Étape 3 : rappel de boire un verre d'eau, sauf entre 22 h et 8 h.
 *
 * Le constructeur à deux paramètres est celui qu'utilise la WorkerFactory par défaut ;
 * le constructeur principal permet aux tests d'injecter une horloge.
 */
class RappelWorker(
    contexte: Context,
    parametres: WorkerParameters,
    private val horloge: Clock,
) : CoroutineWorker(contexte, parametres) {

    constructor(contexte: Context, parametres: WorkerParameters) :
        this(contexte, parametres, Clock.systemDefaultZone())

    override suspend fun doWork(): Result {
        // Le bouton de test force l'affichage, même la nuit
        val forcer = inputData.getBoolean(CLE_FORCER, false)
        if (!forcer && !ReglesRappel(horloge).rappelAutorise()) {
            return Result.success(workDataOf(CLE_NOTIFIE to false))   // nuit : on ne dérange pas
        }
        val notifie = NotificationsRappel.afficher(applicationContext)
        // Permission refusée : ce n'est pas une erreur à retenter
        return Result.success(workDataOf(CLE_NOTIFIE to notifie))
    }

    companion object {
        const val CLE_FORCER = "forcer"
        const val CLE_NOTIFIE = "notifie"
    }
}
