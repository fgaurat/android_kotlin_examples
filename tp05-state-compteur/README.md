# TP 05 — Gestion d'état avec un compteur (corrigé)

Corrigé du TP « Compteur et *state hoisting* » du chapitre **« Interfaces graphiques avec Jetpack
Compose »** (jour 2, `slides/05-jetpack-compose.md`, après les slides sur l'état).

## Objectif

Comprendre **où vit l'état** d'une interface Compose en faisant évoluer un simple compteur :

```text
var  →  mutableStateOf  →  remember  →  rememberSaveable  →  state hoisting
     →  état partagé (ancêtre commun)  →  state holder  →  ViewModel + StateFlow
```

Chaque étape corrige le défaut de la précédente. Les étapes 7 et 8 font le lien avec l'architecture
MVVM du jour 3.

## Lancer le projet

```bash
./gradlew assembleDebug                 # APK de debug
./gradlew testDebugUnitTest             # tests unitaires (JVM, sans émulateur)
./gradlew connectedDebugAndroidTest     # tests UI Compose (émulateur ou appareil requis, API 34+)
```

L'application affiche **un onglet par étape** : pratique pour comparer les versions en démonstration.
Ouvrir Logcat avec le filtre `tag:Compteur` pour les étapes 1 et 2.

## Correspondance étapes → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/compteur/`.

| Étape | Contenu | Ce qu'on observe | Fichier |
|---|---|---|---|
| 1 | `var compteur = 0` | L'écran reste à 0, Logcat montre 1, 2, 3… : la variable change mais **n'est pas observée** | `etapes/Etapes1a4.kt` |
| 2 | `mutableStateOf(0)` sans `remember` | Observé, mais **recréé à 0** à chaque recomposition ; le lint Compose signale `UnrememberedMutableState` | `etapes/Etapes1a4.kt` |
| 3 | `remember { mutableIntStateOf(0) }` | Fonctionne… jusqu'à la **rotation** | `etapes/Etapes1a4.kt` |
| 4 | `rememberSaveable { mutableIntStateOf(0) }` | Survit à la rotation et à la mort du processus | `etapes/Etapes1a4.kt` |
| 5 | **State hoisting** : `Compteur(libelle, valeur, onIncrementer, onDecrementer, modifier, min, max)` sans état ; `CompteurRoute` possède l'état | Previews avec n'importe quelle valeur, boutons désactivés aux bornes | `ui/Compteur.kt`, `ui/Routes.kt` |
| 6 | Deux compteurs + total + « Réinitialiser » : l'état monte au **plus petit ancêtre commun** (`ScoreRoute`) ; `ScoreScreen` est sans état | Impossible avec un état interne à chaque `Compteur` | `ui/ScoreScreen.kt`, `ui/Routes.kt` |
| 7 | **State holder** `ScoreState` + `rememberScoreState()` (avec un `Saver`) | La logique quitte le composable ; `ScoreScreen` ne change pas | `score/ScoreState.kt`, `ui/Routes.kt` |
| 8 | **`ScoreViewModel`** : `MutableStateFlow` privé, `StateFlow` public, `SavedStateHandle` ; `collectAsStateWithLifecycle()` | Survit à la rotation **et** au changement d'onglet ; `ScoreScreen` ne change toujours pas | `score/ScoreViewModel.kt`, `ui/Routes.kt` |

Règles métier communes (bornes 0..99, meneur, total) : `score/ScoreUiState.kt`, une `data class`
immuable sans dépendance Android.

Tests :

| Type | Fichier | Ce qui est vérifié |
|---|---|---|
| Unitaire | `app/src/test/.../score/ScoreUiStateTest.kt` | Ajout, bornes 0 et `SCORE_MAX`, meneur |
| Unitaire | `app/src/test/.../score/ScoreViewModelTest.kt` | `StateFlow` mis à jour, réinitialisation, écriture et restauration via `SavedStateHandle` |
| UI Compose | `app/src/androidTest/.../ui/CompteurTest.kt` | Le composable sans état affiche la valeur reçue, **remonte** le clic sans changer la valeur, désactive les boutons aux bornes |
| UI Compose | `app/src/androidTest/.../ui/RestaurationEtatTest.kt` | `StateRestorationTester` : `remember` perd l'état, `rememberSaveable`, `ScoreRoute` et `ScoreState` le conservent |

## Architecture de l'état (étapes 6 à 8)

```text
ScoreRoute / ScoreStateRoute / ScoreViewModelRoute   ← possèdent l'état (3 variantes)
        │ état ▼ ScoreUiState          ▲ événements onIncrementer(Equipe), onDecrementer(Equipe), onReinitialiser
ScoreScreen                                          ← sans état, identique pour les 3 variantes
        │ état ▼ valeur                ▲ événements onIncrementer, onDecrementer
Compteur × 2                                         ← sans état
```

## Points pédagogiques

- **Observé ≠ mémorisé** : `mutableStateOf` rend une valeur observable (sa modification déclenche une
  recomposition), `remember` la conserve entre recompositions. Il faut les deux.
- **`remember` ou `rememberSaveable`** : tout ce que l'utilisateur a saisi ou choisi doit survivre à
  la rotation. `StateRestorationTester` le vérifie sans tourner l'écran.
- **State hoisting** : un composable sans état reçoit `valeur` et expose `onXxx`. Il devient
  prévisualisable (étape 5 : previews à 42 et à 0), testable (le test vérifie l'événement, pas l'état)
  et réutilisable (étape 6 : deux instances).
- **Où placer l'état ?** Au plus petit ancêtre commun de ceux qui le lisent ou le modifient : le total
  a besoin des deux scores, donc l'état monte dans `ScoreRoute`.
- **Qui décide ?** `Compteur` ne calcule jamais `valeur + 1` : il signale une intention, le
  propriétaire de l'état applique la règle (bornes).
- **Déplacer l'état sans toucher à l'écran** : `ScoreScreen` est le même aux étapes 6, 7 et 8.
  C'est le bénéfice concret du flux de données unidirectionnel.
- **`StateFlow` dans le ViewModel** : `_etat` privé et modifiable, `etat` public en lecture seule ;
  `updateAndGet` pour une mise à jour atomique ; côté Compose, `collectAsStateWithLifecycle()` arrête
  la collecte quand l'écran n'est plus visible.
- **Observation en direct** : changer d'onglet retire l'étape de la composition. Le compteur des
  étapes 3 à 7 repart de 0, celui de l'étape 8 non : le ViewModel vit plus longtemps que la composition.

## Écarts assumés

- Textes en français uniquement (`values/`) : l'internationalisation n'est pas l'objet du TP.
- `@SuppressLint("UnrememberedMutableState")` sur l'étape 2 : l'erreur est **volontaire** ; retirer
  l'annotation pour voir le lint la signaler dans l'éditeur.
- L'étape 8 n'utilise pas d'`AppContainer` ni de `Factory` : `ScoreViewModel` ne dépend que du
  `SavedStateHandle`, que la fabrique par défaut de `viewModel()` sait fournir.
