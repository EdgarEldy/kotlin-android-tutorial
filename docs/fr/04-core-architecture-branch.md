# Branche : feature/core-architecture

Cette branche construit la fondation technique dont toutes les branches de features suivantes dépendent. Aucune fonctionnalité métier n'est implémentée ici - l'objectif est un squelette d'application compilable et exécutable avec toute l'infrastructure câblée.

---

## Ce que vous allez apprendre

- Comment mettre en place un projet Android multi-modules avec Gradle Kotlin DSL et un catalogue de versions
- Comment câbler Hilt pour l'injection de dépendances à l'échelle de l'application
- Comment configurer Retrofit et OkHttp pour un client API REST, incluant la journalisation des requêtes
- Comment intercepter les requêtes HTTP pour attacher automatiquement un token Bearer
- Comment gérer l'expiration des tokens de façon transparente avec un `Authenticator`
- Comment stocker les credentials de façon sécurisée avec le chiffrement backé par Android Keystore
- Comment définir une base de données Room partagée que tous les modules features utiliseront
- Comment construire un graphe de navigation qui sépare le flux d'authentification du flux principal
- Comment appliquer un thème Material 3 avec support du mode clair et du mode sombre

---

## Ce que ça livre

### Configuration Gradle multi-modules

Le projet est structuré comme un build multi-modules dès le départ. `settings.gradle.kts` liste chaque module. Chaque module a son propre `build.gradle.kts` qui déclare uniquement ce dont il a réellement besoin.

Un catalogue de versions central (`gradle/libs.versions.toml`) fixe chaque version de dépendance dans un seul fichier. Aucun module ne code en dur une chaîne de version - ils référencent tous le catalogue. Cela prévient les conflits de versions et rend les mises à jour triviales.

### Configuration de l'application Hilt

`TutorialApplication` est annotée avec `@HiltAndroidApp`. Cela déclenche la génération de code de Hilt et crée le composant au niveau application. Chaque module qui utilise `@Inject` dépend de cette configuration.

`DispatchersModule` fournit trois instances de `CoroutineDispatcher` nommées : `@IoDispatcher` (pour le réseau et la base de données), `@MainDispatcher` (pour les mises à jour UI) et `@DefaultDispatcher` (pour les calculs intensifs). Injecter des dispatchers plutôt que d'utiliser `Dispatchers.IO` directement rend le code testable - les tests peuvent substituer un `TestCoroutineDispatcher`.

### Stack réseau

`NetworkModule` câble trois objets :

1. Un `OkHttpClient` principal avec `AuthInterceptor` et `TokenAuthenticator`. Toutes les requêtes authentifiées passent par ce client.
2. Un `OkHttpClient` secondaire `@AuthOnly` avec uniquement l'`AuthInterceptor` - pas de `TokenAuthenticator`. C'est le client utilisé par `TokenAuthenticator` lui-même quand il a besoin d'appeler le endpoint de refresh. Sans ce client secondaire, appeler le endpoint de refresh déclencherait un nouveau cycle de gestion de 401, créant une boucle infinie.
3. Une instance `Retrofit` construite sur le client principal, avec le converter Kotlinx Serialization.

`AuthInterceptor` lit le `access_token` stocké à chaque requête et injecte l'en-tête `Authorization: Bearer <token>`. C'est un `Interceptor`, pas un `Authenticator` - il s'exécute avant la requête, pas après un échec.

`TokenAuthenticator` s'exécute après une réponse 401. Il appelle le endpoint de refresh, sauvegarde les nouveaux tokens et retourne la requête originale avec l'en-tête `Authorization` mis à jour. Un `Mutex` sérialise les retentatives concurrentes : si trois requêtes échouent avec 401 simultanément, un seul appel de refresh est fait ; les deux autres attendent et utilisent le nouveau token quand le mutex est libéré.

### Stockage sécurisé

`SecureTokenStorage` stocke l'`access_token` et le `refresh_token` en utilisant `EncryptedSharedPreferences` backé par Android Keystore. La clé de chiffrement est générée une fois et stockée dans le key store sécurisé par hardware - elle ne quitte jamais l'appareil et ne peut pas être extraite. Les `SharedPreferences` simples ou `DataStore` ne sont intentionnellement pas utilisés pour les tokens.

`DataStoreModule` fournit une instance `DataStore<Preferences>` pour les préférences non sensibles (préférences utilisateur, flags mis en cache). Il est distinct du stockage des tokens.

### Base de données Room

`TutorialDatabase` est une base de données Room partagée définie dans `:core:database`. Les modules features ajoutent leurs entités ici à mesure que le projet grandit. La version 1 commence avec zéro entité - le schéma grandit avec chaque branche de feature.

`DatabaseModule` fournit une instance singleton de `TutorialDatabase` et tous les DAOs. Comme Room utilise une seule connexion SQLite partagée entre les modules, la base de données doit être centralisée.

### Navigation

Le graphe de navigation est découpé en deux graphes imbriqués : `authGraph` (connexion, inscription, flux OTP) et `mainGraph` (accueil, panier, commandes, profil). L'application décide au démarrage dans quel graphe commencer en vérifiant si un token stocké existe.

`Screen` est une sealed class qui définit chaque route comme un objet typé. Les chaînes de routes sont définies une seule fois ; aucune chaîne magique n'est éparpillée dans les composables.

### Thème Material 3

`TutorialTheme` enveloppe `MaterialTheme` avec un schéma de couleurs personnalisé défini dans `Color.kt`. Les schémas clair et sombre utilisent des tokens basés sur les rôles Material 3, pas des valeurs hexadécimales codées en dur. Tous les composables de l'application sont enveloppés dans `TutorialTheme` via `MainActivity.setContent { TutorialTheme { ... } }`.

### Types de build debug vs release

Le type de build `debug` active le HTTP en clair via `network_security_config.xml`. Cela permet à l'application d'atteindre le backend sur `http://10.0.2.2:8080` pendant le développement sans HTTPS. Le type de build `release` enforce le HTTPS uniquement.

`BASE_URL` est injecté depuis `BuildConfig` pour que le même code puisse cibler différents environnements sans modifications.

---

## Fichiers clés

| Fichier | Module | Rôle |
|---|---|---|
| `settings.gradle.kts` | racine | Déclare tous les modules |
| `gradle/libs.versions.toml` | racine | Catalogue de versions |
| `TutorialApplication.kt` | `:app` | Point d'entrée `@HiltAndroidApp` |
| `MainActivity.kt` | `:app` | Point d'entrée Compose, `TutorialTheme` |
| `NetworkModule.kt` | `:app` | OkHttp, Retrofit, qualificateurs |
| `AuthInterceptor.kt` | `:core:network` | Attache le token Bearer |
| `TokenAuthenticator.kt` | `:core:network` | Gère les 401, rafraîchit le token |
| `SecureTokenStorage.kt` | `:core:storage` | Stockage de tokens backé par Keystore |
| `TutorialDatabase.kt` | `:core:database` | Base de données Room, version du schéma |
| `DatabaseModule.kt` | `:core:database` | Fournit la DB et les DAOs |
| `AppNavigation.kt` | `:app` | NavHost, séparation graphe auth/principal |
| `Screen.kt` | `:app` | Définitions de routes typées |
| `TutorialTheme.kt` | `:core:ui` | Wrapper de thème Material 3 |
| `Color.kt` | `:core:ui` | Schémas de couleurs clair et sombre |
