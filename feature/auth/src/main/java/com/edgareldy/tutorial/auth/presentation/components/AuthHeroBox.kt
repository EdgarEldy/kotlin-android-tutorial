package com.edgareldy.tutorial.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Hero section with gradient background displayed at the top of auth screens.
 * Accepts a content slot to allow each screen to place its own logo, icon or
 * back button inside the gradient area.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Composable
fun AuthHeroBox(
    height: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7C3AED),
            Color(0xFF6750A4),
            Color(0xFF4A3580)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(heroGradient),
        content = content
    )
}
