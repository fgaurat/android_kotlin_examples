package fr.formation.parametres

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.parametres.ui.theme.ParametresTheme

/**
 * Équivalent Compose de activity_parametres.xml.
 * Composable SANS ÉTAT : il reçoit l'état et remonte les événements.
 */
@Composable
fun ParametresScreen(
    etat: ParametresUiState,
    onUniteChoisie: (Unite) -> Unit,
    onThemeSombreChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // LinearLayout vertical + padding 16dp
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = stringResource(R.string.unite_temperature),
            style = MaterialTheme.typography.titleMedium,   // ?attr/textAppearanceTitleMedium
        )

        // RadioGroup : selectableGroup() annonce un groupe aux services d'accessibilité
        Column(Modifier.selectableGroup()) {
            Unite.entries.forEach { unite ->
                LigneUnite(
                    libelle = unite.libelle,
                    selectionnee = etat.unite == unite,
                    onClick = { onUniteChoisie(unite) },
                )
            }
        }

        Spacer(Modifier.padding(top = 8.dp))

        LigneThemeSombre(
            actif = etat.themeSombre,
            onChange = onThemeSombreChange,
        )
    }
}

@get:StringRes
private val Unite.libelle: Int
    get() = when (this) {
        Unite.CELSIUS -> R.string.celsius
        Unite.FAHRENHEIT -> R.string.fahrenheit
    }

/** Une ligne entière cliquable (≥ 48 dp), comme un RadioButton XML avec son texte. */
@Composable
private fun LigneUnite(
    @StringRes libelle: Int,
    selectionnee: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selectionnee, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // onClick = null : c'est la ligne qui gère le clic (une seule cible, un seul nœud sémantique)
        RadioButton(selected = selectionnee, onClick = null)
        Spacer(Modifier.width(16.dp))
        Text(stringResource(libelle), style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Équivalent du MaterialSwitch (texte à gauche, interrupteur à droite).
 * Public : réutilisé tel quel par l'étape intermédiaire « bottom-up » (ComposeView).
 */
@Composable
fun LigneThemeSombre(
    actif: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = actif, onValueChange = onChange, role = Role.Switch),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(R.string.theme_sombre), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = actif, onCheckedChange = null)
    }
}

@Preview(name = "Clair", showBackground = true)
@Composable
private fun ParametresScreenClairPreview() {
    ParametresTheme(darkTheme = false, dynamicColor = false) {
        Surface {
            ParametresScreen(
                etat = ParametresUiState(unite = Unite.CELSIUS, themeSombre = false),
                onUniteChoisie = {},
                onThemeSombreChange = {},
            )
        }
    }
}

@Preview(name = "Sombre", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ParametresScreenSombrePreview() {
    ParametresTheme(darkTheme = true, dynamicColor = false) {
        Surface {
            ParametresScreen(
                etat = ParametresUiState(unite = Unite.FAHRENHEIT, themeSombre = true),
                onUniteChoisie = {},
                onThemeSombreChange = {},
            )
        }
    }
}
