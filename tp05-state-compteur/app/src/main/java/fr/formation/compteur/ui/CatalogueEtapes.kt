package fr.formation.compteur.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.formation.compteur.R
import fr.formation.compteur.etapes.Etape1Var
import fr.formation.compteur.etapes.Etape2SansRemember
import fr.formation.compteur.etapes.Etape3Remember
import fr.formation.compteur.etapes.Etape4RememberSaveable

private class Etape(
    @StringRes val titre: Int,
    @StringRes val explication: Int,
    val contenu: @Composable () -> Unit,
)

private val etapes = listOf(
    Etape(R.string.etape1_titre, R.string.etape1_explication) { Etape1Var() },
    Etape(R.string.etape2_titre, R.string.etape2_explication) { Etape2SansRemember() },
    Etape(R.string.etape3_titre, R.string.etape3_explication) { Etape3Remember() },
    Etape(R.string.etape4_titre, R.string.etape4_explication) { Etape4RememberSaveable() },
    Etape(R.string.etape5_titre, R.string.etape5_explication) { CompteurRoute() },
    Etape(R.string.etape6_titre, R.string.etape6_explication) { ScoreRoute() },
    Etape(R.string.etape7_titre, R.string.etape7_explication) { ScoreStateRoute() },
    Etape(R.string.etape8_titre, R.string.etape8_explication) { ScoreViewModelRoute() },
)

/** Un onglet par étape du TP, pour comparer les versions pendant la démonstration. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogueEtapes(modifier: Modifier = Modifier) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    Column(modifier = modifier) {
        PrimaryScrollableTabRow(selectedTabIndex = index) {
            etapes.forEachIndexed { i, etape ->
                Tab(selected = i == index, onClick = { index = i }, text = { Text(stringResource(etape.titre)) })
            }
        }
        Text(
            text = stringResource(etapes[index].explication),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
        )
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            // Changer d'onglet retire l'étape de la composition : son état remember / rememberSaveable
            // est perdu, alors que le ViewModel (étape 8) survit.
            key(index) { etapes[index].contenu() }
        }
    }
}
