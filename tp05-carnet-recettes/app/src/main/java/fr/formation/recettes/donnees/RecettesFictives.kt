package fr.formation.recettes.donnees

import fr.formation.recettes.modele.Categorie
import fr.formation.recettes.modele.Recette

/** Données fictives (pas de réseau) : réutilisées par l'écran, les previews et les tests. */
val recettesFictives: List<Recette> = listOf(
    Recette(
        id = "1",
        nom = "Velouté de potimarron",
        categorie = Categorie.Entree,
        dureeMinutes = 40,
        ingredients = listOf("1 potimarron", "1 oignon", "50 cl de bouillon de légumes", "10 cl de crème", "Sel, poivre"),
        etapes = listOf(
            "Laver le potimarron et le couper en cubes, sans l'éplucher.",
            "Faire revenir l'oignon émincé dans une casserole.",
            "Ajouter le potimarron et le bouillon, cuire 25 minutes.",
            "Mixer, ajouter la crème, rectifier l'assaisonnement.",
        ),
        difficulte = 1,
    ),
    Recette(
        id = "2",
        nom = "Salade de lentilles au chèvre",
        categorie = Categorie.Entree,
        dureeMinutes = 30,
        ingredients = listOf("200 g de lentilles vertes", "1 échalote", "100 g de chèvre frais", "Vinaigrette moutarde"),
        etapes = listOf(
            "Cuire les lentilles 20 minutes à l'eau non salée.",
            "Émincer l'échalote et préparer la vinaigrette.",
            "Mélanger les lentilles tièdes, l'échalote et la vinaigrette, émietter le chèvre.",
        ),
        difficulte = 1,
        favori = true,
    ),
    Recette(
        id = "3",
        nom = "Bœuf bourguignon",
        categorie = Categorie.Plat,
        dureeMinutes = 210,
        ingredients = listOf(
            "1 kg de bœuf à braiser", "75 cl de vin rouge", "200 g de lardons", "250 g de champignons",
            "2 carottes", "1 oignon", "1 bouquet garni",
        ),
        etapes = listOf(
            "Faire mariner la viande dans le vin la veille.",
            "Saisir la viande égouttée, puis les lardons et l'oignon.",
            "Mouiller avec la marinade, ajouter les carottes et le bouquet garni.",
            "Laisser mijoter 3 heures à feu doux.",
            "Ajouter les champignons poêlés 15 minutes avant la fin.",
        ),
        difficulte = 3,
    ),
    Recette(
        id = "4",
        nom = "Risotto aux champignons",
        categorie = Categorie.Plat,
        dureeMinutes = 35,
        ingredients = listOf("300 g de riz arborio", "300 g de champignons", "1 l de bouillon", "10 cl de vin blanc", "50 g de parmesan"),
        etapes = listOf(
            "Nacrer le riz dans un peu d'huile d'olive.",
            "Déglacer au vin blanc.",
            "Ajouter le bouillon louche par louche en remuant.",
            "Incorporer les champignons poêlés et le parmesan.",
        ),
        difficulte = 2,
    ),
    Recette(
        id = "5",
        nom = "Tarte Tatin",
        categorie = Categorie.Dessert,
        dureeMinutes = 75,
        ingredients = listOf("1 pâte brisée", "6 pommes", "100 g de sucre", "50 g de beurre"),
        etapes = listOf(
            "Préparer un caramel avec le sucre et le beurre dans le moule.",
            "Disposer les quartiers de pommes serrés sur le caramel.",
            "Couvrir de pâte et cuire 40 minutes à 180 °C.",
            "Démouler à chaud en retournant la tarte.",
        ),
        difficulte = 3,
        favori = true,
    ),
    Recette(
        id = "6",
        nom = "Mousse au chocolat",
        categorie = Categorie.Dessert,
        dureeMinutes = 20,
        ingredients = listOf("200 g de chocolat noir", "6 œufs", "1 pincée de sel"),
        etapes = listOf(
            "Faire fondre le chocolat au bain-marie.",
            "Séparer les blancs des jaunes, incorporer les jaunes au chocolat.",
            "Monter les blancs en neige avec le sel et les incorporer délicatement.",
            "Réserver au frais au moins 3 heures.",
        ),
        difficulte = 2,
    ),
)
