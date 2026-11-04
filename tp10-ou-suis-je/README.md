# TP10 — « Où suis-je ? » (corrigé)

Corrigé du TP de fin du chapitre 10, *Notions avancées* (`slides/10-notions-avancees.md`).

## Objectif

Afficher la position de l'appareil (latitude, longitude, précision) et la distance jusqu'à Paris, en
respectant les règles des permissions d'exécution, puis industrialiser le build : product flavors
`gratuit` / `pro` et `dev` / `prod`, signature release sans secret dans le script, R8, AAB.

| | `gratuit` | `pro` |
|---|---|---|
| Position | ponctuelle (bouton « Où suis-je ? ») | ponctuelle **et** suivi continu (`callbackFlow`) |
| `applicationId` (prod) | `fr.formation.ousuisje.gratuit` | `fr.formation.ousuisje` |
| Nom affiché (`resValue`) | Où suis-je ? | Où suis-je ? Pro |
| Icône | repère blanc sur fond bleu | repère doré sur fond anthracite |
| `BuildConfig.SUIVI_CONTINU` | `false` | `true` (`INTERVALLE_SUIVI_MS = 10000`) |

Le flavor `dev` ajoute `.dev` à l'`applicationId` et `-dev` au `versionName`, et affiche une puce
« Environnement : dev ». Les 4 combinaisons s'installent côte à côte sur le même appareil.

## Correspondance étapes → fichiers

| Étape | Contenu | Fichiers |
|---|---|---|
| 1 | Permissions déclarées ; demande avec rationale, refus définitif → paramètres de l'app, revérification au retour (`LifecycleResumeEffect`) | `app/src/main/AndroidManifest.xml`, `permissions/PermissionsLocalisation.kt`, `ui/OuSuisJeScreen.kt` |
| 2 | `play-services-location` + `kotlinx-coroutines-play-services` ; `getCurrentLocation(...).await()` | `gradle/libs.versions.toml`, `localisation/LocalisationDataSource.kt`, `localisation/SourceLocalisation.kt` |
| 3 | Distance jusqu'à Paris avec `Location.distanceBetween` ; position approximative (arrondi au km, bouton « Autoriser la position précise ») | `localisation/SourceLocalisation.kt` (`calculDistanceAndroid`), `geo/Distances.kt`, `ui/OuSuisJeViewModel.kt` |
| 4 | Position simulée, `adb shell pm revoke` | ce README (section *Manipulations*) |
| 5 | Flavors `gratuit` / `pro`, `buildConfigField`, `resValue`, nom et icône par flavor, source sets | `app/build.gradle.kts`, `app/src/gratuit/`, `app/src/pro/` (`ui/BanniereOffre.kt`, `res/drawable/ic_launcher_*.xml`) |
| 6 | Explication du `build.gradle.kts` par l'IA, API obsolètes | ce README (section *Étape 6*) + commentaires de `app/build.gradle.kts` |
| 7 | Clé d'upload, `signingConfigs` via `local.properties`, R8 | `keystore/upload-demo.jks`, `local.properties`, `app/build.gradle.kts`, `app/src/main/keepRules/rules.keep`, `.gitignore`, `.aiexclude` |
| 8 | AAB `proProdRelease`, APK release testé sur l'émulateur | ce README (section *Release*) |
| 9 (bonus) | Suivi continu `callbackFlow` dans `pro` ; dimension `environnement` (`dev` / `prod`) | `LocalisationDataSource.suivrePosition`, `OuSuisJeViewModel` (`flatMapLatest` + `WhileSubscribed`), `app/build.gradle.kts` |

Tests :

| Fichier | Type | Ce qui est testé |
|---|---|---|
| `app/src/test/.../geo/DistancesTest.kt` | JVM | haversine, formatage distance / précision / coordonnées, règle « approximative » |
| `app/src/test/.../ui/OuSuisJeViewModelTest.kt` | JVM (faux FLP + `kotlinx-coroutines-test`) | position précise / approximative, localisation désactivée, `SecurityException`, mode dégradé, suivi indisponible en gratuit, suivi pro et désabonnement quand la permission est retirée |
| `app/src/androidTest/.../localisation/DistanceAndroidTest.kt` | instrumenté | `Location.distanceBetween` contre haversine (< 0,5 %), `applicationId` cohérent avec les flavors |

`Location.distanceBetween` est une API du framework, absente des tests JVM : le ViewModel reçoit
un `CalculDistance` (interface fonctionnelle) ; l'app injecte la version Android, les tests JVM la
formule de haversine.

## Lancer le projet

Ouvrir le dossier dans Android Studio, choisir la variante dans **Build Variants**
(ex. `proDevDebug`), puis *Run*. Utiliser un émulateur **avec Google Play** (sinon pas de Fused
Location Provider).

```bash
# Toutes les variantes debug + tests JVM + APK de tests instrumentés
./gradlew assembleDebug \
  testGratuitDevDebugUnitTest testGratuitProdDebugUnitTest testProDevDebugUnitTest testProProdDebugUnitTest \
  assembleGratuitDevDebugAndroidTest assembleProProdDebugAndroidTest --console=plain

# Installer une variante
./gradlew installProDevDebug

# Tests instrumentés (émulateur démarré)
./gradlew connectedGratuitDevDebugAndroidTest connectedProProdDebugAndroidTest
```

Avec deux dimensions, les tâches sont nommées *offre + environnement + build type* :
`assembleGratuitDevDebug`, `bundleProProdRelease`… La tâche `bundleProRelease` de l'énoncé
devient donc **`bundleProProdRelease`**.

## Manipulations

### Étape 4 — position simulée sur l'émulateur

1. Barre de l'émulateur → **⋯** (*Extended controls*) → **Location**.
2. Chercher « Lyon » sur la carte → **Set location**.
3. Dans l'app : « Où suis-je ? » → environ **392 km** de Paris (« ≈ 392 km » en position approximative).
4. Variante `pro` : activer **Suivi continu**, puis onglet **Routes** des contrôles étendus :
   tracer un itinéraire (ou importer un GPX/KML) et **Play route** ; la distance se met à jour
   toutes les ~10 s.

En ligne de commande (attention : **longitude puis latitude**) :

```bash
adb emu geo fix 4.8357 45.7640        # Lyon
adb emu geo fix 2.3488 48.8534        # Paris : « 0 m » / « moins de 3 km »
adb emu geo fix -4.4861 48.3904       # Brest : ≈ 505 km, longitude affichée « O »
```

Si « Position indisponible » s'affiche : vérifier que la localisation est activée dans les
réglages rapides (l'app propose le bouton **Réglages de localisation**).

### Étape 4 — tester tous les chemins du flux de permission

Identifiants des variantes : `fr.formation.ousuisje.gratuit.dev` (gratuitDevDebug),
`fr.formation.ousuisje.dev` (proDevDebug), `fr.formation.ousuisje` (proProdRelease)…

```bash
PKG=fr.formation.ousuisje.gratuit.dev

# Révoquer la permission (l'app est tuée par le système) puis relancer l'app
adb shell pm revoke $PKG android.permission.ACCESS_FINE_LOCATION
adb shell pm revoke $PKG android.permission.ACCESS_COARSE_LOCATION

# Repartir de zéro (efface aussi le drapeau « refus définitif »)
adb shell pm reset-permissions -p $PKG     # ou : adb shell pm clear $PKG

# N'accorder que la position approximative
adb shell pm grant $PKG android.permission.ACCESS_COARSE_LOCATION
```

Cas à vérifier à la main (checklist du chapitre) :

| Scénario | Résultat attendu |
|---|---|
| 1re demande → « Précise » | position affichée, précision en mètres, distance au dixième de km |
| 1re demande → « Approximative » | badge « Position approximative », « ≈ N km », bouton « Autoriser la position précise » |
| Refus une fois, puis « Où suis-je ? » | boîte de dialogue d'explication (rationale) avant la nouvelle demande |
| Refus deux fois | message + bouton **Ouvrir les paramètres** ; les villes du mode dégradé restent utilisables |
| Autorisation dans les paramètres puis retour | l'état est relu au retour (`LifecycleResumeEffect`), le message disparaît |
| « Seulement cette fois » puis fermeture longue | permission retirée automatiquement, l'app redemande |
| `pm revoke` pendant le suivi (pro) | au retour, le suivi est arrêté (`permissionsMisesAJour`) |
| Localisation désactivée | « Position indisponible » + bouton Réglages de localisation |

### Étape 1 et étape 6 — utilisation de l'IA

Prompt de génération utilisé (repris du chapitre, adapté) :

> *Projet Android Kotlin, Jetpack Compose, Material 3, `minSdk` 34, `targetSdk` 37. Écris un composable
> qui demande la permission de localisation pour afficher la position courante et la distance jusqu'à
> Paris, conforme aux recommandations Android : demande déclenchée par un bouton ; `ACCESS_COARSE_LOCATION`
> et `ACCESS_FINE_LOCATION` demandées ensemble, l'approximative suffit ; Activity Result API sans
> bibliothèque tierce ; rationale dans un `AlertDialog` ; refus définitif → bouton vers les paramètres ;
> revérification avec `LifecycleResumeEffect` ; pas de localisation en arrière-plan ; textes en français
> dans `strings.xml`. Liste ensuite les cas à tester manuellement.*

Relecture avec la checklist : tous les points sont couverts dans `PermissionsLocalisation.kt` et
`OuSuisJeScreen.kt` (en particulier `resultats.values.any { it }` et non `resultats[FINE] == true`).

Prompt d'explication de l'étape 6 :

> *Explique ce `build.gradle.kts` bloc par bloc (AGP 9.5, Gradle 9.6, Kotlin 2.2). Pour chaque propriété,
> indique si elle est dépréciée et sa remplaçante. Liste les variantes générées et les tâches `assemble`
> et `bundle` correspondantes.*

Points qu'une IA signale ou propose souvent, et ce qui a été retenu ici :

| Proposition fréquente | Dans ce projet (AGP 9.5) |
|---|---|
| `isMinifyEnabled = true` + `proguardFiles(getDefaultProguardFile(...), "proguard-rules.pro")` | `optimization { enable = true }` et règles dans `src/main/keepRules/` (DSL AGP 9 du modèle *Empty Activity*) |
| `compileSdk = 37` | `compileSdk { version = release(37) }` (syntaxe du modèle AGP 9) |
| plugin `org.jetbrains.kotlin.android` | inutile : Kotlin est intégré à AGP 9 |
| `kotlinOptions { jvmTarget = "11" }` | absent (obsolète) ; `compileOptions` suffit, sinon `kotlin { compilerOptions { … } }` |
| mots de passe dans `signingConfigs` | lus dans `local.properties` / variables d'environnement |
| `resValue` sans `buildFeatures.resValues = true` | activé explicitement |

Variantes générées : `gratuitDevDebug`, `gratuitDevRelease`, `gratuitProdDebug`,
`gratuitProdRelease`, `proDevDebug`, `proDevRelease`, `proProdDebug`, `proProdRelease`.

## Étape 7 — signature

> **Keystore et mots de passe de DÉMONSTRATION.** `keystore/upload-demo.jks` et ses mots de passe sont
> publics (ils sont dans ce README) : ils ne servent qu'à la formation. Ne jamais les utiliser pour une
> vraie application, ne jamais versionner un vrai keystore.

| Paramètre | Valeur de démonstration |
|---|---|
| Fichier | `keystore/upload-demo.jks` (PKCS12, RSA 2048, validité 10 000 jours) |
| Alias | `upload` |
| Mot de passe du keystore | `demo-store-2026` |
| Mot de passe de la clé | `demo-store-2026` (PKCS12 : identique au précédent) |

Commande utilisée pour le créer :

```bash
keytool -genkeypair -v \
  -keystore keystore/upload-demo.jks -storetype PKCS12 \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass demo-store-2026 -keypass demo-store-2026 \
  -dname "CN=Formation Android (DEMO), OU=Formation, O=Demo, L=Paris, C=FR"
```

Clés ajoutées à `local.properties` (la ligne `sdk.dir` est conservée) :

```properties
signing.storeFile=keystore/upload-demo.jks      # relatif à la racine du projet, ou absolu
signing.storePassword=demo-store-2026
signing.keyAlias=upload
signing.keyPassword=demo-store-2026
```

En CI, les mêmes valeurs peuvent venir des variables d'environnement `OUSUISJE_STORE_FILE`,
`OUSUISJE_STORE_PASSWORD`, `OUSUISJE_KEY_ALIAS`, `OUSUISJE_KEY_PASSWORD`.

**Repli** : si une valeur manque (ou si le fichier n'existe pas), le script n'enregistre pas de
`signingConfig` release, affiche un avertissement et produit un `app-…-release-unsigned.apk`.
Les builds debug utilisent toujours la clé debug d'Android Studio et ne dépendent jamais de ces
propriétés.

Protection : `.gitignore` exclut `local.properties`, `*.jks`, `*.keystore`, `*.p12` (sauf le
keystore de démo, par une exception `!keystore/upload-demo.jks`) ; `.aiexclude` empêche
l'assistant IA de lire `local.properties` et `keystore/`.

Empreintes de la clé (utile pour Maps / Firebase) : `./gradlew signingReport`.

## Étape 8 — release, AAB et APK

```bash
./gradlew bundleProProdRelease     # app/build/outputs/bundle/proProdRelease/app-pro-prod-release.aab
./gradlew assembleProProdRelease   # app/build/outputs/apk/proProd/release/app-pro-prod-release.apk
```

Vérifier la signature et l'obfuscation :

```bash
BT=$ANDROID_HOME/build-tools/36.0.0
$BT/apksigner verify --print-certs app/build/outputs/apk/proProd/release/app-pro-prod-release.apk
jarsigner -verify app/build/outputs/bundle/proProdRelease/app-pro-prod-release.aab
less app/build/outputs/mapping/proProdRelease/mapping.txt     # à envoyer à la Play Console
```

Installer et tester l'APK release sur l'émulateur (R8 : un plantage lié à l'obfuscation n'apparaît
jamais en debug) :

```bash
adb install -r app/build/outputs/apk/proProd/release/app-pro-prod-release.apk
adb shell monkey -p fr.formation.ousuisje -c android.intent.category.LAUNCHER 1
```

Tester l'AAB localement avec `bundletool` (facultatif) :

```bash
bundletool build-apks --bundle=app/build/outputs/bundle/proProdRelease/app-pro-prod-release.aab \
  --output=/tmp/ousuisje.apks --connected-device \
  --ks=keystore/upload-demo.jks --ks-key-alias=upload \
  --ks-pass=pass:demo-store-2026 --key-pass=pass:demo-store-2026
bundletool install-apks --apks=/tmp/ousuisje.apks
```

## Points pédagogiques

- **Permissions** : COARSE + FINE demandées ensemble ; « approximative » acceptée partout ;
  `shouldShowRequestPermissionRationale` interprété **après** la réponse pour détecter le refus
  définitif ; vérification à chaque usage et au retour au premier plan ; mode dégradé (villes).
- **`await(CancellationTokenSource)`** : la requête FLP est annulée si la coroutine l'est ;
  repli sur `lastLocation` (souvent `null` sur un émulateur neuf).
- **`callbackFlow` + `awaitClose`** : désabonnement garanti. Le ViewModel n'abonne le FLP que tant
  que l'écran collecte `etat` (`collectAsStateWithLifecycle` + `WhileSubscribed(5_000)`) : aucune
  localisation en arrière-plan, donc pas de `ACCESS_BACKGROUND_LOCATION` ni de foreground service.
- **Source sets** : `BanniereOffre` existe dans `src/gratuit` **et** `src/pro`, jamais dans `src/main` ;
  les ressources d'icône du flavor remplacent celles de `main`.
- **`namespace` fixe, `applicationId` variable** : `BuildConfig` et `R` restent dans
  `fr.formation.ousuisje` quelle que soit la variante.
- **Secrets** hors du script, repli propre, `.gitignore` / `.aiexclude`.

## Écarts par rapport aux slides

- **Syntaxe AGP 9** (modèle *Empty Activity* d'AGP 9.5) : `optimization { enable = true; packageScope = … }`
  et `src/main/keepRules/rules.keep` remplacent `isMinifyEnabled` / `isShrinkResources` /
  `proguardFiles("proguard-rules.pro")` des slides ; `compileSdk { version = release(37) }` remplace
  `compileSdk = 37`. Le `packageScope` du modèle (`androidx.**`, `kotlin.**`, `kotlinx.**`) a été
  complété par `fr.formation.ousuisje.**` pour que R8 traite aussi le code de l'app (vérifié dans
  `mapping.txt`).
- `bundleProRelease` (énoncé) → `bundleProProdRelease` à cause de la dimension bonus `environnement`.
- `minSdk 34` (modèle) au lieu de 24 dans les slides : aucun test de version n'est nécessaire.
- Priorité `PRIORITY_HIGH_ACCURACY` (on affiche la précision) au lieu de `BALANCED` (cas météo des slides).
- Les mipmaps raster (`mipmap-*dpi/*.webp`) du modèle ont été supprimés : avec `minSdk 34`, seules
  les icônes adaptatives de `mipmap-anydpi` sont utilisées.
