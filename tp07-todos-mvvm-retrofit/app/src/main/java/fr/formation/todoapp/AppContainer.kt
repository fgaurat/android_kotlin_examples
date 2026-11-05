package fr.formation.todoapp

import fr.formation.todoapp.data.TodoRepository
import fr.formation.todoapp.data.TodoRepositoryImpl
import fr.formation.todoapp.data.remote.TodoApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

/**
 * Injection manuelle : le conteneur crée les dépendances partagées par toute l'application.
 * Au TP04, Retrofit était créé dans un fichier Reseau.kt et le repository dans MainActivity ;
 * ici, tout est rassemblé à un seul endroit, et le ViewModel reçoit le repository tout prêt.
 */
interface AppContainer {
    val todoRepository: TodoRepository
}

class DefaultAppContainer : AppContainer {

    private val json = Json {
        ignoreUnknownKeys = true      // ignore les champs non déclarés dans le DTO
        coerceInputValues = true      // null sur un champ non nullable → valeur par défaut
        explicitNulls = false         // n'écrit pas les null : un PATCH n'envoie que les champs modifiés
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // Requêtes et réponses dans Logcat (filtre « okhttp »), en debug seulement
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL)                     // http://10.0.2.2:3000/ (JSON Server)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    override val todoRepository: TodoRepository by lazy {    // instance unique
        TodoRepositoryImpl(retrofit.create<TodoApi>())
    }
}
