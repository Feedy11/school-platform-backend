\# Online School Platform Backend



API REST d'une plateforme e-learning (MVP) : catalogue de formations, inscriptions des étudiants, suivi de progression et tableau de bord administrateur.



\## Description



Le backend expose une API REST sécurisée par JWT, prête à être consommée par un frontend Angular.



\*\*Étudiant\*\* : créer un compte, se connecter, consulter les formations, s'inscrire, consulter ses formations et mettre à jour sa progression.



\*\*Administrateur\*\* : se connecter, créer / modifier / supprimer des formations, consulter les inscriptions et les statistiques.



Hors périmètre du MVP : paiement, chat, notifications, certificats, visioconférence, quiz, IA. L'architecture en couches permet de les ajouter plus tard.



\## Technologies



| Domaine | Technologie |

|---|---|

| Langage | Java 17 |

| Framework | Spring Boot 3.x (Web, Data JPA, Security, Validation) |

| Build | Maven |

| Base de données | PostgreSQL |

| Authentification | JWT (jjwt) + BCrypt |

| Documentation | Swagger / OpenAPI (springdoc) |

| Divers | Lombok, Docker, Docker Compose |



\## Architecture



```text

Controller  →  Service  →  Repository  →  Database

```



```text

src/main/java/com/school/platform

├── config          OpenApiConfig, ApplicationConfig (BCrypt), DataSeeder

├── controller      AuthController, CourseController, EnrollmentController, AdminController

├── service         AuthService, CourseService, EnrollmentService, AdminService

├── repository      UserRepository, CourseRepository, EnrollmentRepository

├── entity          User, Course, Enrollment, Role, Level

├── dto             auth / course / enrollment / admin

├── security        JwtService, JwtAuthenticationFilter, SecurityConfig, ...

├── exception       GlobalExceptionHandler, ApiError, exceptions métier

└── SchoolApplication.java

```



\- Les controllers ne contiennent aucune logique métier.

\- Les entities ne sont jamais exposées : l'API renvoie des DTOs.

\- Erreurs au format JSON : `{ "status": 404, "message": "...", "timestamp": "..." }`.



\## Database



Base : `school\_db` (PostgreSQL). Les tables sont créées par Hibernate au premier démarrage.



| Table | Champs principaux |

|---|---|

| `users` | id, first\_name, last\_name, email (unique), password (BCrypt), role (`STUDENT` / `ADMIN`), created\_at |

| `courses` | id, title, description, category, level (`BEGINNER` / `INTERMEDIATE` / `ADVANCED`), duration, image\_url, created\_at, updated\_at |

| `enrollments` | id, user\_id, course\_id, progress (0 à 100), enrolled\_at |



Contraintes : unicité `(user\_id, course\_id)`, `progress` entre 0 et 100, clés étrangères avec `ON DELETE CASCADE`.



\## Installation



Prérequis : JDK 17, Maven 3.9+, PostgreSQL 14+ (ou Docker).



```bash

git clone <url-du-depot>

cd school-backend

```



Créer la base (sans Docker) :



```sql

CREATE DATABASE school\_db;

```



\## Configuration



Toutes les informations sensibles viennent de variables d'environnement. Aucun secret n'est versionné.



| Variable | Rôle | Défaut (dev uniquement) |

|---|---|---|

| `DATABASE\_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/school\_db` |

| `DATABASE\_USERNAME` | Utilisateur de la base | `postgres` |

| `DATABASE\_PASSWORD` | Mot de passe de la base | `postgres` |

| `JWT\_SECRET` | Clé de signature JWT, \*\*Base64, 256 bits minimum\*\* | clé de développement |

| `JWT\_EXPIRATION\_MS` | Durée de vie du token | `86400000` (24 h) |

| `CORS\_ALLOWED\_ORIGINS` | Origines autorisées, séparées par des virgules | `http://localhost:4200` |

| `PORT` | Port HTTP | `8080` |

| `SEED\_ENABLED` | Crée les données de démonstration | `true` en dev, `false` en prod |

| `SEED\_ADMIN\_PASSWORD` / `SEED\_STUDENT\_PASSWORD` | Mots de passe des comptes de démonstration | `Admin123!` / `Student123!` en dev |

| `SWAGGER\_ENABLED` | Active Swagger UI | `true` en dev, `false` en prod |

| `DDL\_AUTO` | Mode Hibernate (profil `prod`) | `validate` |

| `SPRING\_PROFILES\_ACTIVE` | Profil Spring (`prod` en production) | aucun |



Générer une clé JWT :



```bash

openssl rand -base64 32

```



\## Running the project



\### Sans Docker



```bash

mvn spring-boot:run

```



\### Avec Docker



```bash

cp .env.example .env

docker compose up --build

```



L'API est disponible sur `http://localhost:8080`. Pour tout supprimer, base comprise : `docker compose down -v`.



\### Comptes de démonstration (développement uniquement)



| Rôle | Email | Mot de passe |

|---|---|---|

| Admin | `admin@school.com` | `Admin123!` |

| Étudiant | `student@school.com` | `Student123!` |



Cinq formations sont créées : Angular, Spring Boot, Java, Python, SQL.



\## API Endpoints



| Méthode | URL | Accès |

|---|---|---|

| POST | `/api/auth/register` | Public |

| POST | `/api/auth/login` | Public |

| GET | `/api/courses` | Public |

| GET | `/api/courses/{id}` | Public |

| POST | `/api/courses` | ADMIN |

| PUT | `/api/courses/{id}` | ADMIN |

| DELETE | `/api/courses/{id}` | ADMIN |

| POST | `/api/enrollments` | STUDENT |

| GET | `/api/enrollments/my-courses` | STUDENT |

| PUT | `/api/enrollments/{id}/progress` | STUDENT |

| GET | `/api/admin/statistics` | ADMIN |

| GET | `/api/admin/enrollments` | ADMIN |



Codes d'erreur : `400` validation, `401` non authentifié, `403` droits insuffisants, `404` ressource introuvable, `409` conflit (email déjà utilisé, double inscription), `500` erreur interne.



\## Authentication



1\. `POST /api/auth/login` avec `{ "email": "...", "password": "..." }`.

2\. La réponse contient `token`, `type` (`Bearer`) et `user`.

3\. Envoyer le token dans chaque requête protégée :



```http

Authorization: Bearer <token>

```



Le JWT contient l'email (`sub`), l'identifiant (`userId`) et le rôle (`role`). Les mots de passe sont hashés avec BCrypt. Un compte créé via l'API est toujours `STUDENT`.



\## Postman



1\. Importer `school-platform.postman\_collection.json`.

2\. Lancer `Login (student)` et `Login (admin)` : les tokens sont enregistrés automatiquement dans les variables `studentToken` et `adminToken`.

3\. Manuellement : onglet \*\*Authorization\*\* > \*\*Bearer Token\*\* > coller le token.

4\. Le \*\*Runner\*\* exécute la collection complète avec ses tests.



\## Swagger



\- Interface : `http://localhost:8080/swagger-ui/index.html`

\- Spécification : `http://localhost:8080/v3/api-docs`



Bouton \*\*Authorize\*\* : coller le token, sans le préfixe `Bearer`.



\## Tests



```bash

mvn clean test

```



\## Deployment



1\. Créer une base PostgreSQL managée (Render, Railway, Neon, Supabase, etc.).

2\. Déployer ce dépôt avec son `Dockerfile` sur un hébergeur de conteneurs.

3\. Définir les variables d'environnement :



```text

SPRING\_PROFILES\_ACTIVE=prod

DATABASE\_URL=jdbc:postgresql://<hote>:5432/<base>

DATABASE\_USERNAME=<utilisateur>

DATABASE\_PASSWORD=<mot-de-passe>

JWT\_SECRET=<openssl rand -base64 32>

CORS\_ALLOWED\_ORIGINS=https://<votre-app>.vercel.app

```



4\. \*\*Premier déploiement\*\* : ajouter `DDL\_AUTO=update` pour créer les tables, puis retirer la variable (retour à `validate`).

5\. Pour créer le compte admin : ajouter temporairement `SEED\_ENABLED=true`, `SEED\_ADMIN\_PASSWORD` et `SEED\_STUDENT\_PASSWORD` avec des mots de passe robustes, puis repasser `SEED\_ENABLED` à `false`.

6\. Vérifier : `GET https://<api>/api/courses` doit répondre `200`.

7\. Renseigner l'URL de l'API dans le frontend Angular.



Le profil `prod` désactive Swagger et les logs SQL, et exige que les secrets soient fournis (l'application refuse de démarrer sinon).

