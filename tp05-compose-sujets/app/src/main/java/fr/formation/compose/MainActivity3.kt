package fr.formation.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Sujet 3 — Mise en page : Column, Row, Box
// - Column : les enfants les uns SOUS les autres
// - Row    : les enfants les uns À CÔTÉ des autres
// - Box    : les enfants les uns SUR les autres
// - Arrangement = répartition dans le sens principal ; Alignment = dans l'autre sens
// - weight  = partage de l'espace restant ; Spacer = espace vide

class MainActivity3 : ComponentActivity() {
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
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

        Text("Row + Arrangement.SpaceBetween")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Carre(Color.Red)
            Carre(Color.Green)
            Carre(Color.Blue)
        }

        Text("Row + weight : 1/3 et 2/3 de la largeur")
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f).height(40.dp).background(Color.LightGray))
            Box(Modifier.weight(2f).height(40.dp).background(Color.Gray))
        }

        Text("Row + Spacer(weight) : pousse le dernier élément à droite")
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Titre")
            Spacer(Modifier.weight(1f))
            Carre(Color.Magenta)
        }

        Text("Box : superposition et alignement")
        Box(modifier = Modifier.size(120.dp).background(Color.LightGray)) {
            Text("Haut gauche", modifier = Modifier.align(Alignment.TopStart))
            Text("Centre", modifier = Modifier.align(Alignment.Center))
            Carre(Color.Red, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

@Composable
private fun Carre(couleur: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(40.dp).background(couleur))
}
