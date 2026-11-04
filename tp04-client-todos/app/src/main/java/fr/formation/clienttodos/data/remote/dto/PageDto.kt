package fr.formation.clienttodos.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Enveloppe renvoyée par JSON Server 1.x pour GET /todos?_page=2&_per_page=10 :
 * {"first":1,"prev":1,"next":3,"last":20,"pages":20,"items":200,"data":[…]}
 * `prev` et `next` valent null sur la première et la dernière page.
 */
@Serializable
data class PageDto<T>(
    val first: Int,
    val prev: Int? = null,
    val next: Int? = null,
    val last: Int,
    val pages: Int,
    val items: Int,
    val data: List<T>,
)
