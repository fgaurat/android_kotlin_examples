package fr.formation.clienttodos.data

import fr.formation.clienttodos.data.remote.TodoApi
import fr.formation.clienttodos.data.remote.dto.MajTodoDto
import fr.formation.clienttodos.data.remote.dto.NouveauTodoDto
import fr.formation.clienttodos.data.remote.toDomain
import fr.formation.clienttodos.data.remote.toNouveauDto
import fr.formation.clienttodos.domain.model.PageTodos
import fr.formation.clienttodos.domain.model.Todo
import fr.formation.clienttodos.domain.model.TodoAvecAuteur
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class TodoRepositoryImpl(private val api: TodoApi) : TodoRepository {

    // Version développée, comme sur la slide : chaque exception technique est traduite.
    override suspend fun lister(utilisateurId: Int?): Result<List<Todo>> =
        try {
            val dtos = api.lister(utilisateurId = utilisateurId)
            Result.success(dtos.map { it.toDomain() })
        } catch (e: CancellationException) {
            throw e                                        // ne JAMAIS avaler l'annulation
        } catch (e: IOException) {
            Result.failure(ErreurTodo.Reseau(e))           // ConnectException, UnknownHost, timeout…
        } catch (e: HttpException) {
            Result.failure(
                if (e.code() == 404) ErreurTodo.Introuvable(e) else ErreurTodo.Serveur(e.code(), e)
            )
        } catch (e: SerializationException) {
            Result.failure(ErreurTodo.Donnees(e))
        }

    // Les autres opérations utilisent resultatDe, puis la même traduction des erreurs.

    override suspend fun lire(id: String): Result<Todo> =
        appel { api.lire(id).toDomain() }

    override suspend fun ajouter(titre: String, utilisateurId: Int): Result<Todo> =
        appel { api.creer(NouveauTodoDto(utilisateurId, titre, completed = false)).toDomain() }

    override suspend fun changerStatut(id: String, terminee: Boolean): Result<Todo> =
        appel { api.modifier(id, MajTodoDto(completed = terminee)).toDomain() }

    override suspend fun remplacer(todo: Todo): Result<Todo> =
        appel { api.remplacer(todo.id, todo.toNouveauDto()).toDomain() }

    override suspend fun supprimer(id: String): Result<Unit> =
        appel {
            // Response<Unit> : pas d'exception pour un code hors 2xx, on la lève nous-mêmes
            val reponse = api.supprimer(id)
            if (!reponse.isSuccessful) throw HttpException(reponse)
        }

    // Bonus : deux requêtes simultanées. Durée ≈ la plus longue des deux, pas leur somme.
    // Possible uniquement parce que l'on connaît déjà l'id de l'auteur (sinon : GET todo, puis GET user).
    override suspend fun lireAvecAuteur(todoId: String, auteurId: String): Result<TodoAvecAuteur> =
        appel {
            coroutineScope {
                val todo = async { api.lire(todoId) }
                val auteur = async { api.utilisateur(auteurId) }
                TodoAvecAuteur(todo.await().toDomain(), auteur.await().toDomain())
            }   // si l'une des deux échoue, l'autre est annulée
        }

    override suspend fun listerPage(page: Int, parPage: Int): Result<PageTodos> =
        appel { api.listerPage(page, parPage).toDomain(page) }

    private suspend fun <T> appel(bloc: suspend () -> T): Result<T> =
        resultatDe(bloc).traduireErreur()
}

/** Traduit l'échec technique d'un Result en ErreurTodo ; le succès est inchangé. */
internal fun <T> Result<T>.traduireErreur(): Result<T> =
    exceptionOrNull()?.let { Result.failure(it.versErreurTodo()) } ?: this

internal fun Throwable.versErreurTodo(): Throwable = when (this) {
    is ErreurTodo -> this
    is IOException -> ErreurTodo.Reseau(this)
    is HttpException -> if (code() == 404) ErreurTodo.Introuvable(this) else ErreurTodo.Serveur(code(), this)
    is SerializationException -> ErreurTodo.Donnees(this)
    else -> this
}
