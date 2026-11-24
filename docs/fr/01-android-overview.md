# Fondamentaux d'une application Android

Ce document explique les briques de base d'une application Android moderne et comment elles s'articulent. C'est le fondement pour comprendre tous les patterns utilisés dans ce projet.

---

## De l'Activity à Compose

Une interface Android traditionnelle est construite avec des layouts XML gonflés dans une `Activity`. Jetpack Compose remplace le XML par des fonctions Kotlin appelées **composables**. Un composable décrit à quoi ressemble l'interface pour un état donné - le framework gère le rendu et les mises à jour.

```kotlin
// Un composable est une fonction Kotlin ordinaire annotée avec @Composable
@Composable
fun ProductCard(name: String, price: Double) {
    Column {
        Text(text = name)
        Text(text = "$$price")
    }
}
```

Ce projet utilise toujours une seule `Activity` (`MainActivity`) comme point d'entrée. Tout ce qui est affiché à l'écran est un composable.

---

## Cycle de vie de l'application

Android peut détruire et recréer une `Activity` à tout moment - quand l'utilisateur fait pivoter l'écran, change d'application, ou quand le système récupère de la mémoire. Tout objet créé dans `Activity.onCreate()` est perdu lors de la recréation.

Un `ViewModel` survit aux changements de configuration. Il vit aussi longtemps que l'écran est dans la pile de retour, pas aussi longtemps que l'`Activity`. C'est donc l'endroit approprié pour conserver l'état de l'interface.

```
L'utilisateur fait pivoter l'écran
  ↓
Activity.onDestroy() — l'Activity est détruite
  ↓
Activity.onCreate() — l'Activity est recréée
  ↓
ViewModel.init()    — PAS rappelé ; la même instance de ViewModel est rattachée
```

Les ViewModels sont toujours obtenus via `hiltViewModel()` dans Compose - jamais instanciés directement avec `MyViewModel()`.

---

## L'état dans Compose

Compose est **réactif** : l'interface se met à jour automatiquement quand l'état change. L'état est maintenu en dehors du composable (dans le ViewModel) et transmis vers le bas.

```kotlin
// Le ViewModel maintient l'état
val uiState: StateFlow<ProductListUiState> = ...

// Le composable l'observe
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

`collectAsStateWithLifecycle()` arrête la collecte quand l'écran n'est pas visible (en arrière-plan), économisant la batterie et évitant le travail inutile.

---

## Navigation

Ce projet utilise **Navigation Compose**. Le graphe de navigation est un ensemble de destinations (composables) reliées par des routes. Un `NavController` gère la pile de retour.

```
Graphe auth                 Graphe principal
  Connexion ──────────────── ListeProduits ← destination de départ
  Inscription                      ↓
  ActivationCompte             DetailProduit
  MotDePasseOublié             Panier
  VerificationOtp                  ↓
  RéinitialiserMDP             Checkout
                               ListeCommandes
                                   ↓
                               DetailCommande
                               Profil
```

Les deux graphes sont séparés : un utilisateur non authentifié ne peut pas accéder au graphe principal. Au démarrage, l'application lit le token stocké et route vers le bon graphe.

La navigation profonde dans le graphe principal utilise des arguments typés :

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

Material 3 est le système de design de Google pour Android. Il fournit des composants (boutons, cartes, chips, barres de navigation) et un système de couleurs basé sur des rôles plutôt que des valeurs hexadécimales brutes.

Rôles de couleur utilisés dans ce projet :

| Rôle | Usage |
|---|---|
| `primary` | Boutons d'action principale, indicateur d'onglet actif |
| `onPrimary` | Texte/icônes sur les surfaces primaires |
| `primaryContainer` | Teinte douce du primaire (item de navigation sélectionné) |
| `surface` | Arrière-plans de cartes et de la barre inférieure |
| `error` | États d'erreur, actions destructives |

Toujours référencer les tokens comme `MaterialTheme.colorScheme.primary` plutôt que des valeurs hexadécimales codées en dur. Cela garantit que l'application s'adapte correctement quand l'utilisateur bascule entre mode clair et mode sombre.

---

## Coroutines sur Android

Les coroutines Kotlin remplacent les callbacks pour le travail asynchrone. Android fournit deux scopes de coroutines intégrés :

| Scope | Durée de vie | Usage |
|---|---|---|
| `viewModelScope` | Jusqu'à ce que le ViewModel soit effacé | Appels réseau, requêtes base de données déclenchés par des actions utilisateur |
| `lifecycleScope` | Jusqu'à ce que l'Activity/Fragment soit détruite | Collecte de flows dans les Composables (préférer `collectAsStateWithLifecycle` à la place) |

Un `Flow` est un flux de valeurs dans le temps. `StateFlow` est un Flow spécial qui contient toujours une valeur courante et la rejoue aux nouveaux collecteurs.

```kotlin
// Le repository émet un flux de résultats
fun getProducts(): Flow<Result<List<Product>>>

// Le ViewModel collecte et transforme en UiState
viewModelScope.launch {
    repository.getProducts()
        .collect { result ->
            _uiState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Erreur inconnue") }
            )
        }
}
```

---

## Injection de dépendances avec Hilt

Hilt est le framework DI recommandé pour Android. Il est construit sur Dagger et gère automatiquement le cycle de vie spécifique à Android.

Chaque classe injectable est annotée pour indiquer à Hilt comment la créer :

```kotlin
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel()
```

Les modules fournissent des objets qui ne peuvent pas être annotés directement (interfaces, classes tierces) :

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(...): Retrofit = ...
}
```

`@InstallIn(SingletonComponent::class)` signifie que les objets fournis vivent pendant toute la durée de l'application.

---

## Chargement d'images avec Coil

Coil est une bibliothèque de chargement d'images optimisée pour Compose. Elle gère automatiquement le cache, l'affichage du placeholder et le fallback en cas d'erreur.

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

Aucun code de cache ou de threading manuel n'est nécessaire - Coil gère tout cela en interne.
