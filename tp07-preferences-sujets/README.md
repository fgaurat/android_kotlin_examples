# TP07 — Préférences utilisateur, pas à pas

Une progression en **6 étapes** (0 à 5), de réglages perdus à la fermeture de l'application jusqu'à un écran
de paramètres complet avec Preferences DataStore. Même principe que `tp07-todos-http` : chaque étape ajoute
**une notion** par rapport à la précédente.

L'**étape 6** est à part : l'exemple DataStore le plus simple possible (un compteur), sans rien d'autre.

Pas de serveur : tout est stocké sur l'appareil.

## Exécuter une étape

Chaque étape est dans son package `sujetN` (`app/src/main/java/fr/formation/prefsujets/sujetN/`), avec un seul
fichier `MainActivityN.kt`, complet et lisible de haut en bas. Le commentaire en tête de fichier dit ce qui est
nouveau et ce qu'il faut essayer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité de l'étape** et laisser les autres en
commentaire, puis lancer l'application.

## Les étapes

| Étape | Notion | Ce qu'on observe |
|---|---|---|
| `sujet0` | Prénom et thème sombre dans `rememberSaveable` | ✅ Rotation ; ❌ fermer l'app (balayer dans les récents) : **tout est perdu** |
| `sujet1` | `SharedPreferences` : `getString` / `getBoolean`, `edit { putString(…) }` | ✅ Conservé à la réouverture ; ❌ lecture synchrone, pas d'observation, deux choses à synchroniser (état Compose + fichier) |
| `sujet2` | Preferences DataStore : `preferencesDataStore`, clés typées, `data` (Flow), `edit` (suspend, transactionnel) | L'écran affiche ce que le Flow émet : après « Enregistrer », « Bonjour, … » change tout seul |
| `sujet3` | `Reglages` (data class), `ReglagesRepository` (`catch` des `IOException`), `ReglagesViewModel` (`stateIn`), `Factory` avec `APPLICATION_KEY` | Même écran qu'à l'étape 2 : seule l'organisation change |
| `sujet4` | Migration : `produceMigrations = { listOf(SharedPreferencesMigration(context, "reglages")) }` | Les réglages saisis à l'étape 1 réapparaissent ; le fichier `shared_prefs/reglages.xml` est supprimé |
| `sujet5` | Écran complet : énumérations `Theme` (Système / Clair / Sombre) et `Unite` (°C / °F), thème appliqué par l'Activity, `clear()`, `RadioButton` accessibles | « Système » suit le mode sombre du téléphone ; la température affichée suit l'unité ; « Réinitialiser » |
| `sujet6` | **DataStore seul, l'exemple minimal** : un compteur `intPreferencesKey`, `data.map { it[CLE] ?: 0 }`, `edit { lire + écrire }`, `remove(CLE)` | « +1 » plusieurs fois, fermer l'app, la rouvrir : la valeur est là |

Les étapes 4 et 5 partent de l'étape 3. L'étape 6 ne dépend d'aucune autre : on peut la montrer seule, en
introduction à DataStore, avant l'étape 2.

## Ce qui change d'une étape à l'autre

```text
0  état Compose (rememberSaveable)                                  perdu à la fermeture
1  état Compose ⇄ SharedPreferences      (lecture au démarrage, écriture à chaque changement)
2  écran ◀── dataStore.data (Flow)        écran ──▶ dataStore.edit { } (coroutine)
3  écran ──▶ ViewModel (StateFlow<Reglages?>) ──▶ ReglagesRepository ──▶ DataStore
4  + SharedPreferencesMigration           (une ligne dans la déclaration du DataStore)
5  + Theme, Unite, clear()                 le thème est appliqué par l'Activity à toute l'app
```

## Conseils de démonstration

- **Étape 0** : tourner l'écran (conservé), puis fermer l'app depuis les récents et la rouvrir (perdu).
- **Étape 1** : *Device Explorer* → `data/data/fr.formation.prefsujets/shared_prefs/reglages.xml`. On y lit le
  prénom en clair.
- **Étape 2** : le fichier est `files/datastore/reglages-etape2.preferences_pb` (format binaire Protocol
  Buffers). Montrer qu'aucun `mutableStateOf` ne garde le prénom : il vient du Flow.
- **Étape 4**, dans cet ordre :
  1. étape 1 : saisir un prénom, activer le thème sombre, fermer l'app ;
  2. étape 4 : prénom et thème sont là (Logcat, filtre `PrefSujets`) ;
  3. retour à l'étape 1 : tout est vide, le fichier XML a disparu.
- **Étape 5** : choisir « Système », puis basculer le mode sombre dans les paramètres rapides du téléphone.

Pour repartir de zéro : `adb shell pm clear fr.formation.prefsujets`.

## Points d'attention

- **Une seule instance de DataStore par fichier et par processus** : d'où le délégué `preferencesDataStore`
  déclaré au niveau du fichier. Deux instances sur le même fichier lèvent une `IllegalStateException`.
- **Un fichier DataStore par étape** (`reglages-etape2` … `reglages-etape5`, `compteur` pour l'étape 6) : les étapes ne se gênent pas.
  L'étape 4 lit le fichier `SharedPreferences` de l'étape 1 (`reglages`), exprès.
- **Le champ texte n'écrit pas à chaque frappe** : on garde un brouillon local et on enregistre au clic. Relier
  un `TextField` directement à un Flow asynchrone fait sauter le curseur.
- **Première lecture** : quelques millisecondes pendant lesquelles rien n'est affiché (état `null`). Pour
  éviter tout flash, garder le splash screen pendant ce temps (`tp07-todos-splash`, étape 2).
- **Énumérations** : on enregistre `name` et on relit avec `entries.find { … } ?: défaut`. Une valeur inconnue
  (énumération renommée dans une nouvelle version) ne fait pas planter l'app.
- **DataStore ≠ base de données** : il sert à de petits réglages. Pour une liste de tâches, c'est Room
  (`tp07-todos-room`).

## Organisation en fichiers

Comme dans `tp07-todos-http`, chaque `MainActivityN.kt` commence par un bloc « Organisation en fichiers »
(`data/`, `ui/theme/`, `ui/reglages/`). Une ligne `// 📄 chemin/Fichier.kt` précède chaque déclaration.

## Dépendances

- `androidx.datastore:datastore-preferences` **1.3.0** (même version que `tp12-hydratation`).
- Lifecycle **2.11.0** (`viewModel()`, `viewModels`, `collectAsStateWithLifecycle`).

## Choix assumés

- **Un seul fichier par étape**, découpé par des bannières `// ---------- … ----------`.
- **Pas de tests** dans ce projet. Un test de repository sur un vrai DataStore, dans un fichier temporaire, est
  dans `tp12-hydratation` (`PreferencesHydratationTest.kt`).
- **Proto DataStore** (schéma typé avec Protocol Buffers) n'est pas abordé : Preferences DataStore suffit pour
  des réglages.
