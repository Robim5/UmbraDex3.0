package com.umbra.umbradex.ui.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umbra.umbradex.data.model.Pokemon
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.Constants
import kotlinx.coroutines.launch

@Composable
fun PetDisplay(
    pokemon: Pokemon?,
    petMessage: String?,
    showPetMessage: Boolean,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    // Bounce animation when clicked
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pet_scale"
    )

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PurpleSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Partner Pokémon",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (pokemon != null) {
                    // Pokemon Display
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(y = floatOffset.dp)
                            .scale(scale)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                isPressed = true
                                onPetClick()
                                // Reset press state after animation
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(200)
                                    isPressed = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Glow effect
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PurpleTertiary.copy(alpha = 0.3f),
                                            PurpleSurface.copy(alpha = 0f)
                                        )
                                    )
                                )
                        )

                        // Pokemon sprite
                        AsyncImage(
                            model = "${Constants.POKEAPI_OFFICIAL_ARTWORK}${pokemon.nationalNumber}.png",
                            contentDescription = pokemon.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pokemon name
                    Text(
                        text = pokemon.name.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "#${pokemon.nationalNumber.toString().padStart(4, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Pet message with animation
                    AnimatedVisibility(
                        visible = showPetMessage,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PurplePrimary.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = petMessage ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                    if (!showPetMessage) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap to interact! 👆",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDisabled,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                } else {
                    // No Pokemon equipped
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "❓",
                            fontSize = 80.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Pokémon Equipped",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Visit the Pokédex to equip your partner!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDisabled,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}