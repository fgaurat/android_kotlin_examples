package fr.formation.mvvm.sujet4

// Couche MODEL (domaine) : les données de l'application.
// Une simple classe Kotlin, sans aucune dépendance Android.
data class Tache(val id: Int, val titre: String, val faite: Boolean = false)
