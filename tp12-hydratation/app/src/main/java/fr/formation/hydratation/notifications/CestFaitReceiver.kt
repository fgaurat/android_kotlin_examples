package fr.formation.hydratation.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import fr.formation.hydratation.HydratationApplication
import kotlinx.coroutines.launch

/**
 * Étape 6 : action « C'est fait » de la notification.
 * Déclaré dans le manifest avec exported="false".
 */
class CestFaitReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationsRappel.ACTION_CEST_FAIT) return

        val application = context.applicationContext as HydratationApplication
        // onReceive() tourne sur le thread principal : l'écriture DataStore part en
        // coroutine, goAsync() garde le receiver vivant jusqu'à finish() (quelques secondes max).
        val resultat = goAsync()
        application.portee.launch {
            try {
                application.preferences.ajouterVerre()
                NotificationsRappel.retirer(context)
            } finally {
                resultat.finish()
            }
        }
    }
}
