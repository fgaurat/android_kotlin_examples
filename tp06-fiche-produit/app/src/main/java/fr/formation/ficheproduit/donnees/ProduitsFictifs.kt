package fr.formation.ficheproduit.donnees

import fr.formation.ficheproduit.R
import fr.formation.ficheproduit.modele.CouleurProduit
import fr.formation.ficheproduit.modele.Produit
import fr.formation.ficheproduit.modele.Taille

/** Coloris du catalogue (couleurs réelles des tissus). */
val couleursFictives = listOf(
    CouleurProduit("marine", R.string.couleur_bleu_marine, 0xFF1F3A5F),
    CouleurProduit("ecru", R.string.couleur_blanc_casse, 0xFFF2EFE6),
    CouleurProduit("sauge", R.string.couleur_vert_sauge, 0xFF8DA399),
    CouleurProduit("terracotta", R.string.couleur_terracotta, 0xFFC0623B),
)

/** Produit de démonstration (pas de réseau, image locale). */
val produitFictif = Produit(
    id = "ts-042",
    nom = "T-shirt col rond en coton bio",
    marque = "Atelier Lumière",
    prixCentimes = 2990,
    prixBarreCentimes = 3990,
    note = 4.5f,
    nombreAvis = 128,
    description = "Coupe droite et col rond côtelé, ce t-shirt en jersey de coton biologique (180 g/m²) " +
        "devient vite un essentiel. Tissé et confectionné au Portugal, teint sans métaux lourds. " +
        "Coutures doubles aux épaules et à l'ourlet pour une meilleure tenue au lavage. " +
        "Le mannequin mesure 1,82 m et porte une taille M. Lavage en machine à 30 °C, " +
        "séchage à plat recommandé.",
    image = R.drawable.illustration_tshirt,
    couleurs = couleursFictives,
    taillesEpuisees = setOf(Taille.XL),
)

/** Variantes pour les previews : nom très long, aucun avis, note basse, une seule couleur. */
val produitNomLong = produitFictif.copy(
    id = "ts-043",
    nom = "T-shirt oversize à manches longues en coton biologique peigné, édition limitée hiver",
    prixBarreCentimes = null,
)
val produitSansAvis = produitFictif.copy(
    id = "ts-044",
    note = 0f,
    nombreAvis = 0,
    couleurs = couleursFictives.take(1),
    taillesEpuisees = emptySet(),
)
val produitNoteBasse = produitFictif.copy(id = "ts-045", note = 2.3f, nombreAvis = 1)
