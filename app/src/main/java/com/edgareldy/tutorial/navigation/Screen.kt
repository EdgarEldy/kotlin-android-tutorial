package com.edgareldy.tutorial.navigation

import android.net.Uri

import android.net.Uri

/**
 * Sealed class hierarchy defining all navigation destinations.
 * Auth screens live in the "auth" nested graph; app screens in "main".
 * Destinations with arguments expose a createRoute() helper and an ARG_ constant.
 * Email arguments are URL-encoded via Uri.encode to handle the @ character safely.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
sealed class Screen(val route: String) {
    object SignIn : Screen("sign_in")
    object Register : Screen("register")
    object ActivateAccount : Screen("activate_account/{email}") {
        fun createRoute(email: String) = "activate_account/${Uri.encode(email)}"
        const val ARG_EMAIL = "email"
    }
    object ForgotPassword : Screen("forgot_password")
    object VerifyOtp : Screen("verify_otp/{email}") {
        fun createRoute(email: String) = "verify_otp/${Uri.encode(email)}"
        const val ARG_EMAIL = "email"
    }
    object ResetPassword : Screen("reset_password/{token}") {
        fun createRoute(token: String) = "reset_password/${Uri.encode(token)}"
        const val ARG_TOKEN = "token"
    }
    object ProductList : Screen("product_list")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
        const val ARG_PRODUCT_ID = "productId"
    }
    object Cart : Screen("cart")
    object OrderList : Screen("order_list")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
        const val ARG_ORDER_ID = "orderId"
    }
    object Profile : Screen("profile")
}
