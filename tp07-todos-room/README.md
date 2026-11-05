# TP07 — Liste de tâches avec Room, pas à pas

Une progression en **6 étapes**, de la première table Room jusqu'à une migration de schéma, sur le même
principe que `tp07-todos-http`. Chaque étape ajoute **une seule notion** par rapport à la précédente.

## Exécuter une étape

Chaque étape est dans son package `sujetN` (`app/src/main/java/fr/formation/todosroom/sujetN/`), avec un seul
fichier `MainActivityN.kt`, complet et lisible de haut en bas. Le commentaire en tête de fichier dit ce qui est
nouveau et ce qu'il faut essayer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité de l'étape** et laisser les autres en
commentaire, puis lancer l'application. Pas de serveur : tout est stocké sur l'appareil.

## Les étapes

| Étape | Notion | Ce qu'on observe |
|---|---|---|
| `sujet1` | `@Entity`, `@Dao` (`@Insert`, `@Query` suspend), `@Database`, instance unique | Les tâches survivent à la fermeture de l'app ; ❌ il faut **relire la liste à la main** après un ajout |
| `sujet2` | Le DAO renvoie un `Flow<List<Tache>>` ; `@Update`, `@Delete` | L'écran se met à jour **tout seul** après chaque écriture, même faite depuis le Database Inspector |
| `sujet3` | Un `ViewModel` : `stateIn(WhileSubscribed(5_000))`, écritures dans `viewModelScope`, `Factory` avec `APPLICATION_KEY` | L'écran ne connaît plus Room |
| `sujet4` | Un `TacheRepository` ; écritures en SQL (`UPDATE`, `DELETE` par id) ; `COUNT(*)` observé en `Flow` | Même écran qu'à l'étape 3 ; le compteur est calculé par la base |
| `sujet5` | `@Query` avec paramètres : `LIKE`, filtre optionnel, `ORDER BY` ; `combine` + `flatMapLatest` | Filtre et recherche faits **par SQLite** |
| `sujet6` | Migration : colonne `priorite`, base en version 2, `Migration(1, 2)` | Les tâches créées à l'étape 5 sont conservées, avec la priorité « Normale » |

## Points d'attention

- **Une base par étape** (`taches-etape1.db` … `taches-etape4.db`) : chaque étape a son propre schéma, elles ne
  se gênent pas.
- **Les étapes 5 et 6 partagent la même base** (`taches-etapes5-6.db`), exprès : c'est ce qui permet de montrer
  la migration. Ordre : lancer l'étape 5, créer quelques tâches, puis lancer l'étape 6.
  - Revenir à l'étape 5 après l'étape 6 : la base est en version 2 et l'étape 5 attend la version 1.
    `fallbackToDestructiveMigrationOnDowngrade` repart alors d'une base vide (sans planter).
  - Pour voir l'erreur sans migration : commenter `.addMigrations(MIGRATION_1_2)`, désinstaller l'app, refaire
    étape 5 puis étape 6 → `IllegalStateException: A migration from 1 to 2 was required but not found`.
- **Database Inspector** : *App Inspection → Database Inspector*, l'app en cours d'exécution. On peut lire les
  tables, lancer du SQL et modifier une ligne : à l'étape 2, l'écran suit.
- **Repartir de zéro** : *Paramètres → Applications → Todos Room → Stockage → Effacer les données*, ou
  `adb shell pm clear fr.formation.todosroom`.

## Dépendances

- Room **2.8.4** (`room-runtime`, `room-ktx`, `room-compiler`).
- KSP **2.3.6** : depuis la 2.3, la version de KSP ne suit plus celle de Kotlin, et KSP fonctionne avec le
  Kotlin intégré d'AGP 9 (pas de plugin `kotlin-android`).
- Le code généré par Room est visible dans `app/build/generated/ksp/debug/kotlin/` (`TacheDao_Impl`,
  `BaseDeDonnees_Impl`).

## Choix assumés

- **Un seul fichier par étape**, découpé par des bannières `// ---------- … ----------`. Dans un vrai projet :
  un fichier par classe, et la base créée une fois dans un `AppContainer` (voir `tp07-todos-mvvm-retrofit`).
- **`Tache` sert à la fois d'entité et de modèle** affiché : pas de mapping entité → domaine.
- **`exportSchema = false`** : pas de dossier de schémas JSON. En production, on exporte le schéma (plugin
  Gradle `androidx.room`) pour versionner la base et tester les migrations.
- **Pas de tests** dans ce projet.
