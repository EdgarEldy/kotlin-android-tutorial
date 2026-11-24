package com.edgareldy.tutorial.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.edgareldy.tutorial.auth.presentation.screens.ProfileScreen
import com.edgareldy.tutorial.cart.presentation.screens.CartScreen
import com.edgareldy.tutorial.cart.presentation.viewmodel.SharedCartViewModel
import com.edgareldy.tutorial.orders.presentation.screens.CheckoutConfirmationScreen
import com.edgareldy.tutorial.orders.presentation.screens.OrderDetailScreen
import com.edgareldy.tutorial.orders.presentation.screens.OrderListScreen
import com.edgareldy.tutorial.products.presentation.screens.ProductDetailScreen
import com.edgareldy.tutorial.products.presentation.screens.ProductListScreen

/**
 * Describes a single bottom navigation tab: its route, label and icon.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Main application shell. Hosts the NavHost for the authenticated graph and
 * renders the bottom navigation bar on top-level destinations (Home, Cart,
 * Orders, Profile). Detail screens (ProductDetail, OrderDetail) hide the bar
 * and go full-screen. The cart icon shows a badge with the item count when
 * the cart is non-empty, driven by SharedCartViewModel.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val sharedCartViewModel: SharedCartViewModel = hiltViewModel()
    val cartItemCount by sharedCartViewModel.cartItemCount.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.ProductList.route, "Home", Icons.Default.Home),
        BottomNavItem(Screen.Cart.route, "Cart", Icons.Default.ShoppingCart),
        BottomNavItem(Screen.OrderList.route, "Orders", Icons.Default.ReceiptLong),
        BottomNavItem(Screen.Profile.route, "Profile", Icons.Default.Person)
    )

    val topLevelRoutes = bottomNavItems.map { it.route }.toSet()
    val showBottomBar = currentDestination?.route in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (item.route == Screen.Cart.route) {
                                    sharedCartViewModel.refreshCount()
                                }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item.route == Screen.Cart.route && cartItemCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text(
                                                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString()
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ProductList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ProductList.route) {
                ProductListScreen(
                    onProductClick = { productId ->
                        navController.navigate(Screen.ProductDetail.createRoute(productId))
                    }
                )
            }
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                })
            ) {
                ProductDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = {
                        sharedCartViewModel.refreshCount()
                        navController.navigate(Screen.Cart.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCartUpdated = { sharedCartViewModel.refreshCount() }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    onNavigateToProducts = {
                        navController.navigate(Screen.ProductList.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToCheckout = {
                        navController.navigate(Screen.Checkout.route)
                    },
                    onCartChanged = { sharedCartViewModel.refreshCount() }
                )
            }
            composable(Screen.OrderList.route) {
                OrderListScreen(
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    }
                )
            }
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument(Screen.OrderDetail.ARG_ORDER_ID) {
                    type = NavType.LongType
                })
            ) {
                OrderDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutConfirmationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOrderCreated = { orderId ->
                        sharedCartViewModel.refreshCount()
                        navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
