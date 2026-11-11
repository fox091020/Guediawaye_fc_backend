# Guediawaye FC — API backend

API REST Spring Boot séparée de l'application frontend.

## Prérequis

- Java 21
- MySQL 8.4
- Le Maven Wrapper est fourni (`./mvnw`).

## Préparer MySQL et démarrer

Dans le client MySQL, créez la base et un utilisateur dédié à l'application :

```sql
CREATE DATABASE gfcdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'gfc'@'localhost' IDENTIFIED BY 'mot-de-passe-local';
GRANT ALL PRIVILEGES ON gfcdb.* TO 'gfc'@'localhost';
```

Sur Ubuntu, vous pouvez ouvrir le client administrateur avec `sudo mysql`.
Puis configurez le mot de passe de l'utilisateur applicatif et lancez l'API :

```bash
export DB_USER=gfc
export DB_PASSWORD=mot-de-passe-local
read -rsp 'Mot de passe initial développeur : ' GFC_DEVELOPER_INITIAL_PASSWORD
export GFC_DEVELOPER_INITIAL_PASSWORD
printf '\n'
./mvnw spring-boot:run
unset GFC_DEVELOPER_INITIAL_PASSWORD
```

La connexion utilise par défaut `jdbc:mysql://localhost:3306/gfcdb`. Les tables sont créées ou mises à jour automatiquement par Hibernate.

## Authentification de l'administration

Au premier démarrage, l'API crée le profil développeur permanent `bmd`. Fournissez son mot de passe initial via `GFC_DEVELOPER_INITIAL_PASSWORD` dans l'environnement du serveur : il n'est jamais stocké en clair dans le dépôt. À la première connexion, le développeur doit le remplacer par un mot de passe de 12 caractères minimum contenant une majuscule, une minuscule, un chiffre et un caractère spécial. Une fois le profil créé, cette variable n'est plus utilisée.

Le développeur peut créer au maximum trois profils administrateur du site et bloquer ou réactiver chacun d'eux. Ces profils ne peuvent pas créer d'autres comptes ni supprimer le compte développeur. Les mots de passe sont hachés avec BCrypt, les sessions expirent après 30 minutes d'inactivité, et cinq échecs de connexion bloquent temporairement un compte pendant 15 minutes. Les routes de lecture publiques restent accessibles ; toutes les routes API de modification et les téléversements requièrent une session valide.

En production, définissez `GFC_FRONTEND_ORIGINS` avec les origines exactes du frontend, puis activez les cookies de session sécurisés derrière HTTPS avec `SESSION_COOKIE_SECURE=true`. Gardez le secret initial et les identifiants MySQL dans l'environnement du serveur, jamais dans le dépôt.

## API des actualités

Toutes les routes sont préfixées par `/api/news`.

| Méthode | Route | Description |
|---|---|---|
| `GET` | `/api/news` | Lister les actualités, de la plus récente à la plus ancienne |
| `GET` | `/api/news/{id}` | Lire une actualité |
| `POST` | `/api/news` | Créer une actualité |
| `PUT` | `/api/news/{id}` | Modifier une actualité |
| `DELETE` | `/api/news/{id}` | Supprimer une actualité |

Exemple de corps JSON :

```json
{
  "category": "Équipe",
  "title": "Le Guediawaye FC prépare son prochain match",
  "summary": "Les Crabes poursuivent leur préparation.",
  "content": "Le groupe a travaillé les aspects tactiques et physiques avant sa prochaine rencontre.",
  "imageUrl": "/actu_1.jpeg",
  "publishedAt": "2026-11-09T12:00:00Z"
}
```

Les champs requis sont validés par l'API. Une création réussie renvoie `201 Created` et l'en-tête `Location`.

## Contenu de la page d'accueil

`GET /api/homepage` renvoie les sections administrables de la page d'accueil, avec les valeurs actuelles du site si aucune personnalisation n'a encore été enregistrée. `PUT /api/homepage` valide et enregistre ces sections dans MySQL. Les données comprennent la bannière Hero, les statistiques, la section « Notre histoire » et les images de la boutique.

Le frontend utilise `http://localhost:8080` par défaut. Pour changer l'adresse, définissez `VITE_API_BASE_URL` dans l'environnement de Vite avant de lancer ou reconstruire le frontend.

## Contenu de la page « Le Club »

`GET /api/club` renvoie les valeurs actuelles du site si aucun contenu administré n'a encore été enregistré. `PUT /api/club` valide et enregistre dans MySQL l'identité, les trois étapes de l'histoire, les statistiques, le palmarès et les figures du club. Les images utilisent le même service de téléversement `/api/media`.

## Contenu de la page « Équipe & staff »

`GET /api/team` renvoie les données actuelles de la page de l'équipe si aucune personnalisation n'a encore été enregistrée. `PUT /api/team` enregistre dans MySQL le visuel et les chiffres du groupe, les joueurs de la première équipe ainsi que les membres hommes et femmes du staff.

## Téléversement des images

`POST /api/media` accepte un formulaire `multipart/form-data` avec un champ `file`. Seules les images JPEG, PNG et GIF valides sont acceptées, dans la limite de 10 Mo et de 40 mégapixels. Le nom est généré par le serveur et l'API renvoie une URL publique utilisable dans le contenu du site. Les images sont servies depuis `/media/{nom-du-fichier}`.

Les fichiers sont stockés hors de MySQL, dans `./uploads` par défaut. Configurez `MEDIA_STORAGE_DIR` pour choisir un emplacement persistant en production et `MEDIA_PUBLIC_BASE_URL` pour définir le domaine public utilisé dans les URLs renvoyées. Incluez le dossier de stockage dans vos sauvegardes. Le dossier `uploads` local est ignoré par Git.

L'application ne dispose pas encore d'authentification administrateur. Ne rendez pas les routes d'administration ni l'endpoint de téléversement accessibles publiquement avant d'avoir mis en place une authentification et une autorisation adaptées.

Pour personnaliser l'URL de connexion, définissez `DB_URL` :

```bash
export DB_URL=jdbc:mysql://localhost:3306/gfcdb
```
