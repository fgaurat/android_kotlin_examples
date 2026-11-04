package fr.formation.compteur.etapes

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.compteur.R

private const val TAG = "Compteur"

// Étape 1 — ❌ Une variable locale : modifiée au clic, mais Compose ne l'observe pas.
@Composable
fun Etape1Var(modifier: Modifier = Modifier) {
    var compteur = 0
    CompteurSimple(
        valeur = compteur,
        onIncrementer = {
            compteur++
            Log.d(TAG, "Étape 1 : compteur = $compteur, l'écran affiche toujours 0")
        },
        modifier = modifier,
    )
}

// Étape 2 — ❌ Un état observé, mais recréé à 0 à chaque recomposition.
@SuppressLint("UnrememberedMutableState")   // erreur volontaire : le lint Compose la signale
@Composable
fun Etape2SansRemember(modifier: Modifier = Modifier) {
    var compteur by mutableIntStateOf(0)
    SideEffect { Log.d(TAG, "Étape 2 : recomposition, nouvel état créé à $compteur") }
    CompteurSimple(valeur = compteur, onIncrementer = { compteur++ }, modifier = modifier)
}

// Étape 3 — ✅ remember : l'état survit aux recompositions… mais pas à la rotation.
@Composable
fun Etape3Remember(modifier: Modifier = Modifier) {
    var compteur by remember { mutableIntStateOf(0) }
    CompteurSimple(valeur = compteur, onIncrementer = { compteur++ }, modifier = modifier)
}

// Étape 4 — ✅ rememberSaveable : survit aussi à la rotation et à la mort du processus.
@Composable
fun Etape4RememberSaveable(modifier: Modifier = Modifier) {
    var compteur by rememberSaveable { mutableIntStateOf(0) }
    CompteurSimple(valeur = compteur, onIncrementer = { compteur++ }, modifier = modifier)
}

// Affichage commun aux étapes 1 à 4 : seul l'emplacement de l'état change.
@Composable
private fun CompteurSimple(valeur: Int, onIncrementer: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.compteur_valeur, valeur), style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onIncrementer) { Text(stringResource(R.string.ajouter_un)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun Etape4Preview() {
    MaterialTheme { Etape4RememberSaveable() }
}
