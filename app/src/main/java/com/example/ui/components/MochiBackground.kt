package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun MochiBackground(
    modifier: Modifier = Modifier,
    scrimColor: Color = MaterialTheme.colorScheme.background,
    topAlpha: Float = 0.35f,
    bottomAlpha: Float = 0.20f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_mochi),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            scrimColor.copy(alpha = topAlpha),
                            scrimColor.copy(alpha = (topAlpha + bottomAlpha) / 2f),
                            scrimColor.copy(alpha = bottomAlpha)
                        )
                    )
                )
        )
        content()
    }
}
