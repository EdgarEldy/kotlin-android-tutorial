# Branch: feature/orders

This branch implements the order history and order detail screens. It applies the same cache-first strategy used for products and adds a Room-persisted order cache with a foreign-key relationship between orders and their line items.

---

## What you will learn

- How to model a parent-child relationship in Room (orders and order items) with a foreign key constraint
- How to load a list efficiently (avoid N+1 queries) while still providing full item detail on demand
- How to color-code a status badge using Material 3 color tokens without hardcoding hex values
- How the `:feature:orders` module depends on `:feature:cart` for `ClearCartUseCase`, and why that is an acceptable inter-feature dependency

---

## What it delivers

### Room schema: orders and order items

Orders and their line items are stored in two related tables:

```
orders (id, order_number, status, total_amount, cached_at)
order_items (id, order_id FK→orders.id, product_id, product_name, unit_price, quantity, subtotal)
```

The foreign key ensures that if an order is deleted from the cache, its items are deleted automatically (`onDelete = CASCADE`).

The order list view loads only `orders` rows — no items are loaded for the list. Loading items for every order in the list would cause N+1 queries (one query per order). Items are loaded only when the user opens a specific order.

```kotlin
// Order list: no items loaded
fun getOrders(): Flow<List<OrderEntity>>

// Order detail: items loaded for one order
fun getOrderById(id: Long): Flow<OrderEntity?>
fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>
```

The `OrderRepositoryImpl` combines these two flows for the detail view using `combine`:

```kotlin
override fun getOrderById(id: Long): Flow<Result<Order>> =
    combine(orderDao.getOrderById(id), orderDao.getOrderItems(id)) { entity, items ->
        entity?.let { Result.success(it.toDomain(items)) } ?: Result.failure(NoSuchElementException())
    }
```

### Cache-first reads

`OrderRepositoryImpl` follows the same pattern as `ProductRepositoryImpl`:

1. Emit from Room immediately.
2. Refresh from the network in the background (`@ApplicationScope` coroutine scope).
3. On network success: `deleteAll()` then `upsertAll()` for orders, `deleteItemsByOrderId()` then `upsertItems()` for items.
4. On network failure: swallow the error if the cache is non-empty.

Deleting before upserting is important. If an order's items changed on the backend (a cancelled item, a price correction), a plain upsert would leave stale item rows that no longer exist on the server.

### Status badge

`OrderStatusChip` is a composable that maps `OrderStatus` to a color and a label:

| Status | Color role |
|---|---|
| `PENDING` | `MaterialTheme.colorScheme.tertiary` (amber/orange) |
| `CONFIRMED` | `MaterialTheme.colorScheme.primary` (brand green/blue) |
| `CANCELLED` | `MaterialTheme.colorScheme.error` (red) |

Color roles are used instead of hardcoded hex values. When the user switches to dark mode, Material 3 automatically adjusts the role colors — no extra code is needed.

`OrderStatusChip` is `internal` (accessible within `:feature:orders`) so it can be shared between `OrderListScreen` and `OrderDetailScreen` without being part of the public API of the module.

### Inter-feature dependency: ClearCartUseCase

`CreateOrderUseCase` in `:feature:orders` calls `ClearCartUseCase` from `:feature:cart` after a successful checkout. This is the only direct inter-feature dependency in the project.

The dependency is declared explicitly in `feature/orders/build.gradle.kts`:

```kotlin
implementation(project(":feature:cart"))
```

The alternative — duplicating cart-clearing logic in the orders module — would be worse. The rule is: prefer a declared dependency over duplicated behavior. Cross-feature dependencies are acceptable when they represent a genuine domain relationship (a checkout creates an order and empties the cart).

### Pull-to-refresh

`OrderListScreen` supports pull-to-refresh. The gesture calls `RefreshOrdersUseCase`, which deletes all cached orders and forces a fresh network fetch. This is the same pattern used in the product catalog.

---

## Key files

| File | Layer | Role |
|---|---|---|
| `OrderEntity.kt` | data | Room entity for an order |
| `OrderItemEntity.kt` | data | Room entity for an order line item |
| `OrderDao.kt` | data | upsert, deleteAll, getOrders, getOrderById, getOrderItems |
| `OrderRepositoryImpl.kt` | data | Cache-first reads, combined order+items flow |
| `OrderApiService.kt` | data | Retrofit interface for `/api/v1/orders/` |
| `Order.kt` | domain | Domain model (Order, OrderItem, OrderStatus) |
| `GetOrdersUseCase.kt` | domain | Returns flow of cached orders |
| `GetOrderByIdUseCase.kt` | domain | Returns flow of a single order with its items |
| `OrderListViewModel.kt` | presentation | List state, pull-to-refresh trigger |
| `OrderDetailViewModel.kt` | presentation | Loads one order by id |
| `OrderListScreen.kt` | presentation | Paginated list, status chips, pull-to-refresh |
| `OrderDetailScreen.kt` | presentation | Order header, item breakdown, total |
| `OrderStatusChip.kt` | presentation | Color-coded status badge (internal) |
| `OrdersModule.kt` | DI | Provides `OrderRepository`, `OrderApiService` |
