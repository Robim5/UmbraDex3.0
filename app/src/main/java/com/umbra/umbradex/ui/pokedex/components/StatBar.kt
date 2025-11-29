package com.umbra.umbradex.ui.pokedex.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.umbra.umbradex.ui.theme.*

@Composable
fun StatBar(
    statName: String,
    statValue: Int,
    maxValue: Int = 255,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val progress = (statValue.toFloat() / maxValue).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 100),
        label = "stat_progress"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val statColor = when {
        statValue >= 150 -> LegendaryColor
        statValue >= 100 -> EpicColor
        statValue >= 70 -> RareColor
        else -> CommonColor
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = statName,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(90.dp)
        )

        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = statColor,
            trackColor = PurpleSurfaceVariant,
        )

        Text(
            text = statValue.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(40.dp)
                .padding(start = 8.dp)
        )
    }
}