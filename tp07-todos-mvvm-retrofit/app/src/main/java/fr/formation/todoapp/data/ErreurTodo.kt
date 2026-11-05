package fr.formation.todoapp.data

/**
 * Erreurs du domaine : l'UI n'a pas à connaître Retrofit, OkHttp ni kotlinx.serialization.
 * La cause technique est conservée pour le journal (Logcat).
 */
sealed class ErreurTodo(cause: Throwable) : Exception(cause) {
    class Reseau(cause: Throwable) : ErreurTodo(cause)
    class Introuvable(cause: Throwable) : ErreurTodo(cause)
    class Serveur(val code: Int, cause: Throwable) : ErreurTodo(cause)
    class Donnees(cause: Throwable) : ErreurTodo(cause)
}
