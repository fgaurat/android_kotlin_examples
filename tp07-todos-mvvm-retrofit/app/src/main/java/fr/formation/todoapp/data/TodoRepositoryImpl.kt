package fr.formation.todoapp.data

import fr.formation.todoapp.data.remote.TodoApi
import fr.formation.todoapp.data.remote.dto.MajTodoDto
import fr.formation.todoapp.data.remote.dto.NouveauTodoDto
import fr.formation.todoapp.data.remote.toDomain
import fr.formation.todoapp.domain.Todo
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Implémentation réseau (même code qu'au TP04) :
 * appelle l'API, convertit les DTO en domaine et traduit les exceptions techniques en ErreurTodo.
 */
class TodoRepositoryImpl(private val api: TodoApi) : TodoRepository {

    override suspend fun lister(utilisateurId: Int, recherche: String): Result<List<Todo>> =
        appel {
            api.lister(
                utilisateurId = utilisateurId,
                recherche = recherche.ifBlank { null },        // recherche vide → pas de filtre
            ).map { it.toDomain() }
        }

    override suspend fun ajouter(titre: String, utilisateurId: Int): Result<Todo> =
        appel { api.creer(NouveauTodoDto(utilisateurId, titre, completed = false)).toDomain() }

    override suspend fun changerStatut(id: String, terminee: Boolean): Result<Todo> =
        appel { api.modifier(id, MajTodoDto(completed = terminee)).toDomain() }

    override suspend fun supprimer(id: String): Result<Unit> =
        appel {
            // Response<Unit> : pas d'exception pour un code hors 2xx, on la lève nous-mêmes
            val reponse = api.supprimer(id)
            if (!reponse.isSuccessful) throw HttpException(reponse)
        }

    /** resultatDe relance CancellationException ; les autres exceptions deviennent des ErreurTodo. */
    private suspend fun <T> appel(bloc: suspend () -> T): Result<T> =
        resultatDe(bloc).traduireErreur()
}

/** Traduit l'échec technique d'un Result en ErreurTodo ; le succès est inchangé. */
internal fun <T> Result<T>.traduireErreur(): Result<T> =
    exceptionOrNull()?.let { Result.failure(it.versErreurTodo()) } ?: this

internal fun Throwable.versErreurTodo(): Throwable = when (this) {
    is ErreurTodo -> this
    is IOException -> ErreurTodo.Reseau(this)             // JSON Server arrêté, timeout…
    is HttpException -> if (code() == 404) ErreurTodo.Introuvable(this) else ErreurTodo.Serveur(code(), this)
    is SerializationException -> ErreurTodo.Donnees(this)
    else -> this
}
