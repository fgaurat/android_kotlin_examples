package fr.formation.taches.data

import fr.formation.taches.domain.Priorite
import fr.formation.taches.domain.Tache
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Source unique de vérité des tâches, en mémoire : tout est perdu quand le processus meurt.
 * Le `delay` simule la latence d'un vrai stockage (réseau, base de données).
 */
class TacheRepositoryEnMemoire(
    tachesInitiales: List<Tache> = TACHES_EXEMPLE,
    private val latence: Duration = 300.milliseconds,
) : TacheRepository {

    private val _taches = MutableStateFlow(tachesInitiales)
    private val prochainId = AtomicLong((tachesInitiales.maxOfOrNull { it.id } ?: 0L) + 1)

    // Lecture seule pour l'extérieur ; la première émission arrive après la latence
    override val taches: Flow<List<Tache>> = _taches.asStateFlow()
        .onStart { delay(latence) }

    override suspend fun ajouter(titre: String, priorite: Priorite) {
        delay(latence)
        val tache = Tache(id = prochainId.getAndIncrement(), titre = titre, priorite = priorite)
        _taches.update { it + tache }      // update : modification atomique, sûre entre coroutines
    }

    override suspend fun basculer(id: Long) {
        delay(latence)
        _taches.update { liste -> liste.map { if (it.id == id) it.copy(faite = !it.faite) else it } }
    }

    override suspend fun supprimer(id: Long) {
        delay(latence)
        _taches.update { liste -> liste.filterNot { it.id == id } }
    }

    companion object {
        val TACHES_EXEMPLE = listOf(
            Tache(1, "Installer Android Studio", faite = true, priorite = Priorite.Haute),
            Tache(2, "Lire le guide d'architecture Android", priorite = Priorite.Normale),
            Tache(3, "Écrire TachesViewModel", priorite = Priorite.Haute),
            Tache(4, "Tester la rotation de l'écran", priorite = Priorite.Normale),
            Tache(5, "Ranger le bureau", priorite = Priorite.Basse),
        )
    }
}
