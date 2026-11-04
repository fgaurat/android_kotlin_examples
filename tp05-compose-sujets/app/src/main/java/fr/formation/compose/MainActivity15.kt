package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// Sujet 15 — Internationalisation
// - Aucun texte en dur : tout est dans res/values/strings.xml (langue par défaut, ici l'anglais)
//   et res/values-fr/strings.xml (français). Android choisit selon la langue de l'appareil.
// - stringResource(id, args) : texte avec paramètres (%1$s, %1$d)
// - pluralStringResource : « 1 message » / « 3 messages »
// - Dates et nombres : formatés selon la langue (pas de "dd/MM/yyyy" en dur)
// Tester : changer la langue de l'émulateur, ou Paramètres > Applications > Compose > Langue.

class MainActivity15 : ComponentActivity() {
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
    var messages by rememberSaveable { mutableIntStateOf(1) }
    val langue = LocalConfiguration.current.locales[0]          // langue courante de l'interface

    val date = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(langue))
    val prix = NumberFormat.getCurrencyInstance(langue).format(1234.5)

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.i18n_titre), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.i18n_bonjour, "Ada"))
        Text(stringResource(R.string.i18n_date, date))
        Text(stringResource(R.string.i18n_prix, prix))
        Text(pluralStringResource(R.plurals.i18n_messages, messages, messages))
        Button(onClick = { messages++ }) {
            Text(stringResource(R.string.i18n_ajouter))
        }
    }
}
