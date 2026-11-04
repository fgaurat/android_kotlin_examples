package fr.formation.taches

import android.app.Application

/** Déclarée dans le manifest (android:name=".TachesApplication"). */
class TachesApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()          // créé une seule fois, au démarrage du processus
    }
}
