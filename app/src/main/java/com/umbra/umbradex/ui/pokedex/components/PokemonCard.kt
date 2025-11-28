package com.umbra.umbradex.ui.pokedex.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

@Composable
fun PokemonCard(
    pokemonData: PokemonWithUserData,
    onPokemonClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pokemon = pokemonData.pokemon
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    val primaryTypeColor = getTypeColor(pokemon.typePrimary)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .scale(scale)
            .clickable {
                isPressed = true
                onPokemonClick(pokemon.nationalNumber)
                // Reset after delay
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(100)
                    isPressed = false
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PurpleSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient based on primary type
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                primaryTypeColor.copy(alpha = 0.3f),
                                PurpleSurface
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row: Number and Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${pokemon.nationalNumber.toString().padStart(4, '0')}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    // Favorite button
                    IconButton(
                        onClick = { onFavoriteClick(pokemon.nationalNumber) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (pokemonData.isFavorite)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (pokemonData.isFavorite) ErrorColor else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Pokemon sprite
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = "${Constants.POKEAPI_OFFICIAL_ARTWORK}${pokemon.nationalNumber}.png",
                        contentDescription = pokemon.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )

                    // Owned indicator
                    if (pokemonData.isOwned) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SuccessColor)
                                .border(2.dp, PurpleSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Owned",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Pokemon name
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Types
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TypeBadge(
                        type = pokemon.typePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    pokemon.typeSecondary?.let { secondaryType ->
                        TypeBadge(
                            type = secondaryType,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypeBadge(
    type: String,
    modifier: Modifier = Modifier
) {
    val typeColor = getTypeColor(type)

    Box(
        modifier = modifier
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(typeColor.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = type.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}