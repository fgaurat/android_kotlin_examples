package fr.formation.ficheproduit.modele

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Tailles proposées : libellés identiques dans toutes les langues. */
enum class Taille { S, M, L, XL }

/**
 * Coloris d'un produit. `teinte` (ARGB) est une DONNÉE du catalogue (la couleur réelle du tissu),
 * pas une couleur d'interface : c'est la seule couleur « en dur » légitime de l'écran.
 */
data class CouleurProduit(
    val id: String,
    @param:StringRes val nom: Int,
    val teinte: Long,
)

/** Produit du catalogue. Classe immuable ; prix en centimes pour éviter les erreurs d'arrondi. */
data class Produit(
    val id: String,
    val nom: String,
    val marque: String,
    val prixCentimes: Int,
    val note: Float,                      // de 0 à 5
    val nombreAvis: Int,
    val description: String,
    @param:DrawableRes val image: Int,
    val couleurs: List<CouleurProduit>,
    val tailles: List<Taille> = Taille.entries,
    val taillesEpuisees: Set<Taille> = emptySet(),
    val prixBarreCentimes: Int? = null,   // ancien prix, si le produit est en promotion
)

/** Ligne du panier : un produit dans une taille et un coloris donnés. */
data class LignePanier(
    val produitId: String,
    val nom: String,
    val taille: Taille,
    val couleur: CouleurProduit,
    val prixCentimes: Int,
    val quantite: Int = 1,
) {
    /** Identifiant stable (clé des listes lazy). */
    val id: String get() = "$produitId-${taille.name}-${couleur.id}"
}
