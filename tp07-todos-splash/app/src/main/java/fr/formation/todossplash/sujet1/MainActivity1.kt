package fr.formation.todossplash.sujet1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

// Étape 1 — Le splash screen minimal
// Depuis Android 12, le système affiche TOUJOURS un splash au démarrage (icône de l'app sur fond uni).
// La bibliothèque core-splashscreen permet de le personnaliser, de la même façon sur toutes les versions :
// 1. un thème de démarrage (res/values/themes.xml, Theme.TodosSplash.Demarrage) : fond, icône, thème suivant
// 2. ce thème sur l'activité, dans le manifest (android:theme)
// 3. installSplashScreen() AVANT super.onCreate() : applique postSplashScreenTheme et gère le splash
// Ici, le splash disparaît dès que le premier écran est dessiné : à peine visible.
// À essayer :
// - lancer l'app depuis le LANCEUR de l'émulateur (pas depuis Android Studio) : l'icône centrale s'affiche
// - changer splash_fond dans res/values/colors.xml
// - commenter installSplashScreen() : le thème de démarrage reste appliqué à toute l'activité

class MainActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()                           // toujours AVANT super.onCreate()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Bienvenue !", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }
    }
}
