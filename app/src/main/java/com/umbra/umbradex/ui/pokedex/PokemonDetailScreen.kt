package com.umbra.umbradex.ui.pokedex

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umbra.umbradex.data.repository.*
import com.umbra.umbradex.ui.components.LoadingOverlay
import com.umbra.umbradex.ui.pokedex.components.EvolutionChain
import com.umbra.umbradex.ui.pokedex.components.StatBar
import com.umbra.umbradex.ui.pokedex.components.TypeBadge
import com.umbra.umbradex.ui.theme.*
import com.umbra.umbradex.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        PokemonDetailViewModel(
            pokemonId = pokemonId,
            authRepository = AuthRepository(),
            pokemonRepository = PokemonRepository(),
            favoritesRepository = FavoritesRepository(),
            livingDexRepository = LivingDexRepository(),
            userRepository = UserRepository(),
            context = context
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
        }
    }

    LaunchedEffect(uiState.showSuccessMessage) {
        uiState.showSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissMessage()
        }
    }

    val pokemon = uiState.pokemon
    val primaryTypeColor = pokemon?.let { getTypeColor(it.typePrimary) } ?: PurplePrimary

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top App Bar with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                primaryTypeColor.copy(alpha = 0.8f),
                                primaryTypeColor.copy(alpha = 0.4f),
                                PurpleBackground
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = {
                        pokemon?.let {
                            Text(
                                text = it.name.replaceFirstChar { char -> char.uppercase() },
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Play cry button
                        IconButton(onClick = { viewModel.playCry() }) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Play Cry",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }

            // Content
            if (uiState.isLoading) {
                LoadingOverlay(message = "Loading Pokémon...")
            } else if (pokemon != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PurpleBackground,
                                    PurpleSurface
                                )
                            )
                        )
                        .verticalScroll(scrollState)
                ) {
                    // Pokemon image and basic info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Pokemon number
                        Text(
                            text = "#${pokemon.nationalNumber.toString().padStart(4, '0')}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pokemon sprite
                        Card(
                            modifier = Modifier.size(250.dp),
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = PurpleSurface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Glow effect
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    primaryTypeColor.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                AsyncImage(
                                    model = "${Constants.POKEAPI_OFFICIAL_ARTWORK}${pokemon.nationalNumber}.png",
                                    contentDescription = pokemon.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Types
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TypeBadge(
                                type = pokemon.typePrimary,
                                modifier = Modifier.height(32.dp).widthIn(min = 80.dp)
                            )
                            pokemon.typeSecondary?.let { secondaryType ->
                                TypeBadge(
                                    type = secondaryType,
                                    modifier = Modifier.height(32.dp).widthIn(min = 80.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Favorite
                            ActionButton(
                                icon = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                label = if (uiState.isFavorite) "Favorited" else "Favorite",
                                isActive = uiState.isFavorite,
                                activeColor = ErrorColor,
                                onClick = { viewModel.toggleFavorite() },
                                modifier = Modifier.weight(1f)
                            )

                            // Owned
                            ActionButton(
                                icon = if (uiState.isOwned) Icons.Default.CheckCircle else Icons.Default.Add,
                                label = if (uiState.isOwned) "Owned" else "Add to Dex",
                                isActive = uiState.isOwned,
                                activeColor = SuccessColor,
                                onClick = { viewModel.toggleOwned() },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Equip button
                        if (!uiState.isEquipped) {
                            Button(
                                onClick = { viewModel.equipPokemon() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurplePrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Equip",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Equip as Partner",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = LegendaryColor.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Equipped",
                                        tint = LegendaryColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Currently Equipped",
                                        color = LegendaryColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Stats section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PurpleSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Base Stats",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            uiState.stats?.let { stats ->
                                StatBar(statName = "HP", statValue = stats.hp)
                                Spacer(modifier = Modifier.height(12.dp))
                                StatBar(statName = "Attack", statValue = stats.attack)
                                Spacer(modifier = Modifier.height(12.dp))
                                StatBar(statName = "Defense", statValue = stats.defense)
                                Spacer(modifier = Modifier.height(12.dp))
                                StatBar(statName = "Sp. Attack", statValue = stats.spAttack)
                                Spacer(modifier = Modifier.height(12.dp))
                                StatBar(statName = "Sp. Defense", statValue = stats.spDefense)
                                Spacer(modifier = Modifier.height(12.dp))
                                StatBar(statName = "Speed", statValue = stats.speed)

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider(color = PurpleSurfaceVariant)

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = stats.total.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleTertiary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Evolution chain section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PurpleSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Evolution Chain",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            EvolutionChain(
                                evolutionChain = uiState.evolutionChain,
                                onPokemonClick = { /* Reload with new ID - would need to handle this */ }
                            )
                        }
                    }

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) activeColor else PurpleSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isActive) Color.White else TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) Color.White else TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}