# Branche : feature/auth

Cette branche implémente la couche d'authentification complète : connexion, inscription avec vérification email, restauration de session, rafraîchissement transparent des tokens et un flux de réinitialisation de mot de passe OTP mobile en 3 étapes.

---

## Ce que vous allez apprendre

- Comment implémenter le cycle de vie complet de l'authentification sur Android (login, logout, restauration de session)
- Comment persister les tokens de façon sécurisée et restaurer une session au démarrage à froid
- Comment câbler un `TokenAuthenticator` pour gérer l'expiration des tokens sans aucun code au niveau des écrans
- Comment construire un flux de formulaires multi-étapes dans Compose avec un état partagé entre les écrans
- Comment le reset de mot de passe OTP mobile diffère du flux web, et pourquoi les endpoints sont différents
- Comment tester un ViewModel qui émet une séquence d'états en utilisant Turbine

---

## Ce que ça livre

### Restauration de session au démarrage

Quand l'application démarre, `AppNavigation` lit l'`access_token` stocké dans `SecureTokenStorage`. Si un token existe, l'utilisateur est envoyé directement au graphe principal. Sinon, il voit l'écran de connexion. Cette vérification a lieu avant le premier frame affiché.

Aucune vérification de validité du token n'est faite à ce stade - le `TokenAuthenticator` gère l'expiration de façon paresseuse quand la première requête authentifiée retourne un 401.

### Connexion et inscription

`SignInViewModel` et `RegisterViewModel` suivent le même pattern :
1. L'utilisateur remplit le formulaire.
2. Le ViewModel valide les règles locales (champs non-vides, longueur du mot de passe) avant de faire un appel réseau.
3. À la soumission, le ViewModel appelle le use case, définit l'état `Loading` (désactivant le bouton de soumission), puis définit `Success` ou `Error`.

L'état `Error` porte un message lisible extrait du corps de réponse `ProblemDetail` de l'API. Après que l'erreur est affichée, `errorShown()` réinitialise l'état à `Idle` pour que l'utilisateur puisse réessayer sans qu'un état d'erreur obsolète bloque le formulaire.

### Activation de compte (OTP)

Après l'inscription, le backend envoie un code à 6 chiffres à l'email de l'utilisateur. L'`ActivateAccountScreen` présente un champ OTP et soumet à `POST /api/v1/auth/activate-account`. En cas de succès, la navigation passe à la connexion.

Le code d'activation est conservé uniquement dans l'état de formulaire transitoire. Il n'est jamais persisté ni journalisé.

### Réinitialisation de mot de passe mobile (3 étapes)

Les endpoints de reset de mot de passe web (`/api/v1/auth/forgot-password`) sont conçus pour le frontend Angular et nécessitent une URL de callback. Le flux mobile utilise un ensemble d'endpoints séparé sous `/api/v1/password-reset/` :

| Étape | Endpoint | Objectif |
|---|---|---|
| 1 | `POST /mobile` | L'utilisateur entre son email ; le backend envoie un OTP à 6 chiffres |
| 2 | `POST /verify-otp` | L'utilisateur entre l'OTP ; le backend valide sans le consommer |
| 3 | `POST /confirm?token={otp}` | L'utilisateur entre le nouveau mot de passe ; le backend consomme l'OTP et réinitialise |

L'étape 2 existe pour que l'utilisateur puisse voir l'écran de nouveau mot de passe avec la certitude que son OTP est valide, avant d'investir du temps à taper un nouveau mot de passe. L'OTP est passé comme paramètre de requête à l'étape 3, pas dans le corps de la requête.

La valeur OTP voyage comme état de formulaire dans la pile de navigation. Elle n'est jamais stockée sur disque.

### Câblage du rafraîchissement de token

`TokenAuthenticator` est câblé au use case `AuthRepository.refresh()`. Quand un 401 est reçu :

1. L'authenticator appelle `refresh()` en utilisant le `OkHttpClient` secondaire `@AuthOnly`.
2. Si le refresh réussit, les nouveaux tokens sont sauvegardés et la requête originale est retentée avec l'en-tête mis à jour.
3. Si le refresh échoue (le refresh token est aussi expiré), tous les credentials sont effacés et l'utilisateur est renvoyé à la connexion.

La redirection vers la connexion depuis `TokenAuthenticator` nécessite un `SharedFlow` ou mécanisme similaire parce que `TokenAuthenticator` s'exécute sur le thread OkHttp, pas sur le thread principal.

### Écran de profil

`ProfileScreen` appelle `GET /api/v1/auth/me` pour afficher les informations de profil de l'utilisateur. Cet endpoint est correct pour les données de l'utilisateur authentifié. Les endpoints `/api/v1/customers` sont admin uniquement et ne sont jamais appelés depuis le client mobile.

Le bouton de déconnexion appelle `LogoutUseCase`, qui poste sur `POST /api/v1/auth/logout` puis efface tous les tokens stockés indépendamment du statut de la réponse.

---

## Fichiers clés

| Fichier | Couche | Rôle |
|---|---|---|
| `AuthRepositoryImpl.kt` | data | Login, register, activate, logout, refresh, me |
| `PasswordResetRepositoryImpl.kt` | data | Reset OTP en 3 étapes |
| `AuthApiService.kt` | data | Interface Retrofit pour `/api/v1/auth/` |
| `PasswordResetApiService.kt` | data | Interface Retrofit pour `/api/v1/password-reset/` |
| `SignInUseCase.kt` | domain | Valide les credentials et délègue au repository |
| `RegisterUseCase.kt` | domain | Soumet l'inscription, retourne le signal d'activation requise |
| `LogoutUseCase.kt` | domain | Appelle le endpoint logout et efface les tokens locaux |
| `ResetPasswordUseCase.kt` | domain | Étape 3 du flux de reset OTP |
| `SignInViewModel.kt` | presentation | État du formulaire, loading, gestion des erreurs |
| `VerifyOtpViewModel.kt` | presentation | Validation OTP à l'étape 2 |
| `ResetPasswordViewModel.kt` | presentation | Formulaire nouveau mot de passe à l'étape 3 |
| `ProfileViewModel.kt` | presentation | Appelle `GetProfileUseCase` au démarrage |
| `AuthModule.kt` | DI | Fournit `AuthRepository`, `PasswordResetRepository` |
