# TP 05 bis — Gestion d'état avec un compteur (version simple)

Version courte du TP `tp05-state-compteur`, pensée pour être lue et expliquée en direct.

## Exécuter une étape

Chaque étape est un fichier complet : `MainActivity1.kt` à `MainActivity7.kt`
(`app/src/main/java/fr/formation/compteur/`).

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité de l'étape** et laisser les autres
en commentaire, puis lancer l'application.

## Les étapes

| Fichier | Notion | Ce qu'on observe |
|---|---|---|
| `MainActivity1.kt` | `var compteur = 0` | ❌ Logcat (`tag:Compteur`) affiche 1, 2, 3… mais l'écran reste à 0 |
| `MainActivity2.kt` | `mutableStateOf` sans `remember` | ❌ L'écran se redessine, mais l'état est recréé à 0 |
| `MainActivity3.kt` | `remember` | ✅ Le compteur fonctionne… ❌ jusqu'à la rotation |
| `MainActivity4.kt` | `rememberSaveable` | ✅ Survit à la rotation |
| `MainActivity5.kt` | *State hoisting* | `CompteurEcran` possède l'état, `Compteur` est sans état et prévisualisable |
| `MainActivity6.kt` | Pourquoi remonter l'état | Deux compteurs et un total : l'état monte dans le parent commun |
| `MainActivity7.kt` | `ViewModel` + `StateFlow` | L'état vit dans le ViewModel ; `Compteur` est identique à l'étape 5 |

## À retenir

- L'état doit être **observé** (`mutableStateOf`), **mémorisé** (`remember`) et, s'il vient de
  l'utilisateur, **sauvegardé** (`rememberSaveable`).
- *State hoisting* : l'état **descend** (`valeur`), les événements **remontent** (`onIncrementer`).
- On place l'état dans le **plus petit parent commun** de ceux qui en ont besoin.
- Dans un ViewModel : un `MutableStateFlow` privé, un `StateFlow` public, lu côté Compose avec
  `collectAsStateWithLifecycle()`.
