package fr.formation.ficheproduit.logique

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Prix en euros selon la langue : « 29,90 € » en français, « €29.90 » en anglais (États-Unis). */
fun formaterPrix(centimes: Int, locale: Locale): String =
    NumberFormat.getCurrencyInstance(locale)
        .apply { currency = Currency.getInstance("EUR") }
        .format(centimes / 100.0)

/** Note affichée avec le séparateur décimal de la langue : « 4,5 » / « 4.5 ». */
fun formaterNote(note: Float, locale: Locale): String =
    NumberFormat.getNumberInstance(locale)
        .apply { maximumFractionDigits = 1; minimumFractionDigits = 1 }
        .format(note)
