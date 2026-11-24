# Architecture

This document explains the structural decisions behind the application: how it is split into modules, why the layers are separated the way they are, and what rules govern the flow of data from the network to the screen.

---

## Multi-module Gradle project

The project is split into Gradle modules. Each module is compiled independently and declares only the dependencies it actually needs.

```
:app                      ← entry point: MainActivity, navigation, NetworkModule
:core:network             ← OkHttp, Retrofit, AuthInterceptor, TokenAuthenticator
:core:storage             ← Keystore-backed SecureTokenStorage, DataStore
:core:database            ← shared TutorialDatabase (Room)
:core:ui                  ← TutorialTheme, colors, typography
:feature:auth             ← sign-in, register, OTP, password reset, profile
:feature:product-catalog  ← home, categories, product list, product detail
:feature:cart             ← cart, checkout
:feature:orders           ← order history, order detail
```

**Why split into modules?**

- Faster incremental builds: changing `:feature:cart` does not recompile `:feature:orders`.
- Enforced boundaries: a feature module cannot accidentally import code from another feature module unless it declares that dependency explicitly.
- Clear ownership: each module owns a specific business domain end-to-end.

---

## Clean Architecture layers inside a feature module

Each `:feature:*` module is divided into three layers. Dependencies only point inward.

```
presentation/  →  domain/  ←  data/
                     ↑
              :core:network
              :core:database
              :core:storage
```

### data/ — infrastructure

Talks to the outside world: the network API and the local Room database.

- **dto/** — Kotlin data classes that mirror the JSON structure returned by the API. They are annotated with `@Serializable` and `@SerialName`. They never leave this layer.
- **api/** — Retrofit service interfaces. One interface per domain resource.
- **repository/** — `XxxRepositoryImpl`. Fetches from the network, writes to Room, reads from Room. Returns `Result<T>` or `Flow<Result<T>>`.

### domain/ — business rules

Pure Kotlin. No Android imports, no Retrofit, no Room.

- **model/** — domain models used by ViewModels and composables. These are hand-written and have no dependency on the serialization library.
- **repository/** — interfaces that the data layer implements. The domain layer defines the contract; the data layer fulfills it.
- **usecase/** — one class per use case with a single `operator fun invoke(...)`. All business logic lives here.

### presentation/ — UI

Talks to the domain layer, never to the data layer directly.

- **screens/** — Compose screens. Stateless where possible. Receives state from the ViewModel and emits events back to it.
- **viewmodel/** — one ViewModel per screen. Holds a `StateFlow<UiState>`. Calls use cases. Never talks to Retrofit or Room directly.
- **components/** — composables reusable within this feature only.

---

## Data flow: from screen tap to API and back

```
User taps "Add to cart"
  ↓
CartScreen.onAddClick()         ← composable emits the event
  ↓
CartViewModel.addToCart(...)    ← ViewModel calls the use case
  ↓
AddToCartUseCase.invoke(...)    ← use case validates and delegates
  ↓
CartRepository.addItem(...)     ← repository interface (domain)
  ↓
CartRepositoryImpl.addItem(...) ← implementation (data layer)
  ↓
CartApiService.addItem(...)     ← Retrofit interface → HTTP POST
  ↓
Backend                         ← Spring Boot processes the request
  ↓
(response travels back up)
  ↓
CartRepositoryImpl              ← maps DTO to domain model, upserts in Room
  ↓
CartViewModel                   ← updates _uiState.value
  ↓
CartScreen                      ← re-renders with new state
```

---

## UiState pattern

Every screen is driven by a sealed class:

```kotlin
sealed class CartUiState {
    data object Loading : CartUiState()
    data class Success(val cart: Cart) : CartUiState()
    data class Error(val message: String) : CartUiState()
    data object Empty : CartUiState()
}
```

The ViewModel exposes it as a `StateFlow`:

```kotlin
private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
```

The composable uses a `when` expression to render the right UI for each state. There is no boolean flag like `isLoading` or `hasError` — the state is always exactly one value.

---

## Offline strategy

| Operation | Strategy |
|---|---|
| Read (products, categories, orders) | Cache-first: return Room data immediately, then refresh from the network in the background. If the network fails and the cache is non-empty, the error is swallowed silently. If the cache is empty, the error is surfaced. |
| Pull-to-refresh | Delete the cache and force a network request. |
| Write (cart mutations, checkout) | Network-required: return an explicit error if offline. No optimistic updates. |

The pattern for cache-first reads in a repository:

```kotlin
override fun getOrders(): Flow<Result<List<Order>>> = flow {
    // 1. Emit from Room immediately
    emitAll(
        orderDao.getOrders().map { entities -> Result.success(entities.map { it.toDomain() }) }
    )
}.onStart {
    // 2. Refresh from network in the background
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

## Security architecture

Tokens are the most sensitive data the application handles.

- `access_token` and `refresh_token` are stored **only** in `SecureTokenStorage`, which uses Android Keystore-backed AES encryption.
- No token is ever written to `SharedPreferences`, plain `DataStore`, a file or a log.
- The `AuthInterceptor` reads the token and attaches it to every request. No screen ever reads a token directly.
- The `TokenAuthenticator` intercepts 401 responses, calls the refresh endpoint using a separate `OkHttpClient` (no `TokenAuthenticator` on this secondary client, to avoid an infinite loop), saves the new tokens and retries the failed request.
- Concurrent 401s are serialized: only one refresh call is made even if multiple requests fail simultaneously.
