package fr.formation.clienttodos.data.remote

import fr.formation.clienttodos.data.remote.dto.MajTodoDto
import fr.formation.clienttodos.data.remote.dto.NouveauTodoDto
import fr.formation.clienttodos.data.remote.dto.PageDto
import fr.formation.clienttodos.data.remote.dto.TodoDto
import fr.formation.clienttodos.data.remote.dto.UtilisateurDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Description de l'API REST de JSON Server : Retrofit génère l'implémentation. */
interface TodoApi {

    @GET("todos")
    suspend fun lister(
        @Query("userId") utilisateurId: Int? = null,          // null → paramètre omis de l'URL
        @Query("completed") terminee: Boolean? = null,
        @Query("title:contains") recherche: String? = null,
    ): List<TodoDto>

    @GET("todos/{id}")
    suspend fun lire(@Path("id") id: String): TodoDto

    @POST("todos")
    suspend fun creer(@Body todo: NouveauTodoDto): TodoDto     // 201 : la tâche créée, avec son id

    @PUT("todos/{id}")
    suspend fun remplacer(@Path("id") id: String, @Body todo: NouveauTodoDto): TodoDto

    @PATCH("todos/{id}")
    suspend fun modifier(@Path("id") id: String, @Body maj: MajTodoDto): TodoDto

    @DELETE("todos/{id}")
    suspend fun supprimer(@Path("id") id: String): Response<Unit>

    @GET("users/{id}")
    suspend fun utilisateur(@Path("id") id: String): UtilisateurDto

    // Bonus : pagination. JSON Server 1.x renvoie une enveloppe, pas un tableau.
    @GET("todos")
    suspend fun listerPage(
        @Query("_page") page: Int,
        @Query("_per_page") parPage: Int,
    ): PageDto<TodoDto>
}
