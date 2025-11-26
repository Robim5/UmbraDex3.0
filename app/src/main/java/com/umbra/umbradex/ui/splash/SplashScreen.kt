package com.umbra.umbradex.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umbra.umbradex.R
import com.umbra.umbradex.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Trigger animation on start
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Show splash for 2.5 seconds
        onSplashComplete()
    }

    // Logo scale animation
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    // Logo alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "logo_alpha"
    )

    // Text alpha animation (delayed)
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300),
        label = "text_alpha"
    )

    // Rotating progress indicator
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PurplePrimary.copy(alpha = 0.3f),
                        PurpleBackground,
                        PurpleBackground
                    ),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Pokeball or Logo
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow effect
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .alpha(0.3f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    PurpleTertiary,
                                    PurpleBackground.copy(alpha = 0f)
                                )
                            )
                        )
                )

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "UmbraDex Logo",
                    modifier = Modifier.size(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name with fade-in
            Text(
                text = "UmbraDex",
                style = MaterialTheme.typography.displaySmall,
                color = PurpleTertiary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Catch 'em All, Track 'em All",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Custom rotating progress indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .alpha(textAlpha)
                    .rotate(rotation)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = PurpleTertiary,
                    strokeWidth = 3.dp,
                    trackColor = PurpleSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading text with pulsing animation
            val loadingAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "loading_text_alpha"
            )

            Text(
                text = "Loading your adventure...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = loadingAlpha),
                modifier = Modifier.alpha(textAlpha)
            )
        }

        // Version at bottom
        Text(
            text = "v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = TextDisabled,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(textAlpha)
        )
    }
}