# Jour 2 matin — Jetpack Compose, un exemple par sujet

Exemples de démonstration du chapitre **« Interfaces graphiques avec Jetpack Compose »**
(`slides/05-jetpack-compose.md`), dans l'ordre du plan de cours.

## Exécuter un sujet

Chaque sujet est un fichier complet, lisible de haut en bas : `MainActivity1.kt` à `MainActivity17.kt`
(`app/src/main/java/fr/formation/compose/`). Le commentaire en tête de fichier résume ce qu'il faut
montrer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité du sujet** et laisser les autres en
commentaire, puis lancer l'application.

## Les sujets

| Fichier | Sujet | Ce qu'on montre |
|---|---|---|
| `MainActivity1.kt` | Fonctionnement de Compose | `@Composable`, `setContent`, recomposition (Logcat, `tag:Compose`) |
| `MainActivity2.kt` | Preview | `@Preview` et ses paramètres, `@PreviewLightDark`, `PreviewParameterProvider` (mode *Split*) |
| `MainActivity3.kt` | Mise en page | `Column`, `Row`, `Box`, `Arrangement`, `Alignment`, `weight`, `Spacer` |
| `MainActivity4.kt` | Modifier | L'ordre compte : `padding` / `background`, `clip`, `border`, `clickable` |
| `MainActivity5.kt` | Principaux composants | `Text`, les 5 boutons, `IconButton`, FAB, `Image`, `Card` |
| `MainActivity6.kt` | Listes | `LazyColumn`, `LazyRow`, `items(key = …)` |
| `MainActivity7.kt` | Formulaire | `OutlinedTextField` validé, mot de passe, `RadioButton`, `Switch`, `Slider`, `Checkbox` |
| `MainActivity8.kt` | Composants réutilisables | Paramètres, `modifier`, *slots* (`actions`, `contenu`) |
| `MainActivity9.kt` | Événements | `onClick`, `onValueChange`, `combinedClickable`, événements remontés au parent |
| `MainActivity10.kt` | États | `mutableStateListOf`, valeur dérivée, *state hoisting* (bases : voir `tp05-state-compteur-bis`) |
| `MainActivity11.kt` | Bonnes pratiques | Avec / sans état, `modifier`, `LaunchedEffect`, `remember(cle)`, clés de liste |
| `MainActivity12.kt` | Mise en forme adaptable | `FlowRow`, `weight`, `widthIn(max)`, `maxLines`, `BoxWithConstraints` |
| `MainActivity13.kt` | Thème clair / sombre | Palettes, `isSystemInDarkTheme`, couleurs dynamiques, `MaterialTheme.colorScheme` |
| `MainActivity14.kt` | Animation | `animate*AsState`, `AnimatedVisibility`, `AnimatedContent`, animation infinie |
| `MainActivity15.kt` | Internationalisation | `values/` et `values-fr/`, paramètres, pluriels, dates et prix localisés |
| `MainActivity16.kt` | Tailles d'écran | `currentWindowAdaptiveInfo()`, liste seule ou liste + détail |
| `MainActivity17.kt` | Material 3 | `Scaffold`, `TopAppBar`, `NavigationBar` + badge, FAB, `Snackbar`, `AlertDialog` |

## Conseils de démonstration

- **Sujets 3, 4, 12** : modifier une valeur et observer en direct (*Live Edit*) ou dans la Preview.
- **Sujet 13** : basculer aussi le mode sombre de l'émulateur pour montrer `isSystemInDarkTheme()`.
- **Sujet 15** : l'émulateur est souvent en anglais. Passer l'application en français dans
  *Paramètres > Applications > Compose > Langue* (activé par `generateLocaleConfig`).
- **Sujet 16** : émulateur *Resizable* ou *Pixel Tablet*, ou rotation en paysage.

## Choix assumés

- Chaque fichier est autonome : composables `private`, pas de fichier partagé. La duplication
  (structure de l'Activity, petits composables) est volontaire, pour ne jamais changer de fichier.
- Textes en dur en français, sauf au sujet 15 qui montre justement les ressources de traduction.
- Pas de réseau : l'image du sujet 5 vient des ressources ; Coil (images réseau) est seulement
  présenté dans les slides.
