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

/**
 * Root navigation host that wires every screen placeholder to its route.
 * The auth graph handles unauthenticated flows; the main graph handles the
 * authenticated app experience. Each placeholder will be replaced by its
 * real Composable in subsequent phases.
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
                Box(Modifier.fillMaxSize()) { Text("Sign In - TODO Phase 2") }
            }
            composable(Screen.Register.route) {
                Box(Modifier.fillMaxSize()) { Text("Register - TODO Phase 2") }
            }
            composable(Screen.ActivateAccount.route) {
                Box(Modifier.fillMaxSize()) { Text("Activate Account - TODO Phase 2") }
            }
            composable(Screen.ForgotPassword.route) {
                Box(Modifier.fillMaxSize()) { Text("Forgot Password - TODO Phase 2") }
            }
            composable(Screen.VerifyOtp.route) {
                Box(Modifier.fillMaxSize()) { Text("Verify OTP - TODO Phase 2") }
            }
            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(navArgument(Screen.ResetPassword.ARG_TOKEN) {
                    type = NavType.StringType
                })
            ) {
                Box(Modifier.fillMaxSize()) { Text("Reset Password - TODO Phase 2") }
            }
        }
        navigation(startDestination = Screen.ProductList.route, route = "main") {
            composable(Screen.ProductList.route) {
                Box(Modifier.fillMaxSize()) { Text("Product List - TODO Phase 3") }
            }
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                })
            ) {
                Box(Modifier.fillMaxSize()) { Text("Product Detail - TODO Phase 3") }
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
                Box(Modifier.fillMaxSize()) { Text("Profile - TODO Phase 2") }
            }
        }
    }
}
