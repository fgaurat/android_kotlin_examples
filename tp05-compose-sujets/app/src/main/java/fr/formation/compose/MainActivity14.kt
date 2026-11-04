package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Sujet 14 — Animation
// On anime un CHANGEMENT D'ÉTAT : on modifie la valeur cible, Compose anime la transition.
// 1. animate*AsState    : une valeur (taille, couleur…) glisse vers sa nouvelle cible
// 2. AnimatedVisibility : apparition / disparition
// 3. AnimatedContent    : transition entre deux contenus
// 4. rememberInfiniteTransition : une animation qui tourne en boucle

class MainActivity14 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Ecran()
                }
            }
        }
    }
}

@Composable
private fun Ecran() {
    var grand by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(true) }
    var compteur by remember { mutableIntStateOf(0) }

    // 1. Taille et couleur animées
    val taille by animateDpAsState(targetValue = if (grand) 120.dp else 60.dp, label = "taille")
    val couleur by animateColorAsState(
        targetValue = if (grand) Color(0xFF00658F) else Color(0xFFE57373),
        animationSpec = tween(durationMillis = 600),
        label = "couleur",
    )

    // 4. Opacité qui pulse en continu
    val pulsation by rememberInfiniteTransition(label = "pulsation").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha",
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(taille).background(couleur, CircleShape))
        Button(onClick = { grand = !grand }) { Text("Agrandir / réduire") }

        // 2. Apparition / disparition
        Button(onClick = { visible = !visible }) { Text(if (visible) "Masquer" else "Afficher") }
        AnimatedVisibility(visible = visible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Card { Text("Je glisse et je m'efface", modifier = Modifier.padding(16.dp)) }
        }

        // 3. Le nouveau nombre remplace l'ancien en fondu
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AnimatedContent(targetState = compteur, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "compteur") { valeur ->
                Text("$valeur", style = MaterialTheme.typography.displayMedium)
            }
            Button(onClick = { compteur++ }) { Text("+1") }
        }

        Text("Chargement…", modifier = Modifier.alpha(pulsation))
    }
}
