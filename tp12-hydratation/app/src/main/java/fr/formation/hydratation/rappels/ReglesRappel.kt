package fr.formation.hydratation.rappels

import java.time.Clock
import java.time.LocalTime

/**
 * Étape 3 : pas de rappel la nuit, entre 22 h et 8 h.
 * L'horloge est injectée : les tests fixent l'heure sans attendre la nuit.
 */
class ReglesRappel(private val horloge: Clock = Clock.systemDefaultZone()) {

    fun rappelAutorise(): Boolean = estDansLaPlageAutorisee(LocalTime.now(horloge))

    companion object {
        val DEBUT: LocalTime = LocalTime.of(8, 0)     // inclus
        val FIN: LocalTime = LocalTime.of(22, 0)      // exclu

        fun estDansLaPlageAutorisee(heure: LocalTime): Boolean =
            !heure.isBefore(DEBUT) && heure.isBefore(FIN)
    }
}
