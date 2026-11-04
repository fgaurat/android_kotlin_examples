# TP 09 — Catalogue de films (corrigé)

Corrigé du TP du chapitre **Navigation** (`slides/09-navigation.md`, jour 4).

## Objectif

Construire une application à une seule Activity qui navigue entre plusieurs écrans Compose avec **Navigation Compose** et des **routes type-safe** :

- une liste de films → le détail d'un film (identifiant passé en argument, relu dans un ViewModel) ;
- des favoris partagés entre les écrans ;
- trois onglets (Films, Favoris, Paramètres) qui conservent leur état ;
- un sous-graphe `ParametresGraphe` (`Parametres` + `APropos`) ;
- en bonus : un deep link `films://detail/{id}` et une navigation adaptative (`NavigationSuiteScaffold`) pour les tablettes.

## Versions

AGP 9.5 (Kotlin intégré), Kotlin 2.2.10, Gradle 9.6, Compose BOM 2026.02.01, `minSdk 34`, `compileSdk 37`.

| Dépendance | Version | Rôle |
|---|---|---|
| `androidx.navigation:navigation-compose` | 2.10.2 | `NavHost`, `composable<T>`, `toRoute()` |
| plugin `org.jetbrains.kotlin.plugin.serialization` | 2.2.10 | routes `@Serializable` |
| `androidx.lifecycle:lifecycle-viewmodel-compose` / `-runtime-compose` | 2.10.0 | `viewModel(factory = …)`, `collectAsStateWithLifecycle` |
| `androidx.compose.material3:material3-adaptive-navigation-suite` | BOM (1.4.0) | `NavigationSuiteScaffold` (bonus) |
| `androidx.compose.material:material-icons-core` | BOM (1.7.8) | icônes `List`, `Favorite`, `Settings`… |
| `androidx.navigation:navigation-testing` (androidTest) | 2.10.2 | `TestNavHostController` |
| `org.robolectric:robolectric` (test) | 4.17 | `Bundle` réel pour `SavedStateHandle.toRoute()` en test JVM |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` (test) | 1.10.2 | `runTest`, `Dispatchers.setMain` |

## Correspondance étape → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/films/`.

| Étape | Fichier(s) |
|---|---|
| Point de départ : `Film` et `filmsFictifs` (12 films) | `data/Film.kt` |
| État partagé (favoris) et injection manuelle | `data/FilmsRepository.kt` (`StateFlow<Set<Int>>`), `data/AppContainer.kt`, `FilmsApplication.kt` (déclarée dans le manifest), `MainActivity.kt` |
| 1. Dépendances et routes | `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts`, `ui/navigation/Routes.kt` |
| 2. `NavHost`, `Films` → `DetailFilm`, callback `onFilmClick` | `ui/navigation/FilmsNavHost.kt`, `ui/films/FilmsScreen.kt`, `ui/films/FilmsViewModel.kt` |
| 3. `toRoute<DetailFilm>()` dans un ViewModel via `SavedStateHandle` | `ui/detail/DetailFilmViewModel.kt` |
| 4. `TopAppBar` : titre, retour, action Favori, `scrollBehavior` | `ui/composants/FilmsTopBar.kt` (barre commune), `ui/detail/DetailFilmScreen.kt` (`LargeTopAppBar` + `exitUntilCollapsed` + action Favori), `ui/films/FilmsScreen.kt` (`enterAlways`), `ui/favoris/FavorisScreen.kt` (`pinned`) |
| 5. `NavigationBar` à trois onglets, `saveState` / `restoreState` | `ui/navigation/Onglets.kt` (`onglets`, `estSelectionne`, `naviguerVersOnglet`), `ui/navigation/FilmsApp.kt` (`FilmsBottomBar`, mode `adaptatif = false`), `ui/favoris/*` |
| 6. Sous-graphe `ParametresGraphe` | `ui/navigation/FilmsNavHost.kt` (`NavGraphBuilder.parametresGraphe`), `ui/parametres/*` |
| 7. Diagramme de flux + IA + relecture | ce README (sections ci-dessous) |
| 8. Tests | `app/src/test/…/ui/detail/DetailFilmViewModelTest.kt`, `app/src/test/…/data/FilmsRepositoryTest.kt`, `app/src/androidTest/…/ui/navigation/NavigationTest.kt` + manipulations ci-dessous |
| 9. Bonus deep link | `navDeepLink<DetailFilm>` dans `FilmsNavHost.kt`, `<intent-filter>` dans `app/src/main/AndroidManifest.xml` |
| 9. Bonus tablettes | `FilmsAppAdaptative` dans `ui/navigation/FilmsApp.kt` (mode par défaut) |

### Deux variantes de la racine

`FilmsApp(container, adaptatif = true)` :

- `adaptatif = true` (défaut, bonus) : `NavigationSuiteScaffold` affiche une barre en bas sur téléphone, un rail sur tablette ; `NavigationSuiteType.None` sur le détail ;
- `adaptatif = false` (étape 5) : `Scaffold` + `NavigationBar` (`FilmsBottomBar`), masquée sur le détail, avec `consumeWindowInsets` comme dans le chapitre.

Les deux variantes partagent la liste `onglets`, la sélection par `hierarchy` et la même fonction `naviguerVersOnglet`. Pour tester la variante de l'étape 5, passer `adaptatif = false` dans `MainActivity`.

## Lancer le projet

```bash
./gradlew assembleDebug                 # compile l'application
./gradlew testDebugUnitTest             # tests JVM (8 tests)
./gradlew assembleDebugAndroidTest      # compile les tests instrumentés
./gradlew connectedDebugAndroidTest     # exécute les tests instrumentés (émulateur démarré)
./gradlew installDebug                  # installe sur l'émulateur
```

Ou ouvrir le dossier dans Android Studio et lancer la configuration `app`.

## Manipulations à faire (étape 8)

| Manipulation | Résultat attendu |
|---|---|
| Clic sur un film | Le détail s'ouvre, la barre d'onglets disparaît |
| Flèche de la `TopAppBar` ou retour système sur le détail | Retour à la liste, la position de défilement est conservée |
| Cœur sur le détail | Le film apparaît dans l'onglet Favoris et un cœur s'affiche dans la liste |
| Onglet Paramètres → À propos | L'onglet **Paramètres** reste sélectionné (`hierarchy`) |
| À propos → onglet Films → onglet Paramètres | On retrouve **À propos** (`saveState` / `restoreState`) |
| Re-clic sur l'onglet courant | Rien ne s'empile (`launchSingleTop`) |
| Retour système depuis Favoris ou Paramètres | Retour à Films (destination de départ), puis sortie de l'application |
| Rotation sur le détail | Même film affiché : l'`id` est dans le `SavedStateHandle`, les favoris dans le repository (lié à `Application`) |
| Défilement de la liste | La barre du haut disparaît puis revient (`enterAlwaysScrollBehavior`) |
| Émulateur tablette (ou fenêtre redimensionnée) | Rail de navigation sur le côté au lieu de la barre du bas |

Deep link (bonus), application fermée ou ouverte :

```bash
adb shell am start -W -a android.intent.action.VIEW -d "films://detail/3" fr.formation.films
```

Le détail de *La Haine* s'ouvre. Le retour mène à la liste : Navigation construit une pile synthétique avec la destination de départ sous le détail. Avec un identifiant inconnu (`films://detail/99`), l'écran affiche « Film introuvable ».

> Les favoris sont gardés en mémoire seulement : ils survivent à la rotation, mais pas à la mort du processus. La persistance (DataStore, Room) fait l'objet d'autres chapitres.

## Étape 7 : diagramme de flux

```text
CatalogueFilms — flux de navigation
Démarrage : Films

[Onglets]  Films | Favoris | Paramètres    (barre visible sauf sur DétailFilm ;
                                            rail sur tablette via NavigationSuiteScaffold)

Films ───(clic sur un film)───▶ DétailFilm(id: Int)
Favoris ─(clic sur un film)───▶ DétailFilm(id: Int)
DétailFilm ─(flèche retour)───▶ écran précédent (Films ou Favoris)
DétailFilm ─(action cœur)─────▶ ajoute / retire le film des favoris (reste sur l'écran)
Paramètres ─(« À propos »)────▶ À propos     (Paramètres + À propos = sous-graphe ParametresGraphe)
Paramètres ─(« Vider »)───────▶ vide les favoris (reste sur l'écran)

Règles
- re-cliquer sur un onglet : pas de doublon, l'état de chaque onglet est conservé
- DétailFilm relit le film via DetailFilmViewModel (id lu dans SavedStateHandle avec toRoute)
- les favoris sont un état partagé : FilmsRepository (StateFlow) fourni par AppContainer
- deep link films://detail/{id} ouvre DétailFilm ; id inconnu → « Film introuvable »
```

## Étape 7 : le prompt utilisé

> *Projet Android en Kotlin, Jetpack Compose, Material 3, Navigation Compose 2.10 avec des **routes type-safe** (`@Serializable`, `composable<T>`, `toRoute()`). Pas de routes en chaînes de caractères, pas de Hilt : les ViewModels sont créés avec `viewModelFactory` et un `AppContainer` qui expose `filmsRepository: FilmsRepository`.*
>
> *À partir du flux ci-dessous, génère :*
> *1. `Routes.kt` : une route par destination (`Films`, `DetailFilm(id: Int)`, `Favoris`, `Parametres`, `APropos`) et un `data object ParametresGraphe` pour le sous-graphe ;*
> *2. `FilmsNavHost.kt` : le `NavHost` ; les écrans reçoivent des **callbacks**, jamais le `NavController` ; le sous-graphe dans une fonction d'extension `NavGraphBuilder.parametresGraphe()` ; un deep link `films://detail/{id}` sur `DetailFilm` avec `navDeepLink<DetailFilm>(basePath = …)` ;*
> *3. `FilmsApp.kt` : le `Scaffold` avec une `NavigationBar` (onglet sélectionné via `currentBackStackEntryAsState` et `hierarchy`, navigation avec `popUpTo(findStartDestination) { saveState = true }`, `launchSingleTop`, `restoreState`), masquée sur `DetailFilm`.*
>
> *Les composables `FilmsScreen`, `DetailFilmScreen`, `FavorisScreen`, `ParametresScreen`, `AProposScreen` et leurs ViewModels (`FilmsViewModel`, `DetailFilmViewModel`, `FavorisViewModel`, `ParametresViewModel`, chacun avec `companion object { fun factory(repository) }`) existent déjà : n'en écris que les appels. Explique brièvement chaque option de `navigate` utilisée.*
>
> *[diagramme de flux ci-dessus]*

Ce prompt couvre `Routes.kt`, `FilmsNavHost.kt` et la partie `Scaffold` de `FilmsApp.kt`. Le corrigé donne la version relue de ces fichiers, à comparer avec ce que l'IA a produit. Les retouches à prévoir après la génération : sortir la logique de `navigate` dans `naviguerVersOnglet` pour la réutiliser dans le `NavigationSuiteScaffold`, ajouter `launchSingleTop` vers `APropos` (double clic), et vérifier les imports (`hasRoute`, `hierarchy`, `findStartDestination`).

## Étape 7 : la checklist de relecture appliquée au code

**Conformité de l'API**

- [x] Routes `@Serializable` et `composable<T>`, aucune route en chaîne ni `navArgument` (`Routes.kt`, `FilmsNavHost.kt`)
- [x] `SavedStateHandle.toRoute<DetailFilm>()` côté ViewModel (`DetailFilmViewModel.kt`) ; `entree.toRoute<DetailFilm>()` est aussi utilisé dans le test instrumenté
- [x] Icônes `Icons.AutoMirrored.Filled.ArrowBack`, `…List`, `…KeyboardArrowRight` (pas les versions dépréciées)
- [x] `collectAsStateWithLifecycle` partout, jamais `collectAsState`
- [x] Pas de `hiltViewModel()` : `viewModel(factory = XxxViewModel.factory(container.filmsRepository))`
- [x] Dépendances en versions réelles résolues par Gradle ; plugin `kotlin.serialization` présent

**Conformité au flux**

- [x] Destination de départ = `Films`
- [x] Onglet sélectionné via `hierarchy` : `Paramètres` reste actif sur `APropos` (vérifié par `NavigationTest`)
- [x] `popUpTo(findStartDestination) { saveState = true }` + `launchSingleTop` + `restoreState` sur les onglets (`Onglets.kt`)
- [x] Barre masquée sur `DetailFilm` uniquement (`hasRoute<DetailFilm>()` dans `FilmsApp.kt`)
- [x] Arguments légers : seul `id: Int` transite, le film est relu dans le repository

**Tester à la main** : voir le tableau des manipulations ci-dessus (retour système, rotation, re-clic, deep link).

## Les tests

### Tests JVM (`app/src/test`)

- `DetailFilmViewModelTest` (5 tests) : lecture de l'`id`, film introuvable, bascule du favori, favori ajouté depuis un autre écran, bascule sans effet sur un film inconnu. Le `SavedStateHandle` est construit à la main : `SavedStateHandle(mapOf("id" to 3))` (la clé est le nom de la propriété de `DetailFilm`).
- `FilmsRepositoryTest` (3 tests) : unicité des identifiants, recherche, ajout / retrait / vidage des favoris.

**Pourquoi Robolectric ?** `SavedStateHandle.toRoute()` décode les arguments en passant par un `android.os.Bundle`. En test JVM pur, les classes Android sont des bouchons et le test échoue avec `Method putInt in android.os.BaseBundle not mocked`. Solution retenue : exécuter la classe de test avec `@RunWith(RobolectricTestRunner::class)`. Robolectric fournit un vrai `Bundle` ; le ViewModel garde son code de production (`toRoute` dans le ViewModel, comme dans le chapitre) et le test construit toujours son `SavedStateHandle` à la main.

Détails de configuration :
- `app/src/test/resources/robolectric.properties` fixe `sdk=36` (niveau d'API simulé) ;
- `testOptions.unitTests.all { jvmArgs(...) }` dans `app/build.gradle.kts` ouvre les internes du JDK : Gradle 9.6 tourne sur le JDK 25, et Robolectric en a besoin (`--add-exports java.base/jdk.internal.access`) ;
- le premier lancement télécharge l'image Android de Robolectric (quelques centaines de Mo).

Alternative possible sans Robolectric : passer la route déjà décodée au ViewModel (`DetailFilmViewModel(route: DetailFilm, …)`) et appeler `toRoute()` dans la factory. Le test devient trivial, mais on s'écarte de l'énoncé (« dans un ViewModel via `SavedStateHandle` »).

### Test instrumenté (`app/src/androidTest`)

`NavigationTest` (5 tests) injecte un `TestNavHostController` (avec `ComposeNavigator`) dans `FilmsApp` puis vérifie : la destination de départ, l'aller-retour liste → détail avec l'`id` décodé, le favori visible dans l'onglet Favoris, la sélection de l'onglet Paramètres sur À propos et la restauration de la pile, puis l'absence de doublon au re-clic. Lancement : `./gradlew connectedDebugAndroidTest`, avec un émulateur démarré.

## Points pédagogiques

- **Single Activity** : chaque écran est une destination ; `MainActivity` ne fait qu'appeler `FilmsApp`.
- **Les écrans ne connaissent pas le `NavController`** : ils exposent des callbacks (`onFilmClick`, `onRetour`, `onAProposClick`). Seul `FilmsNavHost` décide où aller, ce qui rend les écrans prévisualisables (`@Preview`) et testables.
- **Des identifiants, pas des objets** : `DetailFilm(id: Int)`. Le détail fonctionne aussi depuis un deep link ou après une recréation, puisqu'il relit le film dans le repository.
- **ViewModel lié à l'entrée de la pile** : `viewModel()` dans `composable<DetailFilm>` crée un ViewModel par entrée, détruit quand on la quitte.
- **État partagé** : une seule instance de `FilmsRepository`, avec un `StateFlow`, observée par trois ViewModels. Ajouter un favori depuis le détail met à jour la liste, l'onglet Favoris et le compteur des Paramètres.
- **`hierarchy` + graphe imbriqué** : un onglet pointe vers un graphe (`ParametresGraphe`), pas vers un écran. Il reste sélectionné sur toutes les destinations du graphe.
- **`saveState` / `restoreState`** : chaque onglet garde sa propre pile (À propos, défilement).
- **`scrollBehavior`** : trois comportements montrés (`enterAlways` sur la liste, `pinned` sur les favoris, `exitUntilCollapsed` avec `LargeTopAppBar` sur le détail).
- **Insets** : `consumeWindowInsets` évite la double marge entre la barre de navigation et le `Scaffold` de chaque écran.
- **Adaptatif** : `NavigationSuiteScaffold` réutilise exactement la même logique de navigation, seule la présentation change.
