package fr.formation.films.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FilmsRepositoryTest {

    private val repository = FilmsRepository()

    @Test
    fun `les identifiants du catalogue sont uniques`() {
        assertEquals(filmsFictifs.size, filmsFictifs.map { it.id }.toSet().size)
    }

    @Test
    fun `film retrouve un film par son identifiant`() {
        assertEquals("Parasite", repository.film(5)?.titre)
        assertNull(repository.film(0))
    }

    @Test
    fun `basculerFavori ajoute puis retire et viderFavoris remet a zero`() {
        repository.basculerFavori(2)
        repository.basculerFavori(4)
        assertEquals(setOf(2, 4), repository.idsFavoris.value)

        repository.basculerFavori(2)
        assertEquals(setOf(4), repository.idsFavoris.value)

        repository.viderFavoris()
        assertEquals(emptySet<Int>(), repository.idsFavoris.value)
    }
}
