package fr.formation.films

import android.app.Application
import fr.formation.films.data.AppContainer

/** Le conteneur vit aussi longtemps que le processus : les favoris survivent à la rotation. */
class FilmsApplication : Application() {
    val container = AppContainer()
}
