# Branche : feature/orders

Cette branche implémente l'historique des commandes et l'écran de détail. Elle applique la même stratégie cache-first utilisée pour les produits et ajoute un cache de commandes persisté dans Room avec une relation de clé étrangère entre les commandes et leurs lignes d'articles.

---

## Ce que vous allez apprendre

- Comment modéliser une relation parent-enfant dans Room (commandes et articles de commande) avec une contrainte de clé étrangère
- Comment charger une liste efficacement (éviter les requêtes N+1) tout en fournissant le détail complet des articles à la demande
- Comment colorer un badge de statut en utilisant les tokens de couleur Material 3 sans coder de valeurs hexadécimales en dur
- Comment le module `:feature:orders` dépend de `:feature:cart` pour `ClearCartUseCase`, et pourquoi c'est une dépendance inter-feature acceptable

---

## Ce que ça livre

### Schéma Room : commandes et articles de commande

Les commandes et leurs lignes d'articles sont stockées dans deux tables reliées :

```
orders (id, order_number, status, total_amount, cached_at)
order_items (id, order_id FK→orders.id, product_id, product_name, unit_price, quantity, subtotal)
```

La clé étrangère garantit que si une commande est supprimée du cache, ses articles sont supprimés automatiquement (`onDelete = CASCADE`).

La vue liste des commandes ne charge que les lignes `orders` - pas d'articles. Charger les articles pour chaque commande de la liste causerait des requêtes N+1 (une requête par commande). Les articles ne sont chargés que quand l'utilisateur ouvre une commande spécifique.

```kotlin
// Liste de commandes : pas d'articles chargés
fun getOrders(): Flow<List<OrderEntity>>

// Détail d'une commande : articles chargés pour une commande
fun getOrderById(id: Long): Flow<OrderEntity?>
fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>
```

`OrderRepositoryImpl` combine ces deux flows pour la vue détail en utilisant `combine` :

```kotlin
override fun getOrderById(id: Long): Flow<Result<Order>> =
    combine(orderDao.getOrderById(id), orderDao.getOrderItems(id)) { entity, items ->
        entity?.let { Result.success(it.toDomain(items)) } ?: Result.failure(NoSuchElementException())
    }
```

### Lectures cache-first

`OrderRepositoryImpl` suit le même pattern que `ProductRepositoryImpl` :

1. Émettre depuis Room immédiatement.
2. Rafraîchir depuis le réseau en arrière-plan (scope de coroutine `@ApplicationScope`).
3. En cas de succès réseau : `deleteAll()` puis `upsertAll()` pour les commandes, `deleteItemsByOrderId()` puis `upsertItems()` pour les articles.
4. En cas d'échec réseau : ignorer l'erreur si le cache est non-vide.

Supprimer avant d'upserter est important. Si les articles d'une commande ont changé sur le backend (un article annulé, une correction de prix), un simple upsert laisserait des lignes d'articles obsolètes qui n'existent plus sur le serveur.

### Badge de statut

`OrderStatusChip` est un composable qui mappe `OrderStatus` vers une couleur et un libellé :

| Statut | Rôle de couleur |
|---|---|
| `PENDING` | `MaterialTheme.colorScheme.tertiary` (orange/ambre) |
| `CONFIRMED` | `MaterialTheme.colorScheme.primary` (vert/bleu de la marque) |
| `CANCELLED` | `MaterialTheme.colorScheme.error` (rouge) |

Les rôles de couleur sont utilisés à la place des valeurs hexadécimales codées en dur. Quand l'utilisateur bascule en mode sombre, Material 3 ajuste automatiquement les couleurs des rôles - aucun code supplémentaire n'est nécessaire.

`OrderStatusChip` est `internal` (accessible dans `:feature:orders`) pour pouvoir être partagé entre `OrderListScreen` et `OrderDetailScreen` sans faire partie de l'API publique du module.

### Dépendance inter-feature : ClearCartUseCase

`CreateOrderUseCase` dans `:feature:orders` appelle `ClearCartUseCase` de `:feature:cart` après un checkout réussi. C'est la seule dépendance directe inter-feature du projet.

La dépendance est déclarée explicitement dans `feature/orders/build.gradle.kts` :

```kotlin
implementation(project(":feature:cart"))
```

L'alternative - dupliquer la logique de vidage du panier dans le module orders - serait pire. La règle est : préférer une dépendance déclarée à un comportement dupliqué. Les dépendances inter-features sont acceptables quand elles représentent une relation de domaine genuinement existante (un checkout crée une commande et vide le panier).

### Pull-to-refresh

`OrderListScreen` supporte le pull-to-refresh. Le geste appelle `RefreshOrdersUseCase`, qui supprime toutes les commandes cachées et force un fetch réseau frais. C'est le même pattern utilisé dans le catalogue produits.

---

## Fichiers clés

| Fichier | Couche | Rôle |
|---|---|---|
| `OrderEntity.kt` | data | Entité Room pour une commande |
| `OrderItemEntity.kt` | data | Entité Room pour une ligne d'article de commande |
| `OrderDao.kt` | data | upsert, deleteAll, getOrders, getOrderById, getOrderItems |
| `OrderRepositoryImpl.kt` | data | Lectures cache-first, flow commande+articles combiné |
| `OrderApiService.kt` | data | Interface Retrofit pour `/api/v1/orders/` |
| `Order.kt` | domain | Modèle domain (Order, OrderItem, OrderStatus) |
| `GetOrdersUseCase.kt` | domain | Retourne un flow de commandes cachées |
| `GetOrderByIdUseCase.kt` | domain | Retourne un flow d'une seule commande avec ses articles |
| `OrderListViewModel.kt` | presentation | État de la liste, déclencheur pull-to-refresh |
| `OrderDetailViewModel.kt` | presentation | Charge une commande par id |
| `OrderListScreen.kt` | presentation | Liste paginée, chips de statut, pull-to-refresh |
| `OrderDetailScreen.kt` | presentation | En-tête de commande, décomposition des articles, total |
| `OrderStatusChip.kt` | presentation | Badge de statut codé par couleur (internal) |
| `OrdersModule.kt` | DI | Fournit `OrderRepository`, `OrderApiService` |
