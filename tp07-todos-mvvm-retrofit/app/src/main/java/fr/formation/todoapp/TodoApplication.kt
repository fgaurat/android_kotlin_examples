package fr.formation.todoapp

import android.app.Application

/** Déclarée dans le manifest (android:name=".TodoApplication"). */
class TodoApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()          // créé une seule fois, au démarrage du processus
    }
}
