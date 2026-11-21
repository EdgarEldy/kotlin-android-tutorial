package com.edgareldy.tutorial.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edgareldy.tutorial.presentation.navigation.TutorialNavHost
import com.edgareldy.tutorial.presentation.theme.TutorialTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity that hosts the entire Compose UI.
 * @AndroidEntryPoint enables Hilt field injection for this activity
 * and all Composables in its composition tree.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TutorialTheme {
                TutorialNavHost()
            }
        }
    }
}
