package fr.formation.compteur

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Étape 1 — Une simple variable
// ❌ Le clic modifie bien la variable (voir Logcat), mais l'écran reste à 0 :
//    Compose ne sait pas que la variable a changé, il ne redessine rien.

class MainActivity1 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Compteur()
            }
        }
    }
}

@Composable
private fun Compteur() {
    var compteur = 0

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Compteur : $compteur", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {
            compteur++
            Log.d("Compteur", "compteur = $compteur")
        }) {
            Text("+1")
        }
    }
}
