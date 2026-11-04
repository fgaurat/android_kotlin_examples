package fr.formation.clienttodos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import fr.formation.clienttodos.data.TodoRepository
import fr.formation.clienttodos.data.TodoRepositoryImpl
import fr.formation.clienttodos.data.remote.todoApi
import fr.formation.clienttodos.ui.TodosEcran
import fr.formation.clienttodos.ui.theme.ClientTodosTheme

class MainActivity : ComponentActivity() {

    // Jour 1 : création directe. Jour 3 : fourni par un AppContainer.
    private val repository: TodoRepository by lazy { TodoRepositoryImpl(todoApi) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClientTodosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodosEcran(
                        repository = repository,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
