package fr.formation.todoapp.data

import kotlin.coroutines.cancellation.CancellationException

/**
 * Équivalent de `runCatching` qui respecte l'annulation des coroutines :
 * une `CancellationException` est relancée, jamais transformée en échec.
 */
suspend fun <T> resultatDe(
    bloc: suspend () -> T,
): Result<T> = try {
    Result.success(bloc())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
