# TP07 — Liste de tâches HTTP, pas à pas

Jour 3, chapitre « Architecture MVVM » (`slides/07-mvvm.md`).

Une progression en **7 étapes** (0 à 6), d'une liste écrite en dur jusqu'à MVVM avec repository.
Chaque étape ajoute **une seule notion** par rapport à la précédente. Toutes les étapes permettent d'**ajouter**
(bouton flottant → boîte de dialogue) et de **supprimer** (icône corbeille) une tâche : on voit ainsi où vivent ces
actions à chaque niveau d'architecture.

**State hoisting dans toutes les étapes** : l'écran est coupé en deux composables.

- `TodosRoute` (avec état) : récupère l'état (liste locale, puis ViewModel) et branche les événements ;
- `TodosEcran` (sans état) : reçoit ce qu'il affiche et remonte les événements (`onAjouter`, `onSupprimer`…).
  Il a un `@Preview` dans chaque étape.

D'une étape à l'autre, c'est surtout `TodosRoute` qui change ; `TodosEcran` ne change que par ses paramètres
(une liste, puis un `TodosUiState`). Seuls états gardés dans `TodosEcran` : l'ouverture de la boîte de dialogue
et le texte saisi, qui n'intéressent personne d'autre.

Ce projet se situe entre deux autres :

- **`tp04-client-todos`** (jour 1) : Retrofit sans ViewModel ;
- **`tp07-todos-mvvm-retrofit`** : la version complète (un fichier par classe, `AppContainer`, DTO séparés du
  domaine, erreurs du domaine, 14 tests).

## Exécuter une étape

Chaque étape est dans son package `sujetN` (`app/src/main/java/fr/formation/todoshttp/sujetN/`), avec un seul
fichier `MainActivityN.kt`, complet et lisible de haut en bas. Le commentaire en tête de fichier dit ce qui est
nouveau et ce qu'il faut essayer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité de l'étape** et laisser les autres en
commentaire, puis lancer l'application.

Prérequis (à partir de l'étape 1) : JSON Server lancé (Node.js requis).

```bash
cd json-server
npx json-server db.json            # → http://localhost:3000/todos
```

**Version de JSON Server** : le code fonctionne avec la 1.x (`npx json-server`, `"id": "1"`) et avec la 0.17
(`npx json-server@0.17`, `"id": 1`) grâce à `isLenient = true`. Seule différence : à l'étape 6, la 0.17 ignore
`title:contains` ; remplacer par `title_like` dans `TodoApi`.

L'application appelle `http://10.0.2.2:3000/`, c'est-à-dire le poste de dev vu depuis l'**émulateur**.
JSON Server réécrit `db.json` à chaque modification ; pour repartir de zéro : `cp db.original.json db.json`.

## Les étapes

| Étape | Notion | Ce qu'on observe (Logcat, filtre `okhttp`) |
|---|---|---|
| `sujet0` | Affichage : `data class Todo`, liste en dur, `LazyColumn` + `ListItem` ; `Scaffold` + bouton flottant + `AlertDialog` ; state hoisting | La liste s'affiche sans serveur ; ajouter / supprimer modifient un état local, ❌ perdu après une rotation |
| `sujet1` | Retrofit dans le composable, avec `LaunchedEffect` | ❌ Un nouveau `GET` à **chaque rotation** ; réseau, erreurs et affichage mélangés |
| `sujet2` | Un `ViewModel` fait l'appel (`viewModelScope`, `StateFlow<List<Todo>>`) | ✅ Un seul `GET`, même après plusieurs rotations ; ❌ les erreurs ne sont que dans Logcat |
| `sujet3` | État d'écran `sealed` : `Chargement` / `Succes` / `Erreur` | Arrêter JSON Server : « Serveur injoignable… » + « Réessayer » |
| `sujet4` | Un `TodoRepository` qui renvoie un `Result` ; ViewModel avec paramètre → `Factory` | Même écran qu'à l'étape 3 : seule la couche data a changé |
| `sujet5` | `PATCH` (cocher) ; try / catch écrit une fois ; liste mise à jour avec la réponse du serveur ; échec → Snackbar ; suppression par glissement + « Annuler » | Cocher, ajouter, supprimer, et regarder `db.json` changer |
| `sujet6` | Recherche côté serveur : `title:contains`, `debounce(300)`, `SavedStateHandle` | Taper vite : **un seul** `GET` ; après la mort du processus, la recherche est restaurée |

Les étapes 5 et 6 partent toutes les deux de l'étape 4 : l'étape 6 ne reprend ni le `PATCH`, ni la Snackbar, ni « Annuler » de l'étape 5,
pour rester courte.

## Ce qui change d'une étape à l'autre

```text
0  Composable ◀── listOf(Todo(…), …)   (en dur)
1  Composable ──────────────────────────────────────────▶ Retrofit
2  Composable ──▶ ViewModel (StateFlow<List<Todo>>) ────▶ Retrofit
3  Composable ──▶ ViewModel (StateFlow<TodosUiState>) ──▶ Retrofit
4  Composable ──▶ ViewModel ──▶ TodoRepository (Result) ──▶ Retrofit
5  + ajouter / basculer / supprimer, message en Snackbar
6  + recherche : SavedStateHandle → debounce → GET ?title:contains=
```

## Conseils de démonstration

- **Étapes 0 et 1** : comparer les deux fichiers. `TodosEcran` ne gagne qu'un paramètre `erreur` ; c'est
  `TodosRoute` qui change : la liste en dur est remplacée par `TodoApi` + `Reseau` + `LaunchedEffect`, et
  ajouter / supprimer deviennent des appels `POST` / `DELETE`.
- **State hoisting** : ouvrir le `@Preview` de `TodosEcran` dans n'importe quelle étape. Il s'affiche sans
  émulateur ni serveur, parce que l'écran ne dépend que de ses paramètres.
- **Étapes 1 et 2** : faire tourner l'émulateur deux fois et compter les `GET` dans Logcat (3 contre 1).
- **Étape 3** : arrêter JSON Server (Ctrl+C), relancer l'application, relancer le serveur, puis « Réessayer ».
- **Étape 4** : comparer avec l'étape 3. Le `when` du composable n'a pas changé ; le `try`/`catch` est passé du
  ViewModel au repository.
- **Étape 5** : supprimer en glissant vers la gauche, puis « Annuler » : aucun `DELETE` dans Logcat. Sans
  annuler, le `DELETE` part à la disparition de la Snackbar (environ 10 s).
- **Étape 5** : ouvrir `json-server/db.json` à côté de l'émulateur. Arrêter le serveur et cocher une tâche :
  une Snackbar s'affiche et la liste ne change pas.
- **Étape 6** : taper « quis » d'un coup. Pour la mort du processus : touche Accueil, *Terminate Application*
  dans Logcat (ou `adb shell run-as fr.formation.todoshttp kill -9 $(adb shell pidof fr.formation.todoshttp)`),
  puis revenir par les applications récentes.

## La suppression, d'une étape à l'autre

Ajouter (`POST /todos`) suit le même chemin. À partir de l'étape 1, la tâche n'entre dans la liste qu'avec la
réponse du serveur, qui contient son `id`. En cas d'échec, l'ajout est traité comme la suppression.

| Étape | Où est le code de la suppression | En cas d'échec |
|---|---|---|
| 0 | `todos = todos - todo` dans l'Activity (état local) | — |
| 1 | `scope.launch { DELETE }` dans le composable, try / catch recopié | Texte d'erreur |
| 2, 3 | `viewModel.supprimer(todo)` : DELETE, puis la liste du `StateFlow` est mise à jour | Logcat seulement |
| 4, 6 | `repository.supprimer(id)` renvoie un `Result<Unit>` | Logcat seulement |
| 5 | Retrait immédiat de l'écran, Snackbar « Annuler », DELETE envoyé quand la Snackbar disparaît | Tâche remise à sa place + Snackbar |

**Ergonomie** (étape 5) : le glissement (`SwipeToDismissBox`) est un **raccourci**, la corbeille reste visible.
Un geste ne se voit pas à l'écran et TalkBack ne le propose pas. Pas de boîte de confirmation : « Annuler » dans
la Snackbar est moins pénible et protège autant. Limite : fermer l'écran pendant la Snackbar → le DELETE n'est
pas envoyé.

## Choix assumés

- **Un seul fichier par étape** : modèle, API, repository, état, ViewModel et écran se suivent, séparés par des
  bannières `// ---------- … ----------`. Dans un vrai projet : un fichier par classe, rangé par couche.
  En tête de chaque `MainActivityN.kt`, un bloc « Organisation en fichiers » donne l'arborescence (`data/`,
  `data/remote/`, `ui/todos/`…) ; une ligne `// 📄 chemin/Fichier.kt` précède chaque déclaration.
- **Duplication volontaire** : chaque étape redéclare `Todo`, `TodoApi` et `Reseau` dans son propre package,
  pour ne jamais avoir à changer de fichier.
- **`Todo` sert à la fois de DTO et de modèle** : la séparation DTO / domaine est montrée dans
  `tp07-todos-mvvm-retrofit`.
- **Pas de tests** dans ce projet : ils sont dans `tp07-todos-mvvm-retrofit`.
