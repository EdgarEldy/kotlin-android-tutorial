# Code conventions

This document describes the naming rules, file organization and documentation standards used throughout the project. Following them consistently keeps the codebase readable and predictable as it grows.

---

## Package structure

Root package: `com.edgareldy.tutorial`

Each feature extends the root:

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

## Naming conventions

### Files and classes

| Type | Convention | Example |
|---|---|---|
| Retrofit service | `XxxApiService` | `ProductApiService` |
| Repository interface | `XxxRepository` | `ProductRepository` |
| Repository implementation | `XxxRepositoryImpl` | `ProductRepositoryImpl` |
| Use case | `VerbNounUseCase` | `GetProductsUseCase`, `AddToCartUseCase` |
| ViewModel | `XxxViewModel` | `ProductListViewModel` |
| Screen composable | `XxxScreen` | `ProductListScreen` |
| Room entity | `XxxEntity` | `ProductEntity` |
| Room DAO | `XxxDao` | `ProductDao` |
| DTO | `XxxResponse` / `XxxRequest` | `ProductResponse`, `AddToCartRequest` |
| Hilt module | `XxxModule` | `ProductsModule`, `DatabaseModule` |
| UiState | `XxxUiState` | `ProductListUiState` |

### JSON field names

All JSON fields use `snake_case` mapped via `@SerialName`:

```kotlin
@Serializable
data class ProductResponse(
    @SerialName("product_id") val productId: Long,
    @SerialName("product_name") val productName: String,
    @SerialName("unit_price") val unitPrice: Double
)
```

Do not rely on `@SerialName` being unnecessary — always declare it explicitly on every field, even when the Kotlin name would already match.

---

## KDoc

Every class, interface, object, data class, enum and sealed class requires a KDoc block:

```kotlin
/**
 * <Description of the role, 1 to 3 lines>
 *
 * Created edgar.muhamyangabo on <M/D/YY>
 * Author: edgar.muhamyangabo
 * Date: <M/D/YY>
 * Project: kotlin-android-tutorial
 */
```

- Date format: `M/D/YY` with no leading zero (e.g., `11/5/26`, not `11/05/2026`).
- Author is always `edgar.muhamyangabo`.
- Describe the role of the class — what it does and why it exists — not how it is implemented.

Do not add KDoc to functions or properties unless the purpose is genuinely non-obvious from the name.

---

## Use cases

Each use case has exactly one public entry point:

```kotlin
class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(filter: ProductFilter): Flow<Result<List<Product>>> =
        repository.getProducts(filter)
}
```

The `operator fun invoke(...)` convention lets callers write `getProductsUseCase(filter)` instead of `getProductsUseCase.invoke(filter)`.

Business logic belongs in the use case, not in the ViewModel or repository. The ViewModel orchestrates; the use case decides.

---

## Repository return types

| Situation | Return type |
|---|---|
| Data that can be observed over time (cached list) | `Flow<Result<List<T>>>` |
| One-shot async operation (login, create order) | `suspend fun ... : Result<T>` |

Use `runCatching { }` to wrap network calls and convert exceptions into `Result.failure`:

```kotlin
override suspend fun login(email: String, password: String): Result<AuthTokens> =
    runCatching {
        val response = apiService.login(LoginRequest(email, password))
        response.data?.toDomain() ?: error("No data in response")
    }
```

---

## ViewModel and UiState

The ViewModel exposes a single `StateFlow<XxxUiState>` per screen. Never expose multiple boolean flags.

```kotlin
// Correct
val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

// Incorrect - avoid
val isLoading: StateFlow<Boolean> = ...
val orders: StateFlow<List<Order>> = ...
val errorMessage: StateFlow<String?> = ...
```

One-off events (a snackbar message, navigation after a successful action) are reset after being consumed to avoid replaying on recomposition:

```kotlin
fun errorShown() {
    if (_uiState.value is OrderListUiState.Error) {
        _uiState.value = OrderListUiState.Idle
    }
}
```

---

## Commit messages

Format: `type(scope): short imperative description`

```
feat(cart): add item quantity update endpoint call
fix(auth): prevent double navigation on successful login
refactor(orders): extract cache-first logic to repository base
test(products): add GetProductsUseCase unit test
chore(deps): bump Compose BOM to 2024.12.01
```

**Types:** `feat` `fix` `refactor` `test` `docs` `chore` `perf` `style`

**Scopes:** `core` `auth` `products` `cart` `orders` `ui` `nav` `db` `network` `storage`

The optional body (separated by a blank line) explains the **why** in English, written for a reader following the tutorial:

```
feat(auth): implement token refresh on 401

The TokenAuthenticator intercepts 401 responses and transparently
retries the original request with a fresh token. A mutex serializes
concurrent retries so only one refresh call is made even when several
requests fail at the same time.
```

One commit per logical file. Creating an entity and its DAO together in one commit is acceptable; mixing an entity with a screen is not.

---

## What to avoid

- **Em dashes (`-`)** anywhere: in code, KDoc, comments, strings, logs or documentation files. Use a hyphen `-` or rephrase.
- **Business logic in composables**: a composable calls `viewModel.someAction()`, never a repository or use case directly.
- **Business logic in ViewModels**: the ViewModel calls a use case; the use case contains the rule.
- **DTOs in the presentation layer**: the ViewModel receives domain models, not `XxxResponse` objects.
- **Hardcoded colors**: use `MaterialTheme.colorScheme.*` tokens, never hex values.
- **`Co-Authored-By: Claude`** in commit messages.
