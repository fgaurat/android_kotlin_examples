package fr.formation.iaenaction.todo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

/** Réponse HTTP autre que 200 (404 pour une tâche inexistante, 5xx…). */
class ErreurHttpException(val code: Int) : IOException("Réponse HTTP inattendue : $code")

/**
 * Étape 5 : requête HTTP « moderne », sans bibliothèque externe.
 *
 * - fonction `suspend` : l'appelant lance une coroutine liée à un cycle de vie
 *   (ici `rememberCoroutineScope()`, au jour 3 `viewModelScope`) ;
 * - `withContext(Dispatchers.IO)` : l'appel réseau bloquant ne s'exécute jamais sur le thread principal
 *   (sinon `NetworkOnMainThreadException`) ;
 * - pas d'`AsyncTask` (déprécié depuis l'API 30), pas de `Thread { }`, pas de `GlobalScope` ;
 * - HTTPS, délais d'attente, fermeture de la connexion, erreurs explicites.
 *
 * Au jour 3, ce client sera remplacé par Retrofit + kotlinx.serialization.
 *
 * @param urlBase modifiable pour les tests (serveur local)
 * @param delaiMs délai de connexion et de lecture
 * @param dispatcher `Dispatchers.IO` par défaut ; injectable pour les tests
 */
class TodoClient(
    private val urlBase: String = "https://jsonplaceholder.typicode.com",
    private val delaiMs: Int = 10_000,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun urlTodo(id: String): String = "$urlBase/todos/${URLEncoder.encode(id, "UTF-8")}"

    /**
     * Lit une tâche, par exemple `lireTodo("1")` -> `GET https://jsonplaceholder.typicode.com/todos/1`.
     *
     * @throws ErreurHttpException si le serveur ne répond pas 200 (404 : tâche inexistante)
     * @throws IOException en cas d'erreur réseau, de délai dépassé ou de réponse illisible
     */
    suspend fun lireTodo(id: String): Todo = withContext(dispatcher) {
        val connexion = URI(urlTodo(id)).toURL().openConnection() as HttpURLConnection
        try {
            connexion.connectTimeout = delaiMs
            connexion.readTimeout = delaiMs
            connexion.setRequestProperty("Accept", "application/json")

            val code = connexion.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw ErreurHttpException(code)

            val corps = connexion.inputStream.bufferedReader().use { it.readText() }
            try {
                TodoJson.analyserTodo(corps)
            } catch (e: JSONException) {
                throw IOException("Réponse illisible : ${e.message}", e)
            }
        } finally {
            connexion.disconnect()
        }
    }
}
