package fr.formation.ficheproduit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.dp

// Typographie Material 3 par défaut : les écrans n'utilisent que ces 15 styles (jamais de fontSize en dur).
val Typography = Typography()

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
)
