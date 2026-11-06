package fr.formation.testsujets.sujet1

// Étape 1 — Le CODE testé : une création qui LÈVE une exception, un mapping DTO → domaine, une priorité
// Les tests sont dans src/test/java/fr/formation/testsujets/sujet1/
//
// ---------- Organisation en fichiers (dans un vrai projet) ----------
// main/…/domain/Todo.kt                Todo, Priorite, nouvelleTodo
// main/…/data/remote/TodoDto.kt        TodoDto
// main/…/data/remote/Mappers.kt        versDomaine
// test/…/domain/NouvelleTodoTest.kt
// test/…/domain/PrioriteParametreTest.kt
// test/…/data/remote/MappersTest.kt

enum class Priorite { BASSE, NORMALE, HAUTE;

    companion object {
        // "haute", " HAUTE " → HAUTE ; inconnu ou absent → NORMALE
        fun depuis(code: String?): Priorite =
            entries.find { it.name.equals(code?.trim(), ignoreCase = true) } ?: NORMALE
    }
}

data class Todo(val id: String, val titre: String, val terminee: Boolean = false, val priorite: Priorite = Priorite.NORMALE)

// Refuse un titre invalide : c'est une erreur du programme appelant, pas un cas normal
fun nouvelleTodo(id: String, titre: String): Todo {
    require(titre.isNotBlank()) { "Le titre est obligatoire" }
    return Todo(id = id, titre = titre.trim())
}

// Ce que renvoie l'API (JSON Server 0.17 : id numérique, champs anglais, priorité facultative)
data class TodoDto(val id: Int, val title: String, val completed: Boolean, val priority: String? = null)

fun TodoDto.versDomaine(): Todo = Todo(
    id = id.toString(),
    titre = title.trim(),
    terminee = completed,
    priorite = Priorite.depuis(priority),
)
