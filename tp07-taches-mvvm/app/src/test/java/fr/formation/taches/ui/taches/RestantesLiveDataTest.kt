package fr.formation.taches.ui.taches

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import fr.formation.taches.MainDispatcherRule
import fr.formation.taches.data.FakeTacheRepository
import fr.formation.taches.domain.Tache
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Bonus : tester une LiveData demande une règle de plus (InstantTaskExecutorRule),
 * car LiveData exige le thread principal Android. Comparer avec restantesEnStateFlow.
 */
class RestantesLiveDataTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `restantes suit le nombre de tâches à faire`() {
        val repository = FakeTacheRepository(listOf(Tache(1, "A"), Tache(2, "B", faite = true)))
        val viewModel = TachesViewModel(repository, SavedStateHandle())
        val valeurs = mutableListOf<Int>()
        val observateur = Observer<Int> { valeurs += it }

        viewModel.restantes.observeForever(observateur)     // pas de LifecycleOwner en test
        viewModel.basculer(Tache(1, "A"))
        viewModel.restantes.removeObserver(observateur)

        assertEquals(listOf(1, 0), valeurs)
    }
}
