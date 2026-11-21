package com.edgareldy.tutorial.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edgareldy.tutorial.domain.usecase.auth.IsLoggedInUseCase
import com.edgareldy.tutorial.presentation.navigation.TutorialNavHost
import com.edgareldy.tutorial.presentation.theme.TutorialTheme
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
