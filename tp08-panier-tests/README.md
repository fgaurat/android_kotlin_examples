# TP 08 — Tester un panier (corrigé)

Corrigé du TP « Tester un panier » du chapitre **Créer une suite de tests** (`slides/08-tests.md`, jour 3).

## Objectif

À partir du code fourni par l'énoncé (`Produit`, `Panier`, `ProduitItem`, `CatalogueRepository`,
`CatalogueUiState`, `CatalogueViewModel`), écrire une suite de tests complète :

- tests unitaires du domaine (JUnit 4, `assertThrows`, test paramétré) ;
- tests du ViewModel avec un **mock** MockK, puis avec un **fake** en temps virtuel et avec **Turbine** ;
- test UI Compose de `ProduitItem` (`createComposeRule`, `printToLog`) ;
- mesure de la couverture (JaCoCo) ;
- bonus : cas limites trouvés avec l'IA (locale, `HttpException`).

Le code fourni est copié **tel quel** dans `src/main`. Un petit écran (`CatalogueScreen`) l'utilise pour que
l'application soit lançable.

## Correspondance étapes → fichiers

Chemins relatifs à `app/src/`.

| Étape | Contenu | Fichier(s) |
|---|---|---|
| Code fourni | Domaine, repository, état, ViewModel, composable | `main/java/fr/formation/panier/domain/Produit.kt`, `domain/Panier.kt`, `data/CatalogueRepository.kt`, `ui/CatalogueUiState.kt`, `ui/CatalogueViewModel.kt`, `ui/ProduitItem.kt` |
| Écran de démo | Catalogue + panier + code promo | `main/…/ui/CatalogueScreen.kt`, `main/…/MainActivity.kt`, `main/…/data/CatalogueRepositoryEnMemoire.kt` |
| 1 | MockK, `kotlinx-coroutines-test`, Turbine | `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| 2 | Tests de `Panier` (ajout, cumul, `assertThrows`, retrait, `nombreArticles`) | `test/…/domain/PanierTest.kt` |
| 3 | `total()` en test paramétré (sans code, code invalide, 49,99 €, 50 € pile, 80 €) | `test/…/domain/PanierTotalParametreTest.kt` |
| 4 | `MainDispatcherRule` | `test/…/MainDispatcherRule.kt` |
| 5 | ViewModel + MockK (`coEvery … returns`, `throws IOException()`, `coVerify`) | `test/…/ui/CatalogueViewModelMockkTest.kt` |
| 6 | `FakeCatalogueRepository` avec `delay(1_000)`, temps virtuel puis Turbine | `test/…/ui/FakeCatalogueRepository.kt`, `test/…/ui/CatalogueViewModelFakeTest.kt` |
| 7 | Test UI de `ProduitItem` + `printToLog` | `androidTest/…/ui/ProduitItemTest.kt` |
| 8 | Couverture JaCoCo + tests des branches restantes | `app/build.gradle.kts` (`enableUnitTestCoverage`), `test/…/domain/PanierCasLimitesTest.kt` |
| 9 (bonus) | Locale et `HttpException` : tests qui montrent le défaut + corrections | `main/…/bonus/`, `test/…/bonus/`, `androidTest/…/bonus/` |

## Lancer

```bash
# Application
./gradlew installDebug

# Tests unitaires (JVM, quelques secondes)
./gradlew testDebugUnitTest
# rapport : app/build/reports/tests/testDebugUnitTest/index.html

# Tests instrumentés (émulateur ou appareil connecté)
./gradlew connectedDebugAndroidTest
# rapport : app/build/reports/androidTests/connected/debug/index.html

# Couverture JaCoCo des tests unitaires
./gradlew createDebugUnitTestCoverageReport
# rapport : app/build/reports/coverage/test/debug/index.html
```

Dans Android Studio : clic droit sur le dossier `app/src/test` → **Run 'Tests in 'panier'' with Coverage**
(ou icône « bouclier » à côté du bouton Run). Le panneau *Coverage* affiche, par classe, le pourcentage
de lignes et de branches couvertes ; les lignes non couvertes sont surlignées en rouge dans l'éditeur.

Le Logcat affiche l'arbre sémantique produit par `printToLog` : filtrer sur `ARBRE`.

### Résultat attendu

- 40 tests unitaires, tous verts, en quelques secondes (dont 6 en temps virtuel : le `delay(1_000)` ne coûte rien) ;
- 5 tests instrumentés (3 pour `ProduitItem`, 2 pour le bonus locale) ;
- couverture : `Panier` et `CatalogueViewModel` à 100 % des lignes et des branches. Les composables, le thème et
  `MainActivity` restent à 0 % côté tests unitaires : ils relèvent des tests instrumentés, et viser 100 % sur ce
  code n'apporte rien.

## Points pédagogiques

- **AAA** : chaque test prépare, exécute **une** action, vérifie. Les noms décrivent le comportement, en français
  entre backticks dans `test/`, en camelCase dans `androidTest/`.
- **`Double`** : `assertEquals(attendu, reel, delta)` systématiquement (`0.1 + 0.2 != 0.3`, voir `PanierCasLimitesTest`).
- **Test paramétré** : le nom `{0} : {1} € avec {2} → {3} €` rend le rapport lisible ; la valeur limite
  **50 € pile** vérifie le `>=` (le test passe au rouge si l'on écrit `>`).
- **Mock ou fake** : MockK vérifie une **interaction** (`coVerify(exactly = 1)`) et simule facilement une exception ;
  le fake vérifie un **résultat** et se prête au temps virtuel (`delay(1_000)`).
- **Dispatchers** : `UnconfinedTestDispatcher` (test MockK) exécute `charger()` immédiatement ;
  `StandardTestDispatcher` (test fake) met la coroutine en file, ce qui permet d'observer `Chargement`
  avec `runCurrent()`, puis `advanceTimeBy(999)` / `advanceTimeBy(2)`.
- **Turbine** : un `StateFlow` ignore une valeur égale à la valeur courante. Au premier `charger()`, le
  `Chargement` réémis est donc invisible ; il réapparaît seulement après un état `Erreur` (second test Turbine).
- **Arbre sémantique** : `printToLog` montre que le `Button` fusionne son `Text` ; `useUnmergedTree = true`
  affiche le nœud texte interne.
- **Voir chaque test rouge une fois** : par exemple remplacer `>=` par `>` dans `total()`, ou `IOException`
  par `IllegalStateException` dans le fake, et constater l'échec.

## Bonus : réponses

### 1. Le prix affiché dépend de la langue de l'appareil

`ProduitItem` affiche `"%.2f €".format(produit.prix)`. `String.format` utilise `Locale.getDefault()` :

| Langue de l'appareil | Affichage |
|---|---|
| Français | `12,50 €` |
| Anglais (US) | `12.50 €` |

Un test UI qui cherche `"79,90 €"` passe sur l'émulateur du formateur et **échoue** sur une CI configurée en
anglais. C'est pourquoi `ProduitItemTest` fixe `Locale.FRANCE` dans un `@Before` (et restaure la locale
d'origine dans `@After`).

**Correction** (`main/…/bonus/FormatPrix.kt`, `bonus/ProduitItemCorrige.kt`) : fixer la locale explicitement.

```kotlin
fun formaterPrix(prix: Double, locale: Locale = Locale.FRANCE): String =
    "%.2f €".format(locale, prix)
```

**Tests qui le prouvent** :

- `test/…/bonus/FormatPrixTest.kt` (JVM) : l'expression fournie donne `12.50 €` en `Locale.US`,
  `formaterPrix` donne `12,50 €` ;
- `androidTest/…/bonus/ProduitItemLocaleTest.kt` : avec `Locale.setDefault(Locale.US)`, `ProduitItem` affiche
  `12.50 €` et `ProduitItemCorrige` affiche `12,50 €`.

Discussion possible : si l'application est **traduite**, mieux vaut un `NumberFormat.getCurrencyInstance(locale)`
ou une ressource `<string name="prix">%1$.2f €</string>` traduite. Le vrai problème, c'est un format qui change
selon l'appareil **sans que personne l'ait décidé**.

### 2. `HttpException` fait planter `CatalogueViewModel`

`CatalogueViewModel` n'intercepte que `IOException`. Or l'`HttpException` de Retrofit (réponse 404, 500…)
hérite de `RuntimeException`, **pas** d'`IOException` : elle traverse le `try/catch`, remonte jusqu'à
`viewModelScope`, qui n'a pas de gestionnaire d'exception, et **l'application plante**.

Démonstration (`test/…/bonus/CatalogueHttpExceptionTest.kt`, Retrofit ajouté en `testImplementation`
uniquement pour construire l'exception) :

```kotlin
@Test
fun `le ViewModel fourni laisse passer une HttpException, qui fait planter l'application`() {
    coEvery { repository.produits() } throws erreur500
    val viewModel = CatalogueViewModel(repository)

    // runTest relance à la fin du test l'exception non interceptée
    val levee = assertThrows(HttpException::class.java) {
        runTest { viewModel.charger() }
    }

    assertEquals(500, levee.code())
    assertEquals(CatalogueUiState.Chargement, viewModel.etat.value)
}
```

**Correction** (`main/…/bonus/CatalogueViewModelCorrige.kt`) : intercepter toute `Exception`, en relançant
`CancellationException` pour ne pas casser l'annulation des coroutines.

```kotlin
_etat.value = try {
    CatalogueUiState.Succes(repository.produits())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    CatalogueUiState.Erreur(e.message)
}
```

Autres corrections possibles : `catch (e: HttpException)` en plus de `IOException` (ce qui exige Retrofit dans
la couche UI), ou mieux, faire renvoyer un `Result<List<Produit>>` par le repository, comme `TodoRepository`
dans le chapitre : la couche données traduit les erreurs, le ViewModel n'a plus de `try/catch`.

**Tests qui la prouvent** : `le ViewModel corrigé transforme une HttpException en état Erreur`,
`le ViewModel corrigé gère toujours une IOException`, `le ViewModel corrigé ne transforme pas une annulation en erreur`.

### 3. Autres cas limites (proposés par l'IA, retenus après relecture)

Dans `PanierCasLimitesTest` : panier vide, multiplication prix × quantité, seuil de 50 € atteint par plusieurs
lignes, code promo sensible à la casse (`"bienvenue"` refusé : comportement à valider avec le métier), retrait
d'un produit absent, **même `id` mais prix différent** (deux lignes : la clé de la `Map` est la data class entière),
arrondi des `Double`. Dans les tests du ViewModel : `IOException` sans message, catalogue vide, nouvel essai après
une erreur.

Les codes générés par l'IA ont été relus : suppression des tests sans assertion réelle et des API obsolètes
(`runBlockingTest`, `TestCoroutineDispatcher`).

## Versions

AGP 9.5, Kotlin 2.2.10, Compose BOM 2026.02.01, MockK 1.14.11, kotlinx-coroutines-test 1.10.2, Turbine 1.2.1,
Lifecycle 2.10.0, Retrofit 3.0.0 (tests uniquement).
