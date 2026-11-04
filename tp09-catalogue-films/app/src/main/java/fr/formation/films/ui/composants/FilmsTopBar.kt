package fr.formation.films.ui.composants

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/**
 * Barre du haut commune aux écrans.
 * [onRetour] à null : écran de premier niveau, pas de flèche de retour.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmsTopBar(
    titre: String,
    modifier: Modifier = Modifier,
    onRetour: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(titre, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        modifier = modifier,
        navigationIcon = {
            if (onRetour != null) {
                IconButton(onClick = onRetour) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
    )
}
