package fr.formation.todoapp.data.remote

import fr.formation.todoapp.data.remote.dto.TodoDto
import fr.formation.todoapp.domain.Todo

// Fonction d'extension : le domaine ne dépend jamais du format réseau.
fun TodoDto.toDomain() = Todo(
    id = id,
    titre = title,
    terminee = completed,
    utilisateurId = userId,
)
