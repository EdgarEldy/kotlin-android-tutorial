# Branch: feature/shopping-cart

This branch implements the shopping cart and the checkout confirmation flow. The cart is entirely server-side — the client never calculates totals, prices or discounts.

---

## What you will learn

- Why cart state belongs on the server and what that means for the client
- How to prevent duplicate form submissions with a simple state flag
- How the checkout endpoint works (`POST /api/v1/orders` with no request body)
- How to clear the navigation back stack after a successful checkout so the user cannot go back to an empty cart
- How to share a ViewModel across multiple composables within the same navigation graph

---

## What it delivers

### Server-side cart model

The cart is owned by the backend. The client does not store cart contents in Room — it fetches the cart on every screen visit and after every mutation. This avoids synchronization bugs between local and server state.

The backend calculates and returns the cart total. The client displays what it receives:

```kotlin
// CartScreen renders the total from the domain model
Text(text = "Total: ${cart.formattedTotal}")
```

There is no local `total = items.sumOf { it.price * it.quantity }` computation anywhere in the client.

### Mutations: add, update, remove, clear

Each cart mutation is a separate network call. The ViewModel optimistically disables the relevant button during the request to prevent double-taps:

```kotlin
fun updateQuantity(itemId: Long, quantity: Int) {
    if (_uiState.value is CartUiState.Updating) return  // already in flight
    viewModelScope.launch {
        _uiState.value = CartUiState.Updating
        updateCartItemUseCase(itemId, quantity)
            .onSuccess { refreshCart() }
            .onFailure { _uiState.value = CartUiState.Error(it.message ?: "Update failed") }
    }
}
```

After any successful mutation, `refreshCart()` fetches the updated cart from the server so the total and line item prices are always accurate.

### Checkout flow

The checkout button in `CartScreen` does not place the order directly. It navigates to `CheckoutConfirmationScreen`, which shows the cart total and a confirm button. This two-step interaction prevents accidental one-tap orders.

`CheckoutConfirmationScreen` calls `CreateOrderUseCase` on confirmation:

1. `CreateOrderUseCase` calls `POST /api/v1/orders` with no request body. The backend reads the authenticated user's current cart and creates the order from it.
2. On success, `ClearCartUseCase` is called to wipe the client's cached cart state.
3. Navigation goes to `OrderDetailScreen` with the new order id. The back stack is popped to the home screen so pressing back does not return to an empty cart.

### Duplicate-submit protection

`CheckoutConfirmationScreen` sets its UiState to `Loading` when the confirm button is tapped. The confirm button is disabled while in `Loading` state:

```kotlin
Button(
    onClick = { viewModel.confirmOrder() },
    enabled = uiState !is CheckoutUiState.Loading
) {
    if (uiState is CheckoutUiState.Loading) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp))
    } else {
        Text("Confirm order")
    }
}
```

### Success state consumed after navigation

After a successful checkout, the ViewModel state is `Success(orderId)`. The `LaunchedEffect` in the composable navigates to `OrderDetailScreen` and immediately calls `viewModel.successHandled()`, which resets the state to `Idle`. Without this reset, returning to the confirmation screen would trigger navigation again on recomposition.

```kotlin
LaunchedEffect(uiState) {
    if (uiState is CheckoutUiState.Success) {
        onOrderCreated((uiState as CheckoutUiState.Success).orderId)
        viewModel.successHandled()
    }
}
```

This pattern — consuming a one-off event by resetting the state — is used in every screen where a success or error state drives a navigation or snackbar that should fire only once.

---

## Key files

| File | Layer | Role |
|---|---|---|
| `CartRepositoryImpl.kt` | data | Fetch cart, add/update/remove/clear items |
| `CartApiService.kt` | data | Retrofit interface for `/api/v1/cart/` |
| `CartDto.kt` | data | DTOs: `CartResponse`, `CartItemResponse`, `AddToCartRequest` |
| `AddToCartUseCase.kt` | domain | Validates qty > 0, delegates to repository |
| `UpdateCartItemUseCase.kt` | domain | Updates quantity for an existing item |
| `RemoveCartItemUseCase.kt` | domain | Removes one item from the cart |
| `ClearCartUseCase.kt` | domain | Removes all items; called after checkout |
| `CreateOrderUseCase.kt` | domain | Calls checkout endpoint, then clears cart on success |
| `CartViewModel.kt` | presentation | Cart state, mutation actions, error handling |
| `CheckoutConfirmationViewModel.kt` | presentation | Order creation, loading/success/error state |
| `CartScreen.kt` | presentation | Line items, quantities, total, checkout button |
| `CheckoutConfirmationScreen.kt` | presentation | Order summary, confirm button, duplicate protection |
| `SharedCartViewModel.kt` | presentation | Item count badge shared with bottom navigation |
