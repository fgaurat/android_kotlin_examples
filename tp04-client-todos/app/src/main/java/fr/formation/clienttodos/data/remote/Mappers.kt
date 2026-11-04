package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.data.remote.dto.NouveauTodoDto
import fr.formation.clienttodos.data.remote.dto.PageDto
import fr.formation.clienttodos.data.remote.dto.TodoDto
import fr.formation.clienttodos.data.remote.dto.UtilisateurDto
import fr.formation.clienttodos.domain.model.PageTodos
import fr.formation.clienttodos.domain.model.Todo
import fr.formation.clienttodos.domain.model.Utilisateur

// Fonctions d'extension : le domaine ne dépend jamais du format réseau.

fun TodoDto.toDomain() = Todo(
    id = id,
    titre = title,
    terminee = completed,
    utilisateurId = userId,
)

fun Todo.toNouveauDto() = NouveauTodoDto(
    userId = utilisateurId,
    title = titre,
    completed = terminee,
)

fun UtilisateurDto.toDomain() = Utilisateur(
    id = id,
    nom = name,
    pseudo = pseudo,
    email = email,
)

fun PageDto<TodoDto>.toDomain(page: Int) = PageTodos(
    todos = data.map { it.toDomain() },
    page = page,
    nombreDePages = pages,
    total = items,
    precedente = prev,
    suivante = next,
)
