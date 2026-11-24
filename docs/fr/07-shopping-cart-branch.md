# Branche : feature/shopping-cart

Cette branche implémente le panier et le flux de confirmation de checkout. Le panier est entièrement côté serveur - le client ne calcule jamais les totaux, les prix ou les remises.

---

## Ce que vous allez apprendre

- Pourquoi l'état du panier appartient au serveur et ce que ça implique pour le client
- Comment prévenir les soumissions de formulaire en double avec un simple flag d'état
- Comment fonctionne le endpoint de checkout (`POST /api/v1/orders` sans corps de requête)
- Comment effacer la pile de navigation après un checkout réussi pour que l'utilisateur ne puisse pas revenir à un panier vide
- Comment partager un ViewModel entre plusieurs composables dans le même graphe de navigation

---

## Ce que ça livre

### Modèle de panier côté serveur

Le panier est possédé par le backend. Le client ne stocke pas le contenu du panier dans Room - il fetch le panier à chaque visite d'écran et après chaque mutation. Cela évite les bugs de synchronisation entre l'état local et l'état serveur.

Le backend calcule et retourne le total du panier. Le client affiche ce qu'il reçoit :

```kotlin
// CartScreen rend le total depuis le modèle domain
Text(text = "Total: ${cart.formattedTotal}")
```

Il n'y a nulle part dans le client de calcul local `total = items.sumOf { it.price * it.quantity }`.

### Mutations : ajouter, mettre à jour, supprimer, vider

Chaque mutation du panier est un appel réseau séparé. Le ViewModel désactive de façon optimiste le bouton concerné pendant la requête pour prévenir les doubles taps :

```kotlin
fun updateQuantity(itemId: Long, quantity: Int) {
    if (_uiState.value is CartUiState.Updating) return  // déjà en vol
    viewModelScope.launch {
        _uiState.value = CartUiState.Updating
        updateCartItemUseCase(itemId, quantity)
            .onSuccess { refreshCart() }
            .onFailure { _uiState.value = CartUiState.Error(it.message ?: "Mise à jour échouée") }
    }
}
```

Après toute mutation réussie, `refreshCart()` fetch le panier mis à jour depuis le serveur pour que le total et les prix des lignes soient toujours exacts.

### Flux de checkout

Le bouton de checkout dans `CartScreen` ne passe pas la commande directement. Il navigue vers `CheckoutConfirmationScreen`, qui affiche le total du panier et un bouton de confirmation. Cette interaction en deux étapes prévient les commandes accidentelles en un tap.

`CheckoutConfirmationScreen` appelle `CreateOrderUseCase` à la confirmation :

1. `CreateOrderUseCase` appelle `POST /api/v1/orders` sans corps de requête. Le backend lit le panier actuel de l'utilisateur authentifié et crée la commande depuis son contenu.
2. En cas de succès, `ClearCartUseCase` est appelé pour effacer l'état du panier côté client.
3. La navigation passe à `OrderDetailScreen` avec le nouvel id de commande. La pile de retour est poppée jusqu'à l'écran d'accueil pour que le retour arrière ne revienne pas sur un panier vide.

### Protection contre les soumissions en double

`CheckoutConfirmationScreen` définit son UiState à `Loading` quand le bouton de confirmation est tapé. Le bouton de confirmation est désactivé en état `Loading` :

```kotlin
Button(
    onClick = { viewModel.confirmOrder() },
    enabled = uiState !is CheckoutUiState.Loading
) {
    if (uiState is CheckoutUiState.Loading) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp))
    } else {
        Text("Confirmer la commande")
    }
}
```

### État de succès consommé après navigation

Après un checkout réussi, l'état du ViewModel est `Success(orderId)`. Le `LaunchedEffect` dans le composable navigue vers `OrderDetailScreen` et appelle immédiatement `viewModel.successHandled()`, qui réinitialise l'état à `Idle`. Sans cette réinitialisation, revenir à l'écran de confirmation déclencherait à nouveau la navigation à la recomposition.

```kotlin
LaunchedEffect(uiState) {
    if (uiState is CheckoutUiState.Success) {
        onOrderCreated((uiState as CheckoutUiState.Success).orderId)
        viewModel.successHandled()
    }
}
```

Ce pattern - consommer un événement ponctuel en réinitialisant l'état - est utilisé dans chaque écran où un état de succès ou d'erreur déclenche une navigation ou une snackbar qui ne doit se déclencher qu'une seule fois.

---

## Fichiers clés

| Fichier | Couche | Rôle |
|---|---|---|
| `CartRepositoryImpl.kt` | data | Fetch panier, ajouter/mettre à jour/supprimer/vider les articles |
| `CartApiService.kt` | data | Interface Retrofit pour `/api/v1/cart/` |
| `CartDto.kt` | data | DTOs : `CartResponse`, `CartItemResponse`, `AddToCartRequest` |
| `AddToCartUseCase.kt` | domain | Valide qty > 0, délègue au repository |
| `UpdateCartItemUseCase.kt` | domain | Met à jour la quantité d'un article existant |
| `RemoveCartItemUseCase.kt` | domain | Supprime un article du panier |
| `ClearCartUseCase.kt` | domain | Supprime tous les articles ; appelé après checkout |
| `CreateOrderUseCase.kt` | domain | Appelle le endpoint checkout, puis vide le panier en cas de succès |
| `CartViewModel.kt` | presentation | État du panier, actions de mutation, gestion des erreurs |
| `CheckoutConfirmationViewModel.kt` | presentation | Création de commande, état loading/success/error |
| `CartScreen.kt` | presentation | Lignes d'articles, quantités, total, bouton checkout |
| `CheckoutConfirmationScreen.kt` | presentation | Récapitulatif commande, bouton confirmer, protection double soumission |
| `SharedCartViewModel.kt` | presentation | Badge compteur d'articles partagé avec la navigation inférieure |
