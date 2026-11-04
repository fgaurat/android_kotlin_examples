package fr.formation.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Sujet 4 — Les Modifier
// Un Modifier décore un composable : taille, marges, fond, bordure, clic…
// Ils s'appliquent DANS L'ORDRE, de l'extérieur vers l'intérieur :
// comparer les deux premiers textes, seuls padding et background sont inversés.

class MainActivity4 : ComponentActivity() {
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
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

        // padding PUIS background : la marge est à l'extérieur du fond jaune
        Text(
            "padding puis background",
            modifier = Modifier.padding(16.dp).background(Color.Yellow),
        )

        // background PUIS padding : le fond jaune inclut la marge
        Text(
            "background puis padding",
            modifier = Modifier.background(Color.Yellow).padding(16.dp),
        )

        // clip avant background : le fond prend la forme arrondie
        Text(
            "clip + background + padding",
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Cyan)
                .padding(16.dp),
        )

        // border + clickable : toute la zone, marge comprise, réagit au clic
        Text(
            "Cliquez-moi",
            modifier = Modifier
                .border(2.dp, Color.Blue, RoundedCornerShape(8.dp))
                .clickable { Toast.makeText(context, "Clic !", Toast.LENGTH_SHORT).show() }
                .padding(16.dp),
        )

        // size + clip(CircleShape) : un rond
        Text(
            "",
            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.Magenta),
        )
    }
}
