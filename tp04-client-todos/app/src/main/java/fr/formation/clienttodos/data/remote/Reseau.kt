package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

/** Configuration JSON partagée : utilisée aussi par les tests unitaires. */
val json = Json {
    ignoreUnknownKeys = true      // ignore les champs non déclarés dans le DTO (address, phone…)
    coerceInputValues = true      // null sur un champ non nullable → valeur par défaut
    explicitNulls = false         // n'écrit pas les null : un PATCH n'envoie que les champs modifiés
}

private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(
        HttpLoggingInterceptor().apply {
            // Requêtes et réponses complètes dans Logcat (filtre « okhttp »), en debug seulement
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
    )
    .build()

/** Construit l'API pour une URL de base donnée (les tests passent l'URL d'un MockWebServer). */
fun creerTodoApi(baseUrl: String, client: OkHttpClient = okHttpClient): TodoApi =
    Retrofit.Builder()
        .baseUrl(baseUrl)                                          // doit finir par « / »
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

/** Instance unique pour toute l'application (au jour 3 : dans un AppContainer). */
val todoApi: TodoApi by lazy { creerTodoApi(BuildConfig.API_URL) }
