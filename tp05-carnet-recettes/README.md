# TP 05 — Carnet de recettes (corrigé)

Corrigé des travaux pratiques du chapitre **« Interfaces graphiques avec Jetpack Compose »** (jour 2,
`slides/05-jetpack-compose.md`).

## Objectif

Construire une petite application Compose complète, **sans réseau** (données fictives) :
liste filtrable de recettes, détail, formulaire d'ajout validé, thème Material 3 clair/sombre,
textes traduits (anglais/français), animations et mise en page adaptative (liste et détail
côte à côte sur tablette, grille adaptative en bonus).

## Lancer le projet

```bash
./gradlew assembleDebug                 # APK de debug
./gradlew testDebugUnitTest             # tests unitaires (JVM, sans émulateur)
./gradlew assembleDebugAndroidTest      # compile les tests UI Compose
./gradlew connectedDebugAndroidTest     # exécute les tests UI (émulateur ou appareil requis)
```

Ou ouvrir le dossier dans Android Studio et lancer la configuration `app`.
Pour voir la disposition à deux volets : émulateur *Pixel Tablet* ou *Resizable*, ou les previews
`@PreviewScreenSizes` de `CarnetRecettesApp.kt`.

## Correspondance étapes → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/recettes/`.

| Étape | Contenu | Fichier(s) |
|---|---|---|
| 1 | `Recette(id, nom, categorie, dureeMinutes, ingredients, etapes)` + `difficulte` et `favori` ; 6 recettes, 3 catégories | `modele/Recette.kt`, `donnees/RecettesFictives.kt` |
| 2 | `RecetteCard`, previews `@PreviewLightDark` + `RecetteProvider` (`PreviewParameterProvider` : cas standard, favori, nom très long, un seul ingrédient) | `ui/composants/RecetteCard.kt` |
| 3 | `RecettesScreen` **sans état** (recherche, `FilterChip` par catégorie, `LazyColumn` avec `key`) ; l'état est porté par `CarnetRecettesApp` (rôle de `RecettesRoute`) avec `rememberSaveable` | `ui/liste/RecettesScreen.kt`, `ui/CarnetRecettesApp.kt`, `logique/FiltreRecettes.kt` |
| 4 | `RecetteDetailScreen` : ingrédients à cocher (ligne entière `toggleable` + `Role.Checkbox`), étapes numérotées ; `RecetteDetailRoute` garde les cases cochées | `ui/detail/RecetteDetailScreen.kt` |
| 5 | État `recetteChoisieId` ; deux volets côte à côte en largeur **Expanded** via `currentWindowAdaptiveInfo()` ; `BackHandler` pour revenir à la liste | `ui/CarnetRecettesApp.kt` |
| 6 | Formulaire : nom obligatoire, durée (clavier numérique, validation), difficulté (`Slider`), catégorie (boutons radio), bouton désactivé tant que le formulaire est invalide | `ui/ajout/AjoutRecetteScreen.kt`, `logique/FormulaireRecette.kt` |
| 7 | Thème Material Theme Builder clair + sombre complet | `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/Type.kt`, `res/values-night/themes.xml` |
| 8 | Textes en anglais (`values/`) et en français (`values-fr/`), pluriels `nb_ingredients` et `nb_recettes` | `res/values/strings.xml`, `res/values-fr/strings.xml`, `res/resources.properties` |
| 9 | `AnimatedVisibility` sur les étapes ; `animateColorAsState` sur le bouton favori | `ui/detail/RecetteDetailScreen.kt`, `ui/composants/RecetteCard.kt` (`BoutonFavori`) |
| 10 | **Bonus** : `LazyVerticalGrid` + `GridCells.Adaptive(260.dp)` dès la largeur Medium (et dans le volet liste en Expanded) | `ui/liste/RecettesScreen.kt` (`Affichage.Grille`) |

Tests :

| Type | Fichier | Ce qui est vérifié |
|---|---|---|
| Unitaire | `app/src/test/.../logique/FiltreRecettesTest.kt` | Recherche (casse, ingrédients), filtres cumulés, `basculer` |
| Unitaire | `app/src/test/.../logique/FormulaireRecetteTest.kt` | Règles de validation, conversion en `Recette` |
| Unitaire | `app/src/test/.../logique/SauvegardesTest.kt` | Aller-retour des `Saver` utilisés par `rememberSaveable` |
| UI Compose | `app/src/androidTest/.../ui/RecettesScreenTest.kt` | Clic sur une carte, saisie, filtres, liste vide, parcours filtrer → détail |
| UI Compose | `app/src/androidTest/.../ui/AjoutRecetteScreenTest.kt` | Bouton désactivé tant que le formulaire est invalide |
| UI Compose | `app/src/androidTest/.../ui/RecetteDetailScreenTest.kt` | Cocher un ingrédient par la ligne, apparition des étapes |

## Architecture de l'état

```text
CarnetRecettesApp  (avec état : recettes, recherche, catégories, recetteChoisieId, ajoutEnCours)
├── RecettesScreen        (sans état)  → RecetteCard (sans état) → BoutonFavori
├── RecetteDetailRoute    (état local : ingrédients cochés, étapes visibles)
│   └── RecetteDetailScreen (sans état)
└── AjoutRecetteRoute     (état local : FormulaireRecette)
    └── AjoutRecetteScreen  (sans état)
```

- L'état **descend**, les événements **remontent** (`onRechercheChange`, `onCategorieClick`,
  `onFavoriChange`…). Aucune mutation : les listes sont recréées (`map`, `+`).
- Toutes les données d'interface survivent à la rotation : `rememberSaveable` avec des `Saver`
  (`logique/Sauvegardes.kt`), car un `Bundle` n'accepte que des types simples.
- La liste filtrée est **mémorisée** : `remember(recettes, recherche, categories) { … }`.
- Au jour 3, l'état de `CarnetRecettesApp` migrera dans un `ViewModel`.

## Étapes outillées : démarche

### Étape 7 — Material Theme Builder

1. Ouvrir `material-foundation.github.io/material-theme-builder`, onglet **Custom**.
2. Couleur source (*Primary*) : `#B4532A` (terracotta, cohérent avec un carnet de cuisine) ;
   laisser *Secondary*/*Tertiary* dérivées automatiquement (schéma **Tonal spot**).
3. **Export → Jetpack Compose (Theme.kt)** : on obtient `Color.kt`, `Theme.kt`, `Type.kt`.
4. Copier `Color.kt` dans `ui/theme/` et reprendre `lightScheme` / `darkScheme` dans `Theme.kt`
   en conservant le nom du thème du projet (`CarnetRecettesTheme`). L'export propose aussi des
   variantes *medium/high contrast* : non reprises ici pour rester lisible.
5. Vérifier le **mode sombre** : previews `@PreviewLightDark`, ou réglage système de l'émulateur.
   `res/values-night/themes.xml` évite le flash blanc au démarrage en thème sombre.

Le fichier `Color.kt` fourni contient les **35 rôles** (dont `surfaceDim`, `surfaceBright` et les
cinq `surfaceContainer*`) pour le clair et le sombre, calculés avec l'algorithme HCT de
*material-color-utilities* (celui de Theme Builder). `dynamicColor` vaut `false` par défaut pour
afficher cette palette ; à `true`, Android 12+ utilise les couleurs du fond d'écran.

Dans les écrans, **aucune couleur en dur** : uniquement `MaterialTheme.colorScheme.xxx`
(par exemple `secondaryContainer` pour la carte sélectionnée, `primaryContainer` pour les pastilles).

### Étape 8 — Traductions et lint

- `values/strings.xml` (anglais) contient toutes les chaînes : c'est le repli.
- `values-fr/strings.xml` : mêmes clés, placeholders numérotés inchangés (`%1$d`), apostrophes
  échappées (`d\'ingrédients`), espace insécable avant les deux-points (` :`).
- Pluriels : en anglais `one`/`other`, en français `one` (0 et 1), `many` (grands nombres,
  « 1 000 000 d'ingrédients ») et `other`. Appel : `pluralStringResource(R.plurals.nb_ingredients, n, n)`.
- `resume_recette` est `translatable="false"` : c'est un simple format `%1$s · %2$s · %3$s`.
- `generateLocaleConfig = true` + `res/resources.properties` : la langue de l'application est
  réglable dans *Paramètres → Applications → Langue* (Android 13+).
- Contrôle : `./gradlew lintDebug` (règles `MissingTranslation`, `StringFormatInvalid`,
  `MissingQuantity`) et le **Translations Editor** d'Android Studio.

## Points pédagogiques

- **Sans état / avec état** : chaque écran a une version sans état (prévisualisable, testable) et
  une version « route » qui porte l'état.
- **Conventions d'API** : données et événements d'abord, `modifier: Modifier = Modifier` en premier
  paramètre optionnel, appliqué une seule fois au nœud racine ; événements nommés `onXxx`.
- **Listes lazy** : `key = { it.id }`, `contentType`, `Modifier.animateItem()` ; jamais de liste
  lazy dans un `verticalScroll` (le formulaire, lui, est une simple `Column` défilante).
- **Accessibilité** :
  - lignes entières cliquables (`toggleable`/`selectable` + `Role`, `Checkbox(onCheckedChange = null)`) ;
  - `IconToggleButton` pour le favori : cible de 48 dp, état « coché » annoncé par TalkBack ;
  - titres marqués `heading()`, étape lue « Étape 2 : … » (`clearAndSetSemantics`) ;
  - nombre de résultats en *live region* ; `stateDescription` du `Slider` (« 3/5 (moyenne) »
    au lieu d'un pourcentage) ;
  - icônes décoratives avec `contentDescription = null`, flèche retour auto-miroir (RTL).
- **Validation** : la règle est de la logique pure (`FormulaireRecette`, testée en unitaire) ;
  l'erreur « obligatoire » n'est pas affichée en rouge sur un formulaire vierge (moins intrusif),
  mais le bouton reste désactivé.
- **Adaptatif** : on raisonne sur la taille de la **fenêtre** (`WindowSizeClass`), pas de l'appareil :
  Compact → liste, Medium → grille, Expanded → grille + détail côte à côte.
- **Animations** : `AnimatedVisibility(fadeIn + expandVertically)` pour les étapes,
  `animateColorAsState(tween(400))` pour le cœur du favori.

## Écarts assumés par rapport à l'énoncé

- `RecettesRoute` est fusionné dans `CarnetRecettesApp` : la recherche et les filtres doivent être
  partagés entre la liste et le volet de détail (plus petit ancêtre commun).
- `@Parcelize` n'est pas utilisé : avec le Kotlin intégré d'AGP 9 et Kotlin 2.2.10, le plugin
  `kotlin-parcelize` n'est pas appliqué au compilateur. Les `Saver` explicites (`listSaver`) font le
  même travail et montrent ce qu'il y a dans le `Bundle`.
- Le contenu des recettes (noms, ingrédients) est en français dans les deux langues : ce sont des
  **données**, pas des textes d'interface.
