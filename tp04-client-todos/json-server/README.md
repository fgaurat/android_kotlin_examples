# JSON Server du TP04

```bash
cd json-server
npx json-server db.json          # http://localhost:3000/todos et /users
```

- `db.json` : 200 tâches et 10 utilisateurs de JSONPlaceholder (régénération : voir le README du projet).
- `db.original.json` : copie de sauvegarde. Pour repartir de zéro : `cp db.original.json db.json`.
- `todos.http` : toutes les requêtes du TP, à lancer depuis Android Studio.

À la première écriture, JSON Server réécrit `db.json` : les `id` numériques d'origine deviennent des chaînes (`"id": "1"`).
