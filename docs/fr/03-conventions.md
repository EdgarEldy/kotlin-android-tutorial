# Conventions de code

Ce document décrit les règles de nommage, l'organisation des fichiers et les standards de documentation utilisés dans tout le projet. Les respecter de façon cohérente garde le code lisible et prévisible à mesure qu'il grandit.

---

## Structure des packages

Package racine : `com.edgareldy.tutorial`

Chaque feature étend la racine :

| Module | Package |
|---|---|
| `:feature:auth` | `com.edgareldy.tutorial.auth` |
| `:feature:product-catalog` | `com.edgareldy.tutorial.products` |
| `:feature:cart` | `com.edgareldy.tutorial.cart` |
| `:feature:orders` | `com.edgareldy.tutorial.orders` |
| `:core:network` | `com.edgareldy.tutorial.core.network` |
| `:core:storage` | `com.edgareldy.tutorial.core.storage` |
| `:core:database` | `com.edgareldy.tutorial.data.local` |
| `:core:ui` | `com.edgareldy.tutorial.core.ui` |

---

## Conventions de nommage

### Fichiers et classes

| Type | Convention | Exemple |
|---|---|---|
| Service Retrofit | `XxxApiService` | `ProductApiService` |
| Interface repository | `XxxRepository` | `ProductRepository` |
| Implémentation repository | `XxxRepositoryImpl` | `ProductRepositoryImpl` |
| Use case | `VerbNomUseCase` | `GetProductsUseCase`, `AddToCartUseCase` |
| ViewModel | `XxxViewModel` | `ProductListViewModel` |
| Composable écran | `XxxScreen` | `ProductListScreen` |
| Entité Room | `XxxEntity` | `ProductEntity` |
| DAO Room | `XxxDao` | `ProductDao` |
| DTO | `XxxResponse` / `XxxRequest` | `ProductResponse`, `AddToCartRequest` |
| Module Hilt | `XxxModule` | `ProductsModule`, `DatabaseModule` |
| UiState | `XxxUiState` | `ProductListUiState` |

### Noms de champs JSON

Tous les champs JSON utilisent le `snake_case` mappé via `@SerialName` :

```kotlin
@Serializable
data class ProductResponse(
    @SerialName("product_id") val productId: Long,
    @SerialName("product_name") val productName: String,
    @SerialName("unit_price") val unitPrice: Double
)
```

Ne pas supposer que `@SerialName` est inutile - toujours le déclarer explicitement sur chaque champ, même quand le nom Kotlin correspondrait déjà.

---

## KDoc

Chaque classe, interface, objet, data class, enum et sealed class requiert un bloc KDoc :

```kotlin
/**
 * <Description du rôle, 1 à 3 lignes>
 *
 * Created edgar.muhamyangabo on <J/M/AA>
 * Author: edgar.muhamyangabo
 * Date: <J/M/AA>
 * Project: kotlin-android-tutorial
 */
```

- Format de date : `M/J/AA` sans zéro de tête (ex. `11/5/26`, pas `11/05/2026`).
- L'auteur est toujours `edgar.muhamyangabo`.
- Décrire le rôle de la classe - ce qu'elle fait et pourquoi elle existe - pas comment elle est implémentée.

Ne pas ajouter de KDoc aux fonctions ou propriétés sauf si l'objectif est véritablement non-évident à partir du nom.

---

## Use cases

Chaque use case a exactement un point d'entrée public :

```kotlin
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(filter: ProductFilter): Flow<Result<List<Product>>> =
        repository.getProducts(filter)
}
```

La convention `operator fun invoke(...)` permet aux appelants d'écrire `getProductsUseCase(filter)` au lieu de `getProductsUseCase.invoke(filter)`.

La logique métier appartient au use case, pas au ViewModel ou au repository. Le ViewModel orchestre ; le use case décide.

---

## Types de retour des repositories

| Situation | Type de retour |
|---|---|
| Données observables dans le temps (liste cachée) | `Flow<Result<List<T>>>` |
| Opération async ponctuelle (login, créer commande) | `suspend fun ... : Result<T>` |

Utiliser `runCatching { }` pour envelopper les appels réseau et convertir les exceptions en `Result.failure` :

```kotlin
override suspend fun login(email: String, password: String): Result<AuthTokens> =
    runCatching {
        val response = apiService.login(LoginRequest(email, password))
        response.data?.toDomain() ?: error("Pas de données dans la réponse")
    }
```

---

## ViewModel et UiState

Le ViewModel expose un seul `StateFlow<XxxUiState>` par écran. Ne jamais exposer plusieurs flags booléens.

```kotlin
// Correct
val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

// Incorrect - à éviter
val isLoading: StateFlow<Boolean> = ...
val orders: StateFlow<List<Order>> = ...
val errorMessage: StateFlow<String?> = ...
```

Les événements ponctuels (message snackbar, navigation après une action réussie) sont réinitialisés après consommation pour éviter leur rejeu à la recomposition :

```kotlin
fun errorShown() {
    if (_uiState.value is OrderListUiState.Error) {
        _uiState.value = OrderListUiState.Idle
    }
}
```

---

## Messages de commit

Format : `type(scope): courte description impérative`

```
feat(cart): add item quantity update endpoint call
fix(auth): prevent double navigation on successful login
refactor(orders): extract cache-first logic to repository base
test(products): add GetProductsUseCase unit test
chore(deps): bump Compose BOM to 2024.12.01
```

**Types :** `feat` `fix` `refactor` `test` `docs` `chore` `perf` `style`

**Scopes :** `core` `auth` `products` `cart` `orders` `ui` `nav` `db` `network` `storage`

Le corps optionnel (séparé par une ligne vide) explique le **pourquoi** en anglais, rédigé pour un lecteur qui suit le tutoriel :

```
feat(auth): implement token refresh on 401

The TokenAuthenticator intercepts 401 responses and transparently
retries the original request with a fresh token. A mutex serializes
concurrent retries so only one refresh call is made even when several
requests fail at the same time.
```

Un commit par fichier logique. Créer une entité et son DAO ensemble dans un seul commit est acceptable ; mélanger une entité avec un écran ne l'est pas.

---

## Ce qu'il faut éviter

- **Tirets cadratins (`-`)** partout : dans le code, les KDoc, les commentaires, les chaînes de caractères, les logs ou les fichiers de documentation. Utiliser un tiret `-` ou reformuler.
- **Logique métier dans les composables** : un composable appelle `viewModel.someAction()`, jamais directement un repository ou un use case.
- **Logique métier dans les ViewModels** : le ViewModel appelle un use case ; le use case contient la règle.
- **DTOs dans la couche présentation** : le ViewModel reçoit des modèles domain, pas des objets `XxxResponse`.
- **Couleurs codées en dur** : utiliser les tokens `MaterialTheme.colorScheme.*`, jamais des valeurs hexadécimales.
- **`Co-Authored-By: Claude`** dans les messages de commit.
