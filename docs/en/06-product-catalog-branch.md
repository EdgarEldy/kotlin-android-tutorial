# Branch: feature/product-catalog

This branch builds the product browsing experience: home screen, category navigation, paginated product list with search and filters, product detail with image gallery, and the bottom navigation shell.

---

## What you will learn

- How to implement a cache-first offline strategy with Room and Retrofit
- How to use `Flow` to observe Room database changes in real time
- How to build a paginated list that loads more items as the user scrolls
- How to filter a list with multiple parameters (name, category, price range) without polling the network on every keystroke
- How to load remote images asynchronously with Coil, including placeholder and error states
- How the bottom navigation bar tracks the selected tab and shows a badge on the cart icon
- How to invalidate the local cache on pull-to-refresh

---

## What it delivers

### Cache-first read strategy

Products and categories are available offline because the application serves them from Room first and refreshes from the network in the background.

The flow:
1. `ProductRepositoryImpl.getProducts()` returns a `Flow` that emits the current Room contents immediately.
2. A background coroutine calls the API, diffs the results against the cache, and upserts new or changed rows.
3. Room emits the updated data automatically — the UI updates without any extra logic.

If the network call fails and the cache is non-empty, the error is ignored and the user sees the cached data. If the cache is empty (first launch with no network), the error is surfaced as `UiState.Error`.

```kotlin
// Simplified excerpt from ProductRepositoryImpl
override fun getProducts(filter: ProductFilter): Flow<Result<List<Product>>> = flow {
    emitAll(productDao.getProducts(filter).map { Result.success(it.map { e -> e.toDomain() }) })
}.onStart {
    backgroundScope.launch {
        runCatching { apiService.getProducts(...) }
            .onSuccess { response ->
                productDao.deleteAll()
                productDao.upsertAll(response.data?.content?.map { it.toEntity() } ?: return@onSuccess)
            }
    }
}
```

`deleteAll()` before `upsertAll()` ensures deleted products (removed from the backend) disappear from the cache. A plain upsert would leave stale rows indefinitely.

### Room DAOs with filtered queries

`ProductDao` accepts a filter object and builds the SQL query accordingly:

```kotlin
@Query("""
    SELECT * FROM products
    WHERE (:categoryId IS NULL OR category_id = :categoryId)
      AND (:minPrice IS NULL OR unit_price >= :minPrice)
      AND (:maxPrice IS NULL OR unit_price <= :maxPrice)
      AND (:query IS NULL OR product_name LIKE '%' || :query || '%')
""")
fun getProducts(
    categoryId: Long?,
    minPrice: Double?,
    maxPrice: Double?,
    query: String?
): Flow<List<ProductEntity>>
```

Filtering happens in SQLite, not in Kotlin. This avoids loading thousands of entities into memory to discard most of them.

### Search debounce

The product list accepts a text query that is debounced before triggering a new filter:

```kotlin
viewModelScope.launch {
    snapshotFlow { searchQuery }
        .debounce(300)
        .collectLatest { query ->
            getProductsUseCase(currentFilter.copy(query = query)).collect { result ->
                _uiState.value = result.fold(...)
            }
        }
}
```

`debounce(300)` waits 300 ms after the last keystroke before updating the filter. `collectLatest` cancels the previous collection when a new query arrives, so only the most recent query is in flight.

### Coil image loading

Product images are loaded from URLs using `AsyncImage`. Coil handles caching, threading and retry automatically:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(product.mainImageUrl)
        .crossfade(true)
        .build(),
    contentDescription = product.name,
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
)
```

A placeholder is shown while the image loads; a fallback icon is shown if the URL is unreachable. Neither requires any manual state management.

### Bottom navigation shell

`MainScreen` wraps the entire main graph inside a `Scaffold` with a `NavigationBar`. The bar is shown only on top-level destinations (home, cart, orders, profile); detail screens hide it.

The cart icon shows a badge with the item count. `SharedCartViewModel` is scoped to the navigation graph and survives screen transitions, keeping the count synchronized across all screens.

### Pull-to-refresh

A swipe gesture on the product list calls `RefreshProductsUseCase`, which deletes the cache and forces a full network fetch:

```kotlin
class RefreshProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke() = repository.refreshProducts()
}

// In the repository
override suspend fun refreshProducts() {
    val response = apiService.getProducts(...)
    productDao.deleteAll()
    productDao.upsertAll(response.data?.content?.map { it.toEntity() } ?: return)
}
```

---

## Key files

| File | Layer | Role |
|---|---|---|
| `ProductRepositoryImpl.kt` | data | Cache-first reads, background refresh |
| `CategoryRepositoryImpl.kt` | data | Same pattern for categories |
| `ProductDao.kt` | data | Filtered queries, upsert, deleteAll |
| `ProductEntity.kt` | data | Room entity for a product |
| `GetProductsUseCase.kt` | domain | Passes filter params to repository |
| `RefreshProductsUseCase.kt` | domain | Forces a cache invalidation |
| `ProductListViewModel.kt` | presentation | Search debounce, filter state, pagination |
| `ProductDetailViewModel.kt` | presentation | Loads a single product by id |
| `ProductListScreen.kt` | presentation | Paginated grid, search bar, pull-to-refresh |
| `ProductDetailScreen.kt` | presentation | Image gallery, quantity picker, add-to-cart |
| `MainScreen.kt` | `:app` | Bottom navigation shell, cart badge |
| `SharedCartViewModel.kt` | presentation | Cart item count shared across tabs |
