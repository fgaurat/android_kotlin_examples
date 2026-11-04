package fr.formation.compteur.score

const val SCORE_MAX = 99

enum class Equipe { A, B }

enum class Meneur { A, B, EGALITE }

/** État du tableau de score : immuable, chaque modification produit une copie. */
data class ScoreUiState(val scoreA: Int = 0, val scoreB: Int = 0) {

    val total: Int get() = scoreA + scoreB

    val meneur: Meneur
        get() = when {
            scoreA > scoreB -> Meneur.A
            scoreB > scoreA -> Meneur.B
            else -> Meneur.EGALITE
        }

    fun score(equipe: Equipe): Int = if (equipe == Equipe.A) scoreA else scoreB

    /** Ajoute [delta] au score de [equipe], borné entre 0 et [SCORE_MAX]. */
    fun ajouter(equipe: Equipe, delta: Int): ScoreUiState {
        val nouveau = (score(equipe) + delta).coerceIn(0, SCORE_MAX)
        return if (equipe == Equipe.A) copy(scoreA = nouveau) else copy(scoreB = nouveau)
    }
}
