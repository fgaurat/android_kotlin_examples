package fr.formation.compteur

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Étape 2 — mutableStateOf, sans remember
// Compose observe maintenant la valeur : le clic déclenche une recomposition.
// ❌ Mais la recomposition rappelle Compteur(), qui recrée l'état… à 0.
//    (Retirer @SuppressLint pour voir l'erreur signalée par Android Studio.)

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Compteur()
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun Compteur() {
    var compteur by mutableIntStateOf(0)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Compteur : $compteur", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { compteur++ }) {
            Text("+1")
        }
    }
}
