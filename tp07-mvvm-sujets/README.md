# Jour 3 matin — Architecture MVVM, un exemple par sujet

Exemples de démonstration du chapitre **« Architecture MVVM »** (`slides/07-mvvm.md`), dans l'ordre
du chapitre. Le fil conducteur : une petite liste de tâches.

## Exécuter un sujet

Chaque sujet est dans son propre package `sujetN` (`app/src/main/java/fr/formation/mvvm/sujetN/`),
avec un fichier `MainActivityN.kt` complet, lisible de haut en bas. Les packages séparés permettent
de garder les mêmes noms d'un sujet à l'autre (`Tache`, `TachesViewModel`…) pour les comparer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité du sujet** et laisser les autres en
commentaire, puis lancer l'application.

## Les sujets

| Sujet | Thème | Ce qu'on montre |
|---|---|---|
| `sujet1` | Pourquoi une architecture ? | Tout dans le composable : perdu à la rotation, règles non testables |
| `sujet2` | MVVM : premier ViewModel | Model / ViewModel / View, `MutableStateFlow` privé, `StateFlow` public, survit à la rotation |
| `sujet3` | Flux unidirectionnel | État d'écran `sealed` (Chargement / Succès / Erreur), `viewModelScope`, « Réessayer » |
| `sujet4` | Isoler les couches | **4 fichiers** : `Tache` (model), `TacheRepository` (data), `TachesViewModel` (+ `Factory`), `MainActivity4` (view) |
| `sujet5` | Flow froid et flow chaud | `flow { }` rejoué par chaque collecteur, `StateFlow` partagé (Logcat, `tag:Flow`) |
| `sujet6` | Opérateurs de Flow | Recherche réactive : `debounce`, `combine`, `flatMapLatest`, `map`, `stateIn` (Logcat, `tag:Recherche`) |
| `sujet7` | LiveData | `MutableLiveData`, `map`, `observeAsState()`, `observe(this)` (Logcat, `tag:LiveData`) |
| `sujet8` | Cycle de vie | Journal `onCreate` … `onDestroy` à l'écran ; le ViewModel survit à la rotation (`tag:CycleDeVie`) |
| `sujet9` | `SavedStateHandle` | Après la mort du processus, seul le compteur du `SavedStateHandle` est restauré |
| `sujet10` | Cycle de vie côté Compose | `collectAsStateWithLifecycle()` contre `collectAsState()`, `LifecycleEventEffect` (`tag:Capteur`) |

## Conseils de démonstration

- **Sujets 1 et 2** : ajouter une tâche puis tourner l'écran, dans les deux sujets.
- **Sujet 5** : lancer le 2e collecteur après quelques secondes : le compte à rebours repart de 10,
  l'horloge affiche la même valeur pour les deux collecteurs.
- **Sujet 6** : taper vite « kotlin » et regarder Logcat : une seule requête.
- **Sujet 8** : tourner l'écran (`onDestroy (recréation)`), puis touche Accueil, puis retour.
- **Sujet 9** : incrémenter, touche Accueil, tuer le processus avec le bouton *Terminate Application*
  de Logcat (ou `adb shell run-as fr.formation.mvvm kill -9 $(adb shell pidof fr.formation.mvvm)`),
  puis revenir par les applications récentes. Attention : `adb shell am kill` ne tue pas toujours
  le processus sur les émulateurs récents.
- **Sujet 10** : dans chaque mode, touche Accueil puis attendre 10 s en regardant Logcat.

## Choix assumés

- Pas d'`AppContainer` ni de classe `Application` : au sujet 4, un simple `object Dependances` joue
  ce rôle, avec un commentaire qui renvoie à la version complète (`tp07-taches-mvvm`).
- Pas de réseau : les appels sont simulés avec `delay()`.
- Les ViewModels sont des classes publiques : la fabrique par défaut de `viewModel()` les instancie
  par réflexion.
