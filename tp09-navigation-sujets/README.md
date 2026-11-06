# TP09 — Navigation Compose, pas à pas

Jour 4, chapitre « Navigation » (`slides/09-navigation.md`).

Une progression en **7 étapes** (0 à 6), de deux écrans sans bibliothèque jusqu'aux onglets, au graphe imbriqué
et au deep link. Chaque étape ajoute **une notion** par rapport à la précédente. On reprend le graphe de TodoApp
des slides : `Todos`, `DetailTodo(id)`, `Recherche`, `ParametresGraphe` (`Parametres` + `APropos`).

Pas de serveur : les tâches sont en mémoire, pour se concentrer sur la navigation. La version complète, un
fichier par classe et avec tests, est dans **`tp09-catalogue-films`**.

## Exécuter une étape

Chaque étape est dans son package `sujetN` (`app/src/main/java/fr/formation/navsujets/sujetN/`), avec un seul
fichier `MainActivityN.kt`, complet et lisible de haut en bas. Le commentaire en tête de fichier dit ce qui est
nouveau et ce qu'il faut essayer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité de l'étape** et laisser les autres en
commentaire, puis lancer l'application.

## Les étapes

| Étape | Notion | Ce qu'on observe |
|---|---|---|
| `sujet0` | Deux écrans choisis par un état (`rememberSaveable`), sans bibliothèque | ❌ Retour système sur le détail : **l'application se ferme** (pas de pile) |
| `sujet1` | `NavHost`, routes `@Serializable data object`, `navigate` / `navigateUp`, écrans avec callbacks | Liste → « À propos » (ⓘ) → Retour système : on revient à la liste |
| `sujet2` | Route avec argument `DetailTodo(id)`, `toRoute()` dans le `NavHost` ; on passe l'**id**, pas l'objet | Ouvrir une tâche, tourner l'écran : l'argument est conservé |
| `sujet3` | `DetailViewModel` lit l'id avec `savedStateHandle.toRoute<DetailTodo>()` ; ViewModel **lié à l'entrée** ; `TodosRepository` partagé | Logcat `NavSujets` : ViewModel créé / détruit à chaque aller-retour ; « Marquer comme faite » → la liste est à jour |
| `sujet4` | Pile de retour : `popUpTo<Connexion> { inclusive = true }`, `popBackStack<Todos>()`, `launchSingleTop` | Un bandeau affiche la pile en direct : « Liste › Détail 1 › Détail 2 » |
| `sujet5` | Onglets : `NavigationBar`, `currentBackStackEntryAsState`, `saveState` / `restoreState`, barre masquée sur le détail | Le texte de Recherche et le détail ouvert sont retrouvés en changeant d'onglet ; ❌ sur « À propos », aucun onglet sélectionné |
| `sujet6` | Graphe imbriqué `navigation<ParametresGraphe>`, `hierarchy`, deep link `todoapp://detail/{id}` | « Paramètres » reste sélectionné sur « À propos » ; le lien ouvre le détail, Retour mène à la liste |

Les étapes 5 et 6 partent de l'étape 3 : elles ne reprennent pas la connexion ni le bandeau de pile de l'étape 4.

## Ce qui change d'une étape à l'autre

```text
0  état idOuvert ──▶ when : liste OU détail                       (pas de pile)
1  NavHost { Todos, APropos }                                    navigate / navigateUp
2  + DetailTodo(id)          toRoute() dans composable<DetailTodo>
3  + DetailViewModel         toRoute() dans le SavedStateHandle ; TodosRepository partagé
4  + Connexion               popUpTo, popBackStack<Todos>, launchSingleTop, pile affichée
5  (base 3) + Recherche, Parametres en onglets     saveState / restoreState
6  + ParametresGraphe { Parametres, APropos }      hierarchy, deep link
```

## Conseils de démonstration

- **Étape 0** : ouvrir une tâche puis faire Retour système. L'app se ferme : c'est le problème que résout la
  bibliothèque.
- **Étapes 0 et 1** : les écrans ne connaissent que des callbacks (`onTodoClick`, `onRetour`). Seul le `NavHost`
  connaît le `NavController`.
- **Étape 3** : Logcat, filtre `NavSujets`. Tourner l'écran sur le détail : pas de nouveau ViewModel. Revenir à
  la liste : « détruit ».
- **Étape 4** : Connexion → tâche 1 → « Tâche suivante » × 2. Retour système dépile un cran, « Retour à la
  liste » dépile tout. Depuis la liste, Retour quitte l'app : la connexion n'est plus dans la pile. Enlever
  `launchSingleTop` puis toucher ⓘ deux fois très vite.
- **Étape 5** : ouvrir une tâche depuis « Tâches », passer sur « Recherche », revenir sur « Tâches » : le détail
  est restauré. Retirer `saveState` / `restoreState` pour voir la différence.
- **Étape 6** : application fermée, puis

  ```bash
  adb shell am start -a android.intent.action.VIEW -d "todoapp://detail/3"
  ```

  Retour mène à la liste : la pile est recréée depuis la destination de départ. `todoapp://detail/42` affiche
  « Tâche introuvable ».

## Organisation en fichiers

Comme dans `tp07-todos-http`, chaque `MainActivityN.kt` commence par un bloc « Organisation en fichiers »
(l'arborescence `data/`, `ui/navigation/`, `ui/todos/`, `ui/detail/`…). Une ligne `// 📄 chemin/Fichier.kt`
précède chaque déclaration.

## Dépendances

- Navigation Compose **2.10.2** (`androidx.navigation:navigation-compose`) : routes type-safe depuis la 2.8.0.
- Plugin `org.jetbrains.kotlin.plugin.serialization` : les routes sont des classes `@Serializable`.
- Lifecycle **2.11.0** (`viewModel()`, `collectAsStateWithLifecycle`).

## Choix assumés

- **Un seul fichier par étape**, découpé par des bannières `// ---------- … ----------`.
- **`TodosRepository` est un `object`** (étapes 3 à 6) pour rester simple. Dans un vrai projet, c'est une classe
  créée dans un `AppContainer` (voir `tp09-catalogue-films`).
- **Le bandeau de pile** de l'étape 4 (`navController.currentBackStack`) sert au TP. On ne l'affiche pas dans
  une vraie application.
- **Pas de tests** dans ce projet. Les tests de navigation (`TestNavHostController`) sont dans
  `tp09-catalogue-films`.
