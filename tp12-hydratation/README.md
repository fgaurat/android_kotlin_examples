# TP12 — Rappels d'hydratation (corrigé)

Corrigé du TP de fin du chapitre 12, *Autres composants* (`slides/12-autres-composants.md`).

## Objectif

Une application qui rappelle toutes les heures de boire un verre d'eau, sauf la nuit :
canal de notification, permission `POST_NOTIFICATIONS` demandée en contexte, `CoroutineWorker`
périodique, action de notification traitée par un `BroadcastReceiver`, receiver dynamique pour le
mode avion, compteur du jour persistant dans DataStore.

## Correspondance étapes → fichiers

Chemins relatifs à `app/src/main/java/fr/formation/hydratation/`.

| Étape | Contenu | Fichiers |
|---|---|---|
| 1 | Canal `rappels` créé dans une classe `Application` déclarée dans le manifest | `HydratationApplication.kt`, `notifications/NotificationsRappel.kt` (`creerCanal`), `AndroidManifest.xml` (`android:name`) |
| 2 | `POST_NOTIFICATIONS` déclarée ; interrupteur « Rappels activés » qui la demande en contexte (rationale, refus définitif → réglages de notification) ; état persistant | `AndroidManifest.xml`, `ui/HydratationScreen.kt` (`CarteRappels`), `data/PreferencesHydratation.kt` |
| 3 | `RappelWorker` : notification BigText « Pensez à boire un verre d'eau 💧 », rien entre 22 h et 8 h ; clic → `PendingIntent` `FLAG_IMMUTABLE` vers `MainActivity` | `rappels/RappelWorker.kt`, `rappels/ReglesRappel.kt`, `notifications/NotificationsRappel.kt` (`construire`, `afficher`) |
| 4 | `PeriodicWorkRequest` d'1 h via `enqueueUniquePeriodicWork` ; `cancelUniqueWork` à la désactivation | `rappels/PlanificationRappels.kt`, `ui/HydratationViewModel.kt` (`definirRappels`) |
| 5 | Bouton « Tester maintenant » : `OneTimeWorkRequest` (forcée même la nuit), état du travail affiché | `PlanificationRappels.testerMaintenant`, `PlanificationRappels.etat` |
| 6 | Action « C'est fait » → `CestFaitReceiver` (`exported="false"`) : incrémente le compteur du jour, retire la notification | `notifications/CestFaitReceiver.kt`, `AndroidManifest.xml` |
| 7 | Bandeau « Mode avion activé » : receiver dynamique `ACTION_AIRPLANE_MODE_CHANGED` dans un `DisposableEffect` | `ui/ModeAvion.kt` (`rememberModeAvion`), `ui/HydratationScreen.kt` (`BandeauModeAvion`) |
| 8 (bonus) | Compteur dans DataStore, affiché, remis à zéro chaque jour ; rappels qui survivent au redémarrage | `data/PreferencesHydratation.kt` (`CompteurJour`), `ui/HydratationScreen.kt` (`CarteCompteur`) |

Ressources : `res/values/strings.xml` (textes), `res/drawable/ic_goutte.xml` (petite icône
monochrome), `ic_check.xml` (action), `ic_launcher_*.xml` (icône de l'app).

Tests :

| Fichier | Type | Ce qui est testé |
|---|---|---|
| `app/src/test/.../rappels/ReglesRappelTest.kt` | JVM, horloge `Clock.fixed` | « pas de rappel entre 22 h et 8 h », bornes 8 h 00 incluse et 22 h 00 exclue |
| `app/src/test/.../data/PreferencesHydratationTest.kt` | JVM, vrai DataStore dans un fichier temporaire | logique du compteur journalier, incrément, remise à zéro le lendemain, interrupteur persistant |
| `app/src/androidTest/.../rappels/RappelWorkerTest.kt` | instrumenté, `work-testing` | Worker la nuit (succès sans notification) via une `WorkerFactory` de test qui injecte l'horloge ; Worker forcé ; travail périodique unique (1 h, pas de doublon) puis annulé |

L'horloge est injectable à deux niveaux : `ReglesRappel(horloge)` pour les tests JVM, et le
constructeur principal de `RappelWorker(contexte, parametres, horloge)` pour `work-testing`.
Le constructeur secondaire à deux paramètres est celui qu'utilise la `WorkerFactory` par défaut
de WorkManager.

## Lancer le projet

```bash
./gradlew assembleDebug testDebugUnitTest assembleDebugAndroidTest --console=plain
./gradlew installDebug
./gradlew connectedDebugAndroidTest       # émulateur démarré
```

## Manipulations

### Étape 1 — vérifier le canal dans les réglages système

1. Lancer l'app une fois (le canal est créé dans `HydratationApplication.onCreate`).
2. Appui long sur l'icône → **Infos sur l'appli** → **Notifications** : la catégorie **Rappels**
   apparaît, avec sa description.

```bash
adb shell am start -a android.settings.APP_NOTIFICATION_SETTINGS \
  --es android.provider.extra.APP_PACKAGE fr.formation.hydratation
adb shell dumpsys notification | grep -A3 "rappels"
```

### Étape 2 — permission en contexte

| Scénario | Résultat attendu |
|---|---|
| Activer l'interrupteur la 1re fois | boîte système « Autoriser Hydratation à vous envoyer des notifications ? » |
| Accepter | interrupteur activé, « Rappel planifié (prochaine exécution vers HH:mm) » |
| Refuser une fois, réactiver | explication (rationale) puis nouvelle demande |
| Refuser deux fois | message « notifications bloquées » + bouton vers les réglages de notification |
| Couper les notifications dans les réglages alors que les rappels sont actifs | même message au retour dans l'app |
| Fermer puis rouvrir l'app | l'interrupteur garde son état (DataStore) |

```bash
# Repartir de zéro pour rejouer le flux
adb shell pm revoke fr.formation.hydratation android.permission.POST_NOTIFICATIONS
adb shell pm clear fr.formation.hydratation      # efface aussi DataStore et WorkManager
```

### Étape 5 — tester et suivre le travail

1. Activer les rappels, puis **Tester maintenant** : la notification apparaît tout de suite
   (la requête de test passe `forcer = true` pour fonctionner aussi la nuit).
2. Android Studio → **View → Tool Windows → App Inspection** → onglet **Background Task Inspector** :
   - `RappelWorker` périodique `rappel_hydratation` en **ENQUEUED** ;
   - chaque test (`OneTimeWorkRequest`, tag `test_rappel`) passe par RUNNING puis **SUCCEEDED** ;
   - sélectionner un travail pour voir ses données d'entrée/sortie (`forcer`, `notifie`).
3. Désactiver l'interrupteur : le travail périodique passe en **CANCELLED**.

Forcer l'exécution du travail périodique sans attendre une heure :

```bash
# 1. Repérer l'identifiant du job (« JOB #u0a123/<JOB_ID> fr.formation.hydratation/androidx.work… »)
adb shell dumpsys jobscheduler | grep "fr.formation.hydratation/androidx.work"
# 2. L'exécuter immédiatement, contraintes ignorées
adb shell cmd jobscheduler run -f fr.formation.hydratation <JOB_ID>
```

Tester la règle de nuit sur l'émulateur : **Réglages → Système → Date et heure**, désactiver
l'heure automatique et régler 23 h 00, puis lancer le job comme ci-dessus : aucun rappel
(sortie `notifie = false` dans le Background Task Inspector).

### Étape 6 — action « C'est fait »

Déplier la notification (BigText), appuyer sur **C'est fait** : la notification disparaît et le
compteur « Verres bus aujourd'hui » augmente de 1, même si l'app est fermée.

### Étape 7 — mode avion

Ouvrir l'app, puis activer le mode avion depuis les réglages rapides : le bandeau
« ✈ Mode avion activé » apparaît immédiatement et disparaît à la désactivation. Sur un émulateur :

```bash
adb shell cmd connectivity airplane-mode enable
adb shell cmd connectivity airplane-mode disable
```

### Bonus — compteur et redémarrage

- Compteur : boutons « +1 verre » / « Remettre à zéro », ou l'action « C'est fait ». Le compteur
  mémorise sa date : le lendemain il s'affiche à 0 sans tâche de remise à zéro (la date est relue
  au retour au premier plan). Pour l'observer : changer la date de l'émulateur.
- Redémarrage : activer les rappels, puis redémarrer l'appareil.

```bash
adb reboot
adb wait-for-device
adb shell dumpsys jobscheduler | grep -A2 fr.formation.hydratation   # le job est de nouveau planifié
```

WorkManager persiste ses travaux dans sa base SQLite et les replanifie seul après le redémarrage
(la bibliothèque déclare elle-même `RECEIVE_BOOT_COMPLETED` et son `RescheduleReceiver`, visibles
dans l'onglet **Merged Manifest**) : l'application n'a **rien** à déclarer ni de receiver de
démarrage à écrire. Cas
particulier : après un **arrêt forcé** (Réglages → Forcer l'arrêt), Android annule tout jusqu'au
prochain lancement manuel de l'app ; WorkManager replanifie alors ses travaux, et le ViewModel
rappelle de toute façon `planifier()` (politique `UPDATE`, sans doublon) si l'interrupteur est actif.

## Points pédagogiques

- **Canal** créé dans `Application.onCreate` : idempotent, l'importance ne peut plus être modifiée
  par l'app ensuite (seul l'utilisateur la règle).
- **`POST_NOTIFICATIONS`** demandée au moment où l'utilisateur active la fonctionnalité, jamais au
  lancement ; `notify()` sans permission est ignoré silencieusement, d'où la vérification préalable
  (permission + `areNotificationsEnabled()`).
- **WorkManager plutôt qu'un service** : différable, garanti, survit au redémarrage ; travail
  **unique** nommé pour éviter les doublons ; 15 min minimum pour un travail périodique.
- **Règle métier isolée** (`ReglesRappel`) avec horloge injectable : testable sans attendre la nuit.
- **`PendingIntent`** : `FLAG_IMMUTABLE` obligatoire ; l'action vise un broadcast **explicite**
  (classe nommée), autorisé malgré les restrictions d'Android 8, receiver `exported="false"`.
- **`goAsync()`** dans `CestFaitReceiver` : `onReceive()` tourne sur le thread principal, l'écriture
  DataStore se fait dans une coroutine et le receiver reste vivant jusqu'à `finish()`.
- **Receiver dynamique dans `DisposableEffect`** : inscription à l'entrée dans la composition,
  `unregisterReceiver` dans `onDispose` ; `RECEIVER_NOT_EXPORTED` obligatoire (`targetSdk` 34+).
- **DataStore** : une seule instance par fichier (délégué `preferencesDataStore`), transaction
  `edit {}` atomique, `Flow` observé par l'écran.

## Écarts et choix

- Modèle AGP 9.5 conservé : `optimization { enable = true … }` et `src/main/keepRules/` au lieu de
  `isMinifyEnabled` / `proguard-rules.pro` des slides ; `compileSdk { version = release(37) }`.
  Aucune règle `-keep` n'est nécessaire : WorkManager fournit les siennes pour les constructeurs
  des Workers.
- `minSdk 34` : pas de test `Build.VERSION.SDK_INT` (les canaux et `POST_NOTIFICATIONS` existent toujours).
- Importance du canal : `IMPORTANCE_DEFAULT` (son, sans bandeau) plutôt que `HIGH` : un rappel
  horaire ne doit pas être intrusif.
- Pas de contrainte réseau ni batterie sur la requête : notifier ne demande rien de tel.
- Tests instrumentés : `runBlocking` plutôt que `runTest`, car `kotlinx-coroutines-test` côté appareil
  doit avoir exactement la version de `kotlinx-coroutines` embarquée dans l'app (sinon
  `NoSuchMethodError`).
- Les mipmaps raster (`mipmap-*dpi/*.webp`) du modèle ont été supprimés : avec `minSdk 34`, seules
  les icônes adaptatives de `mipmap-anydpi` sont utilisées.
