package fr.formation.hydratation

import android.app.Application
import fr.formation.hydratation.data.PreferencesHydratation
import fr.formation.hydratation.data.dataStoreHydratation
import fr.formation.hydratation.notifications.NotificationsRappel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Étape 1 : déclarée dans le manifest (android:name=".HydratationApplication"). */
class HydratationApplication : Application() {

    /** Conteneur de dépendances minimal, partagé par l'UI, le Worker et le receiver. */
    val preferences: PreferencesHydratation by lazy { PreferencesHydratation(dataStoreHydratation) }

    /** Portée qui survit à onReceive() : utilisée avec goAsync() dans CestFaitReceiver. */
    val portee = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationsRappel.creerCanal(this)   // idempotent : sans risque à chaque lancement
    }
}
