# TP 03 — Gestion de tâches (corrigé)

Corrigé du TP « Travaux pratiques — Gestion de tâches » (fin de `slides/03-kotlin.md`).

## Objectif

Pratiquer le Kotlin du chapitre 3 sur un domaine simple : `data class`, `enum class`, `sealed interface`,
`object`, propriétés et fonctions d'extension, fonctions de collections, `when` exhaustif, `copy()`,
fonctions d'ordre supérieur, lambdas et références, `buildString`, `Sequence`, tests JUnit.

Les données reprennent la structure de [`jsonplaceholder.typicode.com/todos`](https://jsonplaceholder.typicode.com/todos)
(`id`, `title`, `completed`, `userId`) **sans appel réseau** : 10 tâches réelles (id 1, 2, 4, 8, 21, 22, 26, 41, 43, 45),
3 utilisateurs, 5 terminées. L'`id` est une chaîne, comme avec JSON Server 1.x.

Le domaine est écrit en **Kotlin pur** (package `domain`, aucun import Android) : il se teste sur la JVM
en quelques secondes. `MainActivity` l'affiche en Compose pour que le TP « se voie ».

## Correspondance étape → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/gestiontaches/` (et `app/src/test/java/…` pour les tests).

| Étape | Ce qui est fait | Fichier(s) |
|---|---|---|
| 1. `Todo`, `Filtre`, `ResultatAction` | `data class Todo(id: String, titre, terminee, utilisateurId)`, `enum class Filtre { TOUTES, A_FAIRE, TERMINEES }`, `sealed interface ResultatAction` (`data class Succes(todo)`, `data object DejaTerminee`, `data object Introuvable`) | `domain/Modele.kt` |
| 2. `object TodoStore` de 10 tâches | 10 tâches copiées de JSONPlaceholder, utilisateurs 1 (×4), 2 (×3), 3 (×3), 5 terminées | `domain/TodoStore.kt` |
| 3. Extensions | `Todo.titreCourt` (propriété, 20 caractères au plus, `…` compris), `aFaire()`, `terminees()`, `selon(filtre)` (`when` sans `else`) | `domain/Extensions.kt` |
| 4. Calculs sur les collections | `parUtilisateur()` (`groupBy`), `nombreParUtilisateur()` (`groupingBy().eachCount()`), `utilisateurLePlusActif()` et `titreLePlusLong()` (`maxByOrNull`), `triees()` (`sortedWith(compareBy(Todo::terminee, Todo::titre))`), `indexParId()` (`associateBy`) | `domain/Extensions.kt` |
| 5. `terminer(id)` + message avec `when` | `TodoStore.terminer()` remplace la tâche par `todo.copy(terminee = true)` ; `messagePour(resultat)` choisit la chaîne de `strings.xml` avec un `when` exhaustif | `domain/TodoStore.kt`, `MainActivity.kt` |
| 6. `filtrer(critere)` + résumé | Fonction d'ordre supérieur ; appels avec lambda (`filtrer { it.utilisateurId == 2 }`), référence de fonction (`filtrer(::estAFaire)`) et de propriété (`filtrer(Todo::terminee)`) ; `resume()` avec `buildString` → `10 tâches, 5 terminées (50 %)` | `domain/TodoStore.kt`, `domain/Extensions.kt` |
| 7. Bonus : ajouter / supprimer | `MutableList` privée, `List` exposée (copie), `ajouter(titre, utilisateurId)`, `supprimer(id)`, `reinitialiser()` | `domain/TodoStore.kt` |
| 7. Bonus : `Sequence` sur 100 000 tâches | `genererTodos()`, `premieresAFaire()` (arrêt anticipé), `Sequence<Todo>.resume()` en un passage | `domain/GrandesListes.kt` |
| 7. Bonus : tests JUnit | 37 tests : store, extensions, séquence (dont la preuve de la paresse) | `domain/TodoStoreTest.kt`, `domain/ExtensionsTest.kt`, `domain/GrandesListesTest.kt` |
| Affichage | Résumé, recherches, carte `Sequence`, ajout, filtre (Toutes / À faire / Terminées), tâches groupées par utilisateur avec boutons Terminer / Supprimer et Snackbar | `MainActivity.kt`, `res/values/strings.xml` |

## Lancer le projet

```bash
./gradlew testDebugUnitTest     # 37 tests JVM, sans émulateur
./gradlew installDebug          # application de démonstration
```

Rapport HTML des tests : `app/build/reports/tests/testDebugUnitTest/index.html`.
Dans Android Studio : clic droit sur le dossier `app/src/test` → **Run 'Tests in …'**.

Dans l'application : « Terminer » sur une tâche à faire affiche le cas `Succes`, sur une tâche déjà barrée
le cas `DejaTerminee` ; « Id inconnu » affiche `Introuvable`.

### Variante Kotlin Playground

L'énoncé autorise `play.kotlinlang.org`. Copier le contenu de `Modele.kt`, `TodoStore.kt`,
`Extensions.kt` et `GrandesListes.kt` (sans les lignes `package` ; garder `import kotlin.math.roundToInt`
en tête), puis ajouter :

```kotlin
fun message(resultat: ResultatAction): String = when (resultat) {
    is ResultatAction.Succes -> "« ${resultat.todo.titreCourt} » est terminée"
    ResultatAction.DejaTerminee -> "Cette tâche est déjà terminée"
    ResultatAction.Introuvable -> "Tâche introuvable"
}

fun main() {
    val todos = TodoStore.todos
    println(todos.resume())                                   // 10 tâches, 5 terminées (50 %)
    println(todos.nombreParUtilisateur())                     // {1=4, 2=3, 3=3}
    println("Le plus actif : ${todos.utilisateurLePlusActif()}")
    println("Titre le plus long : ${todos.titreLePlusLong()?.titreCourt}")
    println(todos.triees().map { it.titreCourt })
    println(todos.selon(Filtre.A_FAIRE).map { it.id })
    listOf("1", "1", "999").forEach { println(message(TodoStore.terminer(it))) }   // Succes, DejaTerminee, Introuvable
    println(TodoStore.filtrer { it.utilisateurId == 2 }.map { it.titreCourt })
    println(TodoStore.filtrer(::estAFaire).map { it.id })
    println(TodoStore.ajouter("préparer le TP", 2))
    println(genererTodos(100_000).resume())                  // 100000 tâches, 25000 terminées (25 %)
}
```

Variante « `src/test` d'un nouveau projet » : c'est exactement ce que fait ce projet ; les tests jouent le rôle du `main`.

## Points pédagogiques à souligner

- **`enum` ou `sealed` ?** `Filtre` est un ensemble de **valeurs** ; `ResultatAction` un ensemble de
  **types** dont un seul porte une donnée (`data object` pour les autres : `toString()` lisible).
  Ajouter un cas à l'un ou à l'autre fait échouer la compilation de `selon()` ou `messagePour()` tant
  qu'il n'est pas traité : le `when` exhaustif sans `else` est un filet de sécurité.
- **Immuabilité** : `Todo` n'a que des `val` ; terminer remplace l'élément par `todo.copy(terminee = true)`.
  Le test `terminer_une_tache_a_faire_renvoie_Succes_avec_une_copie` vérifie que l'instance d'origine n'a pas bougé.
- **Encapsulation des collections** : `_todos` est une `MutableList` privée ; `todos` renvoie une
  **copie** (`toList()`). Un simple `get() = _todos` exposerait la même instance : un cast en
  `MutableList` permettrait de la modifier, et Compose ne détecterait pas de changement (même référence).
  Le test `la_liste_exposee_est_une_copie_qui_ne_modifie_pas_le_store` le démontre.
- **`object` et tests** : un singleton garde son état d'un test à l'autre, d'où `reinitialiser()` dans
  chaque `@Before`. C'est l'argument qui mènera à l'**injection de dépendances** (un `Repository` passé au `ViewModel`).
- **Extensions sur `List<Todo>`** plutôt que méthodes de `TodoStore` : les mêmes calculs servent pour le
  store, une liste filtrée, un groupe d'utilisateur, une liste vide ou les 100 000 tâches générées.
- **`groupBy` vs `groupingBy().eachCount()`** : le second compte sans construire les listes.
- **`maxByOrNull`** renvoie `null` sur une liste vide : le type `Int?` / `Todo?` oblige l'appelant à le traiter.
- **Tri multicritère** : `compareBy(Todo::terminee, Todo::titre)` ; `false` précède `true`, donc les tâches
  à faire viennent d'abord. Attention, la comparaison de `String` est sensible à la casse (ici tous les titres sont en minuscules).
- **Lambda, référence de fonction, référence de propriété** : `filtrer { … }`, `filtrer(::estAFaire)`,
  `filtrer(Todo::terminee)` ; une propriété `Boolean` est utilisable comme prédicat `(Todo) -> Boolean`.
- **`buildString`** : singulier/pluriel (« 0 tâche », « 2 tâches » : en français 0 est au singulier),
  pourcentage arrondi (`roundToInt`, 2 sur 3 → 67 %) et omis pour une liste vide (pas de division par zéro).
- **`titreCourt`** : 20 caractères **au plus**, points de suspension compris (`take(19).trimEnd() + "…"`).
- **`Sequence`** : rien n'est calculé sans opération terminale ; `premieresAFaire(utilisateurId = 4)` ne
  construit que **94** tâches sur 100 000 (compteur `onEach` dans les tests), et un journal montre l'ordre
  `map 1, filter 1, map 2, filter 2…` (élément par élément, au lieu de tous les `map` puis tous les `filter`).
  `Sequence<Todo>.resume()` parcourt la séquence une seule fois. Pour 10 tâches, une `List` suffit.
- **Android** : les textes de l'interface sont dans `strings.xml` (avec un `plurals` pour l'en-tête des groupes) ;
  le `when` de l'étape 5 choisit donc un identifiant de ressource. Le résumé `buildString` reste, lui, produit
  par le domaine comme le demande l'énoncé (à internationaliser plus tard avec des ressources).
- **Compose** : `TodoStore` n'est pas observable ; l'écran garde la liste dans un `mutableStateOf` qu'il
  rafraîchit après chaque action. Ce bricolage prépare l'arrivée de `StateFlow` et du `ViewModel`.
