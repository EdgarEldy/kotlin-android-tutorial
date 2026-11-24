# kotlin-android-tutorial

Application Android e-commerce native consommant le backend Spring Boot local (`spring-boot-tutorial`).
Projet tutoriel — Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture.

**Répondre toujours en français.**

---

## Chemins importants

- Backend local : `C:\Users\edgar\Documents\Workspace\Tutorials\java\Spring Ecosystem\spring-boot-tutorial`
- OpenAPI spec (source de vérité) : `openapi/openapi.json`
- Rafraîchir la spec avant tout travail API : `curl http://localhost:8080/v3/api-docs -o openapi/openapi.json`
- Android SDK : `C:\Users\edgar\AppData\Local\Android\Sdk` (platform android-36, build-tools 36.1.0)
- Java : 17.0.20

---

## Architecture (invariants non négociables)

### Modules Gradle

```
:app                    ← point d'entrée, MainActivity, TutorialApplication, navigation, NetworkModule
:core:network           ← OkHttp, AuthInterceptor, TokenAuthenticator, @AuthOnly qualifier
:core:storage           ← SecureTokenStorage, SecureStorageModule, DataStoreModule
:core:database          ← TutorialDatabase, DatabaseModule (Room partagé)
:core:ui                ← TutorialTheme, Color, Type (Material 3)
:feature:auth           ← tout ce qui concerne l'authentification
:feature:product-catalog ← produits, catégories, home
:feature:cart           ← panier, checkout
:feature:orders         ← historique et détail de commandes
```

### Structure interne de chaque feature module

```
feature/<nom>/src/main/java/com/edgareldy/tutorial/<nom>/
├── data/
│   ├── dto/           ← DTOs Retrofit (@Serializable, @SerialName snake_case)
│   ├── api/           ← interfaces Retrofit (XxxApiService)
│   └── repository/    ← XxxRepositoryImpl
├── domain/
│   ├── model/         ← modèles domain purs (aucune dépendance Retrofit/Room)
│   ├── repository/    ← interfaces XxxRepository
│   └── usecase/       ← un fichier par use case
└── presentation/
    ├── screens/       ← Composables
    ├── viewmodel/     ← ViewModels + sealed UiState
    └── components/    ← Composables réutilisables spécifiques à la feature
```

### Règles d'architecture

- Les Composables ne contactent **jamais** Retrofit directement
- Les DTOs ne dépassent **pas** la couche `data/` — les ViewModels et Composables utilisent les modèles domain
- Les modèles domain n'ont **aucune** dépendance sur Retrofit ou Room
- Repository retourne toujours `Result<T>` ou `Flow<Result<T>>`
- ViewModel expose `StateFlow<UiState>` ; UiState est sealed class
- Sealed UiState pattern : `Loading | Success(data) | Error(message) | Empty`
- Les entités Room d'une feature appartiennent au module de cette feature ; elles sont enregistrées dans `TutorialDatabase` via `addMigrations` / entités listées dans `@Database`

---

## Règles impératives (feedback utilisateur)

- **Jamais de `Co-Authored-By: Claude` / `Anthropic` dans les messages de commit.**
  L'utilisateur commit lui-même ou valide le message avant tout commit.

- **Jamais de tiret cadratin (em dash `—`) dans le code.**
  Ni dans les KDoc, ni dans les commentaires, ni dans les chaînes de caractères, ni dans les
  logs. Utiliser un tiret simple `-`, une virgule, ou reformuler en deux phrases. Eviter aussi
  dans README, YAML, Dockerfile pour la cohérence.

- **Commits atomiques : un commit par fichier créé ou modifié.**
  Ne pas grouper plusieurs fichiers non liés dans un seul commit. Chaque fichier source (entity,
  DAO, repository, use case, ViewModel, Composable, module Hilt, etc.) fait l'objet de son propre
  commit. Les fichiers étroitement couplés créés ensemble (ex. interface + implémentation,
  entity + DAO) peuvent partager un commit si les séparer n'a pas de sens, mais la règle par
  défaut est un fichier = un commit.

- **Format de message de commit (Conventional Commits + corps pédagogique).**
  Sujet : `type(scope): résumé impératif court` (types : feat, fix, refactor, test, docs, chore,
  perf, style ; scopes : core, auth, products, cart, orders, ui, nav, db, network).
  Une ligne vide, puis un corps en prose **en anglais** expliquant ce qui change et **pourquoi**, rédigé pour un
  lecteur qui suit le tutoriel. Ne pas ré-expliquer un concept déjà établi dans le codebase :
  expliquer uniquement ce qui est nouveau ou non-évident dans ce commit précis.

- **KDoc obligatoire sur toute nouvelle classe, interface, object, data class, enum, sealed class.**
  Format exact (adapter la description et la date `M/D/YY` sans zéro de tête) :
  ```kotlin
  /**
   * <Description du rôle, 1 à 3 lignes>
   *
   * Created edgar.muhamyangabo on <M/D/YY>
   * Author: edgar.muhamyangabo
   * Date: <M/D/YY>
   * Project: kotlin-android-tutorial
   */
  ```
  Author fixe : `edgar.muhamyangabo`. S'applique à toutes les couches.

---

## Conventions de code

- Package racine : `com.edgareldy.tutorial`
- Package d'une feature : `com.edgareldy.tutorial.<nom>` (ex. `com.edgareldy.tutorial.auth`)
- Un ViewModel par écran
- Use case : une seule fonction publique `operator fun invoke(...)`
- JSON : tous les champs snake_case via `@SerialName`
- Room entities d'une feature dans `feature/<nom>/src/main/java/.../data/local/entity/`
- DAOs dans `feature/<nom>/src/main/java/.../data/local/dao/`
- Pas de logique métier dans les Composables ni dans les ViewModels (uniquement dans les use cases)

---

## Stratégie offline

- **Lecture** : cache-first + refresh réseau en arrière-plan (produits, catégories, commandes)
- **Écriture** : panier et checkout nécessitent le réseau — retourner une erreur explicite si absent
- Pull-to-refresh invalide le cache local et force un refresh réseau

---

## Contrat API (résumé)

Enveloppe standard : `{ success, message, data, timestamp }`
Pagination : `PageResponse<T> { content[], page, size, totalElements, totalPages }`
Auth header : `Authorization: Bearer <access_token>`

| Domaine | Base URL |
|---------|----------|
| Auth | `/api/v1/auth/` |
| Reset password mobile | `/api/v1/password-reset/` (3 étapes, PAS `/auth/mobile/`) |
| Produits | `/api/v1/products/` |
| Catégories | `/api/v1/categories/` |
| Panier | `/api/v1/cart/` |
| Commandes | `/api/v1/orders/` |

Discrepances importantes vs documentation :
- Reset mobile = `/api/v1/password-reset/mobile` → `/verify-otp` → `/confirm?token=OTP`
- Checkout = `POST /api/v1/orders` **sans corps** (crée depuis le panier)
- `/api/v1/customers/` est ADMIN seulement, pas le profil utilisateur

---

## Workflow Git

```bash
# 1. Créer la branche
git checkout develop
git checkout -b feature/<nom> develop

# 2. Implémenter + committer (un fichier par commit)
git add <fichier>
git commit -m "feat(scope): description"

# 3. Quality gate avant PR
./gradlew assembleDebug
./gradlew test

# 4. Pousser et ouvrir une PR
git push -u origin HEAD
gh pr create --base develop

# 5. Après CI verte : merger et synchroniser
gh pr merge --merge
git checkout develop && git pull
```

Ne jamais committer directement sur `master` ou `develop`.
Chaque branche `feature/*` fait l'objet d'une PR GitHub vers `develop`.
Attendre que la CI soit verte avant de merger (`gh run list`).

### Conventional Commits — règles

Format : `type(scope): description courte en impératif`

**Types** :
| Type | Usage |
|------|-------|
| `feat` | Nouvelle fonctionnalité |
| `fix` | Correction de bug |
| `refactor` | Refactoring sans changement de comportement |
| `test` | Ajout ou modification de tests |
| `docs` | Documentation uniquement |
| `chore` | Maintenance (deps, config, CI) |
| `perf` | Amélioration de performance |
| `style` | Formatage, indentation (pas de logique) |

**Scopes du projet** :
`core` | `auth` | `products` | `cart` | `orders` | `ui` | `nav` | `db` | `network` | `storage`

**Règles** :
- Description en minuscules, pas de point final
- Corps du commit (facultatif) séparé par une ligne vide : explique le POURQUOI
- Breaking change : `feat(scope)!:` + footer `BREAKING CHANGE: description`
- Un commit = une modification cohérente (ne pas mélanger feat + fix dans un même commit)

**Exemples valides** :
```
feat(auth): add JWT token refresh on 401
fix(cart): prevent duplicate add-to-cart on double tap
refactor(products): extract cache-first logic to base repository
test(auth): add unit tests for LoginUseCase
chore(deps): bump Compose BOM to 2024.12.01
```

---

## Phases d'implémentation

| Commande | Branche | Contenu |
|----------|---------|---------|
| `/phase-1-core-architecture` | `feature/core-architecture` | Gradle, DI, Navigation, Room, Retrofit, Keystore |
| `/phase-2-auth` | `feature/auth` | Login, Register, OTP, Reset password, Profil |
| `/phase-3-product-catalog` | `feature/product-catalog` | Produits, Catégories, cache Room, Bottom navigation shell (Home/Cart/Orders/Profile) |
| `/phase-4-shopping-cart` | `feature/shopping-cart` | Panier, ajout/modification/suppression |
| `/phase-5-orders` | `feature/orders` | Commandes, checkout, historique |
| `/phase-6-quality` | `feature/quality` | Tests, lint, ProGuard, performance |
