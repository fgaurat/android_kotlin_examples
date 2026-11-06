# TP08 — Tests, pas à pas

Chapitre « Créer une suite de tests » (`slides/08-tests.md`).

Une progression en **7 étapes** (0 à 6), du premier `assertEquals` jusqu'au test d'un écran Compose. Chaque
étape ajoute **une notion** par rapport à la précédente. On reprend les exemples des slides : `Todo`,
`TodosViewModel`, `RechercheViewModel`, `FakeTodoRepository`.

Le corrigé du TP du chapitre (le panier) est **`tp08-panier-tests`**.

## Organisation : du code, et ses tests

Ici, pas de `MainActivityN` à décommenter : **on lance les tests**. Chaque étape a son package `sujetN` :

| Où | Quoi |
|---|---|
| `app/src/main/java/fr/formation/testsujets/sujetN/` | le **code testé** (un fichier, commenté) |
| `app/src/test/java/fr/formation/testsujets/sujetN/` | ses **tests JVM** : rapides, sans émulateur (étapes 0 à 5) |
| `app/src/androidTest/java/fr/formation/testsujets/sujet6/` | les **tests UI** Compose : sur émulateur (étape 6) |
| `app/src/test/java/fr/formation/testsujets/MainDispatcherRule.kt` | la règle partagée par les étapes 3 à 5 |

Règle à retenir : un test est dans le **même package** que le code qu'il teste, sous `src/test/` (JVM) ou
`src/androidTest/` (appareil). Chaque fichier du code testé précise où ranger ses fichiers dans un vrai projet.

## Lancer

- **Android Studio** : flèche verte à côté d'une classe ou d'un test, ou clic droit sur un package `sujetN`
  → *Run Tests in 'sujetN'*.
- **Ligne de commande** :

  ```bash
  ./gradlew testDebugUnitTest                                            # étapes 0 à 5 (42 tests)
  ./gradlew testDebugUnitTest --tests "fr.formation.testsujets.sujet3.*" # une seule étape
  ./gradlew connectedDebugAndroidTest                                    # étape 6, émulateur lancé
  ```

- Rapport HTML : `app/build/reports/tests/testDebugUnitTest/index.html`.
- L'application elle-même n'affiche que l'écran testé à l'étape 6 (`MainActivity6`), pour le voir.

## Les étapes

| Étape | Code testé | Notion | Tests |
|---|---|---|---|
| `sujet0` | `TodoRegles` : fonctions pures | `@Test`, `assertEquals(attendu, obtenu)`, Arrange / Act / Assert, noms entre backticks, cas limites (liste vide) | 8 |
| `sujet1` | `nouvelleTodo`, `TodoDto.versDomaine()`, `Priorite.depuis` | `assertThrows`, `@Before`, test paramétré (`@RunWith(Parameterized::class)`), mapping DTO → domaine | 12 |
| `sujet2` | `AjouterTodo(repository)` : fonctions `suspend` | `runTest` ; doublures : **fake** (vérifie l'état) et **mock** MockK (`coEvery`, `coVerify`, `coVerifyOrder`) | 8 |
| `sujet3` | `TodosViewModel` | `MainDispatcherRule`, `StandardTestDispatcher`, `advanceUntilIdle`, temps virtuel (`currentTime`), fake avec `delay` | 6 |
| `sujet4` | `RechercheViewModel` : `debounce(300)` + `stateIn(WhileSubscribed)` | `advanceTimeBy`, collecteur dans `backgroundScope`, `coVerify(exactly = 0)` | 4 |
| `sujet5` | `TodosViewModel` + messages (`Channel`) | **Turbine** : `test { awaitItem() }`, `expectNoEvents`, `turbineScope` + `testIn` pour deux flows | 4 |
| `sujet6` | `TodosEcran` sans état (state hoisting) | `createComposeRule`, `onNodeWithText` / `ContentDescription` / `Tag`, `performClick`, `assertIsOn`, `printToLog` | 5 (émulateur) |

## Conseils de démonstration

- **Voir un test échouer** avant de le croire : dans chaque étape, casser une ligne du code testé et relancer.
  Par exemple, à l'étape 0, `count { it.terminee }` dans `restantes`. Lire le message : attendu / obtenu.
- **Étape 1** : ajouter une ligne fausse au tableau du test paramétré. Le rapport dit quel cas échoue
  (`"urgente" → HAUTE`).
- **Étape 2** : écrire le même test avec le fake puis avec le mock, et comparer. Le fake vérifie le résultat,
  le mock vérifie les appels.
- **Étape 3** : retirer `@get:Rule` et lire l'erreur (`Module with the Main dispatcher is missing`). Montrer
  que `currentTime` vaut 1 000 alors que le test dure quelques millisecondes.
- **Étape 4** : passer `advanceTimeBy(2)` à `advanceTimeBy(1)`. Le test échoue, parce que le délai de 300 ms
  n'est pas encore écoulé.
- **Étape 5** : passer la règle en `MainDispatcherRule()` (`UnconfinedTestDispatcher`) et la latence du fake à
  0. Le premier `awaitItem()` reçoit directement `Succes` : `Chargement` n'a jamais été observable.
- **Étape 6** : Logcat, filtre `ARBRE`, pour lire l'arbre sémantique. Renommer « Réessayer » dans l'écran
  casse le test, comme pour un utilisateur qui ne trouverait plus le bouton.

## Aller plus loin

- **Couverture** : dans `app/build.gradle.kts`, `buildTypes { debug { enableUnitTestCoverage = true } }`, puis
  `./gradlew createDebugUnitTestCoverageReport`. Voir `tp08-panier-tests`, étape 8.
- **Tests UI sur la JVM** : avec Robolectric, les tests de l'étape 6 peuvent tourner sans émulateur. C'est
  hors périmètre ici.

## Dépendances

| Dépendance | Version | Étapes |
|---|---|---|
| `junit:junit` | 4.13.2 | toutes |
| `kotlinx-coroutines-test` | 1.11.0 | 2 à 5 |
| `io.mockk:mockk` | 1.14.11 | 2, 4 |
| `app.cash.turbine:turbine` | 1.2.1 | 5 |
| `androidx.compose.ui:ui-test-junit4` + `ui-test-manifest` | BOM | 6 |

## Choix assumés

- **Le code testé est volontairement petit** et dupliqué d'une étape à l'autre (chaque `sujetN` redéclare
  `Todo`, `TodoRepository`…), pour qu'une étape se lise sans aller voir ailleurs.
- **JUnit 4**, comme les modèles Android Studio et les slides. JUnit 5 demande un plugin supplémentaire sur
  Android.
