# Android application fundamentals

This document explains the building blocks of a modern Android application and how they connect. It is the foundation for understanding every pattern used in this project.

---

## From Activity to Compose

A traditional Android UI is built with XML layouts inflated inside an `Activity`. Jetpack Compose replaces XML with Kotlin functions called **composables**. A composable describes what the UI looks like for a given state — the framework handles rendering and updates.

```kotlin
// A composable is a plain Kotlin function annotated with @Composable
@Composable
fun ProductCard(name: String, price: Double) {
    Column {
        Text(text = name)
        Text(text = "$$price")
    }
}
```

This project still uses a single `Activity` (`MainActivity`) as the entry point. Everything rendered on screen is a composable.

---

## Application lifecycle

Android can destroy and recreate an `Activity` at any time — when the user rotates the screen, switches apps, or the system reclaims memory. Any object created inside `Activity.onCreate()` is lost on recreation.

A `ViewModel` survives configuration changes. It lives as long as the screen is in the back stack, not as long as the `Activity`. This makes it the right place to hold UI state.

```
User rotates screen
  ↓
Activity.onDestroy() — Activity is destroyed
  ↓
Activity.onCreate() — Activity recreated
  ↓
ViewModel.init()    — NOT called again; same ViewModel instance reattached
```

ViewModels are always obtained via `hiltViewModel()` in Compose — never instantiated with `MyViewModel()` directly.

---

## State in Compose

Compose is **reactive**: the UI re-renders automatically when state changes. State is held outside the composable (in the ViewModel) and passed down.

```kotlin
// ViewModel holds the state
val uiState: StateFlow<ProductListUiState> = ...

// Composable observes it
@Composable
fun ProductListScreen(viewModel: ProductListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is Loading -> CircularProgressIndicator()
        is Success -> LazyColumn { ... }
        is Error   -> Text(uiState.message)
    }
}
```

`collectAsStateWithLifecycle()` stops collecting when the screen is not visible (in the background), saving battery and preventing unnecessary work.

---

## Navigation

This project uses **Navigation Compose**. The navigation graph is a set of destinations (composables) connected by routes. A `NavController` manages the back stack.

```
Auth graph                 Main graph
  SignIn ──────────────── ProductList ← startDestination
  Register                     ↓
  ActivateAccount          ProductDetail
  ForgotPassword           Cart
  VerifyOtp                    ↓
  ResetPassword            Checkout
                           OrderList
                               ↓
                           OrderDetail
                           Profile
```

The two graphs are separated: an unauthenticated user cannot reach the main graph. On launch, the app reads the stored token and routes to the correct graph.

Deep linking inside the main graph uses typed arguments:

```kotlin
composable(
    route = "product/{productId}",
    arguments = listOf(navArgument("productId") { type = NavType.LongType })
) {
    ProductDetailScreen(...)
}
```

---

## Material 3

Material 3 is Google's design system for Android. It provides components (buttons, cards, chips, navigation bars) and a color system built around roles rather than raw hex values.

Color roles used in this project:

| Role | Purpose |
|---|---|
| `primary` | Main action buttons, active tab indicator |
| `onPrimary` | Text/icons on primary surfaces |
| `primaryContainer` | Softer tint of primary (selected navigation item) |
| `surface` | Card and bottom bar backgrounds |
| `error` | Error states, destructive actions |

Always reference tokens like `MaterialTheme.colorScheme.primary` instead of hardcoded hex values. This ensures the app adapts correctly when the user switches between light and dark mode.

---

## Coroutines on Android

Kotlin coroutines replace callbacks for asynchronous work. Android provides two built-in coroutine scopes:

| Scope | Lifetime | Use |
|---|---|---|
| `viewModelScope` | Until ViewModel is cleared | Network calls, database queries triggered by user actions |
| `lifecycleScope` | Until Activity/Fragment is destroyed | Collecting flows in Composables (prefer `collectAsStateWithLifecycle` instead) |

A `Flow` is a stream of values over time. `StateFlow` is a special Flow that always holds a current value and replays it to new collectors.

```kotlin
// Repository emits a stream of results
fun getProducts(): Flow<Result<List<Product>>>

// ViewModel collects and transforms into UiState
viewModelScope.launch {
    repository.getProducts()
        .collect { result ->
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unknown error") }
            )
        }
}
```

---

## Dependency injection with Hilt

Hilt is the recommended DI framework for Android. It is built on top of Dagger and handles the Android-specific lifecycle automatically.

Each injectable class is annotated to tell Hilt how to create it:

```kotlin
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel()
```

Modules provide objects that cannot be annotated directly (interfaces, third-party classes):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(...): Retrofit = ...
}
```

`@InstallIn(SingletonComponent::class)` means the provided objects live for the entire application lifetime.

---

## Image loading with Coil

Coil is an image loading library optimized for Compose. It handles caching, placeholder display and error fallback automatically.

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(product.imageUrl)
        .crossfade(true)
        .build(),
    contentDescription = product.name,
    placeholder = painterResource(R.drawable.ic_image_placeholder),
    error = painterResource(R.drawable.ic_broken_image)
)
```

No manual caching or threading code is needed — Coil manages all of that internally.
