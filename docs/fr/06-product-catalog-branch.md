# Branche : feature/product-catalog

Cette branche construit l'expérience de navigation produits : écran d'accueil, navigation par catégories, liste de produits paginée avec recherche et filtres, détail produit avec galerie d'images, et la barre de navigation inférieure.

---

## Ce que vous allez apprendre

- Comment implémenter une stratégie offline cache-first avec Room et Retrofit
- Comment utiliser `Flow` pour observer les changements de la base de données Room en temps réel
- Comment construire une liste paginée qui charge plus d'éléments à mesure que l'utilisateur fait défiler
- Comment filtrer une liste avec plusieurs paramètres (nom, catégorie, fourchette de prix) sans interroger le réseau à chaque frappe
- Comment charger des images distantes de façon asynchrone avec Coil, incluant les états placeholder et erreur
- Comment la barre de navigation inférieure suit l'onglet sélectionné et affiche un badge sur l'icône du panier
- Comment invalider le cache local au pull-to-refresh

---

## Ce que ça livre

### Stratégie de lecture cache-first

Les produits et catégories sont disponibles hors-ligne parce que l'application les sert depuis Room en premier et rafraîchit depuis le réseau en arrière-plan.

Le flux :
1. `ProductRepositoryImpl.getProducts()` retourne un `Flow` qui émet immédiatement le contenu Room actuel.
2. Une coroutine d'arrière-plan appelle l'API, diff les résultats contre le cache, et upsert les lignes nouvelles ou modifiées.
3. Room émet les données mises à jour automatiquement - l'interface se met à jour sans logique supplémentaire.

Si l'appel réseau échoue et que le cache est non-vide, l'erreur est ignorée et l'utilisateur voit les données cachées. Si le cache est vide (premier lancement sans réseau), l'erreur est remontée comme `UiState.Error`.

```kotlin
// Extrait simplifié de ProductRepositoryImpl
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

`deleteAll()` avant `upsertAll()` garantit que les produits supprimés (retirés du backend) disparaissent du cache. Un simple upsert laisserait des lignes obsolètes indéfiniment.

### DAOs Room avec requêtes filtrées

`ProductDao` accepte un objet filtre et construit la requête SQL en conséquence :

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

Le filtrage se passe dans SQLite, pas en Kotlin. Cela évite de charger des milliers d'entités en mémoire pour en ignorer la plupart.

### Debounce de la recherche

La liste de produits accepte une requête texte qui est déboncée avant de déclencher un nouveau filtre :

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

`debounce(300)` attend 300 ms après la dernière frappe avant de mettre à jour le filtre. `collectLatest` annule la collecte précédente quand une nouvelle requête arrive, donc seule la requête la plus récente est en vol.

### Chargement d'images avec Coil

Les images de produits sont chargées depuis des URLs en utilisant `AsyncImage`. Coil gère le cache, le threading et les retries automatiquement :

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

Un placeholder est affiché pendant le chargement de l'image ; une icône de fallback est affichée si l'URL est inaccessible. Aucune des deux ne requiert de gestion d'état manuelle.

### Shell de navigation inférieure

`MainScreen` enveloppe tout le graphe principal dans un `Scaffold` avec une `NavigationBar`. La barre est affichée uniquement sur les destinations de niveau supérieur (accueil, panier, commandes, profil) ; les écrans de détail la cachent.

L'icône du panier affiche un badge avec le nombre d'articles. `SharedCartViewModel` est scopé au graphe de navigation et survit aux transitions d'écrans, maintenant le compteur synchronisé sur tous les écrans.

### Pull-to-refresh

Un geste de balayage sur la liste de produits appelle `RefreshProductsUseCase`, qui supprime le cache et force un fetch réseau complet :

```kotlin
class RefreshProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke() = repository.refreshProducts()
}

// Dans le repository
override suspend fun refreshProducts() {
    val response = apiService.getProducts(...)
    productDao.deleteAll()
    productDao.upsertAll(response.data?.content?.map { it.toEntity() } ?: return)
}
```

---

## Fichiers clés

| Fichier | Couche | Rôle |
|---|---|---|
| `ProductRepositoryImpl.kt` | data | Lectures cache-first, rafraîchissement en arrière-plan |
| `CategoryRepositoryImpl.kt` | data | Même pattern pour les catégories |
| `ProductDao.kt` | data | Requêtes filtrées, upsert, deleteAll |
| `ProductEntity.kt` | data | Entité Room pour un produit |
| `GetProductsUseCase.kt` | domain | Passe les paramètres de filtre au repository |
| `RefreshProductsUseCase.kt` | domain | Force une invalidation du cache |
| `ProductListViewModel.kt` | presentation | Debounce de recherche, état du filtre, pagination |
| `ProductDetailViewModel.kt` | presentation | Charge un seul produit par id |
| `ProductListScreen.kt` | presentation | Grille paginée, barre de recherche, pull-to-refresh |
| `ProductDetailScreen.kt` | presentation | Galerie d'images, sélecteur de quantité, ajout au panier |
| `MainScreen.kt` | `:app` | Shell de navigation inférieure, badge panier |
| `SharedCartViewModel.kt` | presentation | Nombre d'articles partagé entre les onglets |
