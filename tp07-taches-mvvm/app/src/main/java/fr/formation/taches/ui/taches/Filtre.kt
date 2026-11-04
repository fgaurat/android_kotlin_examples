package fr.formation.taches.ui.taches

import androidx.annotation.StringRes
import fr.formation.taches.R
import fr.formation.taches.domain.Tache

/** Filtre de l'écran. Une enum est Serializable : elle peut aller dans un SavedStateHandle. */
enum class Filtre(@get:StringRes val libelle: Int) {
    Toutes(R.string.filtre_toutes),
    AFaire(R.string.filtre_a_faire),
    Faites(R.string.filtre_faites);

    fun accepte(tache: Tache): Boolean = when (this) {
        Toutes -> true
        AFaire -> !tache.faite
        Faites -> tache.faite
    }
}
