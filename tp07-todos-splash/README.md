# TP — Splash screen qui charge les données du serveur REST

Un *splash screen* qui reste affiché pendant le chargement des tâches depuis JSON Server, en **3 étapes**, sur
le même principe que `tp07-todos-http` (une étape = une `MainActivityN`, à décommenter dans le manifest).

Bibliothèque : `androidx.core:core-splashscreen` **1.2.0** (API SplashScreen d'Android 12, identique sur toutes
les versions).

## Lancer

```bash
cd json-server
npx json-server db.json            # → http://localhost:3000/todos (1.x ou 0.17 : les deux fonctionnent)
```

Puis, dans `app/src/main/AndroidManifest.xml`, décommenter l'étape voulue et lancer l'application.

> Pour bien voir le splash, lancer l'app depuis le **lanceur** de l'émulateur, après l'avoir fermée
> (balayer dans les récents). Lancée depuis Android Studio, l'icône centrale n'apparaît pas toujours.

## Les étapes

| Étape | Notion | Ce qu'on observe |
|---|---|---|
| `sujet1` | Thème de démarrage + `installSplashScreen()` | Le splash (fond violet, icône) disparaît dès le premier écran dessiné |
| `sujet2` | `setKeepOnScreenCondition { etat is Chargement }`, ViewModel qui charge `GET /todos` dès sa création | Le splash reste ~1 s, puis la liste apparaît **directement**, sans écran de chargement ; serveur arrêté → erreur + « Réessayer » |
| `sujet3` | Délai maximum (2 s) + animation de sortie (`setOnExitAnimationListener`, `remove()`) | Le splash s'efface en glissant vers le haut ; avec `DELAI_DEMO = 5_000`, il part au bout de 2 s et l'écran affiche son indicateur |

## Les trois morceaux du splash

1. **Le thème** (`res/values/themes.xml`) :

   ```xml
   <style name="Theme.TodosSplash.Demarrage" parent="Theme.SplashScreen">
       <item name="windowSplashScreenBackground">@color/splash_fond</item>
       <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
       <item name="postSplashScreenTheme">@style/Theme.TodosSplash</item>
   </style>
   ```

2. **Le manifest** : `android:theme="@style/Theme.TodosSplash.Demarrage"` sur l'activité de démarrage.
3. **L'activité** : `installSplashScreen()` **avant** `super.onCreate()`.

## Points d'attention

- **`DELAI_DEMO`** (1 s) est ajouté dans le ViewModel pour qu'on ait le temps de voir le splash : JSON Server en
  local répond en quelques millisecondes. À retirer dans une vraie application.
- La condition de `setKeepOnScreenCondition` est évaluée **à chaque image** : elle doit seulement lire une valeur.
- Le ViewModel est obtenu dans l'Activity (`by viewModels()`), pas dans le composable : la condition doit pouvoir
  le lire avant `setContent`.
- Le splash n'apparaît qu'au **démarrage** (à froid ou à tiède), pas après une rotation.
- **Ne pas** faire de splash « de décoration » : il doit couvrir un vrai chargement, et le plus court possible.
  Le délai maximum de l'étape 3 évite de bloquer l'utilisateur si le serveur est lent.

## Vérifié

Les 3 étapes compilent. Sur un émulateur API 34 avec JSON Server, j'ai vérifié les étapes 2 et 3 : le splash
s'affiche avec son icône, puis la liste des tâches. L'étape 1, l'animation de sortie et le délai maximum n'ont
pas été testés.
