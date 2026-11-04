# TP 03 — Gestion de tâches (corrigé)

Corrigé du TP « Travaux pratiques — Gestion de tâches » (fin de `slides/03-kotlin.md`).
Il remplace l'ancien TP « Gestion d'une bibliothèque » (`tp03-bibliotheque`, conservé à côté).

## Objectif

Pratiquer le Kotlin du chapitre 3 sur un domaine que tout le monde comprend : les tâches de
[JSONPlaceholder](https://jsonplaceholder.typicode.com/todos) (`userId`, `id`, `title`, `completed`),
**sans réseau** : on reprend seulement la structure et quelques données.

Le modèle `Todo(id, titre, terminee, utilisateurId)` est **exactement celui du TP04** : au TP suivant,
les stagiaires ajoutent le réseau (Retrofit, JSON Server) à un modèle qu'ils connaissent déjà.

Le domaine est écrit en **Kotlin pur** (package `domain`, aucun import Android) : il se teste sur la JVM
en quelques secondes. `MainActivity` l'affiche en Compose pour que le TP « se voie ».

## Correspondance étape → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/todos/` (et `app/src/test/java/…` pour les tests).

| Étape | Ce qui est fait | Fichier(s) |
|---|---|---|
| 1. `Todo`, `Filtre`, `ResultatAction` | `data class Todo`, `enum class Filtre(libelle)`, `sealed interface ResultatAction` (`Succes(todo)`, `data object DejaTerminee`, `data object Introuvable`) | `domain/Modele.kt` |
| 2. `object TodoStore` de 10 tâches | Ids 1, 2, 3, 4, 8, 21, 22, 25, 41, 44 de JSONPlaceholder : 3 utilisateurs, 5 terminées. `MutableList` privée, `List` exposée (copie) | `domain/TodoStore.kt` |
| 3. Extensions | `Todo.titreCourt` (propriété), `aFaire()`, `terminees()` (référence `Todo::terminee`), `selon(filtre)` (`when` sur l'enum) | `domain/Extensions.kt` |
| 4. Collections | `parUtilisateur()` (`groupBy`), `nombreParUtilisateur()` (`groupingBy().eachCount()`), `utilisateurLePlusActif()`, `titreLePlusLong()` (`maxByOrNull`), `triees()` (`sortedWith(compareBy(…))`), `indexParId()` (`associateBy`) | `domain/Extensions.kt` |
| 5. `terminer(id)` + message avec `when` | `TodoStore.terminer()` remplace l'élément par `todo.copy(terminee = true)` ; `ResultatAction.message()` (`when` exhaustif sans `else`) | `domain/TodoStore.kt`, `domain/Modele.kt` |
| 6. `filtrer(critere)` + résumé | Fonction d'ordre supérieur, appelée avec une lambda et avec `::aUnTitreLong` ; `resume()` avec `buildString` | `domain/TodoStore.kt`, `domain/Extensions.kt` |
| Bonus : ajouter / supprimer / rouvrir | `ajouter(titre, utilisateurId)` (id suivant), `supprimer(id)`, `rouvrir(id)` | `domain/TodoStore.kt` |
| Bonus : `Sequence` sur 100 000 tâches | `genererTodos()`, `premieresAFaire()`, `statistiques()` en un passage | `domain/GrandeListe.kt` |
| Bonus : tests JUnit | 19 tests : store, extensions, séquence (y compris la preuve de la paresse) | `TodoStoreTest.kt`, `ExtensionsTest.kt`, `GrandeListeTest.kt` |
| Affichage | Résumé, filtres (`FilterChip`), liste triée avec cases à cocher et Snackbar | `MainActivity.kt` |

## Lancer le projet

```bash
./gradlew testDebugUnitTest     # 19 tests JVM, sans émulateur
./gradlew installDebug          # application de démonstration
```

Dans Android Studio : clic droit sur le dossier `app/src/test` → **Run 'Tests in …'**.

### Variante Kotlin Playground

Copier le contenu de `Modele.kt`, `TodoStore.kt`, `Extensions.kt` (et `GrandeListe.kt` pour le bonus)
dans `play.kotlinlang.org`, **sans les lignes `package`**, puis ajouter :

```kotlin
fun main() {
    val todos = TodoStore.todos
    println(todos.resume())                       // 10 tâches, 5 terminées (50 %), utilisateur le plus actif : 1
    println(todos.nombreParUtilisateur())         // {1=5, 2=3, 3=2}
    println("Titre le plus long : ${todos.titreLePlusLong()?.titreCourt}")
    println(todos.triees().map { it.titreCourt })
    listOf("1", "1", "999").forEach { id ->
        println(TodoStore.terminer(id).message())  // Succès, puis DejaTerminee, puis Introuvable
    }
    println(TodoStore.filtrer { it.utilisateurId == 2 }.map { it.id })   // [21, 22, 25]
    println(TodoStore.filtrer(::aUnTitreLong).map { it.id })            // [21, 22, 25, 41, 44]
    println(TodoStore.todos.selon(Filtre.A_FAIRE).map { it.id })        // [2, 3, 21, 41]
    println(genererTodos(100_000).statistiques())
}
```

## Points pédagogiques à souligner

- **`enum` ou `sealed` ?** `Filtre` est un ensemble de **valeurs** ; `ResultatAction` un ensemble de
  **types** dont un seul porte une donnée. Ajouter un cas (`Supprimee`, par exemple) fait échouer la
  compilation de `message()` tant qu'il n'est pas traité : le `when` exhaustif est un filet de sécurité.
  On retrouvera ce schéma au jour 3 avec un `UiState` (`Chargement`, `Succes(todos)`, `Erreur(message)`).
- **Immuabilité** : `Todo` n'a que des `val` ; terminer une tâche remplace l'élément par
  `todo.copy(terminee = true)`. C'est ce que fera le ViewModel au jour 3 (`_etat.update { it.copy(…) }`).
- **Encapsulation des collections** : `_todos` est une `MutableList` privée ; `todos` renvoie une
  **copie** (`toList()`). Compose ne voit un changement que si la référence change : c'est pour cela
  que l'écran relit `TodoStore.todos` après chaque action. Le test `la_liste_exposee_est_une_copie` le montre.
- **Références** : `Todo::terminee` (référence de propriété) et `::aUnTitreLong` (référence de fonction)
  s'utilisent partout où une lambda `(Todo) -> Boolean` est attendue.
- **`object` et tests** : un singleton garde son état d'un test à l'autre, d'où `reinitialiser()` dans
  `@Before`. C'est l'argument qui mènera à l'**injection de dépendances** (jour 3 : un `Repository`
  passé au `ViewModel`).
- **Tri** : `compareBy(Todo::terminee, Todo::titre)` met les tâches à faire d'abord, car `false < true`.
- **`groupBy` vs `groupingBy().eachCount()`** : le second compte sans construire les listes.
- **Pourquoi `id: String` ?** Pour garder le modèle du TP04 à l'identique : JSON Server 1.x génère
  des identifiants texte pour les tâches créées.
