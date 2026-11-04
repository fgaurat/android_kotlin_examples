package fr.formation.hydratation.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Étape 7 : receiver DYNAMIQUE, inscrit tant que le composable est dans la composition.
 * Utilisation : `val modeAvion by rememberModeAvion()`.
 */
@Composable
fun rememberModeAvion(): State<Boolean> {
    val context = LocalContext.current
    val modeAvion = remember { mutableStateOf(estModeAvionActif(context)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                modeAvion.value = intent.getBooleanExtra("state", false)
            }
        }
        ContextCompat.registerReceiver(
            context, receiver,
            IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,     // les broadcasts système arrivent quand même
        )
        // L'état a pu changer pendant que l'écran n'était pas composé
        modeAvion.value = estModeAvionActif(context)
        onDispose { context.unregisterReceiver(receiver) }   // toujours symétrique
    }
    return modeAvion
}

private fun estModeAvionActif(context: Context) =
    Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
