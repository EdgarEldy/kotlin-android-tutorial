# Architecture

Ce document explique les décisions structurelles derrière l'application : comment elle est découpée en modules, pourquoi les couches sont séparées comme elles le sont, et quelles règles gouvernent le flux de données du réseau à l'écran.

---

## Projet Gradle multi-modules

Le projet est découpé en modules Gradle. Chaque module est compilé indépendamment et ne déclare que les dépendances dont il a réellement besoin.

```
:app                      ← point d'entrée : MainActivity, navigation, NetworkModule
:core:network             ← OkHttp, Retrofit, AuthInterceptor, TokenAuthenticator
:core:storage             ← SecureTokenStorage chiffré par Keystore, DataStore
:core:database            ← TutorialDatabase partagé (Room)
:core:ui                  ← TutorialTheme, couleurs, typographie
:feature:auth             ← connexion, inscription, OTP, réinitialisation mdp, profil
:feature:product-catalog  ← accueil, catégories, liste produits, détail produit
:feature:cart             ← panier, checkout
:feature:orders           ← historique commandes, détail commande
```

**Pourquoi découper en modules ?**

- Builds incrémentaux plus rapides : modifier `:feature:cart` ne recompile pas `:feature:orders`.
- Frontières enforced : un module feature ne peut pas importer accidentellement du code d'un autre module feature sans déclarer explicitement cette dépendance.
- Propriété claire : chaque module possède un domaine métier spécifique de bout en bout.

---

## Couches Clean Architecture dans un module feature

Chaque module `:feature:*` est divisé en trois couches. Les dépendances ne pointent que vers l'intérieur.

```
presentation/  →  domain/  ←  data/
                     ↑
              :core:network
              :core:database
              :core:storage
```

### data/ - infrastructure

Parle au monde extérieur : l'API réseau et la base de données Room locale.

- **dto/** - classes de données Kotlin qui reflètent la structure JSON retournée par l'API. Annotées avec `@Serializable` et `@SerialName`. Ne quittent jamais cette couche.
- **api/** - interfaces de service Retrofit. Une interface par ressource de domaine.
- **repository/** - `XxxRepositoryImpl`. Récupère du réseau, écrit dans Room, lit depuis Room. Retourne `Result<T>` ou `Flow<Result<T>>`.

### domain/ - règles métier

Kotlin pur. Aucun import Android, Retrofit ou Room.

- **model/** - modèles domain utilisés par les ViewModels et les composables. Écrits à la main et sans dépendance sur la bibliothèque de sérialisation.
- **repository/** - interfaces que la couche data implémente. La couche domain définit le contrat ; la couche data le remplit.
- **usecase/** - une classe par use case avec un seul `operator fun invoke(...)`. Toute la logique métier vit ici.

### presentation/ - interface utilisateur

Parle à la couche domain, jamais directement à la couche data.

- **screens/** - écrans Compose. Sans état quand c'est possible. Reçoivent l'état du ViewModel et lui émettent des événements en retour.
- **viewmodel/** - un ViewModel par écran. Contient un `StateFlow<UiState>`. Appelle les use cases. Ne parle jamais directement à Retrofit ou Room.
- **components/** - composables réutilisables dans ce module feature seulement.

---

## Flux de données : d'un tap écran à l'API et retour

```
L'utilisateur tape "Ajouter au panier"
  ↓
CartScreen.onAddClick()         ← le composable émet l'événement
  ↓
CartViewModel.addToCart(...)    ← le ViewModel appelle le use case
  ↓
AddToCartUseCase.invoke(...)    ← le use case valide et délègue
  ↓
CartRepository.addItem(...)     ← interface repository (domain)
  ↓
CartRepositoryImpl.addItem(...) ← implémentation (couche data)
  ↓
CartApiService.addItem(...)     ← interface Retrofit → HTTP POST
  ↓
Backend                         ← Spring Boot traite la requête
  ↓
(la réponse remonte)
  ↓
CartRepositoryImpl              ← mappe le DTO en modèle domain, upsert dans Room
  ↓
CartViewModel                   ← met à jour _uiState.value
  ↓
CartScreen                      ← se re-rend avec le nouvel état
```

---

## Pattern UiState

Chaque écran est piloté par une sealed class :

```kotlin
sealed class CartUiState {
    data object Loading : CartUiState()
    data class Success(val cart: Cart) : CartUiState()
    data class Error(val message: String) : CartUiState()
    data object Empty : CartUiState()
}
```

Le ViewModel l'expose comme `StateFlow` :

```kotlin
private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
```

Le composable utilise une expression `when` pour rendre l'interface appropriée pour chaque état. Il n'y a pas de flag booléen comme `isLoading` ou `hasError` - l'état est toujours exactement une valeur.

---

## Stratégie offline

| Opération | Stratégie |
|---|---|
| Lecture (produits, catégories, commandes) | Cache-first : retourner les données Room immédiatement, puis rafraîchir depuis le réseau en arrière-plan. Si le réseau échoue et le cache est non-vide, l'erreur est silencieusement ignorée. Si le cache est vide, l'erreur est remontée. |
| Pull-to-refresh | Supprimer le cache et forcer une requête réseau. |
| Écriture (mutations panier, checkout) | Réseau requis : retourner une erreur explicite si hors-ligne. Pas de mises à jour optimistes. |

Le pattern pour les lectures cache-first dans un repository :

```kotlin
override fun getOrders(): Flow<Result<List<Order>>> = flow {
    // 1. Émettre depuis Room immédiatement
    emitAll(
        orderDao.getOrders().map { entities -> Result.success(entities.map { it.toDomain() }) }
    )
}.onStart {
    // 2. Rafraîchir depuis le réseau en arrière-plan
    backgroundScope.launch {
        runCatching { apiService.getOrders() }
            .onSuccess { response ->
                val dtos = response.data?.content ?: return@onSuccess
                orderDao.deleteAll()
                orderDao.upsertAll(dtos.map { it.toEntity() })
            }
    }
}
```

---

## Architecture de sécurité

Les tokens sont les données les plus sensibles que l'application gère.

- `access_token` et `refresh_token` sont stockés **uniquement** dans `SecureTokenStorage`, qui utilise le chiffrement AES backé par Android Keystore.
- Aucun token n'est jamais écrit dans `SharedPreferences`, un `DataStore` simple, un fichier ou un log.
- L'`AuthInterceptor` lit le token et l'attache à chaque requête. Aucun écran ne lit jamais un token directement.
- Le `TokenAuthenticator` intercepte les réponses 401, appelle le endpoint de refresh avec un `OkHttpClient` séparé (sans `TokenAuthenticator` sur ce client secondaire, pour éviter une boucle infinie), sauvegarde les nouveaux tokens et retente la requête échouée.
- Les 401 concurrents sont sérialisés : un seul appel de refresh est fait même si plusieurs requêtes échouent simultanément.
