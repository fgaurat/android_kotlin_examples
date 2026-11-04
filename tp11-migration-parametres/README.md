# TP 11 — Migrer `ParametresActivity` vers Compose (corrigé)

Corrigé du TP « Migrer `ParametresActivity` » du chapitre **Composants historiques Android**
(`slides/11-composants-historiques.md`, jour 5).

## Objectif

Migrer un écran historique (layout XML + View Binding + `AppCompatActivity`) vers Jetpack Compose **sans
toucher au ViewModel**, en s'appuyant sur des tests écrits avant la migration.

Ce projet contient **trois états** du même écran :

| État | Où | Compilé par défaut ? |
|---|---|---|
| **Après migration** (état final) | `app/` | Oui |
| **Avant migration** (XML + View Binding) | `avant-migration/` | Non (calque à appliquer) |
| **Bonus « bottom-up »** (XML + `ComposeView` pour le seul interrupteur) | `bonus-bottom-up/` | Non (calque à appliquer) |

Les dossiers `avant-migration/` et `bonus-bottom-up/` reproduisent l'arborescence du projet : ils contiennent
uniquement les fichiers qui **diffèrent** de l'état final. Ils sont hors du build Gradle.

## Correspondance étapes → fichiers

Chemins relatifs à `app/src/` (sauf mention contraire).

| Étape | Contenu | Fichier(s) |
|---|---|---|
| 1 | `Unite`, `ParametresUiState`, `ParametresViewModel` | `main/java/fr/formation/parametres/Unite.kt`, `ParametresUiState.kt`, `ParametresViewModel.kt` |
| 1 | Écran XML d'origine, View Binding, AppCompat, Material Components | `avant-migration/` (voir plus bas) |
| 2 | Tests unitaires du ViewModel, **avant** migration | `test/…/ParametresViewModelTest.kt` |
| 3 et 4 | `ParametresScreen(etat, onUniteChoisie, onThemeSombreChange)` sans état, Material 3, `selectableGroup`, `selectable(role = Role.RadioButton)`, textes en ressources, deux `@Preview` | `main/…/ParametresScreen.kt`, `main/res/values/strings.xml` |
| 5 | `setContent { … collectAsStateWithLifecycle() … }` | `main/…/ParametresActivity.kt` |
| 6 | Tests du ViewModel **inchangés**, plus des tests UI Compose (clic sur « °F ») | `test/…/ParametresViewModelTest.kt`, `androidTest/…/ParametresScreenTest.kt`, `androidTest/…/ParametresActivityTest.kt` |
| 7 | Suppression de `activity_parametres.xml`, du binding, d'AppCompat et de Material Components | Absents de `app/` (conservés dans `avant-migration/`) |
| 8 (bonus) | Seul l'interrupteur en Compose via une `ComposeView` | `bonus-bottom-up/` |

## Lancer

```bash
./gradlew installDebug                 # application (état final)
./gradlew testDebugUnitTest            # 6 tests du ViewModel
./gradlew connectedDebugAndroidTest    # 7 tests UI Compose (émulateur ou appareil)
```

Dans Android Studio, ouvrir `ParametresScreen.kt` en mode **Split** pour voir les deux aperçus (clair et sombre).

## L'état final (`app/`)

- `ParametresActivity` hérite de `ComponentActivity` et appelle `setContent`. Plus de layout XML, de binding
  ni de `repeatOnLifecycle` manuel : `collectAsStateWithLifecycle()` collecte l'état seulement quand l'écran
  est au moins `STARTED`.
- `ParametresScreen` est **sans état** : il reçoit `ParametresUiState` et remonte deux lambdas. Il se teste et
  s'affiche en aperçu sans ViewModel.
- Le `RadioGroup` devient une `Column(Modifier.selectableGroup())`. Chaque ligne entière porte
  `selectable(selected, onClick, role = Role.RadioButton)` et le `RadioButton` reçoit `onClick = null` :
  la ligne est la seule cible tactile, d'au moins 48 dp de haut, et TalkBack annonce « case d'option,
  sélectionnée, 1 sur 2 ».
- Le `MaterialSwitch` devient `LigneThemeSombre` : une ligne `toggleable(role = Role.Switch)` contenant un
  `Text` et un `Switch(onCheckedChange = null)`, selon le même principe.
- Tous les textes viennent de `strings.xml` (`stringResource`), la typographie du thème
  (`titleMedium` remplace `?attr/textAppearanceTitleMedium`).
- **Le thème sombre s'applique réellement** : `ParametresTheme(darkTheme = etat.themeSombre)` enveloppe l'écran.
  Les icônes des barres système suivent aussi le thème choisi (`enableEdgeToEdge` avec un `SystemBarStyle`).
  La version XML fournie, elle, se contentait de cocher l'interrupteur, sans changer le thème.

### Tests

- `ParametresViewModelTest` (6 tests, JVM) : écrits à l'étape 2 et **jamais modifiés** depuis. Ils passent avant
  et après la migration : c'est la preuve que la logique n'a pas bougé.
- `ParametresScreenTest` (4 tests, `createComposeRule`) : clic sur « °F » qui remonte `Unite.FAHRENHEIT` et
  sélectionne la ligne, rôle `RadioButton` exposé à l'accessibilité, clic sur la ligne du thème sombre,
  interrupteur qui reflète l'état.
- `ParametresActivityTest` (3 tests, `createAndroidComposeRule<ParametresActivity>`) : l'écran complet branché
  sur le vrai ViewModel, y compris la survie du choix à une recréation de l'Activity (rotation).

Le test cherche « °F » avec `onNodeWithText("°F", substring = true)` : le libellé complet est
« Fahrenheit (°F) ».

## Montrer la version **avant migration**

`avant-migration/` contient le projet d'origine (*Empty Views Activity*) :

| Fichier | Rôle |
|---|---|
| `app/src/main/res/layout/activity_parametres.xml` | Layout fourni par l'énoncé |
| `app/src/main/java/fr/formation/parametres/ParametresActivity.kt` | Activity fournie (View Binding, `repeatOnLifecycle`) |
| `app/src/main/res/values/themes.xml` | Thème `Theme.Material3.DayNight.NoActionBar` (Material Components), requis par `MaterialSwitch` et `?attr/textAppearanceTitleMedium` |
| `app/build.gradle.kts` | `buildFeatures { viewBinding = true }`, AppCompat, Material Components, `activity-ktx`, `lifecycle-runtime-ktx`, sans Compose |
| `gradle/libs.versions.toml` | Catalogue de versions correspondant |

`Unite.kt`, `ParametresUiState.kt`, `ParametresViewModel.kt`, `strings.xml`, le manifeste et les tests unitaires
sont **communs** aux deux versions : ils ne sont pas dupliqués.

Pour reconstituer l'état « avant » dans une copie du projet :

```bash
cp -R tp11-migration-parametres /tmp/parametres-avant && cd /tmp/parametres-avant
rm -rf app/src/main/java/fr/formation/parametres/ui \
       app/src/main/java/fr/formation/parametres/ParametresScreen.kt \
       app/src/androidTest/java/fr/formation/parametres/*.kt   # tests Compose
cp -R avant-migration/. .
./gradlew installDebug testDebugUnitTest
```

Les mêmes `ParametresViewModelTest` passent dans cet état : c'est le point de départ de l'étape 6.

**Écart avec le code fourni** : `ParametresActivity` (avant et bonus) contient une petite méthode
`appliquerMargesSysteme()`. Avec `targetSdk` 35 ou plus, l'affichage bord à bord est imposé : sans elle, le
contenu passe sous la barre d'état. Le modèle *Empty Views Activity* d'Android Studio génère le même
`ViewCompat.setOnApplyWindowInsetsListener`. En Compose, `Scaffold` et son `innerPadding` s'en chargent.

## Bonus : étape intermédiaire « bottom-up »

`bonus-bottom-up/` montre l'écran XML dans lequel **seul l'interrupteur** est passé en Compose :

| Fichier | Rôle |
|---|---|
| `app/src/main/res/layout/activity_parametres.xml` | Le `MaterialSwitch` est remplacé par une `androidx.compose.ui.platform.ComposeView` (`@+id/composeThemeSombre`) |
| `app/src/main/java/fr/formation/parametres/ParametresActivity.kt` | `binding.composeThemeSombre.setContent { ParametresTheme { … LigneThemeSombre(…) } }` ; le `RadioGroup` reste géré en XML |
| `app/build.gradle.kts`, `gradle/libs.versions.toml` | Compose **et** `viewBinding`, AppCompat, Material Components |
| `app/src/main/res/values/themes.xml` | Thème Material Components de l'Activity AppCompat |

Le composable inséré, `LigneThemeSombre`, est **le même** que celui de l'écran final : on migre un composant,
puis on le réutilise tel quel quand l'écran entier passe en Compose.

Pour reconstituer cet état :

```bash
cp -R tp11-migration-parametres /tmp/parametres-bonus && cd /tmp/parametres-bonus
cp -R bonus-bottom-up/. .
rm app/src/androidTest/java/fr/formation/parametres/ParametresActivityTest.kt  # cible l'écran 100 % Compose
./gradlew installDebug testDebugUnitTest
```

Points à discuter :

- La `ComposeView` collecte elle-même l'état (`collectAsStateWithLifecycle`) : l'interrupteur n'est plus mis
  à jour dans le `collect` de l'Activity.
- Stratégie de libération de la composition : la stratégie par défaut convient dans une Activity ; dans un
  Fragment, il faut `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed`.
- **Deux thèmes coexistent** : le thème XML (Material Components) et `ParametresTheme` (Compose). Leurs
  couleurs peuvent différer légèrement, en particulier avec les couleurs dynamiques : c'est l'un des coûts d'un
  écran hybride, qui disparaît à la fin de la migration.

## Erreurs ou approximations typiques de l'IA (à relire)

- `RadioButton(onClick = { … })` **et** une ligne cliquable : deux cibles, deux nœuds sémantiques ; ou au
  contraire un `RadioButton` seul, avec une zone tactile réduite au rond.
- Oubli de `selectableGroup()` et du `role` : l'écran fonctionne, mais TalkBack n'annonce ni groupe ni rôle.
- Textes en dur (`"°C"`) au lieu de `stringResource`, couleurs codées en dur au lieu du thème.
- `collectAsState()` au lieu de `collectAsStateWithLifecycle()`, ou imports Material 2
  (`androidx.compose.material`).
- Composable **avec état** (`viewModel()` appelé dans `ParametresScreen`) : il n'est plus testable ni
  prévisualisable sans ViewModel.
- Modification « au passage » du ViewModel (ajout d'un `SavedStateHandle`, renommage) : interdit par l'énoncé,
  et les tests de l'étape 2 doivent rester verts sans modification.
- Thème sombre stocké dans l'état mais jamais passé à `ParametresTheme`.

## Limites assumées

- Le ViewModel reste celui de l'étape 1 : l'état survit à une rotation, mais pas à la mort du processus ni à un
  redémarrage. Il faudrait DataStore (préférences persistantes), hors du périmètre du TP, qui interdit de modifier
  le ViewModel.
- Le thème sombre est désactivé par défaut (`themeSombre = false`), même si le système est en mode sombre.

## Versions

AGP 9.5, Kotlin 2.2.10, Compose BOM 2026.02.01, Lifecycle 2.10.0 ; calques : AppCompat 1.8.0,
Material Components 1.14.0.
