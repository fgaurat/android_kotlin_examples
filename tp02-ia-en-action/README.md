# TP 02 — L'IA en action (corrigé)

Corrigé du TP « Travaux pratiques — L'IA en action » (fin de `slides/02-outils-ia.md`).

## Objectif

Utiliser Gemini in Android Studio de façon **critique** : configurer l'exclusion des fichiers sensibles,
faire expliquer du code, générer puis **vérifier et corriger** du code, exploiter *Ask Gemini* sur un
plantage, repérer une réponse datée et reformuler le prompt, comparer plusieurs assistants.

Ce projet représente l'**état final d'une session réussie** : le code généré a été relu, corrigé et testé.
Domaine utilisé : les tâches (*todos*) de <https://jsonplaceholder.typicode.com/todos>, le même que dans
la suite de la formation.

## Correspondance étape → fichiers

| Étape | Ce qui reste dans le code | Fichier(s) |
|---|---|---|
| 1. Gemini + `.aiexclude` | Fichier d'exclusion à la racine du projet | `.aiexclude` |
| 2. Expliquer `MainActivity.kt` / `enableEdgeToEdge()` | Manipulation d'IDE ; réponse attendue ci-dessous | `app/src/main/java/fr/formation/iaenaction/MainActivity.kt` |
| 3. `data class Todo` + `resume()` vérifiées, test JUnit relu | Réponse fautive de l'IA en commentaire, version corrigée (`id: String`, pluriel juste), correspondance JSON → Kotlin à la main | `app/src/main/java/fr/formation/iaenaction/todo/Todo.kt`, `app/src/main/java/fr/formation/iaenaction/todo/TodoJson.kt`, `app/src/test/java/fr/formation/iaenaction/todo/ResumeTest.kt`, `app/src/test/java/fr/formation/iaenaction/todo/TodoJsonTest.kt` |
| 4. Plantage volontaire + *Ask Gemini* | Carte rouge « Provoquer un plantage » (`throw IllegalStateException("test")`) | `MainActivity.kt` (`BoutonPlantage`) |
| 5. Requête HTTP moderne vers `/todos/1` | `suspend fun` + `withContext(Dispatchers.IO)` + `HttpURLConnection`, appelée depuis l'UI via `rememberCoroutineScope()` ; permission `INTERNET` | `app/src/main/java/fr/formation/iaenaction/todo/TodoClient.kt`, `MainActivity.kt`, `app/src/main/AndroidManifest.xml`, `app/src/test/java/fr/formation/iaenaction/todo/TodoClientTest.kt` |
| 6. Comparer les assistants | Grille à remplir ci-dessous | `README.md` |

Dépendances ajoutées (catalogue `gradle/libs.versions.toml`) :
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0` (déjà présent en transitif via `lifecycle-runtime-ktx`, déclaré explicitement car on l'utilise directement) ;
- `org.json:json:20260814` en **`testImplementation`** uniquement : sur l'appareil, `org.json` est fourni par Android, mais l'`android.jar` utilisé par les tests JVM n'en contient que des bouchons (« *Method … not mocked* »).

## Lancer le projet

```bash
./gradlew assembleDebug           # APK de debug
./gradlew testDebugUnitTest       # 25 tests JVM : resume(), analyse JSON, client HTTP (serveur local)
./gradlew installDebug            # installe sur l'émulateur / l'appareil
```

L'écran affiche trois tâches fictives et leur résumé (« 3 tâches, 1 terminée »), une carte
« Tâche distante » (bouton **Charger /todos/1** → requête réelle vers JSONPlaceholder, affichage de la
tâche ou de l'erreur) et, en bas, une carte rouge signalant le **plantage volontaire**.

## Manipulations pas à pas

### Étape 1 — Activer Gemini et configurer `.aiexclude`

1. Icône **Gemini** dans la barre latérale droite → **Sign in** avec un compte Google.
2. Accepter les conditions ; dans **Settings → Tools → Gemini** (ou à la première utilisation),
   choisir d'activer ou non le **partage du contexte du projet**.
3. Créer `.aiexclude` à la racine du projet (voir le fichier fourni : `local.properties`, keystores,
   `secrets.properties`, `google-services.json`…).
4. Vérifier : ouvrir `local.properties`, sélectionner une ligne et demander à Gemini de l'expliquer.
   Gemini doit indiquer que le fichier est exclu (et l'éditeur n'affiche plus de suggestions dans ce fichier).

> `.aiexclude` empêche **Gemini** de lire ces fichiers, mais ne protège ni contre les autres assistants
> (Copilot a ses propres réglages d'exclusion de contenu) ni contre un copier-coller manuel dans ChatGPT.

### Étape 2 — Faire expliquer `MainActivity.kt`

Sélectionner la classe → clic droit → **Gemini → Explain Code**. Points que la réponse doit contenir
(sinon, relancer : « *Explique le rôle de `enableEdgeToEdge()` sur Android 15 et suivants* ») :
- `ComponentActivity` est l'activité de base pour Compose ; `setContent { }` remplace `setContentView`.
- `enableEdgeToEdge()` dessine l'application **sous** la barre d'état et la barre de navigation
  (barres transparentes, couleur des icônes adaptée au thème).
- Sur Android 15 (API 35) et plus, avec `targetSdk ≥ 35`, l'edge-to-edge est **imposé** : l'appel
  garantit un comportement identique sur les versions antérieures.
- Conséquence : il faut gérer les **insets**. Ici, `Scaffold` fournit `innerPadding`, qu'on applique au contenu.

### Étape 3 — Générer puis vérifier `Todo` et `resume()`

**Prompt vague (pour voir ce que l'IA devine) :**

> *Fais une classe pour les todos.*

Réponse fréquente : propriétés en anglais, `id: Int`, parfois un `description` ou une `date` qui
n'existent pas dans l'API, aucune fonction de résumé. L'IA invente faute de contexte.

**Prompt précis (celui du support) :**

> *Écris une data class Kotlin `Todo` qui correspond au JSON de `https://jsonplaceholder.typicode.com/todos` (`userId`, `id`, `title`, `completed`), avec des propriétés en français. Ajoute une fonction d'extension qui résume une liste : « 3 tâches, 1 terminée ».*

**Réponse type de l'IA** (conservée aussi en commentaire dans `Todo.kt`) :

```kotlin
data class Todo(
    val id: Int,
    val titre: String,
    val terminee: Boolean,
    val utilisateurId: Int,
)

fun List<Todo>.resume(): String {
    val terminees = count { it.terminee }
    return "$size tâches, $terminees terminées"
}
```

**Vérification sur le JSON réel** (`curl -s https://jsonplaceholder.typicode.com/todos/1`, le 01/11/2026) :

```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

`curl -s -o /dev/null -w "%{http_code}" https://jsonplaceholder.typicode.com/todos/999` → `404` (corps `{}`).

**Pièges repérés et corrections :**

| Piège dans la réponse | Pourquoi c'est faux | Correction retenue |
|---|---|---|
| `id: Int` | Juste pour JSONPlaceholder (`1`), faux pour **JSON Server 1.x** utilisé plus tard dans la formation (`"1"`, et `"a3f9"` pour une tâche créée) | `id: String` ; le parseur accepte un entier ou une chaîne, refuse un décimal ou une chaîne vide |
| `"$size tâches, $terminees terminées"` | Donne « 1 tâches, 1 terminées », « 0 tâches, 0 terminées ». En français, **0 et 1 sont au singulier** | Fonction `accorder()` : pluriel à partir de 2 → « 0 tâche, 0 terminée », « 1 tâche, 1 terminée », « 3 tâches, 2 terminées » |
| Propriétés renommées (`titre`, `terminee`, `utilisateurId`) sans rien d'autre | Aucun décodeur ne saura que `title` devient `titre` : les champs resteraient vides ou le décodage échouerait | Correspondance faite **à la main** dans `TodoJson.depuis()` (au jour 3 : `@SerialName("title")` avec kotlinx.serialization) |
| Ordre des propriétés différent du JSON, pas de KDoc | Gêne la relecture | Ordre `userId, id, title, completed`, KDoc indiquant le champ JSON |

**Test JUnit généré puis relu** (clic droit sur `resume` → **Gemini → Generate Unit Tests**) :
le test proposé ne vérifiait que « 3 tâches, 1 terminée », cas qui ne détecte pas le problème de
« terminée(s) » pour 1. Ajouts après relecture : liste vide, une tâche, 0 / 1 / plusieurs terminées
(`ResumeTest`), et tests du parseur (`TodoJsonTest` : `id` entier, `id` chaîne, champ manquant, JSON invalide).

> Leçon : un test généré par la même IA que le code reproduit souvent ses angles morts. Les **cas limites**
> (0, 1, 2) sont à ajouter soi-même.

### Étape 4 — Plantage volontaire et *Ask Gemini*

1. Lancer l'application, appuyer sur **Provoquer un plantage** (carte rouge en bas) : l'application se ferme.
2. Dans **Logcat**, filtre `package:mine level:ERROR is:crash` : la trace
   `java.lang.IllegalStateException: test` apparaît, avec la ligne de `MainActivity.kt` en lien cliquable.
3. Clic sur l'icône **Ask Gemini** (ou clic droit sur la trace → *Ask Gemini*).
4. Ce qu'il faut observer : Gemini identifie l'exception, pointe le `onClick` de `BoutonPlantage`,
   et propose de supprimer le `throw` ou de gérer l'erreur. Discuter : l'IA ne peut pas savoir que le
   plantage est **volontaire** ; le contexte reste de notre responsabilité.
5. Bonus : couper le réseau de l'émulateur puis appuyer sur **Charger /todos/1** : pas de plantage, mais
   un message d'erreur à l'écran et une trace `E/IaEnAction` dans Logcat (*Ask Gemini* fonctionne aussi).

### Étape 5 — « Comment faire une requête HTTP en Android ? »

**Prompt vague (à poser tel quel) :**

> *Comment faire une requête HTTP en Android ?* (pour lire `https://jsonplaceholder.typicode.com/todos/1`)

Pièges à repérer dans la réponse :
- `AsyncTask` (déprécié depuis l'API 30) ou `Thread { }` + `runOnUiThread` ;
- appel réseau **sur le thread principal** → `NetworkOnMainThreadException` ;
- `GlobalScope.launch` (pas d'annulation, fuites) ;
- code Java ou API XML (`findViewById`, `TextView`) alors que le projet est en Kotlin + Compose ;
- `http://` au lieu de `https://`, ou suggestion d'`android:usesCleartextTraffic="true"` ;
- oubli de la permission `INTERNET` ;
- versions de bibliothèques inventées ou anciennes (Retrofit 2.3, OkHttp 3…), ajoutées sans le catalogue de versions ;
- `URL(...).readText()` sans timeout, sans gestion d'erreur, sans vérification du code HTTP (un 404 renvoie `{}`), sans fermeture de connexion.

**Prompt reformulé :**

> *Projet Android en Kotlin, Jetpack Compose, Material 3, `minSdk` 34, Gradle Kotlin DSL avec catalogue de versions `libs.versions.toml`. Écris une fonction `suspend fun lireTodo(id: String): Todo` qui appelle `https://jsonplaceholder.typicode.com/todos/{id}` et renvoie ma `data class Todo(utilisateurId: Int, id: String, titre: String, terminee: Boolean)` (JSON : `userId`, `id`, `title`, `completed` ; `id` peut être un nombre ou une chaîne). Contraintes : coroutines avec `withContext(Dispatchers.IO)`, jamais sur le thread principal, pas d'`AsyncTask`, de `Thread` ni de `GlobalScope`, pas de bibliothèque externe (`HttpURLConnection` + `org.json`), timeouts, fermeture de la connexion, exception explicite si le code HTTP n'est pas 200. Sépare l'analyse JSON dans une fonction pure testable. Montre l'appel depuis un composable avec `rememberCoroutineScope()` et un état chargement / succès / erreur. Indique la permission nécessaire.*

Variante pour la suite de la formation :

> *… même chose avec Retrofit et kotlinx.serialization (`@SerialName` pour `title` → `titre`), dans un `TodoRepository` appelé depuis un `ViewModel` (`viewModelScope`), dépendances ajoutées au catalogue de versions avec des versions stables actuelles.*

Résultat retenu :
- `TodoClient.lireTodo()` : transport (timeouts, code HTTP → `ErreurHttpException`, JSON illisible → `IOException`) ;
- `TodoJson.analyserTodo()` : analyse pure, testée sans réseau ;
- `EcranIaEnAction` dans `MainActivity.kt` : état `Inactif / Chargement / Succes / Erreur`.

`TodoClientTest` lance un mini-serveur local (`ServerSocket`) pour tester sans Internet : réponse 200,
404, 500, JSON illisible, serveur muet (délai dépassé), serveur injoignable.

Pour vérifier qu'on ne bloque pas le thread principal : poser un point d'arrêt dans `lireTodo`
après `withContext`, l'onglet **Frames/Threads** montre un thread `DefaultDispatcher-worker-…`.

### Étape 6 — Comparer les assistants

Poser le **même** prompt (vague puis reformulé) à chaque assistant, et remplir :

| Critère | Gemini in Android Studio | ChatGPT | GitHub Copilot |
|---|---|---|---|
| Langage / UI proposés (Kotlin + Compose ?) | | | |
| API dépréciée (`AsyncTask`, `GlobalScope`…) ? | | | |
| Réseau hors du thread principal ? | | | |
| Permission `INTERNET` mentionnée ? | | | |
| Gestion des erreurs, du code HTTP et des timeouts | | | |
| Type de `id` proposé (`Int` ou `String`) ; pluriel de `resume()` | | | |
| Correspondance `title` → `titre` traitée ? | | | |
| Bibliothèques proposées et versions (existent-elles ?) | | | |
| Respect des contraintes du prompt reformulé | | | |
| Le code compile-t-il tel quel ? | | | |
| Explications / sources citées | | | |
| Prise en compte du contexte du projet | | | |
| Temps pour obtenir un code utilisable | | | |
| Note globale /5 | | | |

## Points pédagogiques à souligner

- **Valider, comprendre, adapter** : la `data class` « avait l'air juste » ; seuls le **JSON réel** et les
  **tests des cas limites** révèlent le type de `id` et le pluriel faux.
- **Le contexte fait la qualité du prompt** : technologies, version minimale, contraintes, format attendu,
  et même la forme exacte des données.
- **Coroutines** : `suspend` + `withContext(Dispatchers.IO)` déplace le travail bloquant ; la portée
  (`rememberCoroutineScope`, puis `viewModelScope` au jour 3) gère l'annulation. Limite actuelle :
  l'état est perdu à la rotation de l'écran ; c'est précisément ce que règle le `ViewModel`.
- **Séparer l'analyse du transport** : `TodoJson` est un ensemble de fonctions pures, testables sans réseau.
- **Sécurité** : `.aiexclude`, HTTPS uniquement, aucune clé en dur, permissions minimales.
- **Un plantage volontaire se signale** : commentaire, texte d'avertissement à l'écran, et on ne le livre jamais.
