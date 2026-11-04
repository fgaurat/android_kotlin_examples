# TP 06 — Fiche produit générée par l'IA (corrigé)

Corrigé des travaux pratiques du chapitre **« Générer des interfaces Compose avec l'IA »** (jour 2,
`slides/06-compose-ia.md`).

## Objectif

Faire produire par Gemini (dans Android Studio) un écran Compose réaliste, puis **itérer** et
**auditer** le code généré jusqu'à obtenir un écran conforme aux règles du chapitre 05 (sans état,
thème Material 3, accessibilité, ressources traduites).

Le code de ce projet est **l'écran de référence** : ce qu'une bonne session avec Gemini doit
produire après les prompts de suivi et l'audit. Ce README contient la démarche : prompt
d'interface, prompts de suivi, grille d'audit remplie (avant / après).

> Écran demandé : fiche produit d'une boutique de vêtements : grande photo, nom, prix, note sur
> 5 étoiles, choix de la taille (S à XL), choix de la couleur, description repliable, bouton
> « Ajouter au panier » fixé en bas. Barre du haut avec retour et partage ; barre de navigation
> à 4 onglets (Accueil, Recherche, Panier avec badge, Profil).

## Lancer le projet

```bash
./gradlew assembleDebug                 # APK de debug
./gradlew testDebugUnitTest             # tests unitaires (JVM)
./gradlew assembleDebugAndroidTest      # compile les tests UI Compose
./gradlew connectedDebugAndroidTest     # exécute les tests UI (émulateur requis)
./gradlew lintDebug                     # traductions, placeholders, pluriels…
```

Aucun accès réseau : l'image du produit est un vecteur local (`res/drawable/illustration_tshirt.xml`)
teinté avec la couleur choisie.

## Correspondance étapes → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/ficheproduit/`.

| Étape | Contenu | Fichier(s) |
|---|---|---|
| 1 | `data class Produit` (+ `CouleurProduit`, `Taille`, `LignePanier`), données fictives ; prompt d'interface | `modele/Produit.kt`, `donnees/ProduitsFictifs.kt`, ce README |
| 2 | Écran de référence obtenu après itérations | **`ui/produit/ProduitScreen.kt`** (livrable), `ui/navigation/NavigationPrincipale.kt`, `ui/FicheProduitApp.kt` (état) |
| 3 | Previews `@PreviewLightDark` (× `ProduitProvider`), `@PreviewScreenSizes`, `@PreviewFontScale`, langues `fr` / `ar` (RTL) | fin de `ui/produit/ProduitScreen.kt` |
| 4 | `values/strings.xml` (anglais), `values-fr/strings.xml` ; vérification par le lint | `res/values*/strings.xml` |
| 5 | Grille d'audit avant / après | ce README, section « Grille d'audit » |
| 6 | Corrections issues de UI Check et TalkBack | ce README, section « UI Check et TalkBack » ; `NavigationPrincipale.kt` |
| 7 | **Bonus** : écran panier transcrit d'un croquis | `ui/panier/PanierScreen.kt` (onglet Panier fonctionnel) |

Logique et tests :

| Fichier | Rôle |
|---|---|
| `logique/Note.kt` | Note → 5 états d'étoile (pleine, demie, vide), arrondi à la demi-étoile |
| `logique/Prix.kt` | Prix en euros et note selon la langue (`29,90 €` / `€29.90`) |
| `logique/Panier.kt` | Ajout (fusion des lignes identiques), quantités, total, nombre d'articles |
| `app/src/test/.../NoteTest.kt`, `PrixTest.kt`, `PanierTest.kt` | Tests unitaires de cette logique |
| `app/src/androidTest/.../ProduitScreenTest.kt` | Bouton désactivé sans taille, taille épuisée, note lue en une phrase, badge mis à jour, description dépliable |
| `app/src/androidTest/.../PanierScreenTest.kt` | Panier vide, bouton « + » |

## Étape 1 — Le prompt d'interface

Fichiers ouverts dans l'éditeur avant d'envoyer le prompt : `Produit.kt`, `ProduitsFictifs.kt`,
`ui/theme/Theme.kt` (Gemini les utilise comme contexte ; sinon, les joindre au chat).

```text
Contexte technique
Android, Kotlin 2.2, Jetpack Compose avec Material 3 (BOM 2026.02), minSdk 34.
Thème existant : FicheProduitTheme (ui/theme/Theme.kt). Modèle existant : Produit, CouleurProduit,
Taille (fichier Produit.kt ouvert) et données fictives produitFictif.

Composant
Un écran ProduitScreen : fiche produit d'une boutique de vêtements.

Données
- produit: Produit (immuable, prix en centimes, note: Float sur 5, nombreAvis: Int,
  couleurs: List<CouleurProduit>, tailles, taillesEpuisees, image: @DrawableRes locale)
- selection: SelectionProduit(taille: Taille?, couleurId: String?, descriptionDepliee: Boolean)
- nombreArticlesPanier: Int

Mise en page (de haut en bas)
- TopAppBar : flèche retour à gauche, titre = marque (1 ligne, ellipse), action Partager
- Grande photo carrée (aspectRatio 1f) sur fond surfaceContainerHighest, teintée avec la couleur choisie
- Marque, nom (headlineSmall, 2 lignes possibles), prix (et ancien prix barré s'il existe),
  note en étoiles avec le nombre d'avis
- Choix de la taille S à XL : SingleChoiceSegmentedButtonRow ; tailles épuisées désactivées
- Choix de la couleur : pastilles rondes, sélection exclusive
- Description repliable : 3 lignes en aperçu, en-tête cliquable avec chevron animé
- En bas, fixe : barre avec le prix et le bouton « Ajouter au panier »
- Sous la barre : navigation à 4 onglets (Accueil, Recherche, Panier avec badge du nombre
  d'articles, Profil) avec NavigationSuiteScaffold (rail sur tablette)
- Au-delà de 600 dp de large : photo à gauche, informations à droite

États
- Aucune taille choisie : bouton désactivé + texte « Choisissez une taille »
- Nom très long, aucun avis (afficher « Aucun avis »), une seule couleur
- Badge masqué si le panier est vide, « 99+ » au-delà de 99

Événements (remontés, l'écran ne modifie rien lui-même)
onRetour, onPartager, onTailleChange(Taille), onCouleurChange(CouleurProduit),
onDescriptionDeplieeChange(Boolean), onAjouterAuPanier, onOngletClick(Onglet)

Contraintes
- Composable SANS état ; modifier: Modifier = Modifier en premier paramètre optionnel, appliqué au nœud racine
- Uniquement MaterialTheme.colorScheme / typography / shapes : aucune Color(0x…) ni fontSize en dur
  (seule exception : la teinte du tissu, qui est une donnée du produit)
- Aucun texte en dur : stringResource / pluralStringResource, liste des clés à créer
- Accessibilité : contentDescription pertinents (null si décoratif), cibles ≥ 48 dp,
  sélection exclusive avec Role.RadioButton, note lue en une phrase (« Noté 4,5 sur 5, 128 avis »),
  état « déplié / replié » annoncé, icônes AutoMirrored pour le RTL
- Pas de hauteur fixe autour du texte ; pas de liste lazy dans un verticalScroll
- API Material 3 non dépréciées uniquement

Livrables
- Un fichier ProduitScreen.kt (écran + sous-composables privés)
- Les clés strings.xml à créer (anglais)
- Une preview @PreviewLightDark dans FicheProduitTheme
```

## Étape 2 — Itérations : prompts de suivi

La première réponse **compile et s'affiche** (c'est ce qui rend ses défauts difficiles à voir). Défauts
typiques observés sur ce genre d'écran, et les quatre prompts de suivi qui les corrigent :

| # | Prompt de suivi | Ce qu'il corrige |
|---|---|---|
| 1 | « Rends `ProduitScreen` **sans état** : supprime les `remember { mutableStateOf(…) }` de taille, couleur et description, reçois `selection: SelectionProduit` et remonte `onTailleChange`, `onCouleurChange`, `onDescriptionDeplieeChange`. Ajoute `modifier: Modifier = Modifier` appliqué au nœud racine. Le badge vient de `nombreArticlesPanier`, pas d'un compteur interne. » | État interne non testable et perdu à la rotation ; badge incrémenté localement ; pas de paramètre `modifier` |
| 2 | « Remplace toutes les couleurs en dur (`Color(0xFFFFC107)` des étoiles, `Color.Gray`, `Color.White` du bouton…) par des rôles de `MaterialTheme.colorScheme` (`tertiary`, `onSurfaceVariant`, `outlineVariant`…), et tous les `fontSize` par `MaterialTheme.typography`. Remplace `Divider` par `HorizontalDivider`. Garde la teinte du tissu : c'est une donnée. » | Écran illisible en thème sombre, ignore le thème ; API dépréciée |
| 3 | « Accessibilité : les 5 icônes d'étoiles doivent être lues en une phrase (`clearAndSetSemantics`, « Noté 4,5 sur 5, 128 avis ») ; les pastilles de couleur font 48 dp, utilisent `selectable(role = Role.RadioButton)` dans un `selectableGroup()` et portent le nom de la couleur ; l'en-tête de la description utilise `clickable(onClickLabel, role = Role.Button)` et un `stateDescription` déplié / replié ; flèche retour en `Icons.AutoMirrored`. » | Étoiles lues « image, image, image… » ; pastilles de 32 dp sans nom ; chevron en `Icon.clickable` de 24 dp ; flèche non inversée en RTL |
| 4 | « Externalise tous les textes dans `strings.xml` avec des placeholders numérotés (`%1$s`), un `plurals` pour le nombre d'avis et le badge. Formate le prix avec `NumberFormat.getCurrencyInstance` selon la locale (pas de `"$prix €"`). Au-delà de 600 dp, place la photo à gauche et les infos à droite (`BoxWithConstraints`), et remplace la `NavigationBar` par `NavigationSuiteScaffold`. Ajoute `@PreviewScreenSizes` et `@PreviewFontScale`. » | Textes en dur, concaténations non traduisibles, prix « 29.9 € » ; photo démesurée sur tablette ; barre du bas sur tablette au lieu d'un rail |

Bonnes habitudes appliquées : une conversation dédiée à l'écran, compilation et preview entre
chaque prompt, fichiers du projet ouverts pour donner le contexte.

### Extrait typique de la première réponse (avant)

```kotlin
@Composable
fun ProductScreen(product: Produit) {
    var selectedSize by remember { mutableStateOf("M") }          // état interne, perdu à la rotation
    var expanded by remember { mutableStateOf(false) }
    var cartCount by remember { mutableStateOf(0) }               // le panier vit dans l'écran
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.nom) },                    // pas de maxLines
                navigationIcon = {
                    IconButton(onClick = {}) { Icon(Icons.Filled.ArrowBack, "Back") }   // déprécié, non RTL, texte en dur
                },
            )
        },
        bottomBar = {
            Column {
                Button(
                    onClick = { cartCount++ },                    // ajoute même sans taille
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("Add to cart", color = Color.White, fontSize = 18.sp) }
                NavigationBar { /* 4 NavigationBarItem, BadgedBox { Badge { Text("$cartCount") } } */ }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            Image(painterResource(product.image), null, Modifier.fillMaxWidth().height(400.dp))  // hauteur fixe, pas de description
            Text(product.nom, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("${product.prixCentimes / 100.0} €", color = Color(0xFF6200EE))   // « 29.9 € »
            Row { repeat(5) { i -> Icon(Icons.Filled.Star, null, tint = if (i < product.note) Color(0xFFFFC107) else Color.LightGray) } }
            Row { listOf("S", "M", "L", "XL").forEach { size ->
                FilterChip(selected = size == selectedSize, onClick = { selectedSize = size }, label = { Text(size) })
            } }
            Row { product.couleurs.forEach { c ->
                Box(Modifier.size(32.dp).background(Color(c.teinte), CircleShape).clickable { })   // 32 dp, sans nom, sans état
            } }
            Row { Text("Description", fontWeight = FontWeight.Bold); Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.clickable { expanded = !expanded }) }
            if (expanded) Text(product.description)
            Divider(color = Color.LightGray)                      // déprécié
        }
    }
}
```

## Étape 5 — Grille d'audit remplie

Audit de la première réponse (**avant**) et de `ProduitScreen.kt` (**après**).
✅ conforme · ❌ non conforme · ⚠️ partiel.

| Axe | Critère | Avant | Défaut constaté (première réponse) | Après | Correction (où) |
|---|---|---|---|---|---|
| Accessibilité | `contentDescription` pertinent ou `null` si décoratif | ❌ | Photo `null` ; « Back » en dur ; étoiles sans sens | ✅ | Photo « Photo du produit …, coloris … » ; icônes décoratives `null` ; textes en ressources (`PhotoProduit`, `ProduitTopBar`) |
| | Cibles tactiles ≥ 48 dp | ❌ | Pastilles 32 dp ; chevron cliquable 24 dp | ✅ | Pastilles `size(48.dp)` ; en-tête de description `heightIn(min = 48.dp)` ; `IconButton`, `SegmentedButton` |
| | Contraste ≥ 4,5:1 | ❌ | `Color.LightGray` sur blanc ; violet fixe en thème sombre | ✅ | Rôles `onSurfaceVariant`, `primary`, `onXxx` du thème ; coche noire / blanche selon la luminance du tissu |
| | Sémantique (`heading`, `Role`, fusion) | ❌ | 5 étoiles lues une à une ; tailles en `FilterChip` (rôle case à cocher) ; état de la description non annoncé ; badge non lu | ✅ | `clearAndSetSemantics` sur la note ; `Role.RadioButton` (segmented buttons, pastilles + `selectableGroup`) ; `stateDescription` ; `heading()` ; description de l'onglet Panier |
| Performance | `key` dans les listes lazy, pas de lazy dans `verticalScroll` | ✅ | Pas de liste lazy dans la fiche | ✅ | Fiche = `Column` défilante ; panier (bonus) = `LazyColumn` avec `key = { it.id }` |
| | Calculs mémorisés, pas de travail lourd en composition | ⚠️ | `NumberFormat` recréé à chaque recomposition, sans locale | ✅ | Formatage extrait en fonction pure testée (`logique/Prix.kt`) ; calculs du panier hors UI |
| | Lectures d'état différées | ✅ | — | ✅ | Rotation du chevron et taille animées (`animateFloatAsState`, `animateContentSize`) |
| Material 3 | Couleurs, typographie, formes du thème | ❌ | `Color(0xFF6200EE)`, `Color(0xFFFFC107)`, `fontSize = 24.sp` | ✅ | `MaterialTheme.colorScheme.*`, `typography.*` ; seule couleur « en dur » : la teinte du tissu (donnée) |
| | Composants adaptés, API non dépréciées | ❌ | `Icons.Filled.ArrowBack`, `Divider` ; `NavigationBar` sur tablette | ✅ | `Icons.AutoMirrored.Filled.ArrowBack` ; `SingleChoiceSegmentedButtonRow` ; `NavigationSuiteScaffold` ; `Badge` intégré à l'item |
| API Compose | Sans état | ❌ | Taille, description, panier dans l'écran | ✅ | `SelectionProduit` + `ActionsProduit` ; état dans `FicheProduitApp` (`rememberSaveable`) |
| | `modifier` premier paramètre optionnel | ❌ | Absent | ✅ | Tous les composables publics et privés |
| | Événements `onXxx` | ⚠️ | `onClick = {}` vides, ajout possible sans taille | ✅ | `onTailleChange`, `onAjouterAuPanier`… ; bouton `enabled = produit.peutEtreAjoute(taille)` |
| | Textes en ressources | ❌ | « Add to cart », « Description », `"$prix €"` | ✅ | `strings.xml` en / fr, `plurals` `nb_avis` et `articles_panier`, prix via `NumberFormat` |
| Adaptatif | Pas de dimension fixe, largeur exploitée | ❌ | `height(400.dp)` sur la photo, bouton `height(56.dp)` | ✅ | `aspectRatio(1f)`, `heightIn(min = …)` ; deux colonnes au-delà de 600 dp ; rail sur tablette |

### Audit manuel contre audit par l'IA

Prompt d'audit (celui du chapitre) appliqué à la première réponse :

> « Voici `ProduitScreen.kt`. Audite-le selon cette grille : accessibilité (`contentDescription`,
> cibles 48 dp, contraste, sémantique), performance (clés lazy, `remember`, lectures d'état),
> Material 3 (tokens du thème, API non dépréciées), conventions Compose (sans état, `modifier`,
> `onXxx`, textes en ressources). Rends un tableau : critère, statut ✅ / ❌, ligne concernée,
> correction proposée. N'invente pas de problème s'il n'y en a pas. »

Comparaison typique :

- **Trouvé par les deux** : couleurs et `fontSize` en dur, textes en dur, `Divider` déprécié,
  absence de `modifier`, état interne.
- **Manqué par l'IA** : le **badge non annoncé** par TalkBack (voir ci-dessous) ; l'ajout au panier
  possible sans taille ; le prix mal formaté (`29.9 €`) ; la flèche non inversée en RTL.
- **Inventé ou discutable** : « ajouter `key` à la `Row` des tailles » (pas une liste lazy) ;
  « remplacer `Column` + `verticalScroll` par `LazyColumn` » (inutile pour un contenu fixe et court).

Chaque point de l'audit IA se vérifie dans le code avant d'être appliqué.

## Étape 6 — UI Check et TalkBack

**UI Check** (icône de la preview `@PreviewLightDark` → *Start UI Check Mode*) :

- sur la première version, il signale typiquement les pastilles de 32 dp (*touch target size*) et
  le gris clair (*text contrast*) ; relancer UI Check sur la version finale pour vérifier qu'il ne
  reste rien ;
- en mode *Reference devices*, montre la photo démesurée sur tablette → disposition en deux colonnes.

**TalkBack** (émulateur : *Paramètres → Accessibilité → TalkBack*, balayage gauche / droite) :

Lectures typiques (formulations approximatives, elles varient selon la version de TalkBack) :

| Élément | Lu avant | Lu après (attendu) |
|---|---|---|
| Note | « Image, image, image, image, image, 4.5, (128) » | « Noté 4,5 sur 5, 128 avis » |
| Pastille de couleur | « Bouton » | « Bleu marine, bouton radio, sélectionné, 1 sur 4 » |
| Taille | « S, case à cocher, non cochée » | « S, bouton radio, non sélectionné » / « XL, désactivé » + « XL épuisée » |
| Description | « Description » (le chevron : « bouton » sans nom) | « Description, replié, bouton, appuyer deux fois pour déplier » |
| Prix barré | « 39,90 € » (lu comme un second prix) | « Au lieu de 39,90 € » |
| Onglet Panier | « Panier, onglet » (badge ignoré) | « Panier, 2 articles dans le panier, onglet » |

Le dernier point est un piège réel, détecté par un **test instrumenté** et non par l'IA :
`NavigationBarItem` efface la sémantique de l'icône (donc du badge) quand le libellé est affiché.
La description est posée sur l'onglet entier (`NavigationPrincipale.kt`).

## Étape 4 — Traductions : vérifications

- Mêmes clés dans `values/` et `values-fr/`, placeholders numérotés inchangés (`%1$s`, `%2$s`).
- Apostrophes échappées : `l\'instant`, `d\'avis`, `n\'est pas` ; espace insécable avant les
  deux-points (` :`).
- Pluriels français `one` / `many` / `other` (`nb_avis`, `articles_panier`).
- `onglet_avec_badge` (`%1$s, %2$s`) est `translatable="false"`.
- Commentaires de contexte au-dessus des chaînes ambiguës (« Checkout » = bouton, « 12 caractères max. »).
- `./gradlew lintDebug` : aucune alerte `MissingTranslation`, `StringFormatInvalid`, `MissingQuantity`.

## Étape 7 (bonus) — Du croquis au panier

Croquis dessiné à la main, photographié et joint au chat Gemini :

```text
┌──────────────────────────────┐
│ Panier                       │
├──────────────────────────────┤
│ (●) T-shirt col rond…        │
│     Taille M · Bleu marine   │
│     59,80 €        [-] 2 [+] │
├──────────────────────────────┤
│ (●) T-shirt col rond…        │
│     Taille S · Terracotta    │
│     29,90 €        [-] 1 [+] │
├──────────────────────────────┤
│ Total : 89,70 €  [Commander] │
└──────────────────────────────┘
```

Prompt joint au croquis :

> « Voici le croquis de l'écran Panier. Génère `PanierScreen` (Compose, Material 3), sans état :
> `lignes: List<LignePanier>` (fichier joint), `onQuantiteChange: (LignePanier, Int) -> Unit`,
> `onCommander`, `onOngletClick`, `modifier`. Une `LazyColumn` de `ListItem` (clé `id`) ; pastille de
> couleur décorative ; boutons − / + de 48 dp avec descriptions incluant le nom de l'article ;
> quantité annoncée quand elle change ; total formaté selon la locale et bouton « Commander » fixés
> en bas ; message si le panier est vide. Réutilise `NavigationPrincipale` existant. Couleurs du thème,
> textes dans `strings.xml`. »

Ce que le croquis ne disait pas et qu'il a fallu préciser : l'état vide, le comportement de « − »
à 1 (suppression de la ligne, `logique/Panier.kt`), la lecture de la quantité par TalkBack.
Résultat : `ui/panier/PanierScreen.kt`, accessible depuis l'onglet Panier ; la snackbar
« Voir le panier » de la fiche y mène aussi.

## Points pédagogiques

- Le prompt impose **données, états, événements et contraintes**, pas seulement l'apparence.
- L'IA produit du code qui compile : la revue (grille), les **outils** (lint, UI Check, TalkBack)
  et les **tests** restent indispensables — le défaut du badge n'a été trouvé que par un test.
- Les couleurs du produit (tissu) sont des **données** : elles ne passent pas par le thème ; tout le
  reste vient de `MaterialTheme`.
- Thème `FicheProduitTheme` : palette complète clair / sombre générée avec Material Theme Builder
  (couleur source `#3D5A80`), même démarche qu'au TP 05.
