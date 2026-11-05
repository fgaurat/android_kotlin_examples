package fr.formation.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.formation.todoapp.ui.theme.TodoAppTheme
import fr.formation.todoapp.ui.todos.TodosRoute

/** L'Activity ne fait plus qu'afficher l'écran : ni repository, ni appel réseau ici. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                TodosRoute()
            }
        }
    }
}
