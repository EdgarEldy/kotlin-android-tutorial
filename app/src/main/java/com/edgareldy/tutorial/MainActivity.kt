package com.edgareldy.tutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.edgareldy.tutorial.core.ui.TutorialTheme
import com.edgareldy.tutorial.domain.usecase.auth.IsLoggedInUseCase
import com.edgareldy.tutorial.navigation.TutorialNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single activity that hosts the entire Compose UI.
 * Checks IsLoggedInUseCase synchronously at startup to route the user to the
 * correct navigation graph (auth or main) without a loading screen.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var isLoggedInUseCase: IsLoggedInUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            TutorialTheme {
                TutorialNavHost(
                    startDestination = if (isLoggedInUseCase()) "main" else "auth"
                )
            }
        }
    }
}
