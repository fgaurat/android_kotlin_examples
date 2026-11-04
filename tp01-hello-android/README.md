# TP 01 — Prise en main (corrigé)

Corrigé du TP « Travaux pratiques — Prise en main » (fin de `slides/01-introduction-android.md`).

## Objectif

Être à l'aise avec Android Studio, le manifest, Logcat et le débogueur, à partir d'un projet
*Empty Activity* nommé **HelloAndroid** (`fr.formation.helloandroid`, `minSdk` 24, Kotlin DSL).

## Correspondance étape → fichiers

| Étape | Ce qui est fait | Fichier(s) |
|---|---|---|
| 0. Projet de départ | `minSdk = 24`, `applicationId = "fr.formation.helloandroid"` | `app/build.gradle.kts` |
| 1. Émulateur | Manipulation d'IDE (voir plus bas) | — |
| 2. « Bienvenue sur Android » + Preview | `Greeting` et `GreetingPreview` | `app/src/main/java/fr/formation/helloandroid/MainActivity.kt` |
| 3. Texte dans les ressources | `R.string.welcome` lu par `stringResource(R.string.welcome)` | `app/src/main/res/values/strings.xml`, `MainActivity.kt` |
| 4. Permission `INTERNET` + Merged Manifest | `<uses-permission android:name="android.permission.INTERNET" />` | `app/src/main/AndroidManifest.xml` |
| 5. `Log.d` dans `onCreate` | Constante `TAG = "HelloAndroid"` et `Log.d(TAG, …)` | `MainActivity.kt` |
| 6. Point d'arrêt conditionnel | Manipulation d'IDE, sur `val salutation = …` dans `Greeting` | `MainActivity.kt` |
| 7. Modifier `name` dans Variables | Manipulation d'IDE ; `name` est affiché par la 2ᵉ ligne (« Bonjour … ! ») | `MainActivity.kt`, `strings.xml` (`greeting_name`) |
| 8. Bonus : téléphone et `adb install` | Manipulation (voir plus bas) | — |
| Vérification | Test Compose : le texte de bienvenue et le nom sont affichés | `app/src/androidTest/java/fr/formation/helloandroid/GreetingTest.kt` |

### Remarque sur `minSdk 24`

Le modèle d'origine (généré en `minSdk 34`) plaçait les icônes adaptatives dans `res/mipmap-anydpi/`.
Les icônes adaptatives (`<adaptive-icon>`) n'existent qu'à partir de l'API 26 : avec `minSdk 24`,
le build échoue (`<adaptive-icon> elements require a sdk version of at least 26`). Le dossier a donc
été renommé **`mipmap-anydpi-v26`** ; en dessous de l'API 26, Android utilise les `.webp` des dossiers
`mipmap-*dpi`. C'est exactement ce que génère l'assistant *New Project* quand on choisit API 24 :
un bon exemple de **qualificateur de ressources** (`-v26`).

## Lancer le projet

```bash
./gradlew assembleDebug                 # construit app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug                  # installe sur l'appareil ou l'émulateur connecté
./gradlew connectedDebugAndroidTest     # tests instrumentés (émulateur requis)
```

Ou, dans Android Studio : **Run ▶** (`⌃R` / `Shift+F10`).

## Manipulations d'IDE pas à pas

### Étape 1 — Créer un émulateur et lancer l'application

1. **Tools → Device Manager** (ou l'icône de téléphone dans la barre latérale droite), puis **+ → Create Virtual Device**.
2. Choisir un **Pixel** récent, puis une image système récente avec Google Play :
   **arm64-v8a** sur Mac Apple Silicon, **x86_64** sur PC Intel/AMD.
3. Terminer l'assistant, sélectionner l'émulateur dans la liste déroulante des appareils, cliquer sur **Run ▶**.

### Étape 2 — Vérifier la Preview

Ouvrir `MainActivity.kt`, passer en mode **Split** (en haut à droite de l'éditeur) : `GreetingPreview`
affiche « Bienvenue sur Android » et « Bonjour Android ! ». La Preview se met à jour à chaque modification
(sinon **Build & Refresh**).

### Étape 3 — Extraire la chaîne

Écrire d'abord `Text("Bienvenue sur Android")`, placer le curseur sur la chaîne, **`⌥↵` / `Alt+Entrée`
→ Extract string resource**, nommer la ressource `welcome`. Android Studio crée l'entrée dans
`strings.xml` ; remplacer ensuite l'appel par `stringResource(R.string.welcome)`.

### Étape 4 — Merged Manifest

Ouvrir `app/src/main/AndroidManifest.xml`, cliquer sur l'onglet **Merged Manifest** en bas de l'éditeur.
- La permission `INTERNET` apparaît, attribuée au manifest de l'application (code couleur à droite).
- On y voit aussi ce qui vient des **bibliothèques** : par exemple l'activité `ComposeActivity` de
  `ui-tooling` (en debug) ou le `provider` d'`androidx.startup`.
- On y retrouve `minSdkVersion` / `targetSdkVersion`, injectés depuis `build.gradle.kts` : ils ne
  s'écrivent plus dans le manifest.

### Étape 5 — Logcat

1. Lancer l'application, ouvrir **View → Tool Windows → Logcat**.
2. Saisir le filtre `package:mine` : on voit la ligne
   `D  onCreate() appelé, savedInstanceState = null`.
3. Variante : `tag:HelloAndroid`, ou `package:mine level:DEBUG`.
4. Faire pivoter l'émulateur : `onCreate` est rappelé (l'activité est recréée) et `savedInstanceState`
   n'est plus `null`. C'est une bonne introduction au cycle de vie (jour 2).

En ligne de commande : `adb logcat -s HelloAndroid`.

### Étape 6 — Point d'arrêt conditionnel

1. Dans `Greeting`, cliquer dans la marge sur la ligne `val salutation = stringResource(R.string.greeting_name, name)`.
2. **Clic droit** sur le point rouge → champ **Condition** : `name == "Android"`, valider.
3. Lancer en **Debug** (`⌃D` / `Shift+F9`, icône d'insecte).
4. L'exécution s'arrête : le panneau **Debug → Threads & Variables** montre `name = "Android"`.
   Essayer une condition fausse (`name == "Lyon"`) : l'application ne s'arrête plus.

> Astuce : un point d'arrêt dans un composable est atteint à **chaque recomposition**. La condition
> évite d'être interrompu en permanence.

### Étape 7 — Modifier une variable pendant la pause

1. Toujours en pause, dans le panneau **Variables**, clic droit sur `name` → **Set Value…** (`F2`).
2. Saisir `"Kotlin"` (avec les guillemets), valider, puis **Resume** (`F9` / `⌥⌘R`).
3. L'écran affiche « Bonjour Kotlin ! » : la valeur modifiée a été utilisée pour construire la chaîne,
   le code n'a pas changé.

On peut aussi **évaluer une expression** (`⌥F8` / `Alt+F8`), par exemple `name.uppercase()`.

### Étape 8 (bonus) — Téléphone et `adb install`

1. Sur le téléphone : **Paramètres → À propos du téléphone → Numéro de build**, taper 7 fois.
2. **Options pour les développeurs → Débogage USB** (ou **Débogage sans fil**, appairage par QR code
   depuis *Device Manager → Pair Devices Using Wi-Fi*).
3. Brancher, accepter l'empreinte RSA de l'ordinateur, choisir l'appareil dans Android Studio, **Run ▶**.
4. En ligne de commande :

   ```bash
   ./gradlew assembleDebug
   adb devices
   adb install -r app/build/outputs/apk/debug/app-debug.apk   # -r : réinstalle en gardant les données
   adb shell am start -n fr.formation.helloandroid/.MainActivity
   ```

## Points pédagogiques à souligner

- **Ressources et classe `R`** : aucune chaîne visible en dur dans l'UI ; c'est le prérequis de la
  traduction (`values-en/strings.xml`) et de l'accessibilité. Une chaîne peut avoir des arguments
  (`%1$s`) : `stringResource(R.string.greeting_name, name)`.
- **`minSdk` / `targetSdk` / `compileSdk`** : `minSdk 24` impose des ressources compatibles
  (qualificateur `-v26` des icônes adaptatives) et des tests de version (`Build.VERSION.SDK_INT >= S`
  pour les couleurs dynamiques dans `ui/theme/Theme.kt`).
- **`INTERNET` est une permission normale** : déclarée dans le manifest, jamais demandée à l'exécution,
  contrairement aux permissions *dangereuses* (localisation, caméra…).
- **Logcat** : un `TAG` constant par fichier, le filtre `package:mine`, et pas de données personnelles
  dans les journaux.
- **Débogueur** : les points d'arrêt conditionnels et la modification de variables évitent d'ajouter
  des `Log` ou de recompiler pour tester une hypothèse.
- **Compose et recomposition** : `Greeting` est une fonction appelée à nouveau chaque fois que ses
  paramètres changent, d'où l'intérêt des conditions sur les points d'arrêt.
