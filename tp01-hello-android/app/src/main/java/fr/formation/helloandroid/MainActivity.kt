package fr.formation.helloandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.formation.helloandroid.ui.theme.HelloAndroidTheme

// Tag utilisé dans Logcat : filtre « tag:HelloAndroid » ou « package:mine »
private const val TAG = "HelloAndroid"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Étape 5 : visible dans Logcat avec le filtre « package:mine »
        Log.d(TAG, "onCreate() appelé, savedInstanceState = $savedInstanceState")

        enableEdgeToEdge()
        setContent {
            HelloAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

/**
 * Étapes 2 et 3 : le message de bienvenue vient de res/values/strings.xml.
 *
 * Le paramètre [name] est conservé pour l'exercice de débogage (étapes 6 et 7) :
 * poser un point d'arrêt conditionnel sur la ligne `val salutation = …`
 * puis modifier `name` dans le panneau Variables.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val salutation = stringResource(R.string.greeting_name, name)
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.welcome),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(text = salutation)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloAndroidTheme {
        Greeting("Android")
    }
}
