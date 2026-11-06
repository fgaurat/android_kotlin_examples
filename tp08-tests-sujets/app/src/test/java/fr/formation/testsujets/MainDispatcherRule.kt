package fr.formation.testsujets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Partagée par les étapes 3 à 5.
// viewModelScope utilise Dispatchers.Main… qui n'existe pas sur la JVM (pas de thread principal Android).
// Cette règle JUnit le remplace par un dispatcher de test avant chaque test, et le rétablit après.
// - UnconfinedTestDispatcher (défaut) : les coroutines lancées démarrent tout de suite
// - StandardTestDispatcher : elles attendent advanceUntilIdle() / advanceTimeBy() → on voit les états intermédiaires
// runTest utilise alors la MÊME horloge virtuelle que ce dispatcher.
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
