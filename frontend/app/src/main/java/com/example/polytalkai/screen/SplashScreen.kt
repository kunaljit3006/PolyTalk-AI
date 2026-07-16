package com.example.polytalkai.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.polytalkai.ui.theme.*

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0.7f) }
    val opacity = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f))
        )
    }
    LaunchedEffect(Unit) {
        opacity.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp)
            ) {
                // Pulse Glowing Background
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(glowScale)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryColor.copy(alpha = glowAlpha),
                                        PrimaryColor.copy(alpha = glowAlpha * 0.4f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.width * 0.5f
                                )
                            )
                        }
                )
                
                // Square App Icon containing custom Canvas symbol
                PolyTalkAppIcon(
                    modifier = Modifier.size(130.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Custom Horizontal Logo
            PolyTalkLogoDark(
                modifier = Modifier.height(44.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtext with Dotted Font
            Text(
                text = "Translate. Speak. Connect.",
                fontFamily = DotGothicFontFamily,
                fontSize = 14.sp,
                color = TextMutedColor,
                letterSpacing = 1.sp
            )
        }
    }
}
