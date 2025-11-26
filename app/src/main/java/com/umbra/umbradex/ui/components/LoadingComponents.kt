package com.umbra.umbradex.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.umbra.umbradex.ui.theme.*

// 1. Full Screen Loading Overlay
@Composable
fun LoadingOverlay(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PurpleBackground.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        LoadingCard(message = message)
    }
}

// 2. Loading Card (used in overlay and dialogs)
@Composable
fun LoadingCard(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PurpleSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PokeballLoader(size = 60.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 3. Loading Dialog
@Composable
fun LoadingDialog(
    message: String = "Loading...",
    onDismissRequest: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        LoadingCard(message = message)
    }
}

// 4. Pokeball Loader (Custom animated loader)
@Composable
fun PokeballLoader(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pokeball_rotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        // Outer circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PurplePrimary,
                            PurpleSecondary
                        )
                    )
                )
        )

        // Middle line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .background(PurpleBackground)
        )

        // Center circle
        Box(
            modifier = Modifier
                .size(size / 3)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(PurpleBackground)
        )

        // Center dot
        Box(
            modifier = Modifier
                .size(size / 6)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(PurpleTertiary)
        )
    }
}

// 5. Pulsing Dots Loader
@Composable
fun PulsingDotsLoader(
    modifier: Modifier = Modifier,
    dotSize: Dp = 12.dp,
    dotColor: Color = PurpleTertiary,
    animationDelay: Int = 150
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")

            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * animationDelay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale_$index"
            )

            Box(
                modifier = Modifier
                    .size(dotSize * scale)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// 6. Shimmer Loading Effect (for cards/items)
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000
) {
    val shimmerColors = listOf(
        PurpleSurfaceVariant.copy(alpha = 0.3f),
        PurpleSurfaceVariant.copy(alpha = 0.5f),
        PurpleSurfaceVariant.copy(alpha = 1.0f),
        PurpleSurfaceVariant.copy(alpha = 0.5f),
        PurpleSurfaceVariant.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(
            x = translateAnimation - widthOfShadowBrush,
            y = 0f
        ),
        end = androidx.compose.ui.geometry.Offset(
            x = translateAnimation,
            y = angleOfAxisY
        )
    )

    Box(
        modifier = modifier
            .background(brush)
    )
}

// 7. Shimmer Card Placeholder
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = PurpleSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Title placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Subtitle placeholder
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

// 8. Inline Spinner (small loading indicator)
@Composable
fun InlineSpinner(
    size: Dp = 24.dp,
    color: Color = PurpleTertiary,
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}

// 9. Bouncing Pokeball Loader
@Composable
fun BouncingPokeballLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_offset"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY.dp)
            .rotate(rotation)
    ) {
        PokeballLoader(size = 48.dp)
    }
}

// 10. Progress Bar with Text
@Composable
fun ProgressBarWithText(
    progress: Float,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PurplePrimary,
            trackColor = PurpleSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = TextDisabled,
            fontSize = 12.sp
        )
    }
}

// 11. Loading State Container
@Composable
fun LoadingStateContainer(
    isLoading: Boolean,
    loadingMessage: String = "Loading...",
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (isLoading) {
            LoadingOverlay(message = loadingMessage)
        }
    }
}