package fr.formation.todoapp.data.remote

import fr.formation.todoapp.data.remote.dto.MajTodoDto
import fr.formation.todoapp.data.remote.dto.NouveauTodoDto
import fr.formation.todoapp.data.remote.dto.TodoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Data source réseau : l'API REST de JSON Server. Retrofit génère l'implémentation. */
interface TodoApi {

    @GET("todos")
    suspend fun lister(
        @Query("userId") utilisateurId: Int? = null,          // null → paramètre omis de l'URL
        @Query("title:contains") recherche: String? = null,   // filtre côté serveur (JSON Server 1.x)
    ): List<TodoDto>

    @POST("todos")
    suspend fun creer(@Body todo: NouveauTodoDto): TodoDto     // 201 : la tâche créée, avec son id

    @PATCH("todos/{id}")
    suspend fun modifier(@Path("id") id: String, @Body maj: MajTodoDto): TodoDto

    @DELETE("todos/{id}")
    suspend fun supprimer(@Path("id") id: String): Response<Unit>
}
