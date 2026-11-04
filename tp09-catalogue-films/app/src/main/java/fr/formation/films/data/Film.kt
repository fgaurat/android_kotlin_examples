package fr.formation.films.data

/** Un film du catalogue. L'[id] est la seule donnée transmise par la navigation. */
data class Film(
    val id: Int,
    val titre: String,
    val annee: Int,
    val realisateur: String,
    val synopsis: String,
)

/** Données de départ du TP : pas de réseau ni de base, une simple liste en mémoire. */
val filmsFictifs = listOf(
    Film(1, "Le Fabuleux Destin d'Amélie Poulain", 2001, "Jean-Pierre Jeunet",
        "À Montmartre, une jeune serveuse timide décide de faire le bonheur des gens qui l'entourent, au risque d'oublier le sien."),
    Film(2, "Intouchables", 2011, "Olivier Nakache et Éric Toledano",
        "Un riche aristocrate tétraplégique engage comme aide à domicile un jeune de banlieue tout juste sorti de prison."),
    Film(3, "La Haine", 1995, "Mathieu Kassovitz",
        "Vingt-quatre heures dans la vie de trois amis d'une cité de la banlieue parisienne, au lendemain d'une nuit d'émeutes."),
    Film(4, "Le Voyage de Chihiro", 2001, "Hayao Miyazaki",
        "Une fillette se retrouve piégée dans un monde peuplé d'esprits et doit travailler dans un établissement de bains pour sauver ses parents."),
    Film(5, "Parasite", 2019, "Bong Joon-ho",
        "Une famille pauvre s'infiltre, membre après membre, au service d'une famille aisée de Séoul."),
    Film(6, "Inception", 2010, "Christopher Nolan",
        "Un voleur spécialisé dans l'extraction de secrets au cœur des rêves se voit confier la mission inverse : implanter une idée."),
    Film(7, "La La Land", 2016, "Damien Chazelle",
        "À Los Angeles, une aspirante actrice et un pianiste de jazz tombent amoureux tout en poursuivant leurs rêves."),
    Film(8, "Les Incorruptibles", 1987, "Brian De Palma",
        "Pendant la Prohibition, l'agent Eliot Ness monte une équipe incorruptible pour faire tomber Al Capone."),
    Film(9, "Le Seigneur des anneaux : La Communauté de l'anneau", 2001, "Peter Jackson",
        "Un jeune hobbit hérite d'un anneau maléfique et entreprend, avec huit compagnons, un long voyage pour le détruire."),
    Film(10, "Portrait de la jeune fille en feu", 2019, "Céline Sciamma",
        "En 1770, une peintre doit réaliser en secret le portrait de mariage d'une jeune femme qui refuse de poser."),
    Film(11, "Mad Max: Fury Road", 2015, "George Miller",
        "Dans un désert post-apocalyptique, une guerrière en fuite et un vagabond affrontent un tyran et son armée."),
    Film(12, "Les Quatre Cents Coups", 1959, "François Truffaut",
        "Antoine Doinel, adolescent parisien incompris, multiplie les fugues et les bêtises jusqu'à la maison de redressement."),
)
