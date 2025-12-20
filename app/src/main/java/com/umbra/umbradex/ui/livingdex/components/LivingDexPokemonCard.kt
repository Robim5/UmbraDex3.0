package com.umbra.umbradex.ui.livingdex.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umbra.umbradex.data.model.PokemonWithUserData
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LivingDexPokemonCard(
    pokemonData: PokemonWithUserData,
    onPokemonClick: (Int) -> Unit,
    onToggleOwned: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pokemon = pokemonData.pokemon
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (pokemonData.isOwned)
            SuccessColor.copy(alpha = 0.15f)
        else
            PurpleSurface,
        animationSpec = tween(300),
        label = "card_color"
    )

    val imageAlpha by animateFloatAsState(
        targetValue = if (pokemonData.isOwned) 1f else 0.3f,
        animationSpec = tween(300),
        label = "image_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable {
                isPressed = true
                onPokemonClick(pokemon.nationalNumber)
                scope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Pokemon number
                Text(
                    text = "#${pokemon.nationalNumber.toString().padStart(4, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (pokemonData.isOwned) SuccessColor else TextDisabled,
                    fontSize = 9.sp
                )

                // Pokemon sprite
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (!pokemonData.isOwned) {
                        // Silhouette effect when not owned
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            TextDisabled.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    AsyncImage(
                        model = "${Constants.POKEAPI_OFFICIAL_ARTWORK}${pokemon.nationalNumber}.png",
                        contentDescription = pokemon.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(imageAlpha)
                    )
                }

                // Pokemon name
                Text(
                    text = if (pokemonData.isOwned)
                        pokemon.name.replaceFirstChar { it.uppercase() }
                    else
                        "???",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (pokemonData.isOwned) FontWeight.Bold else FontWeight.Normal,
                    color = if (pokemonData.isOwned) TextPrimary else TextDisabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp
                )
            }

            // Owned indicator / Add button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (pokemonData.isOwned) SuccessColor else PurpleSurfaceVariant
                    )
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        onToggleOwned(pokemon.nationalNumber)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (pokemonData.isOwned) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (pokemonData.isOwned) "Owned" else "Add to Living Dex",
                    tint = if (pokemonData.isOwned) Color.White else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}