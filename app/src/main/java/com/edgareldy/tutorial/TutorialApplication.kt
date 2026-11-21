package com.edgareldy.tutorial

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point that initializes Hilt's dependency injection container.
 * The @HiltAndroidApp annotation triggers code generation for all injection sites.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@HiltAndroidApp
class TutorialApplication : Application()
