package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// Sujet 7 — Formulaire
// Chaque champ suit le même principe : il AFFICHE une valeur (value / checked / selected)
// et SIGNALE les changements (onValueChange / onCheckedChange / onClick).
// C'est nous qui mettons l'état à jour : sans cela, le champ ne bouge pas.

class MainActivity7 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    Formulaire()
                }
            }
        }
    }
}

@Composable
private fun Formulaire() {
    var email by rememberSaveable { mutableStateOf("") }
    var motDePasse by rememberSaveable { mutableStateOf("") }
    var formule by rememberSaveable { mutableStateOf("Mensuelle") }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var nombreArticles by rememberSaveable { mutableFloatStateOf(5f) }
    var cguAcceptees by rememberSaveable { mutableStateOf(false) }

    // Validation : de simples expressions calculées à partir de l'état
    val emailInvalide = email.isNotEmpty() && "@" !in email
    val formulaireValide = "@" in email && motDePasse.length >= 8 && cguAcceptees

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            isError = emailInvalide,
            supportingText = { if (emailInvalide) Text("Adresse invalide") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = motDePasse,
            onValueChange = { motDePasse = it },
            label = { Text("Mot de passe (8 caractères min.)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Formule", style = MaterialTheme.typography.titleMedium)
        listOf("Mensuelle", "Annuelle").forEach { option ->
            // Toute la ligne est cliquable, pas seulement le petit rond
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().selectable(
                    selected = formule == option,
                    onClick = { formule = option },
                    role = Role.RadioButton,
                ),
            ) {
                RadioButton(selected = formule == option, onClick = null)
                Text(option)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recevoir les notifications", modifier = Modifier.weight(1f))
            Switch(checked = notifications, onCheckedChange = { notifications = it })
        }

        Text("Articles par page : ${nombreArticles.roundToInt()}")
        Slider(value = nombreArticles, onValueChange = { nombreArticles = it }, valueRange = 1f..20f, steps = 18)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = cguAcceptees, onCheckedChange = { cguAcceptees = it })
            Text("J'accepte les conditions d'utilisation")
        }

        // Le bouton n'est actif que si le formulaire est valide
        Button(onClick = {}, enabled = formulaireValide, modifier = Modifier.fillMaxWidth()) {
            Text("S'inscrire")
        }
    }
}
