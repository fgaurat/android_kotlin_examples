# TP07 — Liste de tâches en MVVM (corrigé)

Jour 3, chapitre « Architecture MVVM » (`slides/07-mvvm.md`).

## Objectif

Construire un écran de liste de tâches selon le guide d'architecture Android : couches `data` / `domain` / `ui`,
repository source unique de vérité exposant un `Flow`, injection manuelle (`AppContainer`), ViewModel qui combine
plusieurs flux en un **état d'écran** (`StateFlow`), UI Compose séparée en `Route` (branchée au ViewModel) et
`Screen` (sans état), puis étude de la survie de l'état à la rotation et à la mort du processus.
Les données sont **en mémoire** : pas de réseau.

## Correspondance étape → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/taches/`.

| Étape de l'énoncé | Fichier(s) |
|---|---|
| 1. Packages, `Tache`, interface `TacheRepository` | `domain/Tache.kt` (`Tache`, `Priorite`), `data/TacheRepository.kt` |
| 2. `TacheRepositoryEnMemoire` (`MutableStateFlow` + `delay(300)`) | `data/TacheRepositoryEnMemoire.kt` |
| 3. `AppContainer` + classe `Application` dans le manifest | `AppContainer.kt`, `TachesApplication.kt`, `app/src/main/AndroidManifest.xml` (`android:name=".TachesApplication"`) |
| 4. `TachesUiState` et `TachesViewModel` (`combine` + `stateIn(WhileSubscribed(5_000))`) | `ui/taches/TachesUiState.kt`, `ui/taches/Filtre.kt`, `ui/taches/TachesViewModel.kt` |
| 5. `debounce(300)` sur la recherche | `ui/taches/TachesViewModel.kt` (`rechercheAppliquee`) |
| 6. `TachesRoute` / `TachesScreen` | `ui/taches/TachesRoute.kt`, `ui/taches/TachesScreen.kt` (formulaire d'ajout, filtres, recherche, liste, suppression, prévisualisations), `MainActivity.kt` |
| 7. Rotation, mort du processus, `SavedStateHandle` | `TachesViewModel` (`savedStateHandle.getStateFlow(CLE_FILTRE, …)`, `Factory` avec `createSavedStateHandle()`) — manipulations ci-dessous |
| 8. Bonus `LiveData` + `observeAsState()` | `TachesViewModel.restantes` (`asLiveData()`), `TachesRoute` (`observeAsState(initial = 0)`), comparaison avec `restantesEnStateFlow` |

Tests unitaires (`app/src/test/java/fr/formation/taches/`) :

| Fichier | Ce qui est vérifié |
|---|---|
| `MainDispatcherRule.kt` | Règle JUnit du chapitre 08 : remplace `Dispatchers.Main` |
| `data/FakeTacheRepository.kt` | Faux repository instantané, utilisé par les tests du ViewModel |
| `data/TacheRepositoryEnMemoireTest.kt` | Latence de 300 ms en **temps virtuel**, ids, bascule, suppression |
| `ui/taches/FiltreTest.kt` | `Filtre.accepte` |
| `ui/taches/TachesViewModelTest.kt` | Chargement initial, filtres, **debounce** (rien à 299 ms, filtré à 300 ms), filtre + recherche combinés, filtre écrit dans le `SavedStateHandle` et **restauré** après mort du processus, validation de l'ajout, bascule, suppression, compteur en `StateFlow` |
| `ui/taches/RestantesLiveDataTest.kt` | Bonus : le compteur en `LiveData` (nécessite `InstantTaskExecutorRule`) |

Test instrumenté (`app/src/androidTest/…/TachesScreenTest.kt`) : `TachesScreen` seul, avec un état et des callbacks
(à lancer sur un émulateur).

## Lancer le projet

Ouvrir `tp07-taches-mvvm` dans Android Studio et lancer sur un émulateur, ou :

```bash
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest --console=plain
./gradlew installDebug          # avec un émulateur démarré
```

Au démarrage, un indicateur de chargement s'affiche 300 ms (latence simulée), puis cinq tâches d'exemple.

## Architecture

```text
TachesApplication ──crée──▶ DefaultAppContainer ──lazy──▶ TacheRepositoryEnMemoire (MutableStateFlow)
                                                                   │ taches : Flow<List<Tache>>
TachesRoute ── viewModel(factory = TachesViewModel.Factory) ──▶ TachesViewModel
   │  collectAsStateWithLifecycle()                                 │ combine(taches, filtre, recherche debounced)
   │  observeAsState() (bonus)                                      │   .stateIn(WhileSubscribed(5_000))
   ▼                                                                ▼
TachesScreen(etat, recherche, restantes, callbacks)  ◀── état ── etat : StateFlow<TachesUiState>
           └──────────── événements : onFiltreChange, onRechercheChange, ajouter, basculer, supprimer ──▶
```

## Manipulations pas à pas (étape 7)

### a) Première version : filtre dans un `MutableStateFlow`

Pour reproduire la situation de départ, remplacer temporairement dans `TachesViewModel` :

```kotlin
// Version naïve : le filtre vit seulement dans le ViewModel
private val filtre = MutableStateFlow(Filtre.Toutes)
fun onFiltreChange(nouveau: Filtre) { filtre.value = nouveau }
```

### b) Rotation

1. Ajouter une tâche, cocher une tâche, choisir le filtre **Faites**, taper « view » dans la recherche.
2. Faire tourner l'émulateur (boutons de rotation de la barre latérale, ou `⌘←` / `Ctrl+←`).
3. **Tout est conservé** : l'`Activity` est recréée, mais `viewModel()` renvoie la **même** instance de
   `TachesViewModel` (stockée dans le `ViewModelStore`) ; le repository vit dans l'`AppContainer` de l'`Application`.
   `WhileSubscribed(5_000)` évite même de relancer le flux pendant les quelques millisecondes sans collecteur.
   Le titre en cours de saisie dans le formulaire survit grâce à `rememberSaveable`.

### c) Mort du processus

1. Mettre l'application en arrière-plan (bouton **Accueil** : `am kill` ne tue qu'un processus en arrière-plan).
2. Tuer le processus :

   ```bash
   adb shell am kill fr.formation.taches
   adb shell pidof fr.formation.taches     # plus rien : le processus est mort
   ```

   Variante : *Options pour les développeurs → Ne pas conserver les activités*, ou le bouton *Terminate
   application* de l'onglet Logcat d'Android Studio **après** être passé en arrière-plan.
3. Revenir dans l'application par l'écran des applications récentes (et non par l'icône, qui démarrerait une
   nouvelle tâche).

| Ce qui était à l'écran | Version naïve | Avec `SavedStateHandle` |
|---|---|---|
| Filtre **Faites** | ❌ revient à *Toutes* | ✅ restauré |
| Texte de la recherche | ❌ perdu (`MutableStateFlow` du ViewModel) | ❌ perdu (volontairement non sauvegardé, à faire en exercice) |
| Titre en cours dans le formulaire | ✅ (`rememberSaveable`) | ✅ |
| Tâches ajoutées, cochées, supprimées | ❌ retour aux 5 tâches d'exemple | ❌ idem |

Pourquoi : le ViewModel et le repository en mémoire meurent avec le processus. Seul le **`Bundle`** d'état
sauvegardé par le système survit ; `SavedStateHandle` (comme `rememberSaveable`) y écrit. On n'y met que de petites
données qui permettent de **reconstruire** l'écran (un filtre, un identifiant, un texte), jamais la liste de tâches :
la persistance des données relève d'une base Room ou de DataStore (jour 4).

### d) Avec `SavedStateHandle` (version du corrigé)

```kotlin
private val filtre: StateFlow<Filtre> = savedStateHandle.getStateFlow(CLE_FILTRE, Filtre.Toutes)
fun onFiltreChange(nouveau: Filtre) { savedStateHandle[CLE_FILTRE] = nouveau }
```

`Filtre` est une `enum`, donc `Serializable` : elle peut aller dans un `Bundle`. La `Factory` fournit le
`SavedStateHandle` avec `createSavedStateHandle()`. Recommencer la manipulation c) : le filtre est restauré, et il
est utilisé dès la valeur initiale de `stateIn` (pas de clignotement sur *Toutes*).
Le test `après la mort du processus, le filtre est restauré` simule ce scénario en créant le ViewModel avec
`SavedStateHandle(mapOf("filtre" to Filtre.AFaire))`.

## Bonus (étape 8) : LiveData ou StateFlow ?

```kotlin
val restantes: LiveData<Int> = repository.taches.map { l -> l.count { !it.faite } }.asLiveData()
val restantesEnStateFlow: StateFlow<Int> = repository.taches.map { l -> l.count { !it.faite } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
```

| | `LiveData` + `observeAsState()` | `StateFlow` + `collectAsStateWithLifecycle()` |
|---|---|---|
| Valeur initiale | Aucune : `observeAsState(initial = 0)` obligatoire, sinon `Int?` | Toujours une valeur (`0` ici) |
| Cycle de vie | Natif (observateurs actifs seulement) | Apporté par `collectAsStateWithLifecycle` |
| Opérateurs | `map`, `switchMap` | Tous ceux de `Flow` (`combine`, `debounce`…) : impossible d'écrire l'état de l'écran en `LiveData` aussi simplement |
| Tests JVM | `InstantTaskExecutorRule` + `observeForever` (voir `RestantesLiveDataTest`) | `runTest` + `MainDispatcherRule` |
| Couches data/domain | Non (dépend d'Android) | Oui (Kotlin pur) |

Les deux affichent la même valeur ; l'écran utilise la version `LiveData` pour le bonus.

## Points pédagogiques

- **Source unique de vérité** : seul `TacheRepositoryEnMemoire` modifie la liste (`_taches.update { … }`) ;
  le ViewModel ne garde aucune copie, il **dérive** l'état par `combine`. Après `basculer`, l'écran se met à jour
  tout seul parce que le flux du repository émet.
- **Deux flux pour la recherche** : `recherche` (texte brut, pour que le champ réagisse sans délai) et
  `TachesUiState.recherche` (texte appliqué après `debounce(300)`). Passer le texte du champ par
  `combine` + `stateIn` + `debounce` exposerait le champ à des mises à jour asynchrones (saisie retardée, curseur qui saute).
- **`debounce` et valeur initiale** : `debounce(300)` retarde **aussi** la première valeur `""`, donc le premier
  affichage. `onStart { emit(_recherche.value.trim()) }` + `distinctUntilChanged()` publient la valeur initiale
  immédiatement. Le test du debounce vérifie qu'à 299 ms rien n'est filtré, et qu'à 300 ms le filtre s'applique.
- **`TachesUiState.chargement`** : champ ajouté aux trois de l'énoncé, vrai seulement dans la valeur initiale de
  `stateIn` (avant la première émission du repository, 300 ms).
- **Validation dans le ViewModel** (`ajouter` ignore un titre vide et retire les espaces) : testable sans UI.
- **`TachesScreen` sans ViewModel** : deux `@Preview` (liste, recherche sans résultat) et un test Compose qui
  vérifie les callbacks, impossible si l'écran créait lui-même son ViewModel.
- **Temps virtuel** : `delay(300)` du repository et `debounce(300)` se testent instantanément avec `runTest`.
- **Revue par l'IA** (encadré de l'énoncé) : soumettre `TachesViewModel.kt` avec la question
  « *Ce ViewModel respecte-t-il le guide d'architecture Android ?* » et confronter la réponse aux points ci-dessus.

## Versions

AGP 9.5, Kotlin 2.2.10, Compose BOM 2026.02.01, AndroidX Lifecycle **2.11.0** (`lifecycle-viewmodel-compose`,
`lifecycle-runtime-compose`, `lifecycle-livedata` ; le modèle partait de 2.6.1), `runtime-livedata` et
`material-icons-core` via le BOM, kotlinx-coroutines-test 1.11.0, `androidx.arch.core:core-testing` 2.2.0.
