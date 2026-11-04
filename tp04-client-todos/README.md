# TP04 — Client REST d'une liste de tâches (corrigé)

Jour 1, chapitre « Utiliser des services Web » (`slides/04-services-web.md`).

## Objectif

Écrire un client Android qui parle à une API REST **locale** (JSON Server servant les données de JSONPlaceholder) :
DTO `kotlinx.serialization`, interface Retrofit, repository qui renvoie des `Result` en traduisant les exceptions
techniques en erreurs du domaine, sans jamais avaler `CancellationException`. L'affichage reste volontairement
simple (niveau jour 1 : pas de ViewModel, qui arrive au jour 3).

## Correspondance étape → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/clienttodos/`.

| Étape de l'énoncé | Fichier(s) |
|---|---|
| 1. `db.json`, JSON Server, `todos.http` | `json-server/db.json`, `json-server/db.original.json`, `json-server/todos.http` |
| 2. Permission, sécurité réseau, dépendances | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/network_security_config.xml`, `gradle/libs.versions.toml`, `build.gradle.kts`, `app/build.gradle.kts` (`buildConfigField API_URL`) |
| 3. DTO, modèle, mappers | `data/remote/dto/TodoDto.kt` (`TodoDto`, `NouveauTodoDto`, `MajTodoDto`), `data/remote/dto/UtilisateurDto.kt`, `domain/model/Todo.kt`, `data/remote/Mappers.kt` |
| 4. `TodoApi` et `TodoRepository` | `data/remote/TodoApi.kt`, `data/remote/Reseau.kt` (Json, OkHttp, Retrofit), `data/TodoRepository.kt`, `data/TodoRepositoryImpl.kt`, `data/ErreurTodo.kt`, `data/Resultat.kt` (`resultatDe`) |
| 5. Liste de l'utilisateur 1 dans un `Text` avec `LaunchedEffect` | `ui/TodosEcran.kt` (haut de l'écran), `MainActivity.kt` |
| 6. Ajouter / terminer / supprimer | `ui/TodosEcran.kt` (boutons *Ajouter*, *Basculer*, *Renommer* (PUT), *Supprimer*) |
| 7. Cas d'erreur | `ui/TodosEcran.kt` (boutons *Lire 9999*, *Supprimer 9999*), `ui/MessagesErreur.kt` |
| 8. Bonus `async` | `TodoRepositoryImpl.lireAvecAuteur`, `domain/model/Utilisateur.kt` (`TodoAvecAuteur`), bouton *Tâche 1 + auteur* |
| 8. Bonus pagination | `data/remote/dto/PageDto.kt`, `TodoApi.listerPage`, `domain/model/PageTodos.kt`, boutons ◀ ▶ |

Tests (`app/src/test/…`) :

| Fichier | Ce qui est vérifié |
|---|---|
| `data/remote/MappersTest.kt` | DTO ↔ domaine, enveloppe → `PageTodos` |
| `data/remote/DtoJsonTest.kt` | `id` en chaîne, `ignoreUnknownKeys`, `coerceInputValues`, `explicitNulls = false` (PATCH), enveloppe de pagination |
| `data/remote/TodoApiTest.kt` | Requêtes réellement émises par Retrofit (méthode, URL, corps) avec **MockWebServer** |
| `data/ResultatDeTest.kt` | `resultatDe` relance `CancellationException`, `runCatching` l'avale |
| `data/TodoRepositoryImplTest.kt` | MockK : succès, `ConnectException` → `Reseau`, 404 → `Introuvable`, 500 → `Serveur`, JSON invalide → `Donnees`, annulation propagée, PATCH avec le seul `completed`, DELETE 404, **parallélisme** de `lireAvecAuteur` (1 s de temps virtuel au lieu de 2) |
| `ui/MessagesErreurTest.kt` | Messages affichés à l'utilisateur |
| `data/remote/JsonServerIntegrationTest.kt` | **Contre un vrai JSON Server** sur `localhost:3000` : cycle POST → PATCH → PUT → DELETE → 404, filtres, recherche, pagination. **Ignoré** si le serveur n'est pas lancé |

Test instrumenté (`app/src/androidTest/…/TodosEcranTest.kt`) : l'écran avec un faux repository (à lancer sur un émulateur).

## Lancer le projet

### 1. JSON Server

```bash
cd json-server
npx json-server db.json            # → http://localhost:3000/todos, http://localhost:3000/users
```

Testé avec **json-server 1.0.0-beta.15** et Node 22. Pour régénérer `db.json` depuis JSONPlaceholder
(procédure de la slide « JSON Server : une API REST locale ») :

```bash
curl -o todos.json https://jsonplaceholder.typicode.com/todos
curl -o users.json https://jsonplaceholder.typicode.com/users
node -e 'const fs = require("fs"); fs.writeFileSync("db.json", JSON.stringify(
  { todos: require("./todos.json"), users: require("./users.json") }, null, 2))'
cp db.json db.original.json
```

Repartir des données d'origine : `cp db.original.json db.json` (le serveur surveille le fichier).

### 2. L'application

Ouvrir le dossier `tp04-client-todos` dans Android Studio, lancer sur un **émulateur** : l'URL
`http://10.0.2.2:3000/` (définie dans `app/build.gradle.kts`) désigne le `localhost` du poste.

- Appareil USB : `adb reverse tcp:3000 tcp:3000`, puis remplacer l'URL par `http://localhost:3000/`.
- Appareil Wi-Fi : `npx json-server db.json --host 0.0.0.0`, URL `http://<IP du poste>:3000/` **et** ajouter cette IP
  dans `network_security_config.xml`.

Les requêtes et réponses complètes apparaissent dans **Logcat** (filtre `okhttp`) grâce à `HttpLoggingInterceptor`
(niveau `BODY`, en debug uniquement).

### 3. Vérification en ligne de commande

```bash
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest --console=plain
```

Lancer JSON Server **avant** pour que `JsonServerIntegrationTest` s'exécute (sinon il est marqué *skipped*).
Il crée et supprime sa propre tâche : `db.json` garde ses 200 tâches (mais JSON Server le réécrit, voir plus bas).

## Manipulations pas à pas

### Étape 1 — Tester les routes avant de coder

1. Lancer `npx json-server db.json` dans `json-server/`.
2. Ouvrir `json-server/todos.http` dans Android Studio et cliquer sur ▶ devant chaque requête, dans l'ordre.
3. Observer : `"id": "1"` (chaîne), l'enveloppe de `_page`, `[]` pour `_limit`, `201` et l'id généré pour le `POST`.
4. Le script `> {% client.global.set("id", response.body.id); %}` mémorise l'id créé pour le `PATCH`, le `PUT`
   et le `DELETE` suivants. Si votre client HTTP ne gère pas ces scripts, remplacez `{{id}}` à la main.
5. Après chaque écriture, ouvrir `db.json` : la tâche apparaît, change, puis disparaît.

> À la première écriture, JSON Server **réécrit** tout `db.json` : les `id` numériques d'origine deviennent des
> chaînes (`"id": 1` → `"id": "1"`). C'est normal ; `db.original.json` garde la version initiale.

### Étape 6 — Ajouter, terminer, supprimer, vérifier dans `db.json`

1. Dans l'app, saisir un titre et appuyer sur **Ajouter** : le message affiche `POST : créée avec l'id …`.
   La tâche apparaît en bas de la liste de l'utilisateur 1. Dans `db.json`, chercher le titre (`⌘F`) :
   `"userId": 1, "title": "…", "completed": false, "id": "…"` en fin du tableau `todos`.
2. **Basculer** : `PATCH : terminee = true`, la case devient ☑, et dans `db.json` `"completed": true`
   **sans que le titre ait disparu** (c'est le rôle d'`explicitNulls = false` : le corps envoyé est `{"completed":true}`,
   visible dans Logcat).
3. **Renommer** (bonus PUT) : modifier le champ titre puis appuyer : le titre change dans `db.json`, l'id est conservé.
4. **Supprimer** : `DELETE : tâche … supprimée`, la tâche disparaît de la liste et de `db.json`.

### Étape 7 — Provoquer les erreurs

| Manipulation | Ce que l'on voit | Exception d'origine |
|---|---|---|
| Arrêter JSON Server (`Ctrl+C`), puis **Recharger** | `Erreur : Serveur injoignable : JSON Server est-il lancé ? (ConnectException : Failed to connect to /10.0.2.2:3000)` | `ConnectException` (`IOException`) → `ErreurTodo.Reseau` |
| Relancer le serveur, bouton **Lire 9999** | `GET /todos/9999 — Tâche introuvable (404)` | `HttpException` 404 → `ErreurTodo.Introuvable` |
| Bouton **Supprimer 9999** | `DELETE /todos/9999 — Tâche introuvable (404)` | `Response<Unit>` non `isSuccessful` → `HttpException` levée par le repository |
| Retirer `android:networkSecurityConfig="@xml/network_security_config"` du manifest, réinstaller, **Recharger** | `… (UnknownServiceException : CLEARTEXT communication to 10.0.2.2 not permitted by network security policy)` | Trafic HTTP en clair bloqué depuis Android 9 |
| Mode avion sur l'émulateur | Selon la version : `ConnectException` ou `SocketTimeoutException` (≈ 5 s, `connectTimeout`) | `IOException` → `ErreurTodo.Reseau` |
| (facultatif) Passer `TodoDto.id` en `Int` | `Réponse inattendue du serveur (… JsonDecodingException …)` | `SerializationException` → `ErreurTodo.Donnees` |

La trace complète de chaque erreur est dans Logcat (tag `TodosEcran`) : l'`ErreurTodo` garde la cause technique.
Pensez à remettre le `network_security_config` après l'essai.

### Étape 8 — Bonus

- **Tâche 1 + auteur (async)** : affiche `delectus aut autem — Leanne Graham (xx ms)`. Dans Logcat, les deux requêtes
  `GET /todos/1` et `GET /users/1` partent **avant** la première réponse. Le test
  `lireAvecAuteur lance les deux requêtes en parallèle` le prouve : deux appels de 1 s prennent 1 s de temps virtuel.
- **Pagination** : ◀ / ▶ parcourent `GET /todos?_page=N&_per_page=10`. `PageDto<T>` décrit l'enveloppe
  `{first, prev, next, last, pages, items, data}` ; `prev` / `next` à `null` désactivent les boutons aux extrémités.

## Points pédagogiques

- **Trois formats, trois classes** : `TodoDto` (lecture), `NouveauTodoDto` (POST/PUT, sans id), `MajTodoDto` (PATCH,
  champs nullables). Le domaine `Todo` ne dépend ni de Retrofit ni de kotlinx.serialization.
- **`id: String`** : imposé par JSON Server 1.x. Conséquence inattendue : `_sort=-id` trie des **chaînes**
  (`"9"` avant `"20"`) — essayez la requête dans `todos.http`.
- **Configuration `Json`** : `ignoreUnknownKeys` (sinon `address` fait planter `/users`), `coerceInputValues`,
  `explicitNulls = false` (sinon un PATCH enverrait `"title": null` et effacerait le titre). Chaque option a son test.
- **`title:contains`** : Retrofit encode le `:` en `%3A` (`?title%3Acontains=dolor`) ; JSON Server le décode
  correctement (vérifié avec curl et le test d'intégration).
- **Traduction des erreurs** : `lister` montre la version développée de la slide (un `catch` par type) ; les autres
  méthodes réutilisent `resultatDe` puis `traduireErreur()`. Dans les deux cas, `CancellationException` est relancée.
- **`Response<Unit>`** pour `DELETE` : pas d'exception automatique, c'est au repository de tester `isSuccessful`.
- **`async` sous `coroutineScope`** : si l'une des deux requêtes échoue, l'autre est annulée et l'erreur remonte une
  seule fois dans le `Result`. Le parallélisme n'est possible que si l'on connaît l'id de l'auteur à l'avance ;
  sinon il faut d'abord lire la tâche (`userId`), puis l'utilisateur.
- **Niveau jour 1** : l'état est dans des `remember` et disparaît à la rotation (tout est rechargé). C'est exactement
  le problème que résoudra le ViewModel au jour 3 (TP07).
- **MockWebServer** (dépendance de test en plus de l'énoncé) : il vérifie ce que Retrofit envoie vraiment sur le
  réseau, ce qu'un mock MockK de `TodoApi` ne peut pas faire.

## Versions

AGP 9.5, Kotlin 2.2.10, Compose BOM 2026.02.01, Retrofit 3.0.0, OkHttp 4.12.0 (versions des slides),
kotlinx-serialization-json **1.9.0** (et non 1.12.0 comme sur la slide : la 1.12 dépend de `kotlin-stdlib` 2.4,
illisible par le compilateur Kotlin 2.2.10 du projet), kotlinx-coroutines-test 1.11.0, MockK 1.14.11.
