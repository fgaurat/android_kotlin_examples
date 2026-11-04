package fr.formation.panier.bonus

import java.util.Locale

/**
 * Bonus : formatage du prix indépendant de la langue de l'appareil.
 *
 * `"%.2f €".format(prix)` utilise Locale.getDefault() : « 12,50 € » en français,
 * mais « 12.50 € » en anglais. La boutique affiche des euros au format français :
 * on fixe donc la locale explicitement.
 */
fun formaterPrix(prix: Double, locale: Locale = Locale.FRANCE): String =
    "%.2f €".format(locale, prix)
