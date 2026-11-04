package fr.formation.ficheproduit.logique

import fr.formation.ficheproduit.modele.LignePanier
import fr.formation.ficheproduit.modele.Produit
import fr.formation.ficheproduit.modele.Taille

/** L'ajout au panier n'est possible qu'avec une taille choisie et disponible. */
fun Produit.peutEtreAjoute(taille: Taille?): Boolean =
    taille != null && taille in tailles && taille !in taillesEpuisees

/** Ajoute une ligne ; si la même (produit, taille, coloris) existe, sa quantité augmente. */
fun List<LignePanier>.ajouter(ligne: LignePanier): List<LignePanier> =
    if (any { it.id == ligne.id }) {
        map { if (it.id == ligne.id) it.copy(quantite = it.quantite + ligne.quantite) else it }
    } else {
        this + ligne
    }

/** Change la quantité d'une ligne ; une quantité nulle ou négative la supprime. */
fun List<LignePanier>.avecQuantite(ligneId: String, quantite: Int): List<LignePanier> =
    if (quantite <= 0) {
        filterNot { it.id == ligneId }
    } else {
        map { if (it.id == ligneId) it.copy(quantite = quantite) else it }
    }

/** Nombre d'articles (affiché dans le badge de l'onglet Panier). */
fun List<LignePanier>.nombreArticles(): Int = sumOf { it.quantite }

fun List<LignePanier>.totalCentimes(): Int = sumOf { it.prixCentimes * it.quantite }
