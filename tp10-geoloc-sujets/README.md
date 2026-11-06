# TP10 — Géolocalisation, pas à pas

Chapitre « Notions avancées » (`slides/10-notions-avancees.md`).

Une progression en **6 étapes** (0 à 5), de la déclaration des permissions jusqu'au suivi continu dans un
ViewModel. Même principe que `tp07-todos-http` : chaque étape ajoute **une notion** par rapport à la précédente.

Le corrigé complet du chapitre, avec variantes de build, signature et tests, est **`tp10-ou-suis-je`**. Ce
projet-ci ne garde que la géolocalisation.

## Prérequis

- Un émulateur ou un appareil **avec Google Play** : le *Fused Location Provider* vient de Google Play
  Services (`play-services-location`).
- **Simuler une position** sur l'émulateur :
  - ⋮ (*Extended controls*) → **Location** : saisir un point, puis *Set location* ;
  - en ligne de commande, **longitude d'abord** : `adb emu geo fix 2.3522 48.8566` ;
  - pour un trajet (étape 4) : *Location* → *Routes*, choisir deux points, *Play route*.

## Exécuter une étape

Chaque étape est dans son package `sujetN` (`app/src/main/java/fr/formation/geosujets/sujetN/`), avec un seul
fichier `MainActivityN.kt`, complet et lisible de haut en bas. Le commentaire en tête de fichier dit ce qui est
nouveau et ce qu'il faut essayer.

Dans `app/src/main/AndroidManifest.xml`, **décommenter l'activité de l'étape** et laisser les autres en
commentaire, puis lancer l'application. Les deux `<uses-permission>` sont communs à toutes les étapes.

## Les étapes

| Étape | Notion | Ce qu'on observe |
|---|---|---|
| `sujet0` | `<uses-permission>` FINE et COARSE ; `checkSelfPermission` ; `LifecycleResumeEffect` | Déclarées mais ❌ non accordées ; les accorder dans les Paramètres, revenir : ✅ sans relancer |
| `sujet1` | `rememberLauncherForActivityResult(RequestMultiplePermissions())`, explication (`shouldShowRequestPermissionRationale`), refus définitif → paramètres de l'app | Refuser deux fois : plus de boîte de dialogue, bouton « Ouvrir les paramètres » ; choisir « Approximative » |
| `sujet2` | `getFusedLocationProviderClient`, `getCurrentLocation(…).await()`, `lastLocation`, précision en mètres, `SecurityException` | Position simulée, puis « Où suis-je ? » : latitude, longitude, ± précision |
| `sujet3` | Position approximative (≈ 3 km) et « Autoriser la position précise » ; priorité adaptée ; localisation coupée (`LocationManagerCompat.isLocationEnabled`) | Couper la localisation dans les paramètres rapides : message et bouton « Activer la localisation » |
| `sujet4` | Suivi continu : `requestLocationUpdates` dans un `callbackFlow`, `awaitClose { removeLocationUpdates }`, `collectAsStateWithLifecycle` | Trajet simulé : les positions défilent ; touche Accueil → Logcat `Suivi : ARRÊTÉ` |
| `sujet5` | `SourceLocalisation` (interface) + `SourceFused`, `GeolocViewModel` (`flatMapLatest`, `stateIn(WhileSubscribed)`), distance jusqu'à Paris (`Location.distanceBetween`) | Lyon → ≈ 392 km ; rotation pendant le suivi : il continue sans redémarrer |

## Ce qui change d'une étape à l'autre

```text
0  manifest + checkSelfPermission                                   (on constate)
1  + lanceur de permission, explication, refus définitif            rememberPermissionLocalisation()
2  + FusedLocationProviderClient : getCurrentLocation / lastLocation   (dans le composable)
3  + précise / approximative, priorité, localisation coupée
4  + suivi : callbackFlow ⇄ requestLocationUpdates / removeLocationUpdates
5  écran ──▶ GeolocViewModel ──▶ SourceLocalisation (SourceFused)    + distance
```

La gestion de la permission (`rememberPermissionLocalisation`, `BlocPermission`) est écrite à l'étape 1, puis
reprise telle quelle dans les étapes suivantes.

## Conseils de démonstration

- **Étape 0** : insister sur la différence entre *déclarer* (manifest) et *accorder* (l'utilisateur).
- **Étape 1** : Refuser → l'explication s'affiche → Refuser encore → refus définitif. Pour recommencer :
  `adb shell pm reset-permissions fr.formation.geosujets`, ou désinstaller l'app. Montrer aussi
  `adb shell pm revoke fr.formation.geosujets android.permission.ACCESS_FINE_LOCATION`.
- **Étape 2** : comparer « Où suis-je ? » (position fraîche) et « Dernière connue » (immédiate, peut-être
  ancienne, ou `null` juste après un redémarrage).
- **Étape 3** : dans les paramètres de l'app, désactiver « Utiliser la position exacte » : la précision passe
  à environ 3 km.
- **Étape 4** : lancer un trajet simulé, puis touche Accueil et retour, en regardant Logcat (`GeoSujets`).
- **Étape 5** : tourner l'écran pendant le suivi. Le ViewModel survit, le suivi ne redémarre pas.

## Points d'attention

- **Toujours demander FINE et COARSE ensemble** : depuis Android 12, l'utilisateur choisit la précision. Ne
  demander que FINE est refusé par le système.
- **Le refus définitif est une approximation** : refusée et `shouldShowRequestPermissionRationale` faux. Fermer
  la toute première boîte sans répondre donne le même résultat.
- **`removeLocationUpdates` est obligatoire** : sans lui, le GPS reste allumé et la batterie se vide. Le
  `callbackFlow` le garantit dans `awaitClose`.
- **Pas de localisation en arrière-plan** : elle demande `ACCESS_BACKGROUND_LOCATION` ou un service de premier
  plan, et une justification auprès de Google Play.
- **`@SuppressLint("MissingPermission")`** : Lint ne voit pas que la permission est vérifiée avant l'appel. La
  `SecurityException` reste rattrapée, car la permission peut être retirée à tout moment.
- **Émulateur sans Google Play** : le Fused Location Provider n'existe pas. Il faudrait passer par
  `LocationManager` (hors périmètre).

## Organisation en fichiers

Comme dans `tp07-todos-http`, chaque `MainActivityN.kt` commence par un bloc « Organisation en fichiers »
(`permissions/`, `localisation/`, `data/`, `ui/`). Une ligne `// 📄 chemin/Fichier.kt` précède chaque
déclaration.

## Dépendances

- `com.google.android.gms:play-services-location` **21.4.0**.
- `org.jetbrains.kotlinx:kotlinx-coroutines-play-services` **1.11.0** (`Task.await()`).
- Mêmes versions que `tp10-ou-suis-je`.

## Choix assumés

- **Un seul fichier par étape**, découpé par des bannières `// ---------- … ----------`.
- **Pas de carte** : on affiche des coordonnées. Afficher une carte demande une clé d'API (Maps SDK) ou une
  bibliothèque tierce.
- **Pas de tests** dans ce projet. Le test du ViewModel avec une fausse source de localisation est dans
  `tp10-ou-suis-je`.
