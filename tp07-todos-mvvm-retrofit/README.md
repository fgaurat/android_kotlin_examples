# TP07 variante — Liste de tâches en MVVM avec Retrofit

Jour 3, chapitre « Architecture MVVM » (`slides/07-mvvm.md`).

C'est la réunion de deux projets :

- **`tp04-client-todos`** (jour 1) : la couche data Retrofit (DTO, `TodoApi`, repository, erreurs du domaine),
  mais l'écran appelait le repository lui-même, avec `LaunchedEffect` ;
- **`tp07-taches-mvvm`** (jour 3) : l'architecture MVVM, mais avec des données en mémoire.

Ici, la couche data du TP04 passe **derrière un ViewModel**. Le code reprend celui des slides du chapitre 07 :
`fr.formation.todoapp`, `AppContainer`, `TodoApplication`, `TodosViewModel` et `TodosUiState` en `sealed interface`.

## Lancer le projet

1. JSON Server (Node.js requis) :

   ```bash
   cd json-server
   npx json-server db.json            # → http://localhost:3000/todos
   ```

   JSON Server réécrit `db.json` à chaque modification. Pour repartir des données d'origine :
   `cp db.original.json db.json`.

2. Ouvrir `tp07-todos-mvvm-retrofit` dans Android Studio et lancer sur un **émulateur**
   (l'URL `http://10.0.2.2:3000/` désigne le poste de dev). Sur un appareil USB : `adb reverse tcp:3000 tcp:3000`
   et `API_URL` = `http://localhost:3000/` dans `app/build.gradle.kts`.

```bash
./gradlew assembleDebug testDebugUnitTest --console=plain     # 14 tests, sans serveur
```

## Organisation du code

Chemins relatifs à `app/src/main/java/fr/formation/todoapp/`.

```text
TodoApplication.kt         crée l'AppContainer au démarrage (déclarée dans le manifest)
AppContainer.kt            Json + OkHttp + Retrofit + TodoRepositoryImpl : tout est construit ici
MainActivity.kt            setContent { TodosRoute() } : plus de repository dans l'Activity
domain/
  Todo.kt                  modèle métier, Kotlin pur
data/
  remote/TodoApi.kt        GET (userId, title:contains), POST, PATCH, DELETE
  remote/dto/TodoDto.kt    TodoDto, NouveauTodoDto, MajTodoDto (copiés du TP04)
  remote/Mappers.kt        TodoDto → Todo
  TodoRepository.kt        le contrat vu par le ViewModel : Result<…>
  TodoRepositoryImpl.kt    appelle l'API, traduit les exceptions en ErreurTodo
  ErreurTodo.kt, Resultat.kt   (copiés du TP04)
ui/todos/
  TodosUiState.kt          Chargement | Succes(todos) | Erreur(message)
  TodosViewModel.kt        état, recherche (SavedStateHandle + debounce), actions, Factory
  TodosRoute.kt            le seul composable qui connaît le ViewModel
  TodosScreen.kt           écran sans état : état en entrée, callbacks en sortie, 4 previews
  MessagesErreur.kt        ErreurTodo → texte pour l'utilisateur
```

```text
TodosScreen ──événements──▶ TodosRoute ──▶ TodosViewModel ──suspend──▶ TodoRepository ──▶ TodoApi (Retrofit)
     ▲                                          │                        Result<Todo>            │
     └──── état (StateFlow<TodosUiState>) ──────┘                                          JSON Server
```

## Ce que montre le ViewModel

| Besoin | Code dans `TodosViewModel` |
|---|---|
| État de l'écran | `_etat = MutableStateFlow<TodosUiState>(Chargement)` privé, `etat` public en lecture seule |
| Chargement | `charger()` : `Chargement`, puis `repository.lister(…).fold(Succes, Erreur)` ; une nouvelle requête **annule** la précédente (`Job.cancel()`) |
| Recherche côté serveur | `savedStateHandle.getStateFlow("recherche", "")` → `debounce(300)` → `GET /todos?title:contains=…` |
| Survie à la mort du processus | la recherche est dans le `SavedStateHandle` : restaurée et rejouée sans attendre 300 ms |
| Actions | `ajouter` (POST), `basculer` (PATCH), `supprimer` (DELETE) : en cas de succès, la liste affichée est mise à jour avec la réponse du serveur, **sans tout recharger** |
| Échec d'une action | `message: StateFlow<String?>` : l'écran l'affiche en Snackbar, puis appelle `messageAffiche()` |
| Erreur de chargement | `TodosUiState.Erreur(message)` + bouton « Réessayer » → `rafraichir()` |
| Dépendances | `Factory` : `viewModelFactory { initializer { … app.container.todoRepository, createSavedStateHandle() } }` |

## Démonstration

| Manipulation | Ce qu'on observe |
|---|---|
| Lancer l'app | Indicateur de chargement, puis les 20 tâches de l'utilisateur 1. Logcat, filtre `okhttp` : `GET /todos?userId=1` |
| Cocher une tâche | `PATCH /todos/1` avec `{"completed":true}` ; vérifier dans `db.json` |
| Ajouter « Tester le ViewModel » | `POST /todos` ; la tâche apparaît en fin de liste, avec l'id choisi par le serveur |
| Taper vite « porro » | **Une seule** requête `GET …title%3Acontains=porro`, 300 ms après la dernière lettre |
| Tourner l'écran | Aucune requête : le ViewModel a survécu, la liste et la recherche sont conservées |
| Touche Accueil, puis tuer le processus (*Terminate Application* dans Logcat, ou `adb shell run-as fr.formation.todoapp kill -9 $(adb shell pidof fr.formation.todoapp)`), puis revenir par les applications récentes | La recherche est restaurée et la requête rejouée **immédiatement** ; la liste, elle, est rechargée depuis le serveur |
| Arrêter JSON Server, puis « Recharger » | `Serveur injoignable : JSON Server est-il lancé ? (ConnectException …)` et bouton « Réessayer » |
| Serveur arrêté, cocher une tâche | La liste ne change pas ; Snackbar « Modification impossible : … » |

Tout cela a été vérifié sur un émulateur API 34 avec JSON Server.

## Tests unitaires (`app/src/test/java/fr/formation/todoapp/`)

| Fichier | Ce qui est vérifié |
|---|---|
| `MainDispatcherRule.kt` | Remplace `Dispatchers.Main` (absent sur la JVM) |
| `data/FakeTodoRepository.kt` | Faux repository : latence en temps virtuel, erreur simulée, requêtes enregistrées |
| `data/TodoRepositoryImplTest.kt` | Avec une fausse `TodoApi` (MockK) : conversion DTO → domaine, recherche vide omise, PATCH, `ConnectException` → `Reseau`, 404 → `Introuvable`, l'annulation n'est pas avalée |
| `ui/todos/TodosViewModelTest.kt` | `Chargement` puis `Succes` ; `Erreur` puis « Réessayer » ; **debounce** (rien à 299 ms, une requête à 300 ms) ; recherche restaurée après la mort du processus ; bascule avec la réponse du serveur ; ajout (titre vide ignoré) ; suppression ; échec d'une suppression → liste inchangée + message consommé |

## Choix assumés

- **Pas de `Flow` dans le repository** : une API REST répond à une requête, elle ne prévient pas quand les données
  changent. Le repository garde donc des fonctions `suspend` qui renvoient un `Result`, comme au TP04, et c'est
  le ViewModel qui tient l'état dans un `StateFlow`. Avec Room (jour 4), le repository exposera un
  `Flow<List<Todo>>` et le ViewModel utilisera `combine` + `stateIn`, comme dans `tp07-taches-mvvm`.
- **Pas de mise à jour optimiste** : la case ne se coche qu'après la réponse du serveur, qui reste la source de vérité.
- **Un seul utilisateur** (`TodosViewModel.UTILISATEUR_ID = 1`), pour ne pas ajouter de navigation.
- **Textes en dur en français**, sans `strings.xml`, pour garder l'écran lisible ; la traduction est vue au jour 2.
- Les messages d'échec d'une action sont un **état consommé** (`message` + `messageAffiche()`), pas un
  `SharedFlow` : un événement émis pendant une rotation serait perdu.
