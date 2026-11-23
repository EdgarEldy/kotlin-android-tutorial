package com.edgareldy.tutorial.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.edgareldy.tutorial.features.auth.screens.ActivateAccountScreen
import com.edgareldy.tutorial.features.auth.screens.ForgotPasswordScreen
import com.edgareldy.tutorial.features.auth.screens.ProfileScreen
import com.edgareldy.tutorial.features.auth.screens.RegisterScreen
import com.edgareldy.tutorial.features.auth.screens.ResetPasswordScreen
import com.edgareldy.tutorial.features.auth.screens.SignInScreen
import com.edgareldy.tutorial.features.auth.screens.VerifyOtpScreen
import com.edgareldy.tutorial.features.products.screens.ProductDetailScreen
import com.edgareldy.tutorial.features.products.screens.ProductListScreen

/**
 * Root navigation host wiring every screen to its route.
 * The startDestination is determined by the caller (MainActivity) which checks
 * IsLoggedInUseCase synchronously before composition, ensuring the NavHost
 * receives a stable value and avoids recomposition-driven graph restarts.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun TutorialNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "auth"
) {
    NavHost(navController = navController, startDestination = startDestination) {
        navigation(startDestination = Screen.SignIn.route, route = "auth") {

            composable(Screen.SignIn.route) {
                SignInScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onSignInSuccess = {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onRegistrationSuccess = { email ->
                        navController.navigate(Screen.ActivateAccount.createRoute(email))
                    }
                )
            }

            composable(
                route = Screen.ActivateAccount.route,
                arguments = listOf(navArgument(Screen.ActivateAccount.ARG_EMAIL) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString(Screen.ActivateAccount.ARG_EMAIL)
                    ?: ""
                ActivateAccountScreen(
                    email = email,
                    onNavigateBack = { navController.popBackStack() },
                    onActivationSuccess = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo("auth") { inclusive = false }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOtpSent = { email ->
                        navController.navigate(Screen.VerifyOtp.createRoute(email))
                    }
                )
            }

            composable(
                route = Screen.VerifyOtp.route,
                arguments = listOf(navArgument(Screen.VerifyOtp.ARG_EMAIL) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString(Screen.VerifyOtp.ARG_EMAIL) ?: ""
                VerifyOtpScreen(
                    email = email,
                    onNavigateBack = { navController.popBackStack() },
                    onOtpVerified = { otp ->
                        navController.navigate(Screen.ResetPassword.createRoute(otp))
                    }
                )
            }

            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(navArgument(Screen.ResetPassword.ARG_TOKEN) {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString(Screen.ResetPassword.ARG_TOKEN)
                    ?: ""
                ResetPasswordScreen(
                    otp = token,
                    onNavigateBack = { navController.popBackStack() },
                    onResetSuccess = {
                        navController.navigate(Screen.SignIn.route) {
                            popUpTo("auth") { inclusive = false }
                        }
                    }
                )
            }
        }

        navigation(startDestination = Screen.ProductList.route, route = "main") {
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Cart.route) {
                Box(Modifier.fillMaxSize()) { Text("Cart - TODO Phase 4") }
            }
            composable(Screen.OrderList.route) {
                Box(Modifier.fillMaxSize()) { Text("Orders - TODO Phase 5") }
            }
            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument(Screen.OrderDetail.ARG_ORDER_ID) {
                    type = NavType.LongType
                })
            ) {
                Box(Modifier.fillMaxSize()) { Text("Order Detail - TODO Phase 5") }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
